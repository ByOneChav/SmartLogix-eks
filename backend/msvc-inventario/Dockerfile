#Build (compilar el código)
FROM maven:3.9.6-eclipse-temurin-17 as build
# Usa una imagen que tiene Maven + Java 17 para compilar
WORKDIR /app

#Paso 1 : Copia solo el pom padre que todos los msvc heredan -> docekr cachea esta capa
COPY pom.xml ./parent-pom.xml
RUN mvn install -f parent-pom.xml -N -B
# -N = solo instala el padre, no los hijos
# -B = modo batch (sin colores ni preguntas interactivas)

#Pom hijo pom.xml especifico de msvc-pedido
COPY msvc-inventario/pom.xml ./pom.xml
RUN mvn dependency:go-offline -B
# Descarga TODAS las dependencias antes de copiar el código
# ¿Por qué? Docker cachea cada capa. Si el código cambia pero
# las dependencias no, no las vuelve a descargar. Más rápido.

#Copia el codigo fuente y compila
COPY msvc-inventario/src ./src
RUN mvn clean package -DskipTests -B
# Genera el .jar ejecutable. -DskipTests porque los tests
# ya se corrieron antes (en CI), aquí solo queremos el .jar

#Paso 2 : RUNTIME (ejecutar)
FROM eclipse-temurin:17-jre-alpine
# Imagen ligera (~150MB vs ~800MB de la de build)
# Solo tiene el JRE (runtime), no el JDK completo
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Copia SOLO el .jar de la etapa de build. No arrastra Maven,
# ni código fuente, ni dependencias de compilación.
EXPOSE 8083
#Documenta que este servicio usa el puerto 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
# Comando que arranca cuando el contenedor inicia