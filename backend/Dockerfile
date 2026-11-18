# ==========================================
# ETAPA 1: Compilacion del backend
# ==========================================

FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copiar la configuracion de Maven
COPY pom.xml .

# Copiar el codigo fuente del backend
COPY src ./src

# Compilar y generar el archivo JAR
RUN mvn clean package -DskipTests -B


# ==========================================
# ETAPA 2: Ejecucion en produccion
# ==========================================

FROM eclipse-temurin:21-jre

WORKDIR /app

# Copiar el JAR generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Activar el perfil de produccion
ENV SPRING_PROFILES_ACTIVE=prod

# Documentar el puerto predeterminado
EXPOSE 8080

# Ejecutar el backend de EduNeko
ENTRYPOINT ["java", "-jar", "app.jar"]