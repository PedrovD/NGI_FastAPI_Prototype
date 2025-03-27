from typing import Any, Dict, Optional
from fastapi import APIRouter, Depends, HTTPException, status, Response, Cookie
from fastapi.security import OAuth2PasswordRequestForm
from pydantic import BaseModel
from sqlalchemy.orm import Session
from app.db.session import get_db
from app.auth import (
    create_access_token,
    get_verification,
    Provider,
    Token,
    TokenData
)
from app.schemas.user import Verification, VerificationType
from app.services import user_service, student_service, teacher_service, supervisor_service
from app.models.user import User

router = APIRouter()


class LoginRequest(BaseModel):
    user_id: int

@router.post("/login", response_model=Dict[str, Any])
async def login(
    request: LoginRequest,
    response: Response,
    db: Session = Depends(get_db)
) -> Any:
    """
    Login as a specific user.
    
    This endpoint is for the simplified authentication system where
    users are pre-defined and selected from the login page.
    """
    user_id = request.user_id
    
    # Get the user by ID
    user = user_service.get(db, user_id)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User not found"
        )
    
    # Set a cookie with the user ID
    response.set_cookie(
        key="user_id",
        value=str(user_id),
        httponly=True,
        max_age=3600 * 24 * 7,  # 7 days
        samesite="lax"
    )
    
    # Create a token for compatibility with existing code
    access_token = create_access_token(
        data={"sub": user.provider_id, "provider": "DIRECT"}
    )
    
    # Get user type
    verification = get_verification(db, user)
    
    return {
        "access_token": access_token,
        "token_type": "bearer",
        "user_id": user_id,
        "user_type": verification.type,
        "message": f"Logged in as {user.username}"
    }

@router.get("/verify", response_model=Verification)
async def verify_token(
    verification: Verification = Depends(get_verification)
) -> Any:
    """
    Verify the current user's token and return their verification status.
    """
    return verification

@router.post("/logout")
async def logout(response: Response) -> Any:
    """
    Logout the current user.
    
    Clears the user_id cookie.
    """
    response.delete_cookie(key="user_id")
    return {"message": "Logged out successfully"}

@router.get("/users", response_model=Dict[str, Any])
async def get_available_users(
    db: Session = Depends(get_db)
) -> Any:
    """
    Get a list of available users for the login page.
    
    Returns users with their roles for the simplified login system.
    """
    # Get all users with their roles
    users = []
    all_users = user_service.get_multi(db)
    
    for user in all_users:
        user_data = {
            "user_id": user.user_id,
            "username": user.username,
            "image_path": user.image_path,
            "role": "unknown"
        }
        
        # Check role
        student = student_service.get_by_user_id(db, user.user_id)
        if student:
            user_data["role"] = "student"
            users.append(user_data)
            continue
            
        teacher = teacher_service.get_by_user_id(db, user.user_id)
        if teacher:
            user_data["role"] = "teacher"
            users.append(user_data)
            continue
            
        supervisor = supervisor_service.get_by_user_id(db, user.user_id)
        if supervisor:
            user_data["role"] = "supervisor"
            user_data["business_id"] = supervisor.business_id
            users.append(user_data)
            continue
    
    return {"users": users}
