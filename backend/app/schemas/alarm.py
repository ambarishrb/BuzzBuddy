from pydantic import BaseModel, ConfigDict, Field


class AlarmCreate(BaseModel):
    title: str
    hour: int = Field(ge=0, le=23)
    minute: int = Field(ge=0, le=59)
    enabled: bool = True


class AlarmUpdate(BaseModel):
    title: str | None = None
    hour: int | None = Field(default=None, ge=0, le=23)
    minute: int | None = Field(default=None, ge=0, le=59)
    enabled: bool | None = None


class AlarmRead(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    title: str
    hour: int
    minute: int
    enabled: bool
