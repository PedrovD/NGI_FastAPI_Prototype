from typing import Any
from fastapi import APIRouter, Depends, HTTPException, status, FastAPI
from fastapi.security import OAuth2PasswordRequestForm
from sqlalchemy.orm import Session
from app.db.session import get_db
from app.auth import (
    create_access_token,
    get_verification,
    Provider,
    Token,
    TokenData
)
from app.schemas.user import Verification
from app.services import user_service

router = APIRouter()

app = FastAPI()

# Force OpenAPI schema generation
@router.get("/custom-openapi")
def get_custom_openapi():
    return get_openapi(
        title="Your API",
        version="1.0.0",
        routes=app.routes,
    )

@router.post("/token", response_model=Token)
async def login_for_access_token(
    form_data: OAuth2PasswordRequestForm = Depends(),
    db: Session = Depends(get_db)
) -> Any:
    """
    OAuth2 compatible token login, get an access token for future requests.
    
    This endpoint is for testing purposes only, as the actual authentication
    is handled by OAuth2 providers (Google, GitHub).
    """
    # In a real OAuth2 flow, we would validate the user credentials
    # For testing, we'll just create a token with the provided username
    user = user_service.get_by_provider_id(db, form_data.username)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect username or password",
            headers={"WWW-Authenticate": "Bearer"},
        )
    
    access_token = create_access_token(
        data={"sub": user.provider_id, "provider": "TEST"}
    )
    return {"access_token": access_token, "token_type": "bearer"}

@router.get("/verify", response_model=Verification)
async def verify_token(
    verification: Verification = Depends(get_verification)
) -> Any:
    """
    Verify the current user's token and return their verification status.
    """
    return verification

@router.post("/logout")
async def logout() -> Any:
    """
    Logout the current user.
    
    In a stateless API, this is a no-op as the client should simply
    discard the token.
    """
    return {"message": "Logged out successfully"}
