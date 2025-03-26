from typing import List, Optional, Dict, Any
from fastapi import HTTPException, status
from sqlalchemy.orm import Session
from app.models.project import Project, Task, Skill, TaskSkill, StudentSkill, TaskRegistration
from app.schemas.project import ProjectCreate, ProjectUpdate, ProjectWithTasksAndSkills
from app.schemas.task import TaskCreate, TaskUpdate, TaskWithSkills
from app.schemas.skill import SkillCreate, SkillUpdate
from app.repositories.project_repository import (
    ProjectRepository,
    TaskRepository,
    SkillRepository,
    TaskSkillRepository,
    StudentSkillRepository,
    TaskRegistrationRepository
)
from app.services.base import BaseService
from app.repositories import (
    project_repository,
    task_repository,
    skill_repository,
    task_skill_repository,
    student_skill_repository,
    task_registration_repository
)

class ProjectService(BaseService[Project, ProjectCreate, ProjectUpdate, ProjectRepository]):
    """Service for Project operations."""
    
    def get_by_id(self, db: Session, project_id: int) -> Optional[Project]:
        """Get a project by ID."""
        return self.repository.get_by_id(db, project_id)
    
    def get_with_tasks(self, db: Session, project_id: int) -> Optional[Project]:
        """Get a project with tasks loaded."""
        return self.repository.get_with_tasks(db, project_id)
    
    def get_by_supervisor_id(self, db: Session, user_id: int) -> List[Project]:
        """Get all projects for a supervisor."""
        return self.repository.get_by_supervisor_id(db, user_id)
    
    def get_by_business_id(self, db: Session, business_id: int) -> List[Project]:
        """Get all projects for a business."""
        return self.repository.get_by_business_id(db, business_id)
    
    def create_project(self, db: Session, user_id: int, title: str, description: str, image_path: str) -> Project:
        """
        Create a new project.
        
        Args:
            db: Database session
            user_id: Supervisor user ID
            title: Project title
            description: Project description
            image_path: Path to project image
            
        Returns:
            Created project
        """
        # Check if project name is already taken by this supervisor
        if self.repository.check_project_name_taken(db, title, user_id):
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Project with this title already exists for this supervisor"
            )
        
        # Create new project
        project_in = ProjectCreate(
            user_id=user_id,
            title=title,
            description=description,
            image_path=image_path
        )
        return self.repository.create(db, obj_in=project_in)
    
    def update_project(self, db: Session, project_id: int, obj_in: ProjectUpdate) -> Project:
        """
        Update a project.
        
        Args:
            db: Database session
            project_id: Project ID
            obj_in: Update data
            
        Returns:
            Updated project
        """
        project = self.repository.get_by_id(db, project_id)
        if not project:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Project not found"
            )
        
        # Check if title is being updated and is already taken
        if obj_in.title and obj_in.title != project.title:
            if self.repository.check_project_name_taken(db, obj_in.title, project.user_id):
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail="Project with this title already exists for this supervisor"
                )
        
        return self.update(db, id=project_id, obj_in=obj_in)
    
    def get_project_with_details(self, db: Session, project_id: int) -> ProjectWithTasksAndSkills:
        """
        Get a project with tasks and skills.
        
        Args:
            db: Database session
            project_id: Project ID
            
        Returns:
            Project with tasks and skills
        """
        project = self.repository.get_by_id(db, project_id)
        if not project:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Project not found"
            )
        
        # Get tasks for project
        tasks = task_repository.get_by_project_id(db, project_id)
        
        # Get skills for each task
        task_with_skills_list = []
        for task in tasks:
            skills = skill_repository.get_all_for_task(db, task.task_id)
            
            # Get registration counts
            registrations = task_registration_repository.get_by_task_id(db, task.task_id)
            total_registered = sum(1 for r in registrations if r.accepted is None)
            total_accepted = sum(1 for r in registrations if r.accepted is True)
            
            task_with_skills = TaskWithSkills(
                task_id=task.task_id,
                project_id=task.project_id,
                title=task.title,
                description=task.description,
                total_needed=task.total_needed,
                total_registered=total_registered,
                total_accepted=total_accepted,
                skills=skills
            )
            task_with_skills_list.append(task_with_skills)
        
        return ProjectWithTasksAndSkills(
            project_id=project.project_id,
            title=project.title,
            description=project.description,
            image_path=project.image_path,
            user_id=project.user_id,
            tasks=task_with_skills_list
        )

