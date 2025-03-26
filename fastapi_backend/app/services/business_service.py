from typing import List, Optional, Dict, Any
import uuid
from datetime import datetime, timedelta
from fastapi import HTTPException, status
from sqlalchemy.orm import Session
from app.models.business import Business, InviteKey
from app.schemas.business import BusinessCreate, BusinessUpdate, InviteKeyCreate, LinkDto
from app.repositories.business_repository import BusinessRepository, InviteKeyRepository
from app.services.base import BaseService
from app.repositories import (
    business_repository,
    invite_key_repository
)

class BusinessService(BaseService[Business, BusinessCreate, BusinessUpdate, BusinessRepository]):
    """Service for Business operations."""
    
    def get_by_id(self, db: Session, business_id: int) -> Optional[Business]:
        """Get a business by ID."""
        return self.repository.get_by_id(db, business_id)
    
    def get_by_name(self, db: Session, name: str) -> Optional[Business]:
        """Get a business by name."""
        return self.repository.get_by_name(db, name)
    
    def get_with_supervisors(self, db: Session, business_id: int) -> Optional[Business]:
        """Get a business with supervisors loaded."""
        return self.repository.get_with_supervisors(db, business_id)
    
    def get_by_project_id(self, db: Session, project_id: int) -> Optional[Business]:
        """Get a business by project ID."""
        return self.repository.get_by_project_id(db, project_id)
    
    def create_business(self, db: Session, name: str, description: str = "", location: str = "", image_path: str = "") -> Business:
        """
        Create a new business.
        
        Args:
            db: Database session
            name: Business name
            description: Business description
            location: Business location
            image_path: Path to business image
            
        Returns:
            Created business
        """
        # Check if business already exists
        existing_business = self.repository.get_by_name(db, name)
        if existing_business:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Business with this name already exists"
            )
        
        # Create new business
        business_in = BusinessCreate(
            name=name,
            description=description,
            location=location,
            image_path=image_path
        )
        return self.repository.create(db, obj_in=business_in)
    
    def update_business(self, db: Session, business_id: int, obj_in: BusinessUpdate) -> Business:
        """
        Update a business.
        
        Args:
            db: Database session
            business_id: Business ID
            obj_in: Update data
            
        Returns:
            Updated business
        """
        business = self.repository.get_by_id(db, business_id)
        if not business:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Business not found"
            )
        
        # Check if name is being updated and is already taken
        if obj_in.name and obj_in.name != business.name:
            existing_business = self.repository.get_by_name(db, obj_in.name)
            if existing_business and existing_business.business_id != business_id:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail="Business with this name already exists"
                )
        
        return self.update(db, id=business_id, obj_in=obj_in)

class InviteKeyService(BaseService[InviteKey, InviteKeyCreate, None, InviteKeyRepository]):
    """Service for InviteKey operations."""
    
    def get_by_key(self, db: Session, key: str) -> Optional[InviteKey]:
        """Get an invite key by key value."""
        return self.repository.get_by_key(db, key)
    
    def get_by_business_id(self, db: Session, business_id: int) -> List[InviteKey]:
        """Get all invite keys for a business."""
        return self.repository.get_by_business_id(db, business_id)
    
    def create_invite_key(self, db: Session, business_id: Optional[int] = None) -> InviteKey:
        """
        Create a new invite key.
        
        Args:
            db: Database session
            business_id: Optional business ID
            
        Returns:
            Created invite key
        """
        # Generate a unique key
        key = str(uuid.uuid4())
        
        # Create new invite key
        invite_key_in = InviteKeyCreate(
            business_id=business_id
        )
        invite_key = self.repository.create(db, obj_in=invite_key_in)
        invite_key.key = key
        db.add(invite_key)
        db.commit()
        db.refresh(invite_key)
        
        return invite_key
    
    def delete_key(self, db: Session, key: str) -> None:
        """
        Delete an invite key.
        
        Args:
            db: Database session
            key: Key value
        """
        self.repository.delete_key(db, key)
    
    def create_invite_link(self, db: Session, business_id: Optional[int] = None, base_url: str = "http://localhost:5173") -> LinkDto:
        """
        Create an invite link.
        
        Args:
            db: Database session
            business_id: Optional business ID
            base_url: Base URL for the invite link
            
        Returns:
            Link DTO with link and expiration timestamp
        """
        # Create invite key
        invite_key = self.create_invite_key(db, business_id)
        
        # Create link
        link = f"{base_url}/invite?code={invite_key.key}"
        
        # Set expiration timestamp (7 days from now)
        timestamp = datetime.utcnow() + timedelta(days=7)
        
        return LinkDto(link=link, timestamp=timestamp)

# Create service instances
business_service = BusinessService(repository=business_repository)
invite_key_service = InviteKeyService(repository=invite_key_repository)
