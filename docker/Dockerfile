FROM eclipse-temurin:21-jdk

COPY docker/app.jar /opt/app.jar

WORKDIR /opt

ENTRYPOINT ["java", "-jar", "app.jar"]

EXPOSE 8181