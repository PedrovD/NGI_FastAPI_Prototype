from typing import List, Optional
from sqlalchemy.orm import Session
from app.models.user import User, Student, Teacher, Supervisor
from app.repositories.base import BaseRepository
from app.schemas.user import UserCreate, UserUpdate, StudentCreate, StudentUpdate

class UserRepository(BaseRepository[User, UserCreate, UserUpdate]):
    """Repository for User operations."""
    
    def get_by_provider_id(self, db: Session, provider_id: str) -> Optional[User]:
        """Get a user by provider ID."""
        return db.query(User).filter(User.provider_id == provider_id).first()
    
    def get_by_email(self, db: Session, email: str) -> Optional[User]:
        """Get a user by email."""
        return db.query(User).filter(User.email == email).first()
    
    def get_with_roles(self, db: Session, user_id: int) -> Optional[User]:
        """Get a user with all role relationships loaded."""
        return db.query(User).filter(User.user_id == user_id).first()

class StudentRepository(BaseRepository[Student, StudentCreate, StudentUpdate]):
    """Repository for Student operations."""
    
    def get_by_user_id(self, db: Session, user_id: int) -> Optional[Student]:
        """Get a student by user ID."""
        return db.query(Student).filter(Student.user_id == user_id).first()
    
    def get_with_skills(self, db: Session, user_id: int) -> Optional[Student]:
        """Get a student with skills loaded."""
        return db.query(Student).filter(Student.user_id == user_id).first()
    
    def get_by_skill(self, db: Session, skill_id: int) -> List[Student]:
        """Get all students with a specific skill."""
        return (
            db.query(Student)
            .join(Student.skills)
            .filter(Student.skills.any(skill_id=skill_id))
            .all()
        )

class TeacherRepository(BaseRepository[Teacher, UserCreate, UserUpdate]):
    """Repository for Teacher operations."""
    
    def get_by_user_id(self, db: Session, user_id: int) -> Optional[Teacher]:
        """Get a teacher by user ID."""
        return db.query(Teacher).filter(Teacher.user_id == user_id).first()

class SupervisorRepository(BaseRepository[Supervisor, UserCreate, UserUpdate]):
    """Repository for Supervisor operations."""
    
    def get_by_user_id(self, db: Session, user_id: int) -> Optional[Supervisor]:
        """Get a supervisor by user ID."""
        return db.query(Supervisor).filter(Supervisor.user_id == user_id).first()
    
    def get_by_business_id(self, db: Session, business_id: int) -> List[Supervisor]:
        """Get all supervisors for a business."""
        return db.query(Supervisor).filter(Supervisor.business_id == business_id).all()
