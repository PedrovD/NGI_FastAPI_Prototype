from typing import List, Optional
from sqlalchemy.orm import Session, joinedload
from app.models.project import Project, Task, Skill, TaskSkill, StudentSkill, TaskRegistration
from app.repositories.base import BaseRepository
from app.schemas.project import ProjectCreate, ProjectUpdate
from app.schemas.task import TaskCreate, TaskUpdate
from app.schemas.skill import SkillCreate, SkillUpdate

class ProjectRepository(BaseRepository[Project, ProjectCreate, ProjectUpdate]):
    """Repository for Project operations."""
    
    def get_by_id(self, db: Session, project_id: int) -> Optional[Project]:
        """Get a project by ID."""
        return db.query(Project).filter(Project.project_id == project_id).first()
    
    def get_with_tasks(self, db: Session, project_id: int) -> Optional[Project]:
        """Get a project with tasks loaded."""
        return (
            db.query(Project)
            .options(joinedload(Project.tasks))
            .filter(Project.project_id == project_id)
            .first()
        )
    
    def get_by_supervisor_id(self, db: Session, user_id: int) -> List[Project]:
        """Get all projects for a supervisor."""
        return db.query(Project).filter(Project.user_id == user_id).all()
    
    def get_by_business_id(self, db: Session, business_id: int) -> List[Project]:
        """Get all projects for a business."""
        return (
            db.query(Project)
            .join(Project.supervisor)
            .filter(Project.supervisor.has(business_id=business_id))
            .all()
        )
    
    def check_project_name_taken(self, db: Session, title: str, user_id: int) -> bool:
        """Check if a project name is already taken by a supervisor."""
        return db.query(Project).filter(
            Project.title == title,
            Project.user_id == user_id
        ).first() is not None

class TaskRepository(BaseRepository[Task, TaskCreate, TaskUpdate]):
    """Repository for Task operations."""
    
    def get_by_id(self, db: Session, task_id: int) -> Optional[Task]:
        """Get a task by ID."""
        return db.query(Task).filter(Task.task_id == task_id).first()
    
    def get_with_skills(self, db: Session, task_id: int) -> Optional[Task]:
        """Get a task with skills loaded."""
        return (
            db.query(Task)
            .options(joinedload(Task.skills))
            .filter(Task.task_id == task_id)
            .first()
        )
    
    def get_by_project_id(self, db: Session, project_id: int) -> List[Task]:
        """Get all tasks for a project."""
        return db.query(Task).filter(Task.project_id == project_id).all()

class SkillRepository(BaseRepository[Skill, SkillCreate, SkillUpdate]):
    """Repository for Skill operations."""
    
    def get_by_id(self, db: Session, skill_id: int) -> Optional[Skill]:
        """Get a skill by ID."""
        return db.query(Skill).filter(Skill.skill_id == skill_id).first()
    
    def get_by_name(self, db: Session, name: str) -> Optional[Skill]:
        """Get a skill by name."""
        return db.query(Skill).filter(Skill.name == name).first()
    
    def get_top_by_project_id(self, db: Session, project_id: int, limit: int = 5) -> List[Skill]:
        """Get top skills for a project."""
        return (
            db.query(Skill)
            .join(TaskSkill)
            .join(Task)
            .filter(Task.project_id == project_id)
            .group_by(Skill.skill_id)
            .order_by(db.func.count(TaskSkill.task_id).desc())
            .limit(limit)
            .all()
        )
    
    def get_top_by_business_id(self, db: Session, business_id: int, limit: int = 5) -> List[Skill]:
        """Get top skills for a business."""
        return (
            db.query(Skill)
            .join(TaskSkill)
            .join(Task)
            .join(Project)
            .join(Project.supervisor)
            .filter(Project.supervisor.has(business_id=business_id))
            .group_by(Skill.skill_id)
            .order_by(db.func.count(TaskSkill.task_id).desc())
            .limit(limit)
            .all()
        )
    
    def get_all_for_task(self, db: Session, task_id: int) -> List[Skill]:
        """Get all skills for a task."""
        return (
            db.query(Skill)
            .join(TaskSkill)
            .filter(TaskSkill.task_id == task_id)
            .all()
        )

