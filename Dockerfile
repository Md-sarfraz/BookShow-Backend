# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml first to cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application, skip tests
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built JAR from Stage 1
COPY --from=build /app/target/*.jar app.jar

# Create uploads directory
RUN mkdir -p /app/uploads

# Expose port
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
