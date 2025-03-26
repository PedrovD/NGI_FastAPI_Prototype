from typing import Optional
from fastapi import Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.db.session import get_db
from app.auth.oauth2 import get_token_data, TokenData
from app.repositories.user_repository import UserRepository, StudentRepository, TeacherRepository, SupervisorRepository
from app.models.user import User, Student, Teacher, Supervisor
from app.schemas.user import VerificationType, Verification

# Repository instances
user_repository = UserRepository(model=User)
student_repository = StudentRepository(model=Student)
teacher_repository = TeacherRepository(model=Teacher)
supervisor_repository = SupervisorRepository(model=Supervisor)

def get_current_user(
    db: Session = Depends(get_db),
    token_data: Optional[TokenData] = Depends(get_token_data)
) -> Optional[User]:
    """
    Get the current authenticated user.
    
    Args:
        db: Database session
        token_data: Token data from OAuth2 token
        
    Returns:
        User object if authenticated, None otherwise
    """
    if token_data is None:
        return None
    
    user = user_repository.get_by_provider_id(db, token_data.provider_id)
    return user

def get_verification(
    db: Session = Depends(get_db),
    user: Optional[User] = Depends(get_current_user)
) -> Verification:
    """
    Get the verification status of the current user.
    
    Args:
        db: Database session
        user: Current user
        
    Returns:
        Verification object with user type and IDs
    """
    if user is None:
        return Verification(type=VerificationType.NONE)
    
    # Check if user is a student
    student = student_repository.get_by_user_id(db, user.user_id)
    if student:
        return Verification(
            type=VerificationType.STUDENT,
            user_id=user.user_id,
            business_id=None
        )
    
    # Check if user is a supervisor
    supervisor = supervisor_repository.get_by_user_id(db, user.user_id)
    if supervisor:
        return Verification(
            type=VerificationType.SUPERVISOR,
            user_id=user.user_id,
            business_id=supervisor.business_id
        )
    
    # Check if user is a teacher
    teacher = teacher_repository.get_by_user_id(db, user.user_id)
    if teacher:
        return Verification(
            type=VerificationType.TEACHER,
            user_id=user.user_id,
            business_id=None
        )
    
    # User exists but has no role
    return Verification(
        type=VerificationType.INVALID,
        user_id=user.user_id,
        business_id=None
    )

def require_authentication(
    user: Optional[User] = Depends(get_current_user)
) -> User:
    """
    Require authentication for a route.
    
    Args:
        user: Current user
        
    Returns:
        User object if authenticated
        
    Raises:
        HTTPException: If not authenticated
    """
    if user is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Not authenticated",
            headers={"WWW-Authenticate": "Bearer"},
        )
    return user

def require_student(
    verification: Verification = Depends(get_verification)
) -> Verification:
    """
    Require student role for a route.
    
    Args:
        verification: Verification object
        
    Returns:
        Verification object if user is a student
        
    Raises:
        HTTPException: If not a student
    """
    if verification.type != VerificationType.STUDENT:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Student role required"
        )
    return verification

def require_supervisor(
    verification: Verification = Depends(get_verification)
) -> Verification:
    """
    Require supervisor role for a route.
    
    Args:
        verification: Verification object
        
    Returns:
        Verification object if user is a supervisor
        
    Raises:
        HTTPException: If not a supervisor
    """
    if verification.type != VerificationType.SUPERVISOR:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Supervisor role required"
        )
    return verification

def require_teacher(
    verification: Verification = Depends(get_verification)
) -> Verification:
    """
    Require teacher role for a route.
    
    Args:
        verification: Verification object
        
    Returns:
        Verification object if user is a teacher
        
    Raises:
        HTTPException: If not a teacher
    """
    if verification.type != VerificationType.TEACHER:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Teacher role required"
        )
    return verification

def require_invalid(
    verification: Verification = Depends(get_verification)
) -> Verification:
    """
    Require invalid role for a route (for email setting).
    
    Args:
        verification: Verification object
        
    Returns:
        Verification object if user has invalid role
        
    Raises:
        HTTPException: If not invalid
    """
    if verification.type != VerificationType.INVALID:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Invalid role required"
        )
    return verification
