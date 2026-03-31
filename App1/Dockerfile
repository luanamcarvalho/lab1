FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml /app
COPY src /app/src
RUN mvn package -DskipTests

FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=build /app/target/app1.jar ./app.jar
RUN chown -R appuser:appgroup /app
USER appuser
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
