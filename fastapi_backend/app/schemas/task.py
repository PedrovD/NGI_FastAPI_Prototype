from typing import Optional, List
from pydantic import BaseModel, Field
from app.schemas.base import BaseSchema, TimestampMixin

# Task schemas
class TaskBase(BaseSchema):
    """Base schema for Task."""
    
    project_id: int
    title: str
    description: str
    total_needed: int

class TaskCreate(TaskBase):
    """Schema for creating a Task."""
    pass

class TaskUpdate(BaseSchema):
    """Schema for updating a Task."""
    
    title: Optional[str] = None
    description: Optional[str] = None
    total_needed: Optional[int] = None

class TaskInDB(TaskBase, TimestampMixin):
    """Schema for Task in database."""
    
    task_id: int

class Task(TaskInDB):
    """Schema for Task response."""
    pass

class TaskWithSkills(Task):
    """Schema for Task with skills."""
    
    total_accepted: int = 0
    total_registered: int = 0
    skills: List["app.schemas.skill.GetSkillDto"] = []

# Registration schemas
class RegistrationBase(BaseSchema):
    """Base schema for Registration."""
    
    task_id: int
    user_id: int
    description: str
    accepted: Optional[bool] = None
    response: str = ""

class RegistrationCreate(BaseSchema):
    """Schema for creating a Registration."""
    
    description: str

class RegistrationUpdate(BaseSchema):
    """Schema for updating a Registration."""
    
    accepted: bool
    response: str

class RegistrationInDB(RegistrationBase, TimestampMixin):
    """Schema for Registration in database."""
    pass

class Registration(RegistrationInDB):
    """Schema for Registration response."""
    pass

class GetRegistrationDto(BaseSchema):
    """Schema for Registration with student."""
    
    task_id: int
    reason: str
    accepted: Optional[bool] = None
    response: str
    student: "app.schemas.user.StudentDto"
