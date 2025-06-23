# Stage 1: Build the Spring Boot app using Maven
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy the Maven project files
COPY pom.xml ./

# Download dependencies
RUN mvn dependency:go-offline

# Copy the entire project source code
COPY . .

# Build the application and skip tests for the final JAR
RUN mvn clean package -DskipTests

# Stage 2: Create a lightweight image to run the Spring Boot app
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 9999

# Run the Spring Boot app
CMD ["java", "-jar", "app.jar"]
