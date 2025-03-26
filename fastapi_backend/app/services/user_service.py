from typing import List, Optional, Dict, Any
from fastapi import HTTPException, status
from sqlalchemy.orm import Session
from app.models.user import User, Student, Teacher, Supervisor
from app.schemas.user import UserCreate, UserUpdate, StudentCreate, StudentUpdate
from app.repositories.user_repository import UserRepository, StudentRepository, TeacherRepository, SupervisorRepository
from app.services.base import BaseService
from app.repositories import (
    user_repository,
    student_repository,
    teacher_repository,
    supervisor_repository
)

class UserService(BaseService[User, UserCreate, UserUpdate, UserRepository]):
    """Service for User operations."""
    
    def get_by_provider_id(self, db: Session, provider_id: str) -> Optional[User]:
        """Get a user by provider ID."""
        return self.repository.get_by_provider_id(db, provider_id)
    
    def get_by_email(self, db: Session, email: str) -> Optional[User]:
        """Get a user by email."""
        return self.repository.get_by_email(db, email)
    
    def create_user(self, db: Session, user_data: Dict[str, Any]) -> User:
        """
        Create a new user.
        
        Args:
            db: Database session
            user_data: User data from OAuth provider
            
        Returns:
            Created user
        """
        # Check if user already exists
        existing_user = self.repository.get_by_provider_id(db, user_data["provider_id"])
        if existing_user:
            return existing_user
        
        # Create new user
        user_in = UserCreate(
            provider_id=user_data["provider_id"],
            username=user_data["username"],
            email=user_data.get("email"),
            image_path=user_data["image_path"]
        )
        return self.repository.create(db, obj_in=user_in)
    
    def update_email(self, db: Session, user_id: int, email: str) -> User:
        """
        Update user email.
        
        Args:
            db: Database session
            user_id: User ID
            email: New email
            
        Returns:
            Updated user
        """
        # Check if email is already used
        existing_user = self.repository.get_by_email(db, email)
        if existing_user and existing_user.user_id != user_id:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Email already in use"
            )
        
        return self.update(db, id=user_id, obj_in={"email": email})

class StudentService(BaseService[Student, StudentCreate, StudentUpdate, StudentRepository]):
    """Service for Student operations."""
    
    def get_by_user_id(self, db: Session, user_id: int) -> Optional[Student]:
        """Get a student by user ID."""
        return self.repository.get_by_user_id(db, user_id)
    
    def get_with_skills(self, db: Session, user_id: int) -> Optional[Student]:
        """Get a student with skills loaded."""
        return self.repository.get_with_skills(db, user_id)
    
    def create_student(self, db: Session, user_id: int, description: str = "", cv_path: Optional[str] = None) -> Student:
        """
        Create a new student.
        
        Args:
            db: Database session
            user_id: User ID
            description: Student description
            cv_path: Path to CV file
            
        Returns:
            Created student
        """
        # Check if student already exists
        existing_student = self.repository.get_by_user_id(db, user_id)
        if existing_student:
            return existing_student
        
        # Create new student
        student_in = StudentCreate(
            user_id=user_id,
            description=description,
            cv_path=cv_path
        )
        return self.repository.create(db, obj_in=student_in)
    
    def update_student(self, db: Session, user_id: int, obj_in: StudentUpdate) -> Student:
        """
        Update a student.
        
        Args:
            db: Database session
            user_id: User ID
            obj_in: Update data
            
        Returns:
            Updated student
        """
        student = self.repository.get_by_user_id(db, user_id)
        if not student:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Student not found"
            )
        
        return self.repository.update(db, db_obj=student, obj_in=obj_in)

class TeacherService(BaseService[Teacher, UserCreate, UserUpdate, TeacherRepository]):
    """Service for Teacher operations."""
    
    def get_by_user_id(self, db: Session, user_id: int) -> Optional[Teacher]:
        """Get a teacher by user ID."""
        return self.repository.get_by_user_id(db, user_id)
    
    def create_teacher(self, db: Session, user_id: int) -> Teacher:
        """
        Create a new teacher.
        
        Args:
            db: Database session
            user_id: User ID
            
        Returns:
            Created teacher
        """
        # Check if teacher already exists
        existing_teacher = self.repository.get_by_user_id(db, user_id)
        if existing_teacher:
            return existing_teacher
        
        # Create new teacher
        from app.schemas.user import TeacherCreate
        teacher_in = TeacherCreate(user_id=user_id)
        return self.repository.create(db, obj_in=teacher_in)

class SupervisorService(BaseService[Supervisor, UserCreate, UserUpdate, SupervisorRepository]):
    """Service for Supervisor operations."""
    
    def get_by_user_id(self, db: Session, user_id: int) -> Optional[Supervisor]:
        """Get a supervisor by user ID."""
        return self.repository.get_by_user_id(db, user_id)
    
    def get_by_business_id(self, db: Session, business_id: int) -> List[Supervisor]:
        """Get all supervisors for a business."""
        return self.repository.get_by_business_id(db, business_id)
    
    def create_supervisor(self, db: Session, user_id: int, business_id: int) -> Supervisor:
        """
        Create a new supervisor.
        
        Args:
            db: Database session
            user_id: User ID
            business_id: Business ID
            
        Returns:
            Created supervisor
        """
        # Check if supervisor already exists
        existing_supervisor = self.repository.get_by_user_id(db, user_id)
        if existing_supervisor:
            return existing_supervisor
        
        # Create new supervisor
        from app.schemas.user import SupervisorCreate
        supervisor_in = SupervisorCreate(user_id=user_id, business_id=business_id)
        return self.repository.create(db, obj_in=supervisor_in)

# Create service instances
user_service = UserService(repository=user_repository)
student_service = StudentService(repository=student_repository)
teacher_service = TeacherService(repository=teacher_repository)
supervisor_service = SupervisorService(repository=supervisor_repository)
