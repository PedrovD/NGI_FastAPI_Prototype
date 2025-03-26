from sqlalchemy import Column, Integer, String, Text, ForeignKey, Boolean
from sqlalchemy.orm import relationship
from app.db.session import Base

class Project(Base):
    """Project model representing projects offered by businesses."""
    
    __tablename__ = "projects"
    
    project_id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    user_id = Column(Integer, ForeignKey("supervisors.user_id"), nullable=False)
    title = Column(String(255), nullable=False)
    description = Column(Text, nullable=False)
    image_path = Column(String(255), nullable=False)
    
    # Relationships
    supervisor = relationship("Supervisor", back_populates="projects")
    tasks = relationship("Task", back_populates="project")

class Task(Base):
    """Task model representing tasks within projects."""
    
    __tablename__ = "tasks"
    
    task_id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    project_id = Column(Integer, ForeignKey("projects.project_id"), nullable=False)
    title = Column(String(50), nullable=False)
    description = Column(Text, nullable=False)
    total_needed = Column(Integer, nullable=False)
    
    # Relationships
    project = relationship("Project", back_populates="tasks")
    skills = relationship("TaskSkill", back_populates="task")
    registrations = relationship("TaskRegistration", back_populates="task")

class Skill(Base):
    """Skill model representing skills required for tasks or possessed by students."""
    
    __tablename__ = "skills"
    
    skill_id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    name = Column(String(50), nullable=False)
    is_pending = Column(Boolean, nullable=False, default=True)
    
    # Relationships
    task_skills = relationship("TaskSkill", back_populates="skill")
    student_skills = relationship("StudentSkill", back_populates="skill")

class TaskSkill(Base):
    """Association table between tasks and skills."""
    
    __tablename__ = "tasks_skills"
    
    task_id = Column(Integer, ForeignKey("tasks.task_id"), primary_key=True)
    skill_id = Column(Integer, ForeignKey("skills.skill_id"), primary_key=True)
    
    # Relationships
    task = relationship("Task", back_populates="skills")
    skill = relationship("Skill", back_populates="task_skills")

class StudentSkill(Base):
    """Association table between students and skills with description."""
    
    __tablename__ = "students_skills"
    
    user_id = Column(Integer, ForeignKey("students.user_id"), primary_key=True)
    skill_id = Column(Integer, ForeignKey("skills.skill_id"), primary_key=True)
    description = Column(String(400), nullable=False, default="")
    
    # Relationships
    student = relationship("Student", back_populates="skills")
    skill = relationship("Skill", back_populates="student_skills")

class TaskRegistration(Base):
    """Registration model for students applying to tasks."""
    
    __tablename__ = "tasks_registrations"
    
    task_id = Column(Integer, ForeignKey("tasks.task_id"), primary_key=True)
    user_id = Column(Integer, ForeignKey("students.user_id"), primary_key=True)
    description = Column(Text, nullable=False)
    accepted = Column(Boolean, nullable=True, default=None)
    response = Column(String(400), nullable=False, default="")
    
    # Relationships
    task = relationship("Task", back_populates="registrations")
    student = relationship("Student", back_populates="registrations")
