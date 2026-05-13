FROM eclipse-temurin:21-jdk-alpine
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-Xms64m", "-Xmx256m", "-jar", "/app.jar"]
EXPOSE 8080
