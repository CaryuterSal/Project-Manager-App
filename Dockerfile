ARG APP=/home/app
# Etapa de construcción
FROM maven:3.9.4-eclipse-temurin-21 AS build
ARG APP=/root/.m2
ARG MVN_REPO
WORKDIR $APP

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

RUN --mount=type=cache,target=${MVN_REPO} ./mvnw -Dmaven.repo.local=${MVN_REPO} dependency:go-offline

COPY . .

RUN --mount=type=cache,target=${MVN_REPO} ./mvnw -Dmaven.repo.local=${MVN_REPO} -Dmaven.test.skip=true clean package

#----------------------------------

FROM eclipse-temurin:21-jre

ARG APP

WORKDIR $APP

COPY --from=build ${APP}/target/Project-Manager-App-1.0.jar app.jar

ENTRYPOINT ["java","-jar", "app.jar"]
