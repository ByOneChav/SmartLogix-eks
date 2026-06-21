# Build
FROM maven:3.9.6-eclipse-temurin-17 as build
WORKDIR /app
#POM PADRE
COPY pom.xml ./parent-pom.xml
RUN mvn install -f parent-pom.xml -N -B
#POM HIJO
COPY msvc-authService/pom.xml ./pom.xml
RUN mvn dependency:go-offline -B

COPY msvc-authService/src ./src
RUN mvn clean package -DskipTests -B

# .jar runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

#test





# trigger
# rebuild
