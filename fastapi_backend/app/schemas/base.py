from datetime import datetime
from typing import Optional
from pydantic import BaseModel, Field

class BaseSchema(BaseModel):
    """Base schema with common fields."""
    
    class Config:
        """Pydantic config."""
        from_attributes = True
        populate_by_name = True

class TimestampMixin(BaseModel):
    """Mixin for models with created_at timestamp."""
    
    created_at: Optional[datetime] = Field(default=None)
