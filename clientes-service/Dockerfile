FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY guarderia-commons /workspace/guarderia-commons
RUN mvn -B -f /workspace/guarderia-commons/pom.xml install -DskipTests

COPY clientes-service /workspace/clientes-service
RUN mvn -B -f /workspace/clientes-service/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/clientes-service/target/*.jar /app/app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]