from sqlalchemy.orm import Session
from app.db.session import Base, engine
from app.core.config import settings
from app.models import User, Teacher, Student, Supervisor, Business, Project, Task, Skill, TaskSkill, StudentSkill, TaskRegistration, InviteKey
from app.schemas.user import UserCreate, StudentCreate, SupervisorCreate
from app.services import user_service, teacher_service, student_service, supervisor_service, business_service
import logging
import os

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

def create_test_data() -> None:
    """
    Create test data for the application.
    
    This function creates dummy users, businesses, projects, and tasks.
    """
    # Create database session
    from app.db.session import SessionLocal
    db = SessionLocal()
    
    try:
        # Check if we already have users
        users = user_service.get_multi(db, limit=10)
        if users and len(users) >= 3:
            logger.info("Test data already exists")
            return
        
        # Create a business
        business_name = "HAN University"
        business = business_service.get_by_name(db, business_name)
        if not business:
            business = business_service.create_business(db, business_name)
            logger.info(f"Created business: {business_name}")
        
        # Create a student user
        student_user = user_service.get_by_provider_id(db, "student")
        if not student_user:
            student_user_in = UserCreate(
                provider_id="student",
                username="Student User",
                email="student@example.com",
                image_path="/default_profile_picture.png"
            )
            student_user = user_service.create(db, obj_in=student_user_in)
            
            # Create student profile
            student_in = StudentCreate(
                user_id=student_user.user_id,
                description="I am a student looking for interesting projects.",
                cv_path=None
            )
            student = student_service.create(db, obj_in=student_in)
            logger.info(f"Created student user: {student_user.username}")
        
        # Create a teacher user
        teacher_user = user_service.get_by_provider_id(db, "teacher")
        if not teacher_user:
            teacher_user_in = UserCreate(
                provider_id="teacher",
                username="Teacher User",
                email="teacher@example.com",
                image_path="/default_profile_picture.png"
            )
            teacher_user = user_service.create(db, obj_in=teacher_user_in)
            
            # Create teacher profile
            teacher = teacher_service.create_teacher(db, teacher_user.user_id)
            logger.info(f"Created teacher user: {teacher_user.username}")
        
        # Create a supervisor user
        supervisor_user = user_service.get_by_provider_id(db, "supervisor")
        if not supervisor_user:
            supervisor_user_in = UserCreate(
                provider_id="supervisor",
                username="Supervisor User",
                email="supervisor@example.com",
                image_path="/default_profile_picture.png"
            )
            supervisor_user = user_service.create(db, obj_in=supervisor_user_in)
            
            # Create supervisor profile
            supervisor_in = SupervisorCreate(
                user_id=supervisor_user.user_id,
                business_id=business.business_id
            )
            supervisor = supervisor_service.create(db, obj_in=supervisor_in)
            logger.info(f"Created supervisor user: {supervisor_user.username}")
        
        # Create some skills
        skills = ["Python", "JavaScript", "React", "FastAPI", "SQL", "UI/UX Design"]
        for skill_name in skills:
            skill = Skill(name=skill_name, is_pending=False)
            db.add(skill)
        
        db.commit()
        logger.info("Created basic skills")
        
        # Create a project
        if not db.query(Project).first():
            project = Project(
                title="Web Development Project",
                description="A project to develop a web application for student management.",
                user_id=supervisor_user.user_id,
                image_path="/default_profile_picture.png"
            )
            db.add(project)
            db.commit()
            
            # Create tasks for the project
            task = Task(
                project_id=project.project_id,
                title="Frontend Development",
                description="Develop the frontend of the application using React.",
                total_needed=2
            )
            db.add(task)
            
            task2 = Task(
                project_id=project.project_id,
                title="Backend Development",
                description="Develop the backend of the application using FastAPI.",
                total_needed=1
            )
            db.add(task2)
            
            db.commit()
            logger.info("Created sample project with tasks")
        
    finally:
        db.close()

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
create_test_data()
