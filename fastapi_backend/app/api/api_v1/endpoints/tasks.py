from typing import Any, List
from fastapi import APIRouter, Depends, HTTPException, status, Body
from sqlalchemy.orm import Session
from app.db.session import get_db
from app.auth import (
    get_current_user,
    require_authentication,
    require_supervisor
)
from app.schemas.task import Task, TaskCreate, TaskUpdate, TaskWithSkills
from app.schemas.user import Verification
from app.services import task_service, project_service, task_skill_service
from app.models.user import User as UserModel

router = APIRouter()

@router.get("/{project_id}", response_model=List[TaskWithSkills])
async def read_tasks(
    project_id: int,
    db: Session = Depends(get_db)
) -> Any:
    """
    Get all tasks for a project.
    """
    # Check if project exists
    project = project_service.get_by_id(db, project_id)
    if not project:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Project not found"
        )
    
    # Get tasks
    tasks = task_service.get_by_project_id(db, project_id)
    return [task_service.get_task_with_skills(db, task.task_id) for task in tasks]

@router.post("/{project_id}", response_model=Task)
async def create_task(
    project_id: int,
    task: TaskCreate,
    verification: Verification = Depends(require_supervisor),
    db: Session = Depends(get_db)
) -> Any:
    """
    Create a new task for a project.
    
    This endpoint is only accessible to supervisors who own the project.
    """
    # Check if project exists
    project = project_service.get_by_id(db, project_id)
    if not project:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Project not found"
        )
    
    # Check if supervisor owns the project
    if project.user_id != verification.user_id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not authorized to create tasks for this project"
        )
    
    # Create task
    return task_service.create_task(
        db,
        project_id,
        task.title,
        task.description,
        task.total_needed
    )

@router.put("/{task_id}", response_model=Task)
async def update_task(
    task_id: int,
    task_update: TaskUpdate,
    verification: Verification = Depends(require_supervisor),
    db: Session = Depends(get_db)
) -> Any:
    """
    Update a task.
    
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
            detail="Not authorized to update tasks for this project"
        )
    
    # Update task
    return task_service.update_task(db, task_id, task_update)

@router.put("/{task_id}/skills", response_model=TaskWithSkills)
async def update_task_skills(
    task_id: int,
    skill_ids: List[int] = Body(...),
    verification: Verification = Depends(require_supervisor),
    db: Session = Depends(get_db)
) -> Any:
    """
    Update skills for a task.
    
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
            detail="Not authorized to update tasks for this project"
        )
    
    # Update task skills
    task_skill_service.update_task_skills(db, task_id, skill_ids)
    
    # Return updated task with skills
    return task_service.get_task_with_skills(db, task_id)
