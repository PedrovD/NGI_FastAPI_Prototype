CREATE TABLE Users (
    userId SERIAL PRIMARY KEY,
    providerId VARCHAR(50) NOT NULL,
    email VARCHAR(256) NULL,
    username VARCHAR(50) NOT NULL,
    imagePath VARCHAR(255) NOT NULL
);

CREATE TABLE Teachers (
    userId INT PRIMARY KEY,
    FOREIGN KEY (userId) REFERENCES Users(userId)
);

CREATE TABLE Business (
    businessId SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    location VARCHAR(255) NOT NULL,
    imagePath VARCHAR(255) NOT NULL
);

CREATE TABLE InviteKeys (
    key VARCHAR(255) PRIMARY KEY,
    businessId INT,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (businessId) REFERENCES Business(businessId)
);

CREATE TABLE Supervisors (
    userId INT PRIMARY KEY,
    businessId INT NOT NULL,
    FOREIGN KEY (userId) REFERENCES Users(userId),
    FOREIGN KEY (businessId) REFERENCES Business(businessId)
);

CREATE TABLE Projects (
    projectId SERIAL PRIMARY KEY,
    userId INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    imagePath VARCHAR(255) NOT NULL,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (userId) REFERENCES Supervisors(userId)
);

CREATE TABLE Tasks (
    taskId SERIAL PRIMARY KEY,
    projectId INT NOT NULL,
    title VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    totalNeeded INT NOT NULL,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (projectId) REFERENCES Projects(projectId)
);

CREATE TABLE Skills (
    skillId SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    isPending BOOLEAN NOT NULL DEFAULT TRUE,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE TasksSkills (
    taskId INT NOT NULL,
    skillId INT NOT NULL,
    PRIMARY KEY (taskId, skillId),
    FOREIGN KEY (taskId) REFERENCES Tasks(taskId),
    FOREIGN KEY (skillId) REFERENCES Skills(skillId)
);

CREATE TABLE Students (
    userId INT PRIMARY KEY,
    description TEXT NOT NULL,
    CVPath VARCHAR(255) NULL,
    FOREIGN KEY (userId) REFERENCES Users(userId)
);

CREATE TABLE StudentsSkills (
    userId INT NOT NULL,
    skillId INT NOT NULL,
    description VARCHAR(400) NOT NULL DEFAULT '',
    PRIMARY KEY (userId, skillId),
    FOREIGN KEY (userId) REFERENCES Students(userId),
    FOREIGN KEY (skillId) REFERENCES Skills(skillId)
);

CREATE TABLE TasksRegistrations (
    taskId INT NOT NULL,
    userId INT NOT NULL,
    description TEXT NOT NULL,
    accepted BOOLEAN DEFAULT NULL,
    response VARCHAR(400) NOT NULL DEFAULT '',
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (taskId, userId),
    FOREIGN KEY (taskId) REFERENCES Tasks(taskId),
    FOREIGN KEY (userId) REFERENCES Students(userId)
);

CREATE TABLE CronJobRuns (
    runDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    emailsSent INT NOT NULL
);