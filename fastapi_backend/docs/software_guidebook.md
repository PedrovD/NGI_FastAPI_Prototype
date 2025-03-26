# NGI FastAPI Backend - Software Guidebook

## 1. Context

The NGI (Next Generation Internships) application is designed to connect students with businesses for internship opportunities. The application consists of a React frontend and a FastAPI backend. This document focuses on the backend architecture and design.

### System Context

The NGI backend serves as the central component that:
- Provides API endpoints for the frontend
- Manages authentication and authorization
- Handles data persistence
- Implements business logic

### Users and Roles

The system supports three main user roles:
1. **Students**: Can browse projects, register for tasks, and manage their skills
2. **Supervisors**: Represent businesses, create projects and tasks, and manage registrations
3. **Teachers**: Approve skills and manage businesses

## 2. Functional Overview

The backend provides the following key functionalities:

### Authentication and Authorization
- OAuth2 authentication with Google and GitHub
- Role-based access control
- JWT token management

### User Management
- User profiles and roles
- Student skills and CV management
- Supervisor and business association

### Project and Task Management
- Project creation and management
- Task definition with required skills
- Registration and acceptance workflow

### Business Management
- Business profiles and information
- Supervisor invitation system
- Business-project association

## 3. Quality Attributes

### Scalability
- Stateless API design allows horizontal scaling
- Efficient database queries with proper indexing
- Asynchronous processing where appropriate

### Maintainability
- Clean architecture with separation of concerns
- Consistent coding style and documentation
- Comprehensive test coverage

### Security
- OAuth2 authentication
- Role-based access control
- Input validation and sanitization
- HTTPS support

### Performance
- Optimized database queries
- Efficient data serialization
- Proper caching strategies

## 4. Constraints

### Technical Constraints
- Python 3.11+ compatibility
- RESTful API design
- OAuth2 authentication
- Docker deployment

### Business Constraints
- Must support the existing frontend
- Must maintain data integrity
- Must be extensible for future features

## 5. Principles

The backend follows these key design principles:

### SOLID Principles
- **Single Responsibility**: Each class has a single responsibility
- **Open/Closed**: Open for extension, closed for modification
- **Liskov Substitution**: Subtypes must be substitutable for their base types
- **Interface Segregation**: Many specific interfaces are better than one general interface
- **Dependency Inversion**: Depend on abstractions, not concretions

### Additional Principles
- **Composition over Inheritance**: Prefer composition over inheritance
- **Law of Demeter**: Minimize dependencies between components
- **Information Hiding**: Hide implementation details
- **Program to an Interface**: Depend on interfaces, not implementations
- **Encapsulate What Varies**: Isolate what varies from what stays the same
- **Cohesion**: Keep related things together
- **Separation of Concerns**: Separate different concerns into different modules

## 6. Software Architecture

### Architecture Overview

The backend follows a clean architecture with the following layers:

1. **API Layer**: FastAPI routes and endpoints
2. **Service Layer**: Business logic and use cases
3. **Repository Layer**: Data access and persistence
4. **Model Layer**: Database models and schemas

### Component Diagram

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│                 │     │                 │     │                 │
│   API Layer     │────▶│  Service Layer  │────▶│ Repository Layer│
│  (Controllers)  │     │ (Business Logic)│     │  (Data Access)  │
│                 │     │                 │     │                 │
└─────────────────┘     └─────────────────┘     └─────────────────┘
                                │                        │
                                │                        │
                                ▼                        ▼
                        ┌─────────────────┐     ┌─────────────────┐
                        │                 │     │                 │
                        │  Model Layer    │◀────│    Database     │
                        │  (Schemas)      │     │                 │
                        │                 │     │                 │
                        └─────────────────┘     └─────────────────┘
```

### Key Components

#### API Layer
- **Endpoints**: FastAPI route handlers
- **Dependencies**: Authentication and authorization
- **Validation**: Request validation with Pydantic

#### Service Layer
- **Services**: Business logic implementation
- **Use Cases**: Application-specific logic
- **Coordination**: Orchestrates repositories

#### Repository Layer
- **Repositories**: Data access patterns
- **Queries**: Database queries
- **Transactions**: Transaction management

#### Model Layer
- **Models**: SQLAlchemy ORM models
- **Schemas**: Pydantic validation schemas
- **DTOs**: Data transfer objects

## 7. Code Organization

### Project Structure

```
app/
├── api/                  # API endpoints
│   └── api_v1/           # API version 1
│       ├── endpoints/    # API endpoints
│       └── api.py        # API router
├── auth/                 # Authentication
│   ├── dependencies.py   # Auth dependencies
│   └── oauth2.py         # OAuth2 implementation
├── core/                 # Core functionality
│   └── config.py         # Application configuration
├── db/                   # Database
│   ├── init_db.py        # Database initialization
│   └── session.py        # Database session
├── models/               # Database models
├── repositories/         # Data access layer
├── schemas/              # Pydantic schemas
├── services/             # Business logic
└── main.py               # Application entry point
```

### Key Design Patterns

#### Repository Pattern
- Abstracts data access logic
- Provides a collection-like interface
- Decouples business logic from data access

#### Dependency Injection
- Inversion of control
- Improves testability
- Reduces coupling

#### Factory Pattern
- Creates objects without specifying concrete classes
- Centralizes object creation
- Simplifies object creation logic

#### Strategy Pattern
- Defines a family of algorithms
- Makes algorithms interchangeable
- Isolates algorithm implementation details

## 8. Data Model

### Entity Relationship Diagram

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│    User     │     │  Business   │     │   Project   │
├─────────────┤     ├─────────────┤     ├─────────────┤
│  user_id    │     │ business_id │     │ project_id  │
│ provider_id │     │    name     │     │    title    │
│  username   │     │ description │     │ description │
│    email    │     │  location   │     │ image_path  │
│ image_path  │     │ image_path  │     │   user_id   │
└─────────────┘     └─────────────┘     └─────────────┘
      │ │                  │                   │
      │ │                  │                   │
      │ │                  ▼                   │
      │ │           ┌─────────────┐            │
      │ │           │ Supervisor  │            │
      │ │           ├─────────────┤            │
      │ │           │   user_id   │◀───────────┘
      │ │           │ business_id │
      │ │           └─────────────┘
      │ │
      │ ▼
┌─────────────┐                         ┌─────────────┐
│   Student   │                         │    Task     │
├─────────────┤                         ├─────────────┤
│   user_id   │                         │   task_id   │
│ description │                         │ project_id  │
│   cv_path   │                         │    title    │
└─────────────┘                         │ description │
      │                                 │total_needed │
      │                                 └─────────────┘
      │                                       │
      │                                       │
      ▼                                       ▼
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│StudentSkill │     │    Skill    │     │  TaskSkill  │
├─────────────┤     ├─────────────┤     ├─────────────┤
│   user_id   │     │  skill_id   │     │   task_id   │
│  skill_id   │◀───▶│    name     │◀───▶│  skill_id   │
│ description │     │ is_pending  │     └─────────────┘
└─────────────┘     └─────────────┘
                                        ┌─────────────┐
                                        │Registration │
                                        ├─────────────┤
                                        │   task_id   │
                                        │   user_id   │
                                        │ description │
                                        │  accepted   │
                                        │  response   │
                                        └─────────────┘
```

