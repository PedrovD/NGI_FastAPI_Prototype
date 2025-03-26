from typing import Any, Dict, Generic, List, Optional, Type, TypeVar, Union
from fastapi import HTTPException, status
from pydantic import BaseModel
from sqlalchemy.orm import Session
from app.db.session import Base
from app.repositories.base import BaseRepository

# Define generic types for SQLAlchemy model, Pydantic schema, and Repository
ModelType = TypeVar("ModelType", bound=Base)
CreateSchemaType = TypeVar("CreateSchemaType", bound=BaseModel)
UpdateSchemaType = TypeVar("UpdateSchemaType", bound=BaseModel)
RepositoryType = TypeVar("RepositoryType", bound=BaseRepository)

class BaseService(Generic[ModelType, CreateSchemaType, UpdateSchemaType, RepositoryType]):
    """
    Base service with default methods to Create, Read, Update, Delete (CRUD).
    
    This class implements the Service pattern to encapsulate business logic.
    It follows the Single Responsibility Principle by separating business logic
    from data access logic (which is handled by repositories).
    """
    
    def __init__(self, repository: RepositoryType):
        """
        Initialize with the repository.
        
        Args:
            repository: Repository instance for data access
        """
        self.repository = repository
    
    def get(self, db: Session, id: Any) -> Optional[ModelType]:
        """
        Get a single record by ID.
        
        Args:
            db: Database session
            id: ID of the record to get
            
        Returns:
            The model instance if found
            
        Raises:
            HTTPException: If record not found
        """
        obj = self.repository.get(db, id)
        if obj is None:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Record with id {id} not found"
            )
        return obj
    
    def get_multi(
        self, db: Session, *, skip: int = 0, limit: int = 100
    ) -> List[ModelType]:
        """
        Get multiple records with pagination.
        
        Args:
            db: Database session
            skip: Number of records to skip
            limit: Maximum number of records to return
            
        Returns:
            List of model instances
        """
        return self.repository.get_multi(db, skip=skip, limit=limit)
    
    def create(self, db: Session, *, obj_in: CreateSchemaType) -> ModelType:
        """
        Create a new record.
        
        Args:
            db: Database session
            obj_in: Pydantic schema with create data
            
        Returns:
            The created model instance
        """
        return self.repository.create(db, obj_in=obj_in)
    
    def update(
        self, db: Session, *, id: Any, obj_in: Union[UpdateSchemaType, Dict[str, Any]]
    ) -> ModelType:
        """
        Update a record.
        
        Args:
            db: Database session
            id: ID of the record to update
            obj_in: Pydantic schema or dict with update data
            
        Returns:
            The updated model instance
            
        Raises:
            HTTPException: If record not found
        """
        db_obj = self.repository.get(db, id)
        if db_obj is None:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Record with id {id} not found"
            )
        return self.repository.update(db, db_obj=db_obj, obj_in=obj_in)
    
    def remove(self, db: Session, *, id: Any) -> ModelType:
        """
        Delete a record.
        
        Args:
            db: Database session
            id: ID of the record to delete
            
        Returns:
            The deleted model instance
            
        Raises:
            HTTPException: If record not found
        """
        db_obj = self.repository.get(db, id)
        if db_obj is None:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Record with id {id} not found"
            )
        return self.repository.remove(db, id=id)
