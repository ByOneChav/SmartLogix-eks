# ── Etapa 1: Build ──
FROM maven:3.9.6-eclipse-temurin-17 as build
WORKDIR /app

COPY pom.xml ./parent-pom.xml
RUN mvn install -f parent-pom.xml -N -B

COPY msvc-gateway/pom.xml ./pom.xml
RUN mvn dependency:go-offline -B

COPY msvc-gateway/src ./src
RUN mvn clean package -DskipTests -B

# ── Etapa 2: Runtime ──
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]