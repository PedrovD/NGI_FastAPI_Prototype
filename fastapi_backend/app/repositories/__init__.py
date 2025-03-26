from app.repositories.user_repository import UserRepository, StudentRepository, TeacherRepository, SupervisorRepository
from app.repositories.business_repository import BusinessRepository, InviteKeyRepository
from app.repositories.project_repository import (
    ProjectRepository,
    TaskRepository,
    SkillRepository,
    TaskSkillRepository,
    StudentSkillRepository,
    TaskRegistrationRepository
)
from app.models.user import User, Student, Teacher, Supervisor
from app.models.business import Business, InviteKey
from app.models.project import Project, Task, Skill, TaskSkill, StudentSkill, TaskRegistration

# Create repository instances
user_repository = UserRepository(model=User)
student_repository = StudentRepository(model=Student)
teacher_repository = TeacherRepository(model=Teacher)
supervisor_repository = SupervisorRepository(model=Supervisor)
business_repository = BusinessRepository(model=Business)
invite_key_repository = InviteKeyRepository(model=InviteKey)
project_repository = ProjectRepository(model=Project)
task_repository = TaskRepository(model=Task)
skill_repository = SkillRepository(model=Skill)
task_skill_repository = TaskSkillRepository(model=TaskSkill)
student_skill_repository = StudentSkillRepository(model=StudentSkill)
task_registration_repository = TaskRegistrationRepository(model=TaskRegistration)

# For convenience, export all repositories
__all__ = [
    "user_repository",
    "student_repository",
    "teacher_repository",
    "supervisor_repository",
    "business_repository",
    "invite_key_repository",
    "project_repository",
    "task_repository",
    "skill_repository",
    "task_skill_repository",
    "student_skill_repository",
    "task_registration_repository"
]
