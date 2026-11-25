FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY guarderia-commons /workspace/guarderia-commons
RUN mvn -B -f /workspace/guarderia-commons/pom.xml install -DskipTests

COPY gateway-service /workspace/gateway-service
RUN mvn -B -f /workspace/gateway-service/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/gateway-service/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]