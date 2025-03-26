from sqlalchemy.orm import Session
from app.db.session import Base, engine
from app.core.config import settings
from app.models import User, Teacher, Student, Supervisor, Business, Project, Task, Skill, TaskSkill, StudentSkill, TaskRegistration, InviteKey
from app.schemas.user import UserCreate
from app.services import user_service, teacher_service
import logging

# Set up logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def init_db() -> None:
    """
    Initialize the database.
    
    This function creates all tables in the database.
    """
    # Create tables
    Base.metadata.create_all(bind=engine)
    logger.info("Database tables created")

def create_first_superuser() -> None:
    """
    Create the first superuser.
    
    This function creates a superuser (teacher) if it doesn't exist.
    """
    # Check if first superuser settings are provided
    if not settings.FIRST_SUPERUSER_EMAIL or not settings.FIRST_SUPERUSER_PASSWORD:
        logger.warning("First superuser settings not provided, skipping")
        return
    
    # Create database session
    from app.db.session import SessionLocal
    db = SessionLocal()
    
    try:
        # Check if superuser already exists
        user = user_service.get_by_email(db, settings.FIRST_SUPERUSER_EMAIL)
        if user:
            logger.info("First superuser already exists")
            return
        
        # Create superuser
        user_in = UserCreate(
            provider_id="admin",
            username="Admin",
            email=settings.FIRST_SUPERUSER_EMAIL,
            image_path=""
        )
        user = user_service.create(db, obj_in=user_in)
        
        # Create teacher
        teacher = teacher_service.create_teacher(db, user.user_id)
        
        logger.info("First superuser created")
    finally:
        db.close()

# Initialize database when module is imported
init_db()
