from typing import Any, List
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.db.session import get_db
from app.auth import (
    get_current_user,
    require_authentication,
    require_supervisor,
    require_teacher
)
from app.schemas.skill import Skill, SkillCreate, SkillUpdate, GetSkillDto
from app.schemas.user import Verification
from app.services import skill_service
from app.models.user import User as UserModel

router = APIRouter()

@router.get("", response_model=List[GetSkillDto])
async def read_skills(
    db: Session = Depends(get_db)
) -> Any:
    """
    Get all skills.
    """
    return skill_service.get_all(db)

@router.get("/{skill_id}", response_model=Skill)
async def read_skill(
    skill_id: int,
    db: Session = Depends(get_db)
) -> Any:
    """
    Get skill by ID.
    """
    skill = skill_service.get_by_id(db, skill_id)
    if not skill:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Skill not found"
        )
    return skill

@router.post("", response_model=Skill)
async def create_skill(
    name: str,
    verification: Verification = Depends(require_supervisor),
    db: Session = Depends(get_db)
) -> Any:
    """
    Create a new skill.
    
    This endpoint is only accessible to supervisors.
    """
    # Check if skill already exists
    existing_skill = skill_service.get_by_name(db, name)
    if existing_skill:
        return existing_skill
    
    # Create skill
    return skill_service.create_skill(db, name)

@router.patch("/{skill_id}/name", response_model=Skill)
async def update_skill_name(
    skill_id: int,
    name: str,
    verification: Verification = Depends(require_supervisor),
    db: Session = Depends(get_db)
) -> Any:
    """
    Update skill name.
    
    This endpoint is only accessible to supervisors.
    """
    # Check if skill exists
    skill = skill_service.get_by_id(db, skill_id)
    if not skill:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Skill not found"
        )
    
    # Update skill
    return skill_service.update_skill(db, skill_id, SkillUpdate(name=name))

@router.patch("/{skill_id}/acceptance", response_model=Skill)
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
    # Check if skill exists
    skill = skill_service.get_by_id(db, skill_id)
    if not skill:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Skill not found"
        )
    
    # Update skill
    return skill_service.update_skill_acceptance(db, skill_id, is_pending)
