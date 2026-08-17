from datetime import datetime

from sqlmodel import Field, SQLModel


class PasswordReset(SQLModel, table=True):
    id: int | None = Field(default=None, primary_key=True)
    user_id: int = Field(foreign_key="user.id", index=True)
    code_hash: str
    expires_at: datetime
    used: bool = False
