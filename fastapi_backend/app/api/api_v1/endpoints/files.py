from typing import Any
from fastapi import APIRouter, Depends, HTTPException, status, Path
from fastapi.responses import FileResponse
from sqlalchemy.orm import Session
from app.db.session import get_db
from app.auth import (
    get_current_user,
    require_authentication
)
from app.services import file_service
from app.models.user import User as UserModel
import os

router = APIRouter()

@router.get("/{filename}")
async def get_file(
    filename: str = Path(..., description="Filename to retrieve"),
    current_user: UserModel = Depends(require_authentication)
) -> Any:
    """
    Get a file by filename.
    
    This endpoint is only accessible to authenticated users.
    """
    # Get file path
    file_path = file_service.get_file_path(filename)
    
    # Check if file exists
    if not os.path.exists(file_path):
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="File not found"
        )
    
    # Return file
    return FileResponse(file_path)
