FROM maven:4.0.0-rc-5-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN apk add --no-cache curl && \
    addgroup -S spring && adduser -S spring -G spring

COPY --from=build /app/target/*.jar app.jar
RUN chown spring:spring /app/app.jar

USER spring:spring

EXPOSE 8089

ENTRYPOINT ["java", "-jar", "app.jar"]