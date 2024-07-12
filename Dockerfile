FROM openjdk:17-jdk-slim
# Establecer el directorio de trabajo
WORKDIR /app
# Copiar el archivo JAR
COPY target/technicaltest-0.0.1-SNAPSHOT.jar app.jar
# Copiar el archivo de propiedades
COPY src/main/resources/application.properties /app/application.properties
# Instalar redis-tools
RUN apt-get update && apt-get install -y redis-tools && rm -rf /var/lib/apt/lists/*
# Exponer el puerto
EXPOSE 8080
# Definir el punto de entrada
ENTRYPOINT ["java", "-Dspring.config.location=/app/application.properties", "-jar", "app.jar"]
