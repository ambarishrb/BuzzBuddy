from fastapi import APIRouter, Depends, HTTPException, status
from sqlmodel import Session, select

from app.deps import get_current_user, get_db
from app.models.alarm import Alarm
from app.models.user import User
from app.schemas.alarm import AlarmCreate, AlarmRead, AlarmUpdate

router = APIRouter()


def _owned_alarm(db: Session, alarm_id: int, user: User) -> Alarm:
    alarm = db.get(Alarm, alarm_id)
    if alarm is None or alarm.user_id != user.id:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Alarm not found")
    return alarm


@router.get("", response_model=list[AlarmRead])
def list_alarms(db: Session = Depends(get_db), current_user: User = Depends(get_current_user)) -> list[Alarm]:
    return db.exec(select(Alarm).where(Alarm.user_id == current_user.id)).all()


@router.post("", response_model=AlarmRead, status_code=status.HTTP_201_CREATED)
def create_alarm(
    body: AlarmCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
) -> Alarm:
    alarm = Alarm(
        user_id=current_user.id,
        title=body.title,
        hour=body.hour,
        minute=body.minute,
        enabled=body.enabled,
    )
    db.add(alarm)
    db.commit()
    db.refresh(alarm)
    return alarm


@router.put("/{alarm_id}", response_model=AlarmRead)
def update_alarm(
    alarm_id: int,
    body: AlarmUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
) -> Alarm:
    alarm = _owned_alarm(db, alarm_id, current_user)
    if body.title is not None:
        alarm.title = body.title
    if body.hour is not None:
        alarm.hour = body.hour
    if body.minute is not None:
        alarm.minute = body.minute
    if body.enabled is not None:
        alarm.enabled = body.enabled
    db.add(alarm)
    db.commit()
    db.refresh(alarm)
    return alarm


@router.delete("/{alarm_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_alarm(
    alarm_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
) -> None:
    alarm = _owned_alarm(db, alarm_id, current_user)
    db.delete(alarm)
    db.commit()