class TaskService(BaseService[Task, TaskCreate, TaskUpdate, TaskRepository]):
    """Service for Task operations."""
    
    def get_by_id(self, db: Session, task_id: int) -> Optional[Task]:
        """Get a task by ID."""
        return self.repository.get_by_id(db, task_id)
    
    def get_with_skills(self, db: Session, task_id: int) -> Optional[Task]:
        """Get a task with skills loaded."""
        return self.repository.get_with_skills(db, task_id)
    
    def get_by_project_id(self, db: Session, project_id: int) -> List[Task]:
        """Get all tasks for a project."""
        return self.repository.get_by_project_id(db, project_id)
    
    def create_task(self, db: Session, project_id: int, title: str, description: str, total_needed: int) -> Task:
        """
        Create a new task.
        
        Args:
            db: Database session
            project_id: Project ID
            title: Task title
            description: Task description
            total_needed: Total number of students needed
            
        Returns:
            Created task
        """
        # Create new task
        task_in = TaskCreate(
            project_id=project_id,
            title=title,
            description=description,
            total_needed=total_needed
        )
        return self.repository.create(db, obj_in=task_in)
    
    def update_task(self, db: Session, task_id: int, obj_in: TaskUpdate) -> Task:
        """
        Update a task.
        
        Args:
            db: Database session
            task_id: Task ID
            obj_in: Update data
            
        Returns:
            Updated task
        """
        task = self.repository.get_by_id(db, task_id)
        if not task:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Task not found"
            )
        
        return self.update(db, id=task_id, obj_in=obj_in)
    
    def get_task_with_skills(self, db: Session, task_id: int) -> TaskWithSkills:
        """
        Get a task with skills.
        
        Args:
            db: Database session
            task_id: Task ID
            
        Returns:
            Task with skills
        """
        task = self.repository.get_by_id(db, task_id)
        if not task:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Task not found"
            )
        
        # Get skills for task
        skills = skill_repository.get_all_for_task(db, task_id)
        
        # Get registration counts
        registrations = task_registration_repository.get_by_task_id(db, task_id)
        total_registered = sum(1 for r in registrations if r.accepted is None)
        total_accepted = sum(1 for r in registrations if r.accepted is True)
        
        return TaskWithSkills(
            task_id=task.task_id,
            project_id=task.project_id,
            title=task.title,
            description=task.description,
            total_needed=task.total_needed,
            total_registered=total_registered,
            total_accepted=total_accepted,
            skills=skills
        )

class SkillService(BaseService[Skill, SkillCreate, SkillUpdate, SkillRepository]):
    """Service for Skill operations."""
    
    def get_by_id(self, db: Session, skill_id: int) -> Optional[Skill]:
        """Get a skill by ID."""
        return self.repository.get_by_id(db, skill_id)
    
    def get_by_name(self, db: Session, name: str) -> Optional[Skill]:
        """Get a skill by name."""
        return self.repository.get_by_name(db, name)
    
    def get_all(self, db: Session) -> List[Skill]:
        """Get all skills."""
        return self.repository.get_multi(db)
    
    def create_skill(self, db: Session, name: str, is_pending: bool = True) -> Skill:
        """
        Create a new skill.
        
        Args:
            db: Database session
            name: Skill name
            is_pending: Whether the skill is pending approval
            
        Returns:
            Created skill
        """
        # Check if skill already exists
        existing_skill = self.repository.get_by_name(db, name)
        if existing_skill:
            return existing_skill
        
        # Create new skill
        skill_in = SkillCreate(
            name=name
        )
        skill = self.repository.create(db, obj_in=skill_in)
        skill.is_pending = is_pending
        db.add(skill)
        db.commit()
        db.refresh(skill)
        
        return skill
    
    def update_skill(self, db: Session, skill_id: int, obj_in: SkillUpdate) -> Skill:
        """
        Update a skill.
        
        Args:
            db: Database session
            skill_id: Skill ID
            obj_in: Update data
            
        Returns:
            Updated skill
        """
        skill = self.repository.get_by_id(db, skill_id)
        if not skill:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Skill not found"
            )
        
        # Check if name is being updated and is already taken
        if obj_in.name and obj_in.name != skill.name:
            existing_skill = self.repository.get_by_name(db, obj_in.name)
            if existing_skill and existing_skill.skill_id != skill_id:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail="Skill with this name already exists"
                )
        
        return self.update(db, id=skill_id, obj_in=obj_in)
    
    def update_skill_acceptance(self, db: Session, skill_id: int, is_pending: bool) -> Skill:
        """
        Update skill acceptance status.
        
        Args:
            db: Database session
            skill_id: Skill ID
            is_pending: Whether the skill is pending approval
            
        Returns:
            Updated skill
        """
        skill = self.repository.get_by_id(db, skill_id)
        if not skill:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Skill not found"
            )
        
        skill.is_pending = is_pending
        db.add(skill)
        db.commit()
        db.refresh(skill)
        
        return skill

