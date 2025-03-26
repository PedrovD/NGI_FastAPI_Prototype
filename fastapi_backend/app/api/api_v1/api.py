from fastapi import APIRouter
from app.api.api_v1.endpoints import (
    auth,
    users,
    students,
    teachers,
    supervisors,
    businesses,
    projects,
    tasks,
    skills,
    registrations,
    invites,
    files
)

api_router = APIRouter()

# Include all endpoint routers
api_router.include_router(auth.router, prefix="/auth", tags=["Authentication"])
api_router.include_router(users.router, prefix="/users", tags=["Users"])
api_router.include_router(students.router, prefix="/students", tags=["Students"])
api_router.include_router(teachers.router, prefix="/teachers", tags=["Teachers"])
api_router.include_router(supervisors.router, prefix="/supervisors", tags=["Supervisors"])
api_router.include_router(businesses.router, prefix="/businesses", tags=["Businesses"])
api_router.include_router(projects.router, prefix="/projects", tags=["Projects"])
api_router.include_router(tasks.router, prefix="/tasks", tags=["Tasks"])
api_router.include_router(skills.router, prefix="/skills", tags=["Skills"])
api_router.include_router(registrations.router, prefix="/registrations", tags=["Registrations"])
api_router.include_router(invites.router, prefix="/invites", tags=["Invites"])
api_router.include_router(files.router, prefix="/files", tags=["Files"])
