FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -B -Dmaven.repo.local=/root/.m2/repository dependency:go-offline
RUN mvn -q -B -Dmaven.repo.local=/root/.m2/repository package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/url-shortener-0.0.1-SNAPSHOT.jar ./url-shortener.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "url-shortener.jar"]
