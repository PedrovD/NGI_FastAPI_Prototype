from fastapi import FastAPI, Response
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import uvicorn

app = FastAPI()

# Configure CORS - allow everything
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Dummy users data
USERS = [
    {"user_id": 1, "username": "Student User", "role": "student", "image_path": "/default_profile_picture.png"},
    {"user_id": 2, "username": "Teacher User", "role": "teacher", "image_path": "/default_profile_picture.png"},
    {"user_id": 3, "username": "Supervisor User", "role": "supervisor", "image_path": "/default_profile_picture.png", "business_id": 1}
]

class LoginRequest(BaseModel):
    user_id: int

@app.get("/")
async def root():
    return {"message": "Simple server is running"}

@app.get("/api/v1/users")
async def get_users():
    return {"users": USERS}

@app.post("/api/v1/login")
async def login(request: LoginRequest, response: Response):
    user_id = request.user_id
    
    # Find user
    user = next((u for u in USERS if u["user_id"] == user_id), None)
    if not user:
        return {"error": "User not found"}
    
    # Set a cookie with the user ID
    response.set_cookie(
        key="user_id",
        value=str(user_id),
        httponly=True,
        max_age=3600 * 24 * 7,  # 7 days
        samesite="lax"
    )
    
    return {
        "access_token": "dummy_token",
        "token_type": "bearer",
        "user_id": user_id,
        "user_type": user["role"],
        "message": f"Logged in as {user['username']}"
    }

@app.post("/api/v1/logout")
async def logout(response: Response):
    response.delete_cookie(key="user_id")
    return {"message": "Logged out successfully"}

@app.get("/api/v1/verify")
async def verify():
    # Always return a student for simplicity
    return {"type": "STUDENT", "user_id": 1}

# Add CORS headers to all responses
@app.middleware("http")
async def add_cors_headers(request, call_next):
    response = await call_next(request)
    response.headers["Access-Control-Allow-Origin"] = "*"
    response.headers["Access-Control-Allow-Credentials"] = "true"
    response.headers["Access-Control-Allow-Methods"] = "GET, POST, PUT, DELETE, OPTIONS, PATCH"
    response.headers["Access-Control-Allow-Headers"] = "Content-Type, Authorization, X-Requested-With"
    return response

# Handle OPTIONS requests for CORS preflight
@app.options("/{rest_of_path:path}")
async def options_handler(rest_of_path: str):
    return Response(
        status_code=200,
        headers={
            "Access-Control-Allow-Origin": "*",
            "Access-Control-Allow-Credentials": "true",
            "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, OPTIONS, PATCH",
            "Access-Control-Allow-Headers": "Content-Type, Authorization, X-Requested-With",
        }
    )

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
