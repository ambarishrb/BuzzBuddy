from datetime import datetime

from pydantic import BaseModel, ConfigDict, EmailStr, Field


class RegisterRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True, extra="ignore")

    name: str
    email: EmailStr
    password: str = Field(min_length=6)
    confirm_password: str | None = Field(default=None, alias="confirmPassword")


class LoginRequest(BaseModel):
    email: EmailStr
    password: str


class TokenResponse(BaseModel):
    access_token: str
    refresh_token: str
    token_type: str = "bearer"
    token: str


class RefreshRequest(BaseModel):
    refresh_token: str


class UserRead(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    name: str
    email: EmailStr
    created_at: str
    updated_at: str


class PasswordResetRequest(BaseModel):
    email: EmailStr


class PasswordResetConfirm(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    email: EmailStr
    code: str
    new_password: str = Field(min_length=6, alias="new_password")


class ChangePasswordRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    current_password: str
    new_password: str = Field(min_length=6)


class LogoutRequest(BaseModel):
    refresh_token: str | None = None
