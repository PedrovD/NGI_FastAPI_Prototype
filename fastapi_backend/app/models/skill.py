from sqlalchemy import Column, Integer, String, Boolean
from sqlalchemy.orm import relationship
from app.db.session import Base

class Skill(Base):
    """Skill model representing various skills."""
    
    __tablename__ = "skills"
    __table_args__ = {'extend_existing': True}

    skill_id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    name = Column(String(100), nullable=False, unique=True)
    is_pending = Column(Boolean, default=True)

    # Relationships
    student_skills = relationship("StudentSkill", back_populates="skill")