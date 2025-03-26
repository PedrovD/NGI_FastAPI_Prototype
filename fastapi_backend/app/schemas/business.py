from typing import Optional, List, ForwardRef, Annotated
from datetime import datetime
from pydantic import BaseModel, Field, ConfigDict
from app.schemas.base import BaseSchema, TimestampMixin
from app.schemas.user import FileSchema, Supervisor

# Business schemas
class BusinessBase(BaseSchema):
    """Base schema for Business."""
    
    name: str
    description: str
    location: str
    image_path: str

class BusinessCreate(BusinessBase):
    """Schema for creating a Business."""
    pass

class BusinessUpdate(BaseSchema):
    """Schema for updating a Business."""
    
    name: Optional[str] = None
    description: Optional[str] = None
    location: Optional[str] = None
    image_path: Optional[str] = None

class BusinessInDB(BusinessBase, TimestampMixin):
    """Schema for Business in database."""
    
    business_id: int

class Business(BusinessInDB):
    """Schema for Business response."""
    pass

class BusinessWithSupervisors(Business):
    """Schema for Business with supervisors."""
    
    model_config = ConfigDict(arbitrary_types_allowed=True)
    supervisors: List[Supervisor] = []

# InviteKey schemas
class InviteKeyBase(BaseSchema):
    """Base schema for InviteKey."""
    
    key: str
    business_id: Optional[int] = None

class InviteKeyCreate(BaseSchema):
    """Schema for creating an InviteKey."""
    
    business_id: Optional[int] = None

class InviteKeyUpdate(BaseSchema):
    """Schema for updating an InviteKey."""
    
    business_id: Optional[int] = None

class InviteKeyInDB(InviteKeyBase, TimestampMixin):
    """Schema for InviteKey in database."""
    pass

class InviteKey(InviteKeyInDB):
    """Schema for InviteKey response."""
    pass

class LinkDto(BaseSchema):
    """Schema for link response."""
    
    link: str
    timestamp: datetime
