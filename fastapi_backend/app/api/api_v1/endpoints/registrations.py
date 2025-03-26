from typing import Any, List
from fastapi import APIRouter, Depends, HTTPException, status, Body
from sqlalchemy.orm import Session
from app.db.session import get_db
from app.auth import (
    get_current_user,
    require_authentication,
    require_student,
    require_supervisor
)
from app.schemas.task import Registration, RegistrationCreate, RegistrationUpdate, GetRegistrationDto
from app.schemas.user import Verification
from app.services import task_registration_service, task_service, student_service, project_service
from app.models.user import User as UserModel

router = APIRouter()

@router.get("/existing-user-registrations", response_model=List[int])
async def read_user_registrations(
    verification: Verification = Depends(require_student),
    db: Session = Depends(get_db)
) -> Any:
    """
    Get all task IDs that the current student has registered for.
    
    This endpoint is only accessible to students.
    """
    registrations = task_registration_service.get_by_student_id(db, verification.user_id)
    return [registration.task_id for registration in registrations]

@router.get("/{task_id}", response_model=List[GetRegistrationDto])
async def read_task_registrations(
    task_id: int,
    verification: Verification = Depends(require_supervisor),
    db: Session = Depends(get_db)
) -> Any:
    """
    Get all registrations for a task.
    
    This endpoint is only accessible to supervisors who own the project.
    """
    # Check if task exists
    task = task_service.get_by_id(db, task_id)
    if not task:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Task not found"
        )
    
    # Check if project exists
    project = project_service.get_by_id(db, task.project_id)
    if not project:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Project not found"
        )
    
    # Check if supervisor owns the project
    if project.user_id != verification.user_id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not authorized to view registrations for this task"
        )
    
    # Get registrations
    registrations = task_registration_service.get_by_task_id(db, task_id)
    
    # Build response
    result = []
    for registration in registrations:
        student = student_service.get_by_user_id(db, registration.user_id)
        if student:
            result.append(GetRegistrationDto(
                task_id=registration.task_id,
                reason=registration.description,
                accepted=registration.accepted,
                response=registration.response,
                student=student
            ))
    
    return result

@router.post("/{task_id}")
async def create_registration(
    task_id: int,
    description: str = Body(...),
    verification: Verification = Depends(require_student),
    db: Session = Depends(get_db)
) -> Any:
    """
    Create a new registration for a task.
    
    This endpoint is only accessible to students.
    """
    # Check if task exists
    task = task_service.get_by_id(db, task_id)
    if not task:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Task not found"
        )
    
    # Check if student has already registered for this task
    existing_registration = task_registration_service.get_by_task_and_student(db, task_id, verification.user_id)
    if existing_registration:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Already registered for this task"
        )
    
    # Create registration
    task_registration_service.create_registration(db, task_id, verification.user_id, description)
    
    return {"message": "Registration created successfully"}

@router.patch("")
async def update_registration(
    registration: RegistrationUpdate,
    verification: Verification = Depends(require_supervisor),
    db: Session = Depends(get_db)
) -> Any:
    """
    Update a registration.
    
    This endpoint is only accessible to supervisors who own the project.
    """
    # Check if registration exists
    existing_registration = task_registration_service.get_by_task_and_student(db, registration.task_id, registration.user_id)
    if not existing_registration:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Registration not found"
        )
    
    # Check if task exists
    task = task_service.get_by_id(db, registration.task_id)
    if not task:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Task not found"
        )
    
    # Check if project exists
    project = project_service.get_by_id(db, task.project_id)
    if not project:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Project not found"
        )
    
    # Check if supervisor owns the project
    if project.user_id != verification.user_id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not authorized to update registrations for this task"
        )
    
    # Update registration
    task_registration_service.update_registration(
        db,
        registration.task_id,
        registration.user_id,
        registration.accepted,
        registration.response
    )
    
    return {"message": "Registration updated successfully"}