### Key Entities

- **User**: Base user entity with authentication information
- **Student**: User with student role and skills
- **Supervisor**: User with supervisor role associated with a business
- **Teacher**: User with teacher role
- **Business**: Organization that offers projects
- **Project**: Collection of tasks created by supervisors
- **Task**: Individual task within a project requiring specific skills
- **Skill**: Capability required for tasks and possessed by students
- **Registration**: Student application for a task

## 9. Infrastructure Architecture

### Deployment

The application is designed to be deployed using Docker and Docker Compose:

```
┌─────────────────────────────────────────────┐
│              Docker Environment              │
│                                             │
│  ┌─────────────────┐    ┌─────────────────┐ │
│  │                 │    │                 │ │
│  │  FastAPI API    │    │   Database      │ │
│  │                 │    │  (SQLite/Postgres)│ │
│  └─────────────────┘    └─────────────────┘ │
│                                             │
└─────────────────────────────────────────────┘
```

### Configuration

The application is configured using environment variables:

- `SQLALCHEMY_DATABASE_URI`: Database connection string
- `SECRET_KEY`: Secret key for JWT tokens
- `FRONTEND_URL`: URL of the frontend application
- `GITHUB_CLIENT_ID`: GitHub OAuth client ID
- `GITHUB_CLIENT_SECRET`: GitHub OAuth client secret
- `GOOGLE_CLIENT_ID`: Google OAuth client ID
- `GOOGLE_CLIENT_SECRET`: Google OAuth client secret

## 10. External Interfaces

### Frontend Integration

The backend provides RESTful API endpoints for the React frontend:

- Authentication endpoints
- User management endpoints
- Project and task endpoints
- Business management endpoints

### OAuth2 Providers

The backend integrates with the following OAuth2 providers:

- Google
- GitHub

## 11. Decision Log

### Database Choice

**Decision**: Use SQLAlchemy with SQLite for development and PostgreSQL for production.

**Rationale**:
- SQLite is simple and requires no setup for development
- PostgreSQL provides better performance and reliability for production
- SQLAlchemy provides an abstraction layer that makes switching databases easy

### Authentication Strategy

**Decision**: Use OAuth2 with JWT tokens.

**Rationale**:
- OAuth2 is a widely adopted standard
- JWT tokens are stateless and can be validated without a database lookup
- Social login simplifies the user experience

### Architecture Pattern

**Decision**: Use a clean architecture with repository pattern.

**Rationale**:
- Separation of concerns improves maintainability
- Repository pattern abstracts data access
- Clean architecture makes testing easier

### API Design

**Decision**: Use FastAPI with Pydantic schemas.

**Rationale**:
- FastAPI provides automatic validation and documentation
- Pydantic schemas ensure type safety
- Async support improves performance

## 12. Development Environment

### Setup

1. Clone the repository
2. Install dependencies:

```bash
pip install -r requirements.txt
```

3. Run the application:

```bash
uvicorn app.main:app --reload
```

### Testing

Run tests with pytest:

```bash
pytest
```

### Documentation

API documentation is available at:

- Swagger UI: http://localhost:8000/api/v1/docs
- ReDoc: http://localhost:8000/api/v1/redoc

## 13. Operation and Support

### Monitoring

The application logs to standard output, which can be captured by Docker or a logging service.

### Backup and Recovery

Database backups should be performed regularly. For SQLite, this means copying the database file. For PostgreSQL, standard backup procedures apply.

### Troubleshooting

Common issues:

- **Authentication failures**: Check OAuth2 configuration
- **Database connection issues**: Verify connection string
- **API errors**: Check logs for details

## 14. Evolution

### Future Improvements

- Add caching for improved performance
- Implement full-text search for projects and tasks
- Add real-time notifications
- Implement a message system for communication
- Add analytics for project and task metrics

### Extensibility Points

The architecture is designed to be extensible in the following ways:

- New API endpoints can be added without modifying existing code
- New services can be added to implement new business logic
- New repositories can be added to support new data sources
- New models can be added to support new entities
