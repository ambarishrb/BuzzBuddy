from sqlmodel import Field, SQLModel


class Alarm(SQLModel, table=True):
    id: int | None = Field(default=None, primary_key=True)
    user_id: int = Field(foreign_key="user.id", index=True)
    title: str
    hour: int
    minute: int
    enabled: bool = True
