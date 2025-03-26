# Introductie
Deze repository bevat de code voor de CMD Opdrachtenbox, ontwikkeld voor het lectoraat van de HAN. De applicatie functioneert als een platform waarop externe bedrijven opdrachten kunnen plaatsen, waarna studenten zich hiervoor kunnen aanmelden.

# Installatie
In dit hoofdstuk wordt beschreven hoe de applicatie te installeren is voor zowel productie als development.
## Productie
1. Download Docker
2. Clone de repository en ga naar de root van de repository
3. Pas in de bestanden [`application.properties`](backend/src/main/resources/application.properties) en [`application-docker.properties`](backend/src/main/resources/application-docker.properties) de volgende categoriën aan:
    - `db`
        ```properties
        spring.datasource.url=jdbc:postgresql://[URL VAN DATABASE]/postgres
        spring.datasource.password=[WACHTWOORD VAN DATABASE, TE VINDEN IN DOCKER COMPOSE]
        ```
    - `oauth`
        ```properties
        # google -> https://console.cloud.google.com/apis/dashboard
        spring.security.oauth2.client.registration.google.client-id=[CLIENT ID]
        spring.security.oauth2.client.registration.google.client-secret=[CLIENT SECRET]

        # github -> https://github.com/settings/applications/new
        spring.security.oauth2.client.registration.github.client-id=[CLIENT ID]
        spring.security.oauth2.client.registration.github.client-secret=[CLIENT SECRET]
        ```
    - `frontend`
        ```properties
        frontend.url=http://[FRONTEND URL]
        ```
    - `backend`
        ```properties
        backend.url=http://[BACKEND URL]
        ```
4. Open een terminal in de root van de repository en voer de volgende commando uit:

    ```bash
    docker compose up
    ```

## Development
1. Clone de repository
2. Installeer Docker Desktop en start deze op
3. Open een terminal in de root van de repository en voer de volgende commando uit:

    ```bash
    docker compose up
    ```
4. Via Docker Desktop, zet frontend en backend uit
### Backend opstarten
5. Dubbelklik op [`backend/pom.xml`](backend/pom.xml), dit opent de backend als een project in IntelliJ. ⚠️ Let op: Als je dit niet doet, kan het zijn dat de backend de resources map niet ziet en werken sommige functies dus niet.
6. Start via IntelliJ de backend op
### Frontend opstarten
7. Open een terminal in de root van de repository en voer de volgende commando's uit:

    ```bash
    cd frontend
    npm install
    npm run dev
    ```

### Aanpassingen aan database
8. Wanneer je een aanpassing maakt aan de database ([schema.sql](backend/src/main/resources/database/schema.sql)), voer dan de volgende commando's uit:

    ```bash
    docker compose down -v
    docker compose up --build
    ```

# Testen
In dit hoofdstuk wordt beschreven hoe de applicatie getest kan worden.
## Frontend
- We gebruiken Storybook voor het testen van de componenten. Start Storybook op door de volgende commando op de frontend folder uit te voeren:

    ```bash
    npm run storybook
    ```
- Accessibility is te testen via de 'Accessibility' tab in Storybook
- De interactions en accessibility worden ook door de CI pipeline getest

## Backend
- Voer de unit tests uit door bij de [`test/java`](backend/src/test/) folder rechtermuisknop te klikken en `Run 'Tests in 'java''` te selecteren
- Je kan de mutation tests uitvoeren door via GitHub Actions de Mutation Tests handmatig op jouw branch uit te voeren.
- Test het versturen van de automatische emails in de [MailCronJob](backend/src/main/java/com/han/pwac/pinguins/backend/cron_jobs/MailCronJob.java) klasse door `@Scheduled` annotation te veranderen bij de `execute` methode naar `EVERY_SECOND`

Verdere uitleg van de functies van de applicatie staan uitgelegd in het Software Guidebook.