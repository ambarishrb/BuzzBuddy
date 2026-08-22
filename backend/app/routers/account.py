from datetime import datetime, timezone

from fastapi import APIRouter, Depends, HTTPException, status
from sqlmodel import Session, select

from app.core.security import hash_password, verify_password
from app.deps import get_current_user, get_db
from app.models.alarm import Alarm
from app.models.password_reset import PasswordReset
from app.models.refresh_token import RefreshToken
from app.models.user import User
from app.schemas.auth import ChangePasswordRequest, UserRead

router = APIRouter()


@router.get("/me", response_model=UserRead)
def read_me(current_user: User = Depends(get_current_user)) -> User:
    return current_user


@router.put("/password", status_code=status.HTTP_200_OK)
def change_password(
    body: ChangePasswordRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
) -> dict:
    if not verify_password(body.current_password, current_user.password_hash):
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid credentials")
    current_user.password_hash = hash_password(body.new_password)
    current_user.updated_at = datetime.now(timezone.utc).isoformat()
    tokens = db.exec(select(RefreshToken).where(RefreshToken.user_id == current_user.id)).all()
    for row in tokens:
        row.revoked = True
        db.add(row)
    db.add(current_user)
    db.commit()
    return {"ok": True}


@router.delete("", status_code=status.HTTP_204_NO_CONTENT)
def delete_account(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
) -> None:
    user_id = current_user.id
    for alarm in db.exec(select(Alarm).where(Alarm.user_id == user_id)).all():
        db.delete(alarm)
    for token in db.exec(select(RefreshToken).where(RefreshToken.user_id == user_id)).all():
        db.delete(token)
    for reset in db.exec(select(PasswordReset).where(PasswordReset.user_id == user_id)).all():
        db.delete(reset)
    db.delete(current_user)
    db.commit()
