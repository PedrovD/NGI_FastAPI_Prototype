from sqlalchemy import Column, Integer, String, Text, ForeignKey
from sqlalchemy.orm import relationship
from app.db.session import Base

class Business(Base):
    """Business model representing companies that offer projects."""
    
    __tablename__ = "business"
    
    business_id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    name = Column(String(255), nullable=False)
    description = Column(Text, nullable=False)
    location = Column(String(255), nullable=False)
    image_path = Column(String(255), nullable=False)
    
    # Relationships
    supervisors = relationship("Supervisor", back_populates="business")
    invite_keys = relationship("InviteKey", back_populates="business")

class InviteKey(Base):
    """InviteKey model for storing invitation keys."""
    
    __tablename__ = "invite_keys"
    
    key = Column(String(255), primary_key=True)
    business_id = Column(Integer, ForeignKey("business.business_id"), nullable=True)
    
    # Relationships
    business = relationship("Business", back_populates="invite_keys")
