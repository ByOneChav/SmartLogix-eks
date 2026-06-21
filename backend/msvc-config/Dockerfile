# Build
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
#POM PADRE
COPY pom.xml ./parent-pom.xml
RUN mvn install -f parent-pom.xml -N -B
#POM HIJO
COPY msvc-config/pom.xml ./pom.xml
RUN mvn dependency:go-offline -B

COPY msvc-config/src ./src
RUN mvn clean package -DskipTests -B

# jre runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "app.jar"]

# trigger
