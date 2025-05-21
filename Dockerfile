# Use JDK base image
FROM openjdk:17-jdk-alpine

# Set working directory inside container
WORKDIR /app

# Copy JAR file from host into container
COPY target/*.jar app.jar

# Command to run Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]