class TaskSkillRepository(BaseRepository[TaskSkill, None, None]):
    """Repository for TaskSkill operations."""
    
    def get_by_task_and_skill(self, db: Session, task_id: int, skill_id: int) -> Optional[TaskSkill]:
        """Get a task-skill association by task and skill IDs."""
        return db.query(TaskSkill).filter(
            TaskSkill.task_id == task_id,
            TaskSkill.skill_id == skill_id
        ).first()
    
    def get_by_task_id(self, db: Session, task_id: int) -> List[TaskSkill]:
        """Get all task-skill associations for a task."""
        return db.query(TaskSkill).filter(TaskSkill.task_id == task_id).all()
    
    def create_task_skill(self, db: Session, task_id: int, skill_id: int) -> TaskSkill:
        """Create a new task-skill association."""
        db_obj = TaskSkill(task_id=task_id, skill_id=skill_id)
        db.add(db_obj)
        db.commit()
        db.refresh(db_obj)
        return db_obj
    
    def update_task_skills(self, db: Session, task_id: int, skill_ids: List[int]) -> None:
        """Update the skills for a task."""
        # Delete existing associations
        db.query(TaskSkill).filter(TaskSkill.task_id == task_id).delete()
        
        # Create new associations
        for skill_id in skill_ids:
            db_obj = TaskSkill(task_id=task_id, skill_id=skill_id)
            db.add(db_obj)
        
        db.commit()

class StudentSkillRepository(BaseRepository[StudentSkill, None, None]):
    """Repository for StudentSkill operations."""
    
    def get_by_student_and_skill(self, db: Session, user_id: int, skill_id: int) -> Optional[StudentSkill]:
        """Get a student-skill association by student and skill IDs."""
        return db.query(StudentSkill).filter(
            StudentSkill.user_id == user_id,
            StudentSkill.skill_id == skill_id
        ).first()
    
    def get_by_student_id(self, db: Session, user_id: int) -> List[StudentSkill]:
        """Get all student-skill associations for a student."""
        return db.query(StudentSkill).filter(StudentSkill.user_id == user_id).all()
    
    def create_student_skill(self, db: Session, user_id: int, skill_id: int, description: str = "") -> StudentSkill:
        """Create a new student-skill association."""
        db_obj = StudentSkill(user_id=user_id, skill_id=skill_id, description=description)
        db.add(db_obj)
        db.commit()
        db.refresh(db_obj)
        return db_obj
    
    def update_student_skills(self, db: Session, user_id: int, skill_ids: List[int]) -> None:
        """Update the skills for a student."""
        # Delete existing associations
        db.query(StudentSkill).filter(StudentSkill.user_id == user_id).delete()
        
        # Create new associations
        for skill_id in skill_ids:
            db_obj = StudentSkill(user_id=user_id, skill_id=skill_id)
            db.add(db_obj)
        
        db.commit()

class TaskRegistrationRepository(BaseRepository[TaskRegistration, None, None]):
    """Repository for TaskRegistration operations."""
    
    def get_by_task_and_student(self, db: Session, task_id: int, user_id: int) -> Optional[TaskRegistration]:
        """Get a task registration by task and student IDs."""
        return db.query(TaskRegistration).filter(
            TaskRegistration.task_id == task_id,
            TaskRegistration.user_id == user_id
        ).first()
    
    def get_by_task_id(self, db: Session, task_id: int) -> List[TaskRegistration]:
        """Get all task registrations for a task."""
        return db.query(TaskRegistration).filter(TaskRegistration.task_id == task_id).all()
    
    def get_by_student_id(self, db: Session, user_id: int) -> List[TaskRegistration]:
        """Get all task registrations for a student."""
        return db.query(TaskRegistration).filter(TaskRegistration.user_id == user_id).all()
    
    def create_registration(self, db: Session, task_id: int, user_id: int, description: str) -> TaskRegistration:
        """Create a new task registration."""
        db_obj = TaskRegistration(
            task_id=task_id,
            user_id=user_id,
            description=description,
            accepted=None,
            response=""
        )
        db.add(db_obj)
        db.commit()
        db.refresh(db_obj)
        return db_obj
    
    def update_registration(self, db: Session, task_id: int, user_id: int, accepted: bool, response: str) -> TaskRegistration:
        """Update a task registration."""
        db_obj = self.get_by_task_and_student(db, task_id, user_id)
        if db_obj:
            db_obj.accepted = accepted
            db_obj.response = response
            db.add(db_obj)
            db.commit()
            db.refresh(db_obj)
        return db_obj
