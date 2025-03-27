from typing import Any, List
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.db.session import get_db
from app.auth import (
    get_current_user,
    require_authentication,
    require_invalid
)
from app.schemas.user import User, UserUpdate, Verification
from app.services import user_service
from app.models.user import User as UserModel

router = APIRouter()


@router.get("", response_model=List[User])
async def read_users(
    db: Session = Depends(get_db)
) -> Any:
    """
    Get all users.
    """
    return user_service.get_all(db)

@router.get("/me", response_model=User)
async def read_current_user(
    current_user: UserModel = Depends(require_authentication),
    db: Session = Depends(get_db)
) -> Any:
    """
    Get current user.
    """
    return current_user

@router.patch("/email", response_model=User)
async def update_email(
    email: str,
    verification: Verification = Depends(require_invalid),
    db: Session = Depends(get_db)
) -> Any:
    """
    Update user email.
    
    This endpoint is only accessible to users with the INVALID role,
    i.e., users who have not yet set their email.
    """
    return user_service.update_email(db, verification.user_id, email)
