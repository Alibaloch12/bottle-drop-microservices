FROM maven:4.0.0-rc-5-eclipse-temurin-26 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests


FROM eclipse-temurin:26-jre-alpine

WORKDIR /app

RUN apk add --no-cache curl

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=build /app/target/*.jar app.jar

RUN chown spring:spring app.jar

USER spring:spring

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]