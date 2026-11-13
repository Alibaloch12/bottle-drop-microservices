# Stage 1: Build the application using Maven
FROM maven:4.0.0-rc-5-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the application and skip tests for faster compilation
RUN mvn clean package -DskipTests

# Stage 2: Create the lightweight runtime environment
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache curl
# Create a secure non-root user to run the application
RUN addgroup -S spring && adduser -S spring -G spring


# Copy the built executable JAR from the build stage
COPY --from=build /app/target/*.jar app.jar
RUN chown spring:spring app.jar

USER spring:spring
# Inform Docker that the container listens on port 8080
EXPOSE 8761

# Execute the application
ENTRYPOINT ["java", "-jar", "app.jar"]