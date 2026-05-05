FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Copy db.properties to the expected location INSIDE the container
COPY src/server/db.properties src/server/db.properties
EXPOSE 12345
ENTRYPOINT ["java", "-jar", "app.jar", "--port", "12345"]