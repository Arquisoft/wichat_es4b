# Build stage with Maven and JDK 21
FROM maven:3.9.5-eclipse-temurin-21-slim AS build
WORKDIR ./app
COPY pom.xml .
COPY src src/
# Use Maven directly instead of the Maven Wrapper
RUN mvn clean package -DskipTests

# Run stage with JDK 21
FROM openjdk:21-slim
COPY --from=build ./app/target/*.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Dspring.profiles.active=prod","-jar","/app.jar"]
EXPOSE 443