from typing import Any, List, Optional
from fastapi import APIRouter, Depends, HTTPException, status, UploadFile, File, Form
from sqlalchemy.orm import Session
from app.db.session import get_db
from app.auth import (
    get_current_user,
    require_authentication,
    require_supervisor
)
from app.schemas.project import Project, ProjectUpdate, ProjectWithTasksAndSkills
from app.schemas.user import Verification
from app.services import project_service, file_service
from app.models.user import User as UserModel

router = APIRouter()

@router.get("", response_model=List[Project])
async def read_projects(
    business_id: Optional[int] = None,
    db: Session = Depends(get_db)
) -> Any:
    """
    Get all projects, optionally filtered by business ID.
    """
    if business_id:
        return project_service.get_by_business_id(db, business_id)
    return project_service.get_multi(db)

@router.get("/all", response_model=List[ProjectWithTasksAndSkills])
async def read_all_projects_with_details(
    db: Session = Depends(get_db)
) -> Any:
    """
    Get all projects with tasks and skills.
    """
    projects = project_service.get_multi(db)
    return [project_service.get_project_with_details(db, project.project_id) for project in projects]

@router.get("/{project_id}", response_model=Project)
async def read_project(
    project_id: int,
    db: Session = Depends(get_db)
) -> Any:
    """
    Get project by ID.
    """
    project = project_service.get_by_id(db, project_id)
    if not project:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Project not found"
        )
    return project

@router.get("/{project_id}/details", response_model=ProjectWithTasksAndSkills)
async def read_project_with_details(
    project_id: int,
    db: Session = Depends(get_db)
) -> Any:
    """
    Get project with tasks and skills by ID.
    """
    return project_service.get_project_with_details(db, project_id)

@router.post("", response_model=Project)
async def create_project(
    title: str = Form(...),
    description: str = Form(...),
    image: UploadFile = File(...),
    verification: Verification = Depends(require_supervisor),
    db: Session = Depends(get_db)
) -> Any:
    """
    Create a new project.
    
    This endpoint is only accessible to supervisors.
    """
    # Upload image
    allowed_content_types = ["image/jpeg", "image/png", "image/gif"]
    image_path = await file_service.upload_file(image, allowed_content_types)
    
    # Create project
    return project_service.create_project(
        db,
        verification.user_id,
        title,
        description,
        image_path
    )

@router.put("/{project_id}", response_model=Project)
async def update_project(
    project_id: int,
    title: Optional[str] = Form(None),
    description: Optional[str] = Form(None),
    image: Optional[UploadFile] = File(None),
    verification: Verification = Depends(require_supervisor),
    db: Session = Depends(get_db)
) -> Any:
    """
    Update project.
    
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
            detail="Not authorized to update this project"
        )
    
    # Update project
    update_data = {}
    if title:
        update_data["title"] = title
    if description:
        update_data["description"] = description
    
    # Upload image if provided
    if image:
        allowed_content_types = ["image/jpeg", "image/png", "image/gif"]
        image_path = await file_service.upload_file(image, allowed_content_types)
        update_data["image_path"] = image_path
    
    return project_service.update_project(db, project_id, ProjectUpdate(**update_data))
