from sqlalchemy import Column, Integer, String, Boolean, ForeignKey
from sqlalchemy.orm import relationship
from app.db.session import Base

class User(Base):
    """User model representing all users in the system."""
    
    __tablename__ = "users"
    
    user_id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    provider_id = Column(String(50), nullable=False)
    email = Column(String(256), nullable=True)
    username = Column(String(50), nullable=False)
    image_path = Column(String(255), nullable=False)
    
    # Relationships
    student = relationship("Student", back_populates="user", uselist=False)
    teacher = relationship("Teacher", back_populates="user", uselist=False)
    supervisor = relationship("Supervisor", back_populates="user", uselist=False)

class Student(Base):
    """Student model representing users with student role."""
    
    __tablename__ = "students"
    
    user_id = Column(Integer, ForeignKey("users.user_id"), primary_key=True)
    description = Column(String, nullable=False)
    cv_path = Column(String(255), nullable=True)
    
    # Relationships
    user = relationship("User", back_populates="student")
    skills = relationship("StudentSkill", back_populates="student")
    registrations = relationship("TaskRegistration", back_populates="student")

class Teacher(Base):
    """Teacher model representing users with teacher role."""
    
    __tablename__ = "teachers"
    
    user_id = Column(Integer, ForeignKey("users.user_id"), primary_key=True)
    
    # Relationships
    user = relationship("User", back_populates="teacher")

class Supervisor(Base):
    """Supervisor model representing users with supervisor role."""
    
    __tablename__ = "supervisors"
    
    user_id = Column(Integer, ForeignKey("users.user_id"), primary_key=True)
    business_id = Column(Integer, ForeignKey("business.business_id"), nullable=False)
    
    # Relationships
    user = relationship("User", back_populates="supervisor")
    business = relationship("Business", back_populates="supervisors")
    projects = relationship("Project", back_populates="supervisor")
