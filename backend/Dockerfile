# Build Stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Zorgt ervoor dat de dependencies niet gecached worden
ARG CACHEBUST=1
COPY pom.xml ./
RUN mvn dependency:resolve 

COPY src ./src
RUN mvn clean package -DskipTests

# Host Stage
FROM eclipse-temurin:21-jdk-alpine AS host

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY src/main/resources /app/resources
COPY src/main/resources/static/files /app/src/main/resources/static/files/
# Copy over the deps
COPY --from=build /root/.m2/repository /root/.m2/repository
VOLUME /tmp
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
