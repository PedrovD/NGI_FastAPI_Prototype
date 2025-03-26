from typing import List, Optional
from sqlalchemy.orm import Session, joinedload
from app.models.business import Business, InviteKey
from app.repositories.base import BaseRepository
from app.schemas.business import BusinessCreate, BusinessUpdate, InviteKeyCreate, InviteKeyUpdate

class BusinessRepository(BaseRepository[Business, BusinessCreate, BusinessUpdate]):
    """Repository for Business operations."""
    
    def get_by_id(self, db: Session, business_id: int) -> Optional[Business]:
        """Get a business by ID."""
        return db.query(Business).filter(Business.business_id == business_id).first()
    
    def get_by_name(self, db: Session, name: str) -> Optional[Business]:
        """Get a business by name."""
        return db.query(Business).filter(Business.name == name).first()
    
    def get_with_supervisors(self, db: Session, business_id: int) -> Optional[Business]:
        """Get a business with supervisors loaded."""
        return (
            db.query(Business)
            .options(joinedload(Business.supervisors))
            .filter(Business.business_id == business_id)
            .first()
        )
    
    def get_by_project_id(self, db: Session, project_id: int) -> Optional[Business]:
        """Get a business by project ID."""
        return (
            db.query(Business)
            .join(Business.supervisors)
            .join(Business.supervisors[0].projects)
            .filter(Business.supervisors[0].projects.any(project_id=project_id))
            .first()
        )

class InviteKeyRepository(BaseRepository[InviteKey, InviteKeyCreate, InviteKeyUpdate]):
    """Repository for InviteKey operations."""
    
    def get_by_key(self, db: Session, key: str) -> Optional[InviteKey]:
        """Get an invite key by key value."""
        return db.query(InviteKey).filter(InviteKey.key == key).first()
    
    def get_by_business_id(self, db: Session, business_id: int) -> List[InviteKey]:
        """Get all invite keys for a business."""
        return db.query(InviteKey).filter(InviteKey.business_id == business_id).all()
    
    def delete_key(self, db: Session, key: str) -> None:
        """Delete an invite key."""
        db_obj = db.query(InviteKey).filter(InviteKey.key == key).first()
        if db_obj:
            db.delete(db_obj)
            db.commit()
