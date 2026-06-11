
FROM maven:3.9.16-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY src/server/db.properties src/server/db.properties
EXPOSE 12345
ENTRYPOINT ["java", "-jar", "app.jar", "--port", "12345"]