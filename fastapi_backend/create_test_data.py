import os
import sys
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

# Add the parent directory to the path so we can import the app modules
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

# Import the models and database session
from app.db.session import Base
from app.models.user import User, Student, Teacher, Supervisor
from app.models.business import Business
from app.models.project import Project, Task, Skill

# Create a SQLite database
db_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "ngi.db")
if os.path.exists(db_path):
    os.remove(db_path)

# Create the database engine
engine = create_engine(f"sqlite:///{db_path}")

# Create all tables
Base.metadata.create_all(engine)

# Create a session
Session = sessionmaker(bind=engine)
session = Session()

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
session.add_all(businesses)
session.commit()

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
session.add_all(users)
session.commit()

# Get user IDs
student1 = session.query(User).filter_by(username="Student").first()
student2 = session.query(User).filter_by(username="Student2").first()
supervisor1 = session.query(User).filter_by(username="Supervisor").first()
supervisor2 = session.query(User).filter_by(username="Supervisor2").first()
supervisor3 = session.query(User).filter_by(username="Supervisor3").first()

# Get business IDs
business1 = session.query(Business).filter_by(name="Groen onderhoud gemeente Rheden").first()
business2 = session.query(Business).filter_by(name="Tech Innovators").first()
business3 = session.query(Business).filter_by(name="Hogeschool Arnhem en Nijmegen").first()

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
session.add_all(students)
session.commit()

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
session.add_all(supervisors)
session.commit()

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
session.add_all(skills)
session.commit()

# Create student skills
skill_snoeien = session.query(Skill).filter_by(name="Snoeien").first()
skill_planten = session.query(Skill).filter_by(name="Planten").first()
skill_shovel = session.query(Skill).filter_by(name="Shovel rijden").first()
skill_grasmaaien = session.query(Skill).filter_by(name="Grasmaaien").first()
skill_data = session.query(Skill).filter_by(name="Data Analysis").first()
skill_ai = session.query(Skill).filter_by(name="AI/ML").first()
skill_network = session.query(Skill).filter_by(name="Network Security").first()

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
session.add_all(projects)
session.commit()

# Get project IDs
project1 = session.query(Project).filter_by(title="Velp vergroenen").first()
project2 = session.query(Project).filter_by(title="Innovative AI/ML Project").first()
project3 = session.query(Project).filter_by(title="Water Management Rheden").first()
project4 = session.query(Project).filter_by(title="Blockchain Development").first()
project5 = session.query(Project).filter_by(title="Cybersecurity Enhancement").first()
project6 = session.query(Project).filter_by(title="Promotievideo Open Dag HAN").first()
project7 = session.query(Project).filter_by(title="Schoolfotos maken").first()
project8 = session.query(Project).filter_by(title="Muurschildering HAN").first()
project9 = session.query(Project).filter_by(title="Ontwikkeling HAN App").first()
project10 = session.query(Project).filter_by(title="Speeltuin ontwerp").first()
project11 = session.query(Project).filter_by(title="Renovatie Park Rheden").first()
project12 = session.query(Project).filter_by(title="Fietspaden Netwerk Uitbreiding").first()
project13 = session.query(Project).filter_by(title="Herinrichting Marktplein").first()
project14 = session.query(Project).filter_by(title="LED-Verlichting Installatie").first()

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
session.add_all(tasks)
session.commit()

# Get task IDs for registrations
task_planten_snoeien = session.query(Task).filter_by(title="Planten snoeien").first()

# Create student skills
from app.models.project import StudentSkill
student_skills = [
    StudentSkill(user_id=student1.user_id, skill_id=skill_snoeien.skill_id, description="description"),
    StudentSkill(user_id=student1.user_id, skill_id=skill_planten.skill_id, description="hele lange description: hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh"),
    StudentSkill(user_id=student1.user_id, skill_id=skill_shovel.skill_id, description=""),
    StudentSkill(user_id=student1.user_id, skill_id=skill_grasmaaien.skill_id, description=""),
    StudentSkill(user_id=student2.user_id, skill_id=skill_data.skill_id, description="De volgende skill heeft geen description"),
    StudentSkill(user_id=student2.user_id, skill_id=skill_ai.skill_id, description=""),
    StudentSkill(user_id=student2.user_id, skill_id=skill_network.skill_id, description="")
]
session.add_all(student_skills)
session.commit()

# Create task registrations
from app.models.project import TaskRegistration
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
session.add_all(task_registrations)
session.commit()

print("Test data created successfully!")
print(f"Database created at: {db_path}")
print(f"Users created: {session.query(User).count()}")
print(f"Projects created: {session.query(Project).count()}")
print(f"Tasks created: {session.query(Task).count()}")
print(f"Skills created: {session.query(Skill).count()}")

session.close()
