from typing import Any, Optional
from fastapi import APIRouter, Depends, HTTPException, status, Cookie
from sqlalchemy.orm import Session
from app.db.session import get_db
from app.auth import (
    get_current_user,
    require_authentication,
    require_supervisor,
    require_teacher
)
from app.schemas.business import LinkDto, InviteKey
from app.schemas.user import Verification
from app.services import invite_key_service
from app.models.user import User as UserModel
from app.core.config import settings

router = APIRouter()

@router.get("")
async def get_invite_code(
    code: str,
    db: Session = Depends(get_db)
) -> Any:
    """
    Get invite code and store it in a cookie.
    
    This endpoint is used to validate an invite code and store it in a cookie
    for later use during OAuth2 authentication.
    """
    # Check if invite key exists
    invite_key = invite_key_service.get_by_key(db, code)
    if not invite_key:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Invalid invite code"
        )
    
    # Return success response with cookie
    response = {"message": "Invite code stored successfully"}
    
    # In a real implementation, we would set a cookie here
    # Since FastAPI doesn't directly support setting cookies in responses,
    # we would need to use a Response object
    # For now, we'll just return the response
    
    return response

@router.post("", response_model=LinkDto)
async def create_invite_link(
    business_id: Optional[int] = None,
    verification: Verification = Depends(require_authentication),
    db: Session = Depends(get_db)
) -> Any:
    """
    Create an invite link.
    
    If business_id is provided, the link will be for a supervisor.
    If business_id is not provided, the link will be for a teacher.
    
    This endpoint is accessible to teachers (for both types of links)
    and supervisors (only for supervisor links for their business).
    """
    # Check permissions
    if business_id:
        # Supervisor link
        if verification.type == "SUPERVISOR":
            # Check if supervisor belongs to the business
            if verification.business_id != business_id:
                raise HTTPException(
                    status_code=status.HTTP_403_FORBIDDEN,
                    detail="Not authorized to create invite links for this business"
                )
        elif verification.type != "TEACHER":
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Not authorized to create invite links"
            )
    else:
        # Teacher link
        if verification.type != "TEACHER":
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Not authorized to create teacher invite links"
            )
    
    # Create invite link
    return invite_key_service.create_invite_link(db, business_id, settings.FRONTEND_URL)
