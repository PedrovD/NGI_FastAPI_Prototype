from typing import Optional, List
from enum import Enum
from pydantic import BaseModel, EmailStr, Field
from app.schemas.base import BaseSchema, TimestampMixin

# File schema
class FileSchema(BaseSchema):
    """Schema for file paths."""
    
    path: Optional[str] = Field(default=None)

# User schemas
class UserBase(BaseSchema):
    """Base schema for User."""
    
    provider_id: str
    username: str
    email: Optional[EmailStr] = None
    image_path: str

class UserCreate(UserBase):
    """Schema for creating a User."""
    pass

class UserUpdate(BaseSchema):
    """Schema for updating a User."""
    
    username: Optional[str] = None
    email: Optional[EmailStr] = None
    image_path: Optional[str] = None

class UserInDB(UserBase, TimestampMixin):
    """Schema for User in database."""
    
    user_id: int

class User(UserInDB):
    """Schema for User response."""
    pass

# Student schemas
class StudentBase(BaseSchema):
    """Base schema for Student."""
    
    description: str
    cv_path: Optional[str] = None

class StudentCreate(StudentBase):
    """Schema for creating a Student."""
    
    user_id: int

class StudentUpdate(BaseSchema):
    """Schema for updating a Student."""
    
    description: Optional[str] = None
    cv_path: Optional[str] = None

class StudentSkillBase(BaseSchema):
    """Base schema for StudentSkill."""
    
    skill_id: int
    description: str = ""

class StudentWithSkills(StudentBase):
    """Schema for Student with skills."""
    
    user_id: int
    user: Optional[User] = None
    skills: List[StudentSkillBase] = []

class Student(StudentBase):
    """Schema for Student response."""
    
    user_id: int
    user: Optional[User] = None

# Teacher schemas
class TeacherBase(BaseSchema):
    """Base schema for Teacher."""
    pass

class TeacherCreate(TeacherBase):
    """Schema for creating a Teacher."""
    
    user_id: int

class Teacher(TeacherBase):
    """Schema for Teacher response."""
    
    user_id: int
    user: Optional[User] = None

# Supervisor schemas
class SupervisorBase(BaseSchema):
    """Base schema for Supervisor."""
    
    business_id: int

class SupervisorCreate(SupervisorBase):
    """Schema for creating a Supervisor."""
    
    user_id: int

class Supervisor(SupervisorBase):
    """Schema for Supervisor response."""
    
    user_id: int
    user: Optional[User] = None

# User verification schemas
class VerificationType(str, Enum):
    """Enum for verification types."""
    
    NONE = "none"
    STUDENT = "student"
    SUPERVISOR = "supervisor"
    TEACHER = "teacher"
    INVALID = "invalid"

class VerificationBase(BaseSchema):
    """Base schema for verification."""
    
    type: VerificationType = VerificationType.NONE
    user_id: Optional[int] = None
    business_id: Optional[int] = None

class Verification(VerificationBase):
    """Schema for verification response."""
    pass

# DTO schemas
class StudentDto(BaseSchema):
    """Schema for Student DTO."""
    
    user_id: int
    username: str
    email: Optional[EmailStr] = None
    image_path: str
    description: str
    cv_path: Optional[str] = None