class TaskSkillService:
    """Service for TaskSkill operations."""
    
    def __init__(self, repository: TaskSkillRepository):
        self.repository = repository
    
    def get_by_task_and_skill(self, db: Session, task_id: int, skill_id: int) -> Optional[TaskSkill]:
        """Get a task-skill association by task and skill IDs."""
        return self.repository.get_by_task_and_skill(db, task_id, skill_id)
    
    def get_by_task_id(self, db: Session, task_id: int) -> List[TaskSkill]:
        """Get all task-skill associations for a task."""
        return self.repository.get_by_task_id(db, task_id)
    
    def create_task_skill(self, db: Session, task_id: int, skill_id: int) -> TaskSkill:
        """
        Create a new task-skill association.
        
        Args:
            db: Database session
            task_id: Task ID
            skill_id: Skill ID
            
        Returns:
            Created task-skill association
        """
        # Check if association already exists
        existing_association = self.repository.get_by_task_and_skill(db, task_id, skill_id)
        if existing_association:
            return existing_association
        
        return self.repository.create_task_skill(db, task_id, skill_id)
    
    def update_task_skills(self, db: Session, task_id: int, skill_ids: List[int]) -> None:
        """
        Update the skills for a task.
        
        Args:
            db: Database session
            task_id: Task ID
            skill_ids: List of skill IDs
        """
        self.repository.update_task_skills(db, task_id, skill_ids)

class StudentSkillService:
    """Service for StudentSkill operations."""
    
    def __init__(self, repository: StudentSkillRepository):
        self.repository = repository
    
    def get_by_student_and_skill(self, db: Session, user_id: int, skill_id: int) -> Optional[StudentSkill]:
        """Get a student-skill association by student and skill IDs."""
        return self.repository.get_by_student_and_skill(db, user_id, skill_id)
    
    def get_by_student_id(self, db: Session, user_id: int) -> List[StudentSkill]:
        """Get all student-skill associations for a student."""
        return self.repository.get_by_student_id(db, user_id)
    
    def create_student_skill(self, db: Session, user_id: int, skill_id: int, description: str = "") -> StudentSkill:
        """
        Create a new student-skill association.
        
        Args:
            db: Database session
            user_id: Student user ID
            skill_id: Skill ID
            description: Skill description
            
        Returns:
            Created student-skill association
        """
        # Check if association already exists
        existing_association = self.repository.get_by_student_and_skill(db, user_id, skill_id)
        if existing_association:
            return existing_association
        
        return self.repository.create_student_skill(db, user_id, skill_id, description)
    
    def update_student_skills(self, db: Session, user_id: int, skill_ids: List[int]) -> None:
        """
        Update the skills for a student.
        
        Args:
            db: Database session
            user_id: Student user ID
            skill_ids: List of skill IDs
        """
        self.repository.update_student_skills(db, user_id, skill_ids)

class TaskRegistrationService:
    """Service for TaskRegistration operations."""
    
    def __init__(self, repository: TaskRegistrationRepository):
        self.repository = repository
    
    def get_by_task_and_student(self, db: Session, task_id: int, user_id: int) -> Optional[TaskRegistration]:
        """Get a task registration by task and student IDs."""
        return self.repository.get_by_task_and_student(db, task_id, user_id)
    
    def get_by_task_id(self, db: Session, task_id: int) -> List[TaskRegistration]:
        """Get all task registrations for a task."""
        return self.repository.get_by_task_id(db, task_id)
    
    def get_by_student_id(self, db: Session, user_id: int) -> List[TaskRegistration]:
        """Get all task registrations for a student."""
        return self.repository.get_by_student_id(db, user_id)
    
    def create_registration(self, db: Session, task_id: int, user_id: int, description: str) -> TaskRegistration:
        """
        Create a new task registration.
        
        Args:
            db: Database session
            task_id: Task ID
            user_id: Student user ID
            description: Registration description
            
        Returns:
            Created task registration
        """
        # Check if registration already exists
        existing_registration = self.repository.get_by_task_and_student(db, task_id, user_id)
        if existing_registration:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Registration already exists"
            )
        
        return self.repository.create_registration(db, task_id, user_id, description)
    
    def update_registration(self, db: Session, task_id: int, user_id: int, accepted: bool, response: str) -> TaskRegistration:
        """
        Update a task registration.
        
        Args:
            db: Database session
            task_id: Task ID
            user_id: Student user ID
            accepted: Whether the registration is accepted
            response: Response message
            
        Returns:
            Updated task registration
        """
        registration = self.repository.get_by_task_and_student(db, task_id, user_id)
        if not registration:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Registration not found"
            )
        
        return self.repository.update_registration(db, task_id, user_id, accepted, response)

# Create service instances
project_service = ProjectService(repository=project_repository)
task_service = TaskService(repository=task_repository)
skill_service = SkillService(repository=skill_repository)
task_skill_service = TaskSkillService(repository=task_skill_repository)
student_skill_service = StudentSkillService(repository=student_skill_repository)
task_registration_service = TaskRegistrationService(repository=task_registration_repository)
