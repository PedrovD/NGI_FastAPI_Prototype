-- Inserting into Business table
INSERT INTO Business (name, description, location, imagePath)
VALUES ('Groen onderhoud gemeente Rheden', 'Groen onderhoud in de gemeente Rheden', 'Rheden', '/89vsdj38vg.png'),
       ('Tech Innovators', 'Innovative Tech Solutions', 'Amsterdam', '/tech_innovators.jpeg'),
       ('Hogeschool Arnhem en Nijmegen', 'Open Up New Horizons', 'Arnhem/Nijmegen', '/han_logo.png');

-- Inserting into Users table
INSERT INTO Users (providerId, username, imagePath, email)
VALUES (0, 'Student', '/5aabf84d67.jpg', 'test@email.com'),
       (0, 'Supervisor', '/1234jkvs9s.jpg', 'test2@email.com'),
       (0, 'Supervisor2', '/supervisor2.jpg', 'test3@email.com'),
       (0, 'Student2', '/student2.jpg', 'test4@email.com'),
       (0, 'Supervisor3', '/supervisor2.jpg', 'test5@email.com');

-- Setting variables for supervisor and student references
DO $$
    DECLARE
        studentId1 INT;
        studentId2 INT;
        supervisorId1 INT;
        supervisorId2 INT;
        supervisorId3 INT;
        projectId1 INT;
        projectId2 INT;
        projectId3 INT;
        projectId4 INT;
        projectId5 INT;
        projectId6 INT;
        projectId7 INT;
        projectId8 INT;
        projectId9 INT;
        projectId10 INT;
        projectId11 INT;
        projectId12 INT;
        projectId13 INT;
        projectId14 INT;
        taskId1 INT;
        taskId2 INT;
        taskId3 INT;
        taskId4 INT;
        taskId5 INT;
        taskId6 INT;
        taskId7 INT;
        taskId8 INT;
        taskId9 INT;
        taskId10 INT;
        taskId11 INT;
        taskId12 INT;
        taskId13 INT;
        taskId14 INT;
        taskId15 INT;
        taskId16 INT;
        taskId17 INT;
        taskId18 INT;
        taskId19 INT;
        taskId20 INT;
        taskId21 INT;
        taskId22 INT;
        taskId23 INT;
        taskId24 INT;
        taskId25 INT;
        taskId26 INT;
        taskId27 INT;
        taskId28 INT;
        taskId29 INT;
        taskId30 INT;
    BEGIN
        SELECT userId INTO studentId1 FROM Users WHERE username = 'Student';
        SELECT userId INTO studentId2 FROM Users WHERE username = 'Student2';
        SELECT userId INTO supervisorId1 FROM Users WHERE username = 'Supervisor';
        SELECT userId INTO supervisorId2 FROM Users WHERE username = 'Supervisor2';
        SELECT userId INTO supervisorId3 FROM Users WHERE username = 'Supervisor3';

        -- Inserting into Students table
        INSERT INTO Students (userId, description, CVPath)
        VALUES (studentId1, 'Ik ben een student die graag in de groenvoorziening werkt', '/1234jkvs9s.pdf'),
               (studentId2, 'Tech-savvy student looking for innovative projects', '/2345abcdef.pdf');

        -- Inserting into Supervisors table
        INSERT INTO Supervisors (userId, businessId)
        VALUES (supervisorId1, (SELECT businessId FROM Business WHERE name = 'Groen onderhoud gemeente Rheden')),
               (supervisorId2, (SELECT businessId FROM Business WHERE name = 'Tech Innovators')),
               (supervisorId3, (SELECT businessId FROM Business WHERE name = 'Hogeschool Arnhem en Nijmegen'));

        INSERT INTO Skills (name)
        VALUES ('PHP'), ('SQL'), ('Java'), ('JavaScript'), ('HTML'), ('CSS'),
               ('Snoeien'), ('Planten'), ('Shovel rijden'), ('Grasmaaien'),
               ('Network Security'), ('Cloud Computing'), ('AI/ML'),
               ('Data Analysis'), ('Blockchain Development'),
               ('Water Management'), ('Security Analysis'),
               ('Video Production'), ('Video Editing'),
               ('Photography'), ('Photo Editing'),
               ('Graphic Design'), ('Schilderen'),
               ('UI/UX Design'), ('Mobile App Development'), ('Tekenen'),('Landschapsontwerp'),
               ('Constructiewerk'),
               ('Civiele Techniek'),
               ('Veiligheidsmanagement'),
               ('Tuinieren'),
               ('Stedelijke Planning'),
               ('Elektrotechniek'),
               ('Kwaliteitscontrole');
        INSERT INTO StudentsSkills (userId, skillId, description)
        VALUES (studentId1, (SELECT skillId FROM Skills WHERE name = 'Snoeien'), 'description'),
               (studentId1, (SELECT skillId FROM Skills WHERE name = 'Planten'), 'hele lange description: hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh'),
               (studentId1, (SELECT skillId FROM Skills WHERE name = 'Shovel rijden'), ''),
               (studentId1, (SELECT skillId FROM Skills WHERE name = 'Grasmaaien'), ''),
               (studentId2, (SELECT skillId FROM Skills WHERE name = 'Data Analysis'), 'De volgende skill heeft geen description'),
               (studentId2, (SELECT skillId FROM Skills WHERE name = 'AI/ML'), ''),
               (studentId2, (SELECT skillId FROM Skills WHERE name = 'Network Security'), '');

        INSERT INTO Projects (userId, title, description, imagePath)
        VALUES (supervisorId1, 'Velp vergroenen', 'Vergroen Velp voor 100 SP', '/38dmwn40sj.jpg'),
               (supervisorId2, 'Innovative AI/ML Project', 'Cutting-edge AI/ML research and development', '/ai_ml_project.jpeg'),
               (supervisorId1, 'Water Management Rheden', 'Managing water resources in Rheden', '/water_management.jpeg'),
               (supervisorId2, 'Blockchain Development', 'Blockchain technology research and implementation', '/blockchain_project.jpeg'),
               (supervisorId2, 'Cybersecurity Enhancement', 'Improving cybersecurity measures', '/cybersecurity_project.jpeg'),
               (supervisorId3, 'Promotievideo Open Dag HAN', 'Het maken van een promotievideo voor de open dag van HAN', '/open_dag_han.jpeg'),
               (supervisorId3, 'Schoolfotos maken', 'Het maken van schoolfoto’s voor de Hogeschool Arnhem en Nijmegen', '/schoolfotos.jpeg'),
               (supervisorId3, 'Muurschildering HAN', 'Het maken van een muurschildering op een van de gebouwen van HAN', '/muurschildering.jpeg'),
               (supervisorId3, 'Ontwikkeling HAN App', 'Ontwikkelen van een mobiele app voor studenten en medewerkers van HAN', '/han_app.jpeg'),
               (supervisorId1, 'Speeltuin ontwerp', 'Ontwerp voor een nieuwe speeltuin', '/speeltuin_ontwerp.jpeg'),
               (supervisorId1, 'Renovatie Park Rheden', 'Renovatie van het centrale park in Rheden om de recreatieve faciliteiten te verbeteren', '/park.jpeg'),
               (supervisorId1, 'Fietspaden Netwerk Uitbreiding', 'Uitbreiding van het netwerk van fietspaden om veilig fietsen in Rheden te bevorderen', '/fietspad.jpeg'),
               (supervisorId1, 'Herinrichting Marktplein', 'Herinrichting van het marktplein in Rheden met nieuwe bestrating en groenvoorziening', '/bouwvakkers.jpeg'),
               (supervisorId1, 'LED-Verlichting Installatie', 'Installatie van energie-efficiënte LED-verlichting in de belangrijkste openbare gebieden van Rheden', '/verlichting.jpeg');


        -- Setting variables for project references
        projectId1 := (SELECT projectId FROM Projects WHERE title = 'Velp vergroenen');
        projectId2 := (SELECT projectId FROM Projects WHERE title = 'Innovative AI/ML Project');
        projectId3 := (SELECT projectId FROM Projects WHERE title = 'Water Management Rheden');
        projectId4 := (SELECT projectId FROM Projects WHERE title = 'Blockchain Development');
        projectId5 := (SELECT projectId FROM Projects WHERE title = 'Cybersecurity Enhancement');
        projectId6 := (SELECT projectId FROM Projects WHERE title = 'Promotievideo Open Dag HAN');
        projectId7 := (SELECT projectId FROM Projects WHERE title = 'Schoolfotos maken');
        projectId8 := (SELECT projectId FROM Projects WHERE title = 'Muurschildering HAN');
        projectId9 := (SELECT projectId FROM Projects WHERE title = 'Ontwikkeling HAN App');
        projectId10 := (SELECT projectId FROM Projects WHERE title = 'Speeltuin ontwerp');
        projectId11 := (SELECT projectId FROM Projects WHERE title = 'Renovatie Park Rheden');
        projectId12 := (SELECT projectId FROM Projects WHERE title = 'Fietspaden Netwerk Uitbreiding');
        projectId13 := (SELECT projectId FROM Projects WHERE title = 'Herinrichting Marktplein');
        projectId14 := (SELECT projectId FROM Projects WHERE title = 'LED-Verlichting Installatie');

        -- Inserting into Tasks table
        INSERT INTO Tasks (projectId, title, description, totalNeeded)
        VALUES (projectId1, 'Planten snoeien', 'Het snoeien van planten in de gemeente Rheden', 5),
               (projectId1, 'Gras maaien', 'Het maaien van gras in de gemeente Rheden', 3),
               (projectId1, 'Bomen planten', 'Het planten van bomen in de gemeente Rheden', 2),
               (projectId2, 'Data Preparation', 'Prepare datasets for machine learning models', 3),
               (projectId2, 'Model Training', 'Train various machine learning models', 4),
               (projectId2, 'Result Analysis', 'Analyze the results and metrics from trained models', 2),
               (projectId3, 'Irrigation Setup', 'Setting up irrigation systems', 5),
               (projectId3, 'Water Testing', 'Testing water quality and levels', 3),
               (projectId4, 'Smart Contract Development', 'Developing smart contracts on blockchain', 4),
               (projectId4, 'Blockchain Network Setup', 'Setting up a blockchain network', 3),
               (projectId5, 'Vulnerability Assessment', 'Assessing system vulnerabilities', 2),
               (projectId5, 'Implement Security Protocols', 'Implementing new security protocols', 3),
               (projectId6, 'Video Opnamen', 'Opnemen van video op locatie tijdens de open dag', 4),
               (projectId6, 'Video Editing', 'Bewerken van de opgenomen video’s voor promotiedoeleinden', 2),
               (projectId7, 'Fotografie Setup', 'Opzetten van de fotografielocaties', 3),
               (projectId7, 'Foto Bewerking', 'Bewerken van de gemaakte foto’s voor gebruik in schoolmateriaal', 2),
               (projectId8, 'Ontwerp Voorbereiding', 'Ontwerpen en voorbereiden van de muurschildering', 2),
               (projectId8, 'Schilderen', 'Uitvoeren van de muurschildering', 5),
               (projectId9, 'App Design', 'Ontwerpen van de gebruikersinterface voor de HAN app', 3),
               (projectId9, 'App Development', 'Ontwikkelen van de app functies', 4),
               (projectId10, 'Ontwerptekening', 'Ontwerp maken voor de nieuwe speeltuin', 2),
               (projectId11, 'Landschapsontwerp', 'Ontwerpen van nieuwe landschapsarchitectuur voor het park', 3),
               (projectId11, 'Speeltoestellen Plaatsen', 'Installeren van nieuwe speeltoestellen', 2),
               (projectId12, 'Wegdek Voorbereiden', 'Voorbereiden van de ondergrond voor het leggen van nieuwe fietspaden', 4),
               (projectId12, 'Wegmarkeringen Aanbrengen', 'Aanbrengen van wegmarkeringen op de nieuwe fietspaden', 3),
               (projectId13, 'Plaatsing Groenvoorziening', 'Plaatsen van nieuwe bomen en planten op het marktplein', 4),
               (projectId13, 'Straatmeubilair Installeren', 'Installeren van nieuw straatmeubilair zoals bankjes en afvalbakken', 3),
               (projectId14, 'Elektrische Bedrading', 'Aanleggen van elektrische bedrading voor de LED-verlichting', 5),
               (projectId14, 'Verlichting Testen', 'Testen van de geïnstalleerde LED-verlichting op functionaliteit', 2);

        -- Setting variables for task references
        taskId1 := (SELECT taskId FROM Tasks WHERE title = 'Planten snoeien');
        taskId2 := (SELECT taskId FROM Tasks WHERE title = 'Gras maaien');
        taskId3 := (SELECT taskId FROM Tasks WHERE title = 'Bomen planten');
        taskId4 := (SELECT taskId FROM Tasks WHERE title = 'Data Preparation');
        taskId5 := (SELECT taskId FROM Tasks WHERE title = 'Model Training');
        taskId6 := (SELECT taskId FROM Tasks WHERE title = 'Result Analysis');
        taskId7 := (SELECT taskId FROM Tasks WHERE title = 'Irrigation Setup');
        taskId8 := (SELECT taskId FROM Tasks WHERE title = 'Water Testing');
        taskId9 := (SELECT taskId FROM Tasks WHERE title = 'Smart Contract Development');
        taskId10 := (SELECT taskId FROM Tasks WHERE title = 'Blockchain Network Setup');
        taskId11 := (SELECT taskId FROM Tasks WHERE title = 'Vulnerability Assessment');
        taskId12 := (SELECT taskId FROM Tasks WHERE title = 'Implement Security Protocols');
        taskId13 := (SELECT taskId FROM Tasks WHERE title = 'Video Opnamen');
        taskId14 := (SELECT taskId FROM Tasks WHERE title = 'Video Editing');
        taskId15 := (SELECT taskId FROM Tasks WHERE title = 'Fotografie Setup');
        taskId16 := (SELECT taskId FROM Tasks WHERE title = 'Foto Bewerking');
        taskId17 := (SELECT taskId FROM Tasks WHERE title = 'Ontwerp Voorbereiding');
        taskId18 := (SELECT taskId FROM Tasks WHERE title = 'Schilderen');
        taskId19 := (SELECT taskId FROM Tasks WHERE title = 'App Design');
        taskId20 := (SELECT taskId FROM Tasks WHERE title = 'App Development');
        taskId21 := (SELECT taskId FROM Tasks WHERE title = 'Ontwerptekening');
        taskId22 := (SELECT taskId FROM Tasks WHERE title = 'Landschapsontwerp');
        taskId23 := (SELECT taskId FROM Tasks WHERE title = 'Speeltoestellen Plaatsen');
        taskId24 := (SELECT taskId FROM Tasks WHERE title = 'Wegdek Voorbereiden');
        taskId25 := (SELECT taskId FROM Tasks WHERE title = 'Wegmarkeringen Aanbrengen');
        taskId26 := (SELECT taskId FROM Tasks WHERE title = 'Plaatsing Groenvoorziening');
        taskId27 := (SELECT taskId FROM Tasks WHERE title = 'Straatmeubilair Installeren');
        taskId28 := (SELECT taskId FROM Tasks WHERE title = 'Elektrische Bedrading');
        taskId29 := (SELECT taskId FROM Tasks WHERE title = 'Verlichting Testen');
        taskId30 := (SELECT taskId FROM Tasks WHERE title = 'Website maken');

        INSERT INTO Tasks (projectId, title, description, totalNeeded)
        VALUES  (projectId1, 'Planten snoeien', 'Het snoeien van planten in de gemeente Rheden', 5),
                (projectId1, 'Gras maaien', 'Het maaien van gras in de gemeente Rheden', 3),
                (projectId1, 'Bomen planten', 'Het planten van bomen in de gemeente Rheden', 2),
                (projectId1, 'Website maken', 'Een website maken voor de groen onderhoud tak van de gemeente Rheden.', 4);

        -- Inserting into TasksSkills table
