import os
import uuid
import shutil
from typing import List, Optional, BinaryIO
from fastapi import HTTPException, status, UploadFile
import aiofiles
import aiohttp
from app.core.config import settings

class FileService:
    """Service for file operations."""
    
    def __init__(self, upload_folder: str = settings.UPLOAD_FOLDER):
        """
        Initialize with upload folder.
        
        Args:
            upload_folder: Folder to store uploaded files
        """
        self.upload_folder = upload_folder
        os.makedirs(upload_folder, exist_ok=True)
    
    async def upload_file(self, file: UploadFile, allowed_content_types: List[str] = None) -> str:
        """
        Upload a file.
        
        Args:
            file: File to upload
            allowed_content_types: List of allowed content types
            
        Returns:
            Path to uploaded file
        """
        if allowed_content_types and file.content_type not in allowed_content_types:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"File type {file.content_type} not allowed"
            )
        
        # Generate a unique filename
        file_extension = os.path.splitext(file.filename)[1] if file.filename else ""
        filename = f"{uuid.uuid4().hex}{file_extension}"
        file_path = os.path.join(self.upload_folder, filename)
        
        # Save file
        try:
            async with aiofiles.open(file_path, "wb") as f:
                content = await file.read()
                await f.write(content)
        except Exception as e:
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail=f"Error uploading file: {str(e)}"
            )
        
        return filename
    
    async def download_file(self, url: str) -> str:
        """
        Download a file from a URL.
        
        Args:
            url: URL to download from
            
        Returns:
            Path to downloaded file
        """
        # Generate a unique filename
        filename = f"{uuid.uuid4().hex}"
        file_path = os.path.join(self.upload_folder, filename)
        
        # Download file
        try:
            async with aiohttp.ClientSession() as session:
                async with session.get(url) as response:
                    if response.status != 200:
                        raise HTTPException(
                            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                            detail=f"Error downloading file: {response.status}"
                        )
                    
                    # Get file extension from content type
                    content_type = response.headers.get("Content-Type", "")
                    if content_type.startswith("image/"):
                        extension = content_type.split("/")[1]
                        filename = f"{filename}.{extension}"
                        file_path = os.path.join(self.upload_folder, filename)
                    
                    # Save file
                    async with aiofiles.open(file_path, "wb") as f:
                        await f.write(await response.read())
        except Exception as e:
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail=f"Error downloading file: {str(e)}"
            )
        
        return filename
    
    def delete_file(self, filename: str) -> None:
        """
        Delete a file.
        
        Args:
            filename: Filename to delete
        """
        file_path = os.path.join(self.upload_folder, filename)
        if os.path.exists(file_path):
            os.remove(file_path)
    
    def get_file_path(self, filename: str) -> str:
        """
        Get the full path to a file.
        
        Args:
            filename: Filename
            
        Returns:
            Full path to file
        """
        return os.path.join(self.upload_folder, filename)

# Create service instance
file_service = FileService()
