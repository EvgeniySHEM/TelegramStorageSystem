FROM eclipse-temurin:17-jdk-focal
WORKDIR /app
EXPOSE 8085
COPY target/*.jar /app/app.jar
ENTRYPOINT ["java","-jar","app.jar"]

