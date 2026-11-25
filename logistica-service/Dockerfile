FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY guarderia-commons /workspace/guarderia-commons
RUN mvn -B -f /workspace/guarderia-commons/pom.xml install -DskipTests

COPY logistica-service /workspace/logistica-service
RUN mvn -B -f /workspace/logistica-service/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/logistica-service/target/*.jar /app/app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]