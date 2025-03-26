from typing import Any, List
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.db.session import get_db
from app.auth import (
    get_current_user,
    require_authentication,
    require_supervisor
)
from app.schemas.user import Supervisor, Verification
from app.services import supervisor_service
from app.models.user import User as UserModel

router = APIRouter()

@router.get("/{user_id}", response_model=Supervisor)
async def read_supervisor(
    user_id: int,
    db: Session = Depends(get_db)
) -> Any:
    """
    Get supervisor by ID.
    """
    supervisor = supervisor_service.get_by_user_id(db, user_id)
    if not supervisor:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Supervisor not found"
        )
    return supervisor

@router.get("/business/{business_id}", response_model=List[Supervisor])
async def read_supervisors_by_business(
    business_id: int,
    db: Session = Depends(get_db)
) -> Any:
    """
    Get all supervisors for a business.
    """
    return supervisor_service.get_by_business_id(db, business_id)
