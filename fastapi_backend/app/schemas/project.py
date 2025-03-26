from typing import Optional, List
from pydantic import BaseModel, Field
from app.schemas.base import BaseSchema, TimestampMixin
from app.schemas.user import FileSchema

# Project schemas
class ProjectBase(BaseSchema):
    """Base schema for Project."""
    
    title: str
    description: str
    image_path: str
    user_id: int  # Supervisor ID

class ProjectCreate(BaseSchema):
    """Schema for creating a Project."""
    
    title: str
    description: str
    image_path: Optional[str] = None
    user_id: int  # Supervisor ID

class ProjectUpdate(BaseSchema):
    """Schema for updating a Project."""
    
    title: Optional[str] = None
    description: Optional[str] = None
    image_path: Optional[str] = None

class ProjectInDB(ProjectBase, TimestampMixin):
    """Schema for Project in database."""
    
    project_id: int

class Project(ProjectInDB):
    """Schema for Project response."""
    pass

class ProjectWithTasks(Project):
    """Schema for Project with tasks."""
    
    tasks: List["app.schemas.task.TaskWithSkills"] = []

class ProjectWithTasksAndSkills(ProjectBase):
    """Schema for Project with tasks and skills."""
    
    project_id: int
    tasks: List["app.schemas.task.TaskWithSkills"] = []
