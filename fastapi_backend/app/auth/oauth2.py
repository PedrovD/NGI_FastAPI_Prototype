from typing import Dict, Optional, List, Any
from datetime import datetime, timedelta
from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from jose import JWTError, jwt
from pydantic import BaseModel
from app.core.config import settings
from app.schemas.user import VerificationType, Verification

# OAuth2 token URL (not used for social login, but required for OAuth2PasswordBearer)
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token", auto_error=False)

# Token model
class Token(BaseModel):
    access_token: str
    token_type: str

class TokenData(BaseModel):
    provider_id: Optional[str] = None
    provider: Optional[str] = None

def create_access_token(data: Dict[str, Any], expires_delta: Optional[timedelta] = None) -> str:
    """
    Create a JWT access token.
    
    Args:
        data: Data to encode in the token
        expires_delta: Optional expiration time
        
    Returns:
        JWT token string
    """
    to_encode = data.copy()
    if expires_delta:
        expire = datetime.utcnow() + expires_delta
    else:
        expire = datetime.utcnow() + timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, settings.SECRET_KEY, algorithm=settings.ALGORITHM)
    return encoded_jwt

def verify_token(token: str) -> TokenData:
    """
    Verify a JWT token.
    
    Args:
        token: JWT token string
        
    Returns:
        TokenData object with provider_id and provider
        
    Raises:
        HTTPException: If token is invalid
    """
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        payload = jwt.decode(token, settings.SECRET_KEY, algorithms=[settings.ALGORITHM])
        provider_id: str = payload.get("sub")
        provider: str = payload.get("provider")
        if provider_id is None or provider is None:
            raise credentials_exception
        token_data = TokenData(provider_id=provider_id, provider=provider)
        return token_data
    except JWTError:
        raise credentials_exception

def get_token_data(token: Optional[str] = Depends(oauth2_scheme)) -> Optional[TokenData]:
    """
    Get token data from OAuth2 token.
    
    Args:
        token: JWT token string
        
    Returns:
        TokenData object if token is valid, None otherwise
    """
    if token is None:
        return None
    try:
        return verify_token(token)
    except HTTPException:
        return None

# Provider enum and mapping
class Provider:
    GOOGLE = "GOOGLE"
    GITHUB = "GITHUB"
    
    @staticmethod
    def get_user_info(provider: str, user_info: Dict[str, Any]) -> Dict[str, Any]:
        """
        Extract user info from OAuth2 provider response.
        
        Args:
            provider: Provider name
            user_info: User info from provider
            
        Returns:
            Standardized user info dictionary
        """
        if provider == Provider.GITHUB:
            return {
                "provider": Provider.GITHUB,
                "provider_id": str(user_info.get("id")),
                "username": user_info.get("login"),
                "image_path": user_info.get("avatar_url"),
                "email": user_info.get("email")
            }
        elif provider == Provider.GOOGLE:
            return {
                "provider": Provider.GOOGLE,
                "provider_id": user_info.get("sub"),
                "username": user_info.get("name"),
                "image_path": user_info.get("picture"),
                "email": user_info.get("email")
            }
        else:
            raise ValueError(f"Unsupported provider: {provider}")
