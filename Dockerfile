# Build stage: compile the Spring Boot jar with Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline
COPY src ./src
RUN mvn -B -ntp -DskipTests package

# Runtime stage: only the JRE and the jar (ONNX Runtime needs glibc, so a Debian-based image is used)
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/stockia-platform.jar app.jar
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8181
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
