# Stage 1: Build the application
FROM maven:3.9.8-eclipse-temurin-21 AS build

WORKDIR /app

# Copy only pom.xml and download dependencies first (for better caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Now copy the source code and build the project
COPY src ./src
RUN mvn clean install -DskipTests

# Stage 2: Create the final image
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/centurion-core-0.0.1-SNAPSHOT.jar /app/app.jar

ENV SPRING_PROFILES_ACTIVE=dev

# Expose port 8080
EXPOSE 8080

# Define default command to run the application with JVM optimizations
CMD ["java", "-XX:+UseContainerSupport", "-Xmx512m", "-Xms256m", "-jar", "/app/app.jar"]
