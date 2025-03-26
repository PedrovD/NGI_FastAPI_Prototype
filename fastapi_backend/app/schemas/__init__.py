# Import all schemas
from app.schemas.user import *
from app.schemas.business import *
from app.schemas.project import *
from app.schemas.task import *
from app.schemas.skill import *

# Update forward references to resolve circular dependencies
from app.schemas.business import BusinessWithSupervisors

# Explicitly update forward references for models with circular dependencies
BusinessWithSupervisors.model_rebuild()
