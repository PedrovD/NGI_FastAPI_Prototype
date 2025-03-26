from typing import Any, List, Optional
from fastapi import APIRouter, Depends, HTTPException, status, UploadFile, File, Form
from sqlalchemy.orm import Session
from app.db.session import get_db
from app.auth import (
    get_current_user,
    require_authentication,
    require_supervisor,
    require_teacher
)
from app.schemas.business import Business, BusinessUpdate, BusinessWithSupervisors
from app.schemas.user import Verification
from app.services import business_service, file_service
from app.models.user import User as UserModel

router = APIRouter()

@router.get("/{business_id}", response_model=Business)
async def read_business(
    business_id: int,
    db: Session = Depends(get_db)
) -> Any:
    """
    Get business by ID.
    """
    business = business_service.get_by_id(db, business_id)
    if not business:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Business not found"
        )
    return business

@router.get("/{business_id}/supervisors", response_model=BusinessWithSupervisors)
async def read_business_with_supervisors(
    business_id: int,
    db: Session = Depends(get_db)
) -> Any:
    """
    Get business with supervisors by ID.
    """
    business = business_service.get_with_supervisors(db, business_id)
    if not business:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Business not found"
        )
    return business

@router.post("", response_model=Business)
async def create_business(
    name: str,
    verification: Verification = Depends(require_teacher),
    db: Session = Depends(get_db)
) -> Any:
    """
    Create a new business.
    
    This endpoint is only accessible to teachers.
    """
    return business_service.create_business(db, name)

@router.put("", response_model=Business)
async def update_business(
    name: str = Form(...),
    description: str = Form(...),
    location: str = Form(...),
    image: Optional[UploadFile] = File(None),
    verification: Verification = Depends(require_supervisor),
    db: Session = Depends(get_db)
) -> Any:
    """
    Update business.
    
    This endpoint is only accessible to supervisors of the business.
    """
    # Check if supervisor belongs to the business
    if not verification.business_id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not authorized to update this business"
        )
    
    # Update business
    update_data = {
        "name": name,
        "description": description,
        "location": location
    }
    
    # Upload image if provided
    if image:
        allowed_content_types = ["image/jpeg", "image/png", "image/gif"]
        image_path = await file_service.upload_file(image, allowed_content_types)
        update_data["image_path"] = image_path
    
    return business_service.update_business(db, verification.business_id, BusinessUpdate(**update_data))

@router.get("/email/all", response_model=List[str])
async def get_colleague_emails(
    verification: Verification = Depends(require_supervisor),
    db: Session = Depends(get_db)
) -> Any:
    """
    Get all colleague emails for a business.
    
    This endpoint is only accessible to supervisors.
    """
    # TODO: Implement this endpoint
    return []
