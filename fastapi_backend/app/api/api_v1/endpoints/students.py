from typing import Any, List, Optional
from fastapi import APIRouter, Depends, HTTPException, status, UploadFile, File, Form
from sqlalchemy.orm import Session
from app.db.session import get_db
from app.auth import (
    get_current_user,
    require_authentication,
    require_student,
    require_supervisor
)
from app.schemas.user import Student, StudentUpdate, Verification, StudentWithSkills
from app.schemas.skill import StudentSkill, StudentSkillUpdate
from app.services import student_service, student_skill_service, file_service
from app.models.user import User as UserModel

router = APIRouter()

@router.get("/{user_id}", response_model=StudentWithSkills)
async def read_student(
    user_id: int,
    db: Session = Depends(get_db)
) -> Any:
    """
    Get student by ID.
    """
    student = student_service.get_with_skills(db, user_id)
    if not student:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Student not found"
        )
    return student

@router.put("", response_model=Student)
async def update_student(
    description: str = Form(...),
    profile_picture: Optional[UploadFile] = File(None),
    cv: Optional[UploadFile] = File(None),
    verification: Verification = Depends(require_student),
    db: Session = Depends(get_db)
) -> Any:
    """
    Update current student.
    """
    # Get current student
    student = student_service.get_by_user_id(db, verification.user_id)
    if not student:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Student not found"
        )
    
    # Update student
    update_data = {"description": description}
    
    # Upload profile picture if provided
    if profile_picture:
        allowed_content_types = ["image/jpeg", "image/png", "image/gif"]
        profile_picture_path = await file_service.upload_file(profile_picture, allowed_content_types)
        update_data["profile_picture"] = profile_picture_path
    
    # Upload CV if provided
    if cv:
        allowed_content_types = ["application/pdf"]
        cv_path = await file_service.upload_file(cv, allowed_content_types)
        update_data["cv_path"] = cv_path
    
    return student_service.update_student(db, verification.user_id, StudentUpdate(**update_data))

@router.get("/email", response_model=List[str])
async def get_student_emails(
    selection: int,
    task_id: int,
    verification: Verification = Depends(require_supervisor),
    db: Session = Depends(get_db)
) -> Any:
    """
    Get student emails for a task.
    
    Selection:
    1 = All students
    2 = Students with pending registrations
    3 = Students with accepted registrations
    4 = Students with rejected registrations
    """
    # TODO: Implement this endpoint
    return []

@router.put("/skills", response_model=List[StudentSkill])
async def update_student_skills(
    skill_ids: List[int],
    verification: Verification = Depends(require_student),
    db: Session = Depends(get_db)
) -> Any:
    """
    Update student skills.
    """
    student_skill_service.update_student_skills(db, verification.user_id, skill_ids)
    return student_skill_service.get_by_student_id(db, verification.user_id)

@router.put("/skill", response_model=StudentSkill)
async def update_student_skill_description(
    skill: StudentSkillUpdate,
    verification: Verification = Depends(require_student),
    db: Session = Depends(get_db)
) -> Any:
    """
    Update student skill description.
    """
    # Get student skill
    student_skill = student_skill_service.get_by_student_and_skill(db, verification.user_id, skill.skill_id)
    if not student_skill:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Student skill not found"
        )
    
    # Update student skill
    student_skill.description = skill.description
    db.add(student_skill)
    db.commit()
    db.refresh(student_skill)
    
    return student_skill
