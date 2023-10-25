FROM eclipse-temurin:17-jre
VOLUME /tmp
ARG JAR_FILE
COPY target/availability-checker-service-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]