import logging
from datetime import datetime, timedelta, timezone

from fastapi import APIRouter, Depends, HTTPException, status
from sqlmodel import Session, select

from app.core.config import settings
from app.core.security import (
    create_access_token,
    create_refresh_token,
    decode_refresh_token,
    generate_reset_code,
    hash_password,
    hash_token,
    send_reset_code,
    verify_password,
)
from app.deps import get_current_user, get_db
from app.models.password_reset import PasswordReset
from app.models.refresh_token import RefreshToken
from app.models.user import User
from app.schemas.auth import (
    LoginRequest,
    LogoutRequest,
    PasswordResetConfirm,
    PasswordResetRequest,
    RefreshRequest,
    RegisterRequest,
    TokenResponse,
    UserRead,
)

logger = logging.getLogger(__name__)
router = APIRouter()


def _now() -> datetime:
    return datetime.now(timezone.utc)


def _iso_now() -> str:
    return _now().isoformat()


def _issue_tokens(db: Session, user: User) -> TokenResponse:
    access = create_access_token(user.id)
    refresh = create_refresh_token(user.id)
    db.add(
        RefreshToken(
            user_id=user.id,
            token_hash=hash_token(refresh),
            expires_at=_now() + timedelta(days=settings.REFRESH_TOKEN_EXPIRE_DAYS),
            revoked=False,
        )
    )
    db.commit()
    return TokenResponse(
        access_token=access,
        refresh_token=refresh,
        token_type="bearer",
        token=access,
    )


@router.post("/register", response_model=UserRead, status_code=status.HTTP_201_CREATED)
def register(body: RegisterRequest, db: Session = Depends(get_db)) -> User:
    existing = db.exec(select(User).where(User.email == body.email.lower())).first()
    if existing:
        raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="Email already registered")
    if body.confirm_password is not None and body.confirm_password != body.password:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Passwords do not match")
    stamp = _iso_now()
    user = User(
        name=body.name.strip(),
        email=str(body.email).lower(),
        password_hash=hash_password(body.password),
        created_at=stamp,
        updated_at=stamp,
    )
    db.add(user)
    db.commit()
    db.refresh(user)
    return user


@router.post("/login", response_model=TokenResponse)
def login(body: LoginRequest, db: Session = Depends(get_db)) -> TokenResponse:
    user = db.exec(select(User).where(User.email == str(body.email).lower())).first()
    if user is None:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="User not found")
    if not verify_password(body.password, user.password_hash):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid credentials")
    return _issue_tokens(db, user)


@router.post("/auth/refresh", response_model=TokenResponse)
def refresh(body: RefreshRequest, db: Session = Depends(get_db)) -> TokenResponse:
    try:
        payload = decode_refresh_token(body.refresh_token)
        if payload.get("type") != "refresh":
            raise ValueError("wrong type")
        user_id = int(payload["sub"])
    except Exception:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid or expired token")

    token_hash = hash_token(body.refresh_token)
    stored = db.exec(
        select(RefreshToken).where(
            RefreshToken.token_hash == token_hash,
            RefreshToken.user_id == user_id,
            RefreshToken.revoked == False,  # noqa: E712
        )
    ).first()
    if stored is None or stored.expires_at.replace(tzinfo=timezone.utc) < _now():
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid or expired token")

    stored.revoked = True
    user = db.get(User, user_id)
    if user is None:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid or expired token")
    tokens = _issue_tokens(db, user)
    db.add(stored)
    db.commit()
    return tokens


@router.post("/auth/logout", status_code=status.HTTP_204_NO_CONTENT)
def logout(
    body: LogoutRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
) -> None:
    if body.refresh_token:
        token_hash = hash_token(body.refresh_token)
        stored = db.exec(
            select(RefreshToken).where(
                RefreshToken.token_hash == token_hash,
                RefreshToken.user_id == current_user.id,
            )
        ).first()
        if stored:
            stored.revoked = True
            db.add(stored)
    else:
        tokens = db.exec(select(RefreshToken).where(RefreshToken.user_id == current_user.id)).all()
        for row in tokens:
            row.revoked = True
            db.add(row)
    db.commit()


@router.post("/auth/password-reset/request", status_code=status.HTTP_200_OK)
def request_password_reset(body: PasswordResetRequest, db: Session = Depends(get_db)) -> dict:
    user = db.exec(select(User).where(User.email == str(body.email).lower())).first()
    if user is not None:
        code = generate_reset_code()
        db.add(
            PasswordReset(
                user_id=user.id,
                code_hash=hash_token(code),
                expires_at=_now() + timedelta(minutes=settings.PASSWORD_RESET_EXPIRE_MINUTES),
                used=False,
            )
        )
        db.commit()
        send_reset_code(user.email, code)
    return {"ok": True}


@router.post("/auth/password-reset/confirm", status_code=status.HTTP_200_OK)
def confirm_password_reset(body: PasswordResetConfirm, db: Session = Depends(get_db)) -> dict:
    user = db.exec(select(User).where(User.email == str(body.email).lower())).first()
    if user is None:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid reset code")
    reset = db.exec(
        select(PasswordReset)
        .where(
            PasswordReset.user_id == user.id,
            PasswordReset.code_hash == hash_token(body.code),
            PasswordReset.used == False,  # noqa: E712
        )
        .order_by(PasswordReset.id.desc())
    ).first()
    if reset is None or reset.expires_at.replace(tzinfo=timezone.utc) < _now():
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid reset code")
    user.password_hash = hash_password(body.new_password)
    user.updated_at = _iso_now()
    reset.used = True
    tokens = db.exec(select(RefreshToken).where(RefreshToken.user_id == user.id)).all()
    for row in tokens:
        row.revoked = True
        db.add(row)
    db.add(user)
    db.add(reset)
    db.commit()
    return {"ok": True}
