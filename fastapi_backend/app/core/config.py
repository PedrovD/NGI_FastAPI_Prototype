import secrets
from typing import Any, Dict, List, Optional, Union
from pydantic import AnyHttpUrl, PostgresDsn, validator
from pydantic_settings import BaseSettings
import os
from pathlib import Path

class Settings(BaseSettings):
    """
    Application settings.
    
    These settings can be configured using environment variables.
    """
    
    # API settings
    API_V1_STR: str = "/api/v1"
    PROJECT_NAME: str = "NGI Backend"
    
    # Security settings
    SECRET_KEY: str = secrets.token_urlsafe(32)
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60 * 24 * 7  # 7 days
    
    # Database settings
    SQLALCHEMY_DATABASE_URI: Optional[str] = None
    
    @validator("SQLALCHEMY_DATABASE_URI", pre=True)
    def assemble_db_connection(cls, v: Optional[str], values: Dict[str, Any]) -> Any:
        if isinstance(v, str):
            return v
        
        # Default to SQLite for development
        sqlite_db_path = Path("./ngi.db").absolute()
        return f"sqlite:///{sqlite_db_path}"
    
    # OAuth2 settings
    # GITHUB_CLIENT_ID: Optional[str] = "Ov23li5itKjjah4N6D3v"
    # GITHUB_CLIENT_SECRET: Optional[str] = "1db5f063d4073e8d64e80b296443b2b95a003532"
    # GOOGLE_CLIENT_ID: Optional[str] = "603398769602-45nmb9eiug7jvloi3tl23gtv0pqgdeo7.apps.googleusercontent.com"
    # GOOGLE_CLIENT_SECRET: Optional[str] = "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"
    
    # Frontend URL
    FRONTEND_URL: str = "http://localhost:5173"
    
    # File upload settings
    UPLOAD_FOLDER: str = "./uploads"
    
    # First superuser settings
    FIRST_SUPERUSER_EMAIL: Optional[str] = "admin@example.com"
    FIRST_SUPERUSER_PASSWORD: Optional[str] = "admin"
    
    class Config:
        case_sensitive = True
        env_file = ".env"

# Create settings instance
settings = Settings()

# Create upload folder if it doesn't exist
os.makedirs(settings.UPLOAD_FOLDER, exist_ok=True)
