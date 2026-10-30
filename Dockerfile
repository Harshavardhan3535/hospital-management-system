# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# 👇 THIS IS THE MAGIC LINE 👇
# We explicitly pass the environment variables to the Java process
ENV SPRING_DATASOURCE_URL=${DB_URL}
ENV SPRING_DATASOURCE_USERNAME=${DB_USER}
ENV SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]