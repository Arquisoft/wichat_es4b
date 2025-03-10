# Build stage with Maven and JDK 17
FROM maven:3.8.4-openjdk-17-slim as build
WORKDIR ./app
COPY pom.xml .
COPY src src/
# Use Maven directly instead of the Maven Wrapper
RUN mvn clean package -DskipTests

# Run stage with JDK 17
FROM openjdk:17-slim
COPY --from=build ./app/target/*.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Dspring.profiles.active=prod","-jar","/app.jar"]
EXPOSE 443