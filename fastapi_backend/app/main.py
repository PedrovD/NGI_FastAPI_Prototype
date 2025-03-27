from fastapi import FastAPI, Depends, HTTPException, Response, Cookie
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from typing import Dict, Any, List, Optional
from pydantic import BaseModel
import uvicorn
import json

# Database imports
from app.db.session import get_db, Base, engine
from app.models.user import User, Student, Teacher, Supervisor
from app.models.business import Business
from app.models.project import Project, Task, Skill, StudentSkill, TaskRegistration

import logging

logging.basicConfig(level=logging.DEBUG)

# Create tables
Base.metadata.create_all(bind=engine)

app = FastAPI()

# Configure CORS
origins = [
    "http://localhost",
    "http://localhost:5173",  # Frontend development server
    "http://localhost:3000",
    "http://localhost:8000",
    "*",
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Models for request/response
class UserResponse(BaseModel):
    user_id: int
    username: str
    email: str
    image_path: str
    role: str
    business_id: Optional[int] = None

class LoginRequest(BaseModel):
    user_id: int

# Create test data on startup
@app.on_event("startup")
async def startup_event():
    db = next(get_db())
    
    # Check if we already have users
    if db.query(User).count() > 0:
            # Drop all tables
        Base.metadata.drop_all(bind=engine)

        # Recreate all tables
        Base.metadata.create_all(bind=engine)
    
    # Create businesses
    businesses = [
        Business(
            name="Groen onderhoud gemeente Rheden",
            description="Groen onderhoud in de gemeente Rheden",
            location="Rheden",
            image_path="/89vsdj38vg.png"
        ),
        Business(
            name="Tech Innovators",
            description="Innovative Tech Solutions",
            location="Amsterdam",
            image_path="/tech_innovators.jpeg"
        ),
        Business(
            name="Hogeschool Arnhem en Nijmegen",
            description="Open Up New Horizons",
            location="Arnhem/Nijmegen",
            image_path="/han_logo.png"
        )
    ]
    db.add_all(businesses)
    db.commit()
    
    # Create users
    users = [
        User(
            provider_id="0",
            username="Student",
            email="test@email.com",
            image_path="/5aabf84d67.jpg"
        ),
        User(
            provider_id="0",
            username="Supervisor",
            email="test2@email.com",
            image_path="/1234jkvs9s.jpg"
        ),
        User(
            provider_id="0",
            username="Supervisor2",
            email="test3@email.com",
            image_path="/supervisor2.jpg"
        ),
        User(
            provider_id="0",
            username="Student2",
            email="test4@email.com",
            image_path="/student2.jpg"
        ),
        User(
            provider_id="0",
            username="Supervisor3",
            email="test5@email.com",
            image_path="/supervisor2.jpg"
        )
    ]
    db.add_all(users)
    db.commit()
    
    # Get user IDs
    student1 = db.query(User).filter_by(username="Student").first()
    student2 = db.query(User).filter_by(username="Student2").first()
    supervisor1 = db.query(User).filter_by(username="Supervisor").first()
    supervisor2 = db.query(User).filter_by(username="Supervisor2").first()
    supervisor3 = db.query(User).filter_by(username="Supervisor3").first()
    
    # Get business IDs
    business1 = db.query(Business).filter_by(name="Groen onderhoud gemeente Rheden").first()
    business2 = db.query(Business).filter_by(name="Tech Innovators").first()
    business3 = db.query(Business).filter_by(name="Hogeschool Arnhem en Nijmegen").first()
    
    # Create student profiles
    students = [
        Student(
            user_id=student1.user_id,
            description="Ik ben een student die graag in de groenvoorziening werkt",
            cv_path="/1234jkvs9s.pdf"
        ),
        Student(
            user_id=student2.user_id,
            description="Tech-savvy student looking for innovative projects",
            cv_path="/2345abcdef.pdf"
        )
    ]
    db.add_all(students)
    db.commit()
    
    # Create supervisor profiles
    supervisors = [
        Supervisor(
            user_id=supervisor1.user_id,
            business_id=business1.business_id
        ),
        Supervisor(
            user_id=supervisor2.user_id,
            business_id=business2.business_id
        ),
        Supervisor(
            user_id=supervisor3.user_id,
            business_id=business3.business_id
        )
    ]
    db.add_all(supervisors)
    db.commit()
    
    # Create skills
    skill_names = [
        'PHP', 'SQL', 'Java', 'JavaScript', 'HTML', 'CSS',
        'Snoeien', 'Planten', 'Shovel rijden', 'Grasmaaien',
        'Network Security', 'Cloud Computing', 'AI/ML',
        'Data Analysis', 'Blockchain Development',
        'Water Management', 'Security Analysis',
        'Video Production', 'Video Editing',
        'Photography', 'Photo Editing',
        'Graphic Design', 'Schilderen',
        'UI/UX Design', 'Mobile App Development', 'Tekenen', 'Landschapsontwerp',
        'Constructiewerk',
        'Civiele Techniek',
        'Veiligheidsmanagement',
        'Tuinieren',
        'Stedelijke Planning',
        'Elektrotechniek',
        'Kwaliteitscontrole'
    ]
    skills = [Skill(name=name, is_pending=False) for name in skill_names]
    db.add_all(skills)
    db.commit()
    
    # Get skill references
    skill_snoeien = db.query(Skill).filter_by(name="Snoeien").first()
    skill_planten = db.query(Skill).filter_by(name="Planten").first()
    skill_shovel = db.query(Skill).filter_by(name="Shovel rijden").first()
    skill_grasmaaien = db.query(Skill).filter_by(name="Grasmaaien").first()
    skill_data = db.query(Skill).filter_by(name="Data Analysis").first()
    skill_ai = db.query(Skill).filter_by(name="AI/ML").first()
    skill_network = db.query(Skill).filter_by(name="Network Security").first()
    
    # Create projects
    projects = [
        Project(
            user_id=supervisor1.user_id,
            title="Velp vergroenen",
            description="Vergroen Velp voor 100 SP",
            image_path="/38dmwn40sj.jpg"
        ),
        Project(
            user_id=supervisor2.user_id,
            title="Innovative AI/ML Project",
            description="Cutting-edge AI/ML research and development",
            image_path="/ai_ml_project.jpeg"
        ),
        Project(
            user_id=supervisor1.user_id,
            title="Water Management Rheden",
            description="Managing water resources in Rheden",
            image_path="/water_management.jpeg"
        ),
        Project(
            user_id=supervisor2.user_id,
            title="Blockchain Development",
            description="Blockchain technology research and implementation",
            image_path="/blockchain_project.jpeg"
        ),
        Project(
            user_id=supervisor2.user_id,
            title="Cybersecurity Enhancement",
            description="Improving cybersecurity measures",
            image_path="/cybersecurity_project.jpeg"
        ),
        Project(
            user_id=supervisor3.user_id,
            title="Promotievideo Open Dag HAN",
            description="Het maken van een promotievideo voor de open dag van HAN",
            image_path="/open_dag_han.jpeg"
        ),
        Project(
            user_id=supervisor3.user_id,
            title="Schoolfotos maken",
            description="Het maken van schoolfoto's voor de Hogeschool Arnhem en Nijmegen",
            image_path="/schoolfotos.jpeg"
        ),
        Project(
            user_id=supervisor3.user_id,
            title="Muurschildering HAN",
            description="Het maken van een muurschildering op een van de gebouwen van HAN",
            image_path="/muurschildering.jpeg"
        ),
        Project(
            user_id=supervisor3.user_id,
            title="Ontwikkeling HAN App",
            description="Ontwikkelen van een mobiele app voor studenten en medewerkers van HAN",
            image_path="/han_app.jpeg"
        ),
        Project(
            user_id=supervisor1.user_id,
            title="Speeltuin ontwerp",
            description="Ontwerp voor een nieuwe speeltuin",
            image_path="/speeltuin_ontwerp.jpeg"
        ),
        Project(
            user_id=supervisor1.user_id,
            title="Renovatie Park Rheden",
            description="Renovatie van het centrale park in Rheden om de recreatieve faciliteiten te verbeteren",
            image_path="/park.jpeg"
        ),
        Project(
            user_id=supervisor1.user_id,
            title="Fietspaden Netwerk Uitbreiding",
            description="Uitbreiding van het netwerk van fietspaden om veilig fietsen in Rheden te bevorderen",
            image_path="/fietspad.jpeg"
        ),
        Project(
            user_id=supervisor1.user_id,
            title="Herinrichting Marktplein",
            description="Herinrichting van het marktplein in Rheden met nieuwe bestrating en groenvoorziening",
            image_path="/bouwvakkers.jpeg"
        ),
        Project(
            user_id=supervisor1.user_id,
            title="LED-Verlichting Installatie",
            description="Installatie van energie-efficiënte LED-verlichting in de belangrijkste openbare gebieden van Rheden",
            image_path="/verlichting.jpeg"
        )
    ]
    db.add_all(projects)
    db.commit()
    
    # Get project references
    project1 = db.query(Project).filter_by(title="Velp vergroenen").first()
    project2 = db.query(Project).filter_by(title="Innovative AI/ML Project").first()
    project3 = db.query(Project).filter_by(title="Water Management Rheden").first()
    project4 = db.query(Project).filter_by(title="Blockchain Development").first()
    project5 = db.query(Project).filter_by(title="Cybersecurity Enhancement").first()
    project6 = db.query(Project).filter_by(title="Promotievideo Open Dag HAN").first()
    project7 = db.query(Project).filter_by(title="Schoolfotos maken").first()
    project8 = db.query(Project).filter_by(title="Muurschildering HAN").first()
    project9 = db.query(Project).filter_by(title="Ontwikkeling HAN App").first()
    project10 = db.query(Project).filter_by(title="Speeltuin ontwerp").first()
    project11 = db.query(Project).filter_by(title="Renovatie Park Rheden").first()
    project12 = db.query(Project).filter_by(title="Fietspaden Netwerk Uitbreiding").first()
    project13 = db.query(Project).filter_by(title="Herinrichting Marktplein").first()
    project14 = db.query(Project).filter_by(title="LED-Verlichting Installatie").first()
    
    # Create tasks
    tasks = [
        # Project 1 tasks
        Task(project_id=project1.project_id, title="Planten snoeien", description="Het snoeien van planten in de gemeente Rheden", total_needed=5),
        Task(project_id=project1.project_id, title="Gras maaien", description="Het maaien van gras in de gemeente Rheden", total_needed=3),
        Task(project_id=project1.project_id, title="Bomen planten", description="Het planten van bomen in de gemeente Rheden", total_needed=2),
        Task(project_id=project1.project_id, title="Website maken", description="Een website maken voor de groen onderhoud tak van de gemeente Rheden.", total_needed=4),
        
        # Project 2 tasks
        Task(project_id=project2.project_id, title="Data Preparation", description="Prepare datasets for machine learning models", total_needed=3),
        Task(project_id=project2.project_id, title="Model Training", description="Train various machine learning models", total_needed=4),
        Task(project_id=project2.project_id, title="Result Analysis", description="Analyze the results and metrics from trained models", total_needed=2),
        
        # Project 3 tasks
        Task(project_id=project3.project_id, title="Irrigation Setup", description="Setting up irrigation systems", total_needed=5),
        Task(project_id=project3.project_id, title="Water Testing", description="Testing water quality and levels", total_needed=3),
        
        # Project 4 tasks
        Task(project_id=project4.project_id, title="Smart Contract Development", description="Developing smart contracts on blockchain", total_needed=4),
        Task(project_id=project4.project_id, title="Blockchain Network Setup", description="Setting up a blockchain network", total_needed=3),
        
        # Project 5 tasks
        Task(project_id=project5.project_id, title="Vulnerability Assessment", description="Assessing system vulnerabilities", total_needed=2),
        Task(project_id=project5.project_id, title="Implement Security Protocols", description="Implementing new security protocols", total_needed=3),
        
        # Project 6 tasks
        Task(project_id=project6.project_id, title="Video Opnamen", description="Opnemen van video op locatie tijdens de open dag", total_needed=4),
        Task(project_id=project6.project_id, title="Video Editing", description="Bewerken van de opgenomen video's voor promotiedoeleinden", total_needed=2),
        
        # Project 7 tasks
        Task(project_id=project7.project_id, title="Fotografie Setup", description="Opzetten van de fotografielocaties", total_needed=3),
        Task(project_id=project7.project_id, title="Foto Bewerking", description="Bewerken van de gemaakte foto's voor gebruik in schoolmateriaal", total_needed=2),
        
        # Project 8 tasks
        Task(project_id=project8.project_id, title="Ontwerp Voorbereiding", description="Ontwerpen en voorbereiden van de muurschildering", total_needed=2),
        Task(project_id=project8.project_id, title="Schilderen", description="Uitvoeren van de muurschildering", total_needed=5),
        
        # Project 9 tasks
        Task(project_id=project9.project_id, title="App Design", description="Ontwerpen van de gebruikersinterface voor de HAN app", total_needed=3),
        Task(project_id=project9.project_id, title="App Development", description="Ontwikkelen van de app functies", total_needed=4),
        
        # Project 10 tasks
        Task(project_id=project10.project_id, title="Ontwerptekening", description="Ontwerp maken voor de nieuwe speeltuin", total_needed=2),
        
        # Project 11 tasks
        Task(project_id=project11.project_id, title="Landschapsontwerp", description="Ontwerpen van nieuwe landschapsarchitectuur voor het park", total_needed=3),
        Task(project_id=project11.project_id, title="Speeltoestellen Plaatsen", description="Installeren van nieuwe speeltoestellen", total_needed=2),
        
        # Project 12 tasks
        Task(project_id=project12.project_id, title="Wegdek Voorbereiden", description="Voorbereiden van de ondergrond voor het leggen van nieuwe fietspaden", total_needed=4),
        Task(project_id=project12.project_id, title="Wegmarkeringen Aanbrengen", description="Aanbrengen van wegmarkeringen op de nieuwe fietspaden", total_needed=3),
        
        # Project 13 tasks
        Task(project_id=project13.project_id, title="Plaatsing Groenvoorziening", description="Plaatsen van nieuwe bomen en planten op het marktplein", total_needed=4),
        Task(project_id=project13.project_id, title="Straatmeubilair Installeren", description="Installeren van nieuw straatmeubilair zoals bankjes en afvalbakken", total_needed=3),
        
        # Project 14 tasks
        Task(project_id=project14.project_id, title="Elektrische Bedrading", description="Aanleggen van elektrische bedrading voor de LED-verlichting", total_needed=5),
        Task(project_id=project14.project_id, title="Verlichting Testen", description="Testen van de geïnstalleerde LED-verlichting op functionaliteit", total_needed=2)
    ]
    db.add_all(tasks)
    db.commit()
    
    # Get task for registrations
    task_planten_snoeien = db.query(Task).filter_by(title="Planten snoeien").first()
    
    # Create student skills
    student_skills = [
        StudentSkill(user_id=student1.user_id, skill_id=skill_snoeien.skill_id, description="description"),
        StudentSkill(user_id=student1.user_id, skill_id=skill_planten.skill_id, description="hele lange description: hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh"),
        StudentSkill(user_id=student1.user_id, skill_id=skill_shovel.skill_id, description=""),
        StudentSkill(user_id=student1.user_id, skill_id=skill_grasmaaien.skill_id, description=""),
        StudentSkill(user_id=student2.user_id, skill_id=skill_data.skill_id, description="De volgende skill heeft geen description"),
        StudentSkill(user_id=student2.user_id, skill_id=skill_ai.skill_id, description=""),
        StudentSkill(user_id=student2.user_id, skill_id=skill_network.skill_id, description="")
    ]
    db.add_all(student_skills)
    db.commit()
    
    # Create task registrations
    task_registrations = [
        TaskRegistration(
            task_id=task_planten_snoeien.task_id,
            user_id=student1.user_id,
            description="Ik wil mij graag aanmelden want ...",
            accepted=None,
            response=""
        ),
        TaskRegistration(
            task_id=task_planten_snoeien.task_id,
            user_id=student2.user_id,
            description="Ik wil mij graag nog een keer aanmelden want ...",
            accepted=None,
            response=""
        )
    ]
    db.add_all(task_registrations)
    db.commit()
    
    print("Test data created successfully")

# Authentication endpoints
@app.post("/api/v1/login")
async def login(
    request: LoginRequest,
    response: Response,
    db: Session = Depends(get_db)
):
    user_id = request.user_id
    
    # Get the user by ID
    user = db.query(User).filter(User.user_id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    
    # Set a cookie with the user ID
    response.set_cookie(
        key="user_id",
        value=str(user_id),
        httponly=True,
        max_age=3600 * 24 * 7,  # 7 days
        samesite="lax"
    )
    
    # Determine user role
    role = "unknown"
    business_id = None
    
    student = db.query(Student).filter(Student.user_id == user.user_id).first()
    if student:
        role = "student"
    
    teacher = db.query(Teacher).filter(Teacher.user_id == user.user_id).first()
    if teacher:
        role = "teacher"
    
    supervisor = db.query(Supervisor).filter(Supervisor.user_id == user.user_id).first()
    if supervisor:
        role = "supervisor"
        business_id = supervisor.business_id
    
    return {
        "access_token": "dummy_token",
        "token_type": "bearer",
        "user_id": user_id,
        "user_type": role,
        "message": f"Logged in as {user.username}"
    }

@app.post("/api/v1/logout")
async def logout(response: Response):
    response.delete_cookie(key="user_id")
    return {"message": "Logged out successfully"}

@app.get("/api/v1/verify")
async def verify(
    user_id: Optional[str] = Cookie(None),
    db: Session = Depends(get_db)
):
    if not user_id:
        return {"type": "NONE"}
    
    try:
        user_id_int = int(user_id)
        user = db.query(User).filter(User.user_id == user_id_int).first()
        
        if not user:
            return {"type": "NONE"}
        
        # Determine user role
        student = db.query(Student).filter(Student.user_id == user.user_id).first()
        if student:
            return {"type": "STUDENT", "user_id": user.user_id}
        
        teacher = db.query(Teacher).filter(Teacher.user_id == user.user_id).first()
        if teacher:
            return {"type": "TEACHER", "user_id": user.user_id}
        
        supervisor = db.query(Supervisor).filter(Supervisor.user_id == user.user_id).first()
        if supervisor:
            return {"type": "SUPERVISOR", "user_id": user.user_id, "business_id": supervisor.business_id}
        
        return {"type": "INVALID", "user_id": user.user_id}
    
    except (ValueError, TypeError):
        return {"type": "NONE"}

@app.get("/api/v1/users")
async def get_users(db: Session = Depends(get_db)):
    users = []
    all_users = db.query(User).all()
    
    for user in all_users:
        user_data = {
            "user_id": user.user_id,
            "username": user.username,
            "image_path": user.image_path,
            "role": "unknown"
        }
        
        # Check role
        student = db.query(Student).filter(Student.user_id == user.user_id).first()
        if student:
            user_data["role"] = "student"
            users.append(user_data)
            continue
            
        teacher = db.query(Teacher).filter(Teacher.user_id == user.user_id).first()
        if teacher:
            user_data["role"] = "teacher"
            users.append(user_data)
            continue
            
        supervisor = db.query(Supervisor).filter(Supervisor.user_id == user.user_id).first()
        if supervisor:
            user_data["role"] = "supervisor"
            user_data["business_id"] = supervisor.business_id
            users.append(user_data)
            continue
    
    return {"users": users}

# Students endpoints
@app.get("/students/{student_id}")
async def get_student(student_id: int, db: Session = Depends(get_db)):
    # Import the skill schemas
    from app.schemas.skill import GetSkillDto, GetSkillWithDescriptionDto
    
    # Get the student by ID
    student = db.query(Student).filter(Student.user_id == student_id).first()
    if not student:
        raise HTTPException(status_code=404, detail="Student not found")
    
    # Get the user information
    user = db.query(User).filter(User.user_id == student_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    
    # Get student skills
    student_skills = db.query(StudentSkill).filter(StudentSkill.user_id == student_id).all()
    
    skills_data = []
    for student_skill in student_skills:
        skill = db.query(Skill).filter(Skill.skill_id == student_skill.skill_id).first()
        if skill:
            # Create skill DTO
            skill_dto = GetSkillDto(
                skill_id=skill.skill_id,
                name=skill.name,
                is_pending=skill.is_pending
            )
            
            # Create skill with description DTO
            skill_with_desc_dto = GetSkillWithDescriptionDto(
                skill=skill_dto,
                description=student_skill.description
            )
            
            skills_data.append(skill_with_desc_dto.dict(by_alias=True))
    
    # Return student profile data
    return {
        "userId": student.user_id,
        "username": user.username,
        "description": student.description,
        "profilePicture": {"path": user.image_path},
        "cv": {"path": student.cv_path},
        "skills": skills_data
    }

@app.put("/students/skills")
async def update_student_skills(
    skills: List[int],
    user_id: Optional[str] = Cookie(None),
    db: Session = Depends(get_db)
):
    if not user_id:
        raise HTTPException(status_code=401, detail="Not authenticated")
    
    try:
        user_id_int = int(user_id)
        student = db.query(Student).filter(Student.user_id == user_id_int).first()
        
        if not student:
            raise HTTPException(status_code=403, detail="Not a student")
        
        # In a real implementation, you would update the student's skills in the database
        # For now, we'll just return success
        return {"message": "Skills updated successfully"}
    
    except (ValueError, TypeError):
        raise HTTPException(status_code=401, detail="Invalid user ID")

@app.get("/skills")
async def get_skills(db: Session = Depends(get_db)):
    # Import the skill schema
    from app.schemas.skill import GetSkillDto
    
    # Fetch skills from the database
    skills = db.query(Skill).all()
    
    # Convert to response format using the schema
    result = [
        GetSkillDto(
            skill_id=skill.skill_id,
            name=skill.name,
            is_pending=skill.is_pending
        ).dict(by_alias=True)
        for skill in skills
    ]
    
    return result

# Projects endpoints
@app.get("/projects/all")
async def get_all_projects(db: Session = Depends(get_db)):
    # Group projects by business
    businesses = db.query(Business).all()
    result = []
    
    for business in businesses:
        # Get supervisors for this business
        supervisors = db.query(Supervisor).filter(Supervisor.business_id == business.business_id).all()
        supervisor_ids = [supervisor.user_id for supervisor in supervisors]
        
        # Get projects for these supervisors
        projects = db.query(Project).filter(Project.user_id.in_(supervisor_ids) if supervisor_ids else False).all()
        
        if not projects:
            continue
            
        projects_data = []
        for project in projects:
            # Get tasks for this project
            tasks = db.query(Task).filter(Task.project_id == project.project_id).all()
            tasks_data = []
            
            for task in tasks:
                tasks_data.append({
                    "taskId": task.task_id,
                    "title": task.title,
                    "description": task.description,
                    "skills": []  # Empty skills for now
                })
            
            projects_data.append({
                "id": project.project_id,
                "title": project.title,
                "description": project.description,
                "photo": {"path": project.image_path},
                "tasks": tasks_data
            })
        
        if projects_data:
            result.append({
                "business": {
                    "id": business.business_id,
                    "name": business.name,
                    "description": business.description or "",
                    "location": business.location or "",
                    "photo": {"path": "/default_profile_picture.png"}
                },
                "projects": projects_data
            })
    
    return result

@app.get("/api/v1/projects")
async def get_projects(db: Session = Depends(get_db)):
    projects = db.query(Project).all()
    result = []
    
    for project in projects:
        supervisor = db.query(Supervisor).filter(Supervisor.user_id == project.user_id).first()
        business = None
        if supervisor:
            business = db.query(Business).filter(Business.business_id == supervisor.business_id).first()
        
        result.append({
            "project_id": project.project_id,
            "title": project.title,
            "description": project.description,
            "image_path": project.image_path,
            "supervisor_id": project.user_id,
            "business_name": business.name if business else "Unknown"
        })
    
    return result

@app.get("/api/v1/projects/{project_id}")
async def get_project(project_id: int, db: Session = Depends(get_db)):
    project = db.query(Project).filter(Project.project_id == project_id).first()
    if not project:
        raise HTTPException(status_code=404, detail="Project not found")
    
    supervisor = db.query(Supervisor).filter(Supervisor.user_id == project.user_id).first()
    business = None
    if supervisor:
        business = db.query(Business).filter(Business.business_id == supervisor.business_id).first()
    
    tasks = db.query(Task).filter(Task.project_id == project_id).all()
    tasks_data = []
    
    for task in tasks:
        tasks_data.append({
            "task_id": task.task_id,
            "title": task.title,
            "description": task.description,
            "total_needed": task.total_needed
        })
    
    return {
        "project_id": project.project_id,
        "title": project.title,
        "description": project.description,
        "image_path": project.image_path,
        "supervisor_id": project.user_id,
        "business_name": business.name if business else "Unknown",
        "tasks": tasks_data
    }

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
