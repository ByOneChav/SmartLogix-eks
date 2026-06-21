# Build
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
#POM PADORE
COPY pom.xml ./parent-pom.xml
RUN mvn install -f parent-pom.xml -N -B
#hijo
COPY msvc-eureka/pom.xml ./pom.xml
RUN mvn dependency:go-offline -B

COPY msvc-eureka/src ./src
RUN mvn clean package -DskipTests -B

# ──jre runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]