--         INSERT INTO TasksSkills (taskId, skillId)
--         VALUES  (taskId1, (SELECT skillId FROM Skills WHERE name = 'Snoeien')),
--                 (taskId1, (SELECT skillId FROM Skills WHERE name = 'Planten')),
--                 (taskId2, (SELECT skillId FROM Skills WHERE name = 'Shovel rijden')),
--                 (taskId3, (SELECT skillId FROM Skills WHERE name = 'Grasmaaien')),
--                 (taskId4, (SELECT skillId FROM Skills WHERE name = 'Data Analysis')),
--                 (taskId5, (SELECT skillId FROM Skills WHERE name = 'AI/ML')),
--                 (taskId6, (SELECT skillId FROM Skills WHERE name = 'Network Security')),
--                 (taskId7, (SELECT skillId FROM Skills WHERE name = 'Planten')),
--                 (taskId7, (SELECT skillId FROM Skills WHERE name = 'Water Management')),
--                 (taskId8, (SELECT skillId FROM Skills WHERE name = 'Data Analysis')),
--                 (taskId9, (SELECT skillId FROM Skills WHERE name = 'Blockchain Development')),
--                 (taskId10, (SELECT skillId FROM Skills WHERE name = 'Network Security')),
--                 (taskId11, (SELECT skillId FROM Skills WHERE name = 'Network Security')),
--                 (taskId12, (SELECT skillId FROM Skills WHERE name = 'Security Analysis')),
--                 (taskId12, (SELECT skillId FROM Skills WHERE name = 'Network Security')),
--                 (taskId13, (SELECT skillId FROM Skills WHERE name = 'Video Production')),
--                 (taskId14, (SELECT skillId FROM Skills WHERE name = 'Video Editing')),
--                 (taskId15, (SELECT skillId FROM Skills WHERE name = 'Photography')),
--                 (taskId16, (SELECT skillId FROM Skills WHERE name = 'Photo Editing')),
--                 (taskId17, (SELECT skillId FROM Skills WHERE name = 'Graphic Design')),
--                 (taskId18, (SELECT skillId FROM Skills WHERE name = 'Schilderen')),
--                 (taskId19, (SELECT skillId FROM Skills WHERE name = 'UI/UX Design')),
--                 (taskId20, (SELECT skillId FROM Skills WHERE name = 'Mobile App Development')),
--                 (taskId21, (SELECT skillId FROM Skills WHERE name = 'Tekenen')),
--                 (taskId21, (SELECT skillId FROM Skills WHERE name = 'Graphic Design')),
--                 (taskId22, (SELECT skillId FROM Skills WHERE name = 'Landschapsontwerp')),
--                 (taskId23, (SELECT skillId FROM Skills WHERE name = 'Constructiewerk')),
--                 (taskId24, (SELECT skillId FROM Skills WHERE name = 'Civiele Techniek')),
--                 (taskId25, (SELECT skillId FROM Skills WHERE name = 'Veiligheidsmanagement')),
--                 (taskId26, (SELECT skillId FROM Skills WHERE name = 'Tuinieren')),
--                 (taskId27, (SELECT skillId FROM Skills WHERE name = 'Stedelijke Planning')),
--                 (taskId28, (SELECT skillId FROM Skills WHERE name = 'Elektrotechniek')),
--                 (taskId29, (SELECT skillId FROM Skills WHERE name = 'Kwaliteitscontrole')),
--                 (33, 1),
--                 (33, 2),
--                 (33, 5),
--                 (33, 6),
--                 (30, 7),
--                 (31, 10),
--                 (32, 8);

        -- Inserting into TasksRegistrations table
        INSERT INTO TasksRegistrations (taskId, userId, description, accepted, response)
        VALUES (taskId1, studentId1, 'Ik wil mij graag aanmelden want ...', NULL, '');

        INSERT INTO TasksRegistrations (taskId, userId, description, accepted, response)
        VALUES (taskId1, studentId2, 'Ik wil mij graag nog een keer aanmelden want ...', NULL, '');
    END $$;

