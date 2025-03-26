from typing import Optional, List
from pydantic import BaseModel, Field
from app.schemas.base import BaseSchema, TimestampMixin

# Skill schemas
class SkillBase(BaseSchema):
    """Base schema for Skill."""
    
    name: str
    is_pending: bool = True

class SkillCreate(BaseSchema):
    """Schema for creating a Skill."""
    
    name: str

class SkillUpdate(BaseSchema):
    """Schema for updating a Skill."""
    
    name: Optional[str] = None
    is_pending: Optional[bool] = None

class SkillInDB(SkillBase, TimestampMixin):
    """Schema for Skill in database."""
    
    skill_id: int

class Skill(SkillInDB):
    """Schema for Skill response."""
    pass

class GetSkillDto(BaseSchema):
    """Schema for Skill DTO."""
    
    skill_id: int
    name: str
    is_pending: bool

class GetSkillWithDescriptionDto(BaseSchema):
    """Schema for Skill with description."""
    
    skill: GetSkillDto
    description: str = ""

# TaskSkill schemas
class TaskSkillBase(BaseSchema):
    """Base schema for TaskSkill."""
    
    task_id: int
    skill_id: int

class TaskSkillCreate(TaskSkillBase):
    """Schema for creating a TaskSkill."""
    pass

class TaskSkill(TaskSkillBase):
    """Schema for TaskSkill response."""
    pass

# StudentSkill schemas
class StudentSkillBase(BaseSchema):
    """Base schema for StudentSkill."""
    
    user_id: int
    skill_id: int
    description: str = ""

class StudentSkillCreate(StudentSkillBase):
    """Schema for creating a StudentSkill."""
    pass

class StudentSkillUpdate(BaseSchema):
    """Schema for updating a StudentSkill."""
    
    description: Optional[str] = None

class StudentSkill(StudentSkillBase):
    """Schema for StudentSkill response."""
    pass
