# NGI FastAPI Backend

A modern, efficient backend for the NGI application built with FastAPI, SQLAlchemy, and Pydantic.

## Features

- **Modern Architecture**: Built with FastAPI, SQLAlchemy, and Pydantic
- **Clean Code**: Follows software design principles like SOLID, DRY, and KISS
- **Extensible**: Designed for easy extension and modification
- **OAuth2 Authentication**: Supports Google and GitHub authentication
- **Role-Based Access Control**: Different roles for students, teachers, and supervisors
- **Docker Support**: Easy deployment with Docker and Docker Compose
- **API Documentation**: Automatic API documentation with Swagger UI

## Architecture

The backend follows a clean architecture with the following layers:

- **API Layer**: FastAPI routes and endpoints
- **Service Layer**: Business logic and use cases
- **Repository Layer**: Data access and persistence
- **Model Layer**: Database models and schemas

## Design Principles

The backend is designed with the following principles in mind:

- **Open/Closed Principle**: Open for extension, closed for modification
- **Single Responsibility Principle**: Each class has a single responsibility
- **Dependency Inversion**: High-level modules depend on abstractions
- **Composition over Inheritance**: Prefer composition over inheritance
- **Law of Demeter**: Minimize dependencies between components
- **Information Hiding**: Hide implementation details
- **Program to an Interface**: Depend on interfaces, not implementations
- **Encapsulate What Varies**: Isolate what varies from what stays the same
- **Cohesion**: Keep related things together
- **Separation of Concerns**: Separate different concerns into different modules

## Getting Started

### Prerequisites

- Python 3.11 or higher
- Docker and Docker Compose (optional)

### Installation

1. Clone the repository
2. Install dependencies:

```bash
pip install -r requirements.txt
```

3. Run the application:

```bash
uvicorn app.main:app --reload
```

### Docker

1. Build and run with Docker Compose:

```bash
docker-compose up -d
```

## API Documentation

The API documentation is available at:

- Swagger UI: http://localhost:8000/api/v1/docs
- ReDoc: http://localhost:8000/api/v1/redoc

## Environment Variables

The application can be configured using environment variables:

- `SQLALCHEMY_DATABASE_URI`: Database connection string
- `SECRET_KEY`: Secret key for JWT tokens
- `FRONTEND_URL`: URL of the frontend application
- `GITHUB_CLIENT_ID`: GitHub OAuth client ID
- `GITHUB_CLIENT_SECRET`: GitHub OAuth client secret
- `GOOGLE_CLIENT_ID`: Google OAuth client ID
- `GOOGLE_CLIENT_SECRET`: Google OAuth client secret

## Project Structure

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

## License

This project is licensed under the MIT License.
