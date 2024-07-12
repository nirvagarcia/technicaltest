FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/technicaltest-0.0.1-SNAPSHOT.jar app.jar
COPY src/main/resources/application.properties /app/application.properties
RUN apt-get update && apt-get install -y redis-tools
EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.config.location=/app/application.properties", "-jar", "app.jar"]
