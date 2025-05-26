FROM maven:3.9.6-eclipse-temurin-17-alpine

WORKDIR /app

# Copy the project files
COPY . .

# Build the application
RUN mvn clean package -DskipTests

# Run the application
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "target/tracking-number-generator-1.0.0.jar"]