from typing import Any, List
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.db.session import get_db
from app.auth import (
    get_current_user,
    require_authentication,
    require_teacher
)
from app.schemas.user import Teacher, Verification
from app.schemas.skill import Skill, SkillUpdate
from app.services import teacher_service, skill_service
from app.models.user import User as UserModel

router = APIRouter()

@router.get("/{user_id}", response_model=Teacher)
async def read_teacher(
    user_id: int,
    db: Session = Depends(get_db)
) -> Any:
    """
    Get teacher by ID.
    """
    teacher = teacher_service.get_by_user_id(db, user_id)
    if not teacher:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Teacher not found"
        )
    return teacher

@router.patch("/skills/{skill_id}/acceptance", response_model=Skill)
async def update_skill_acceptance(
    skill_id: int,
    is_pending: bool,
    verification: Verification = Depends(require_teacher),
    db: Session = Depends(get_db)
) -> Any:
    """
    Update skill acceptance status.
    
    This endpoint is only accessible to teachers.
    """
    return skill_service.update_skill_acceptance(db, skill_id, is_pending)
