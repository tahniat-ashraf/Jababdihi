# syntax=docker/dockerfile:1

FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace

COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates \
    && rm -rf /var/lib/apt/lists/*
RUN addgroup --system jababdihi && adduser --system --ingroup jababdihi jababdihi
COPY --from=build /workspace/target/backend-0.0.1-SNAPSHOT.jar /app/backend.jar

# Runtime profile: set SPRING_PROFILES_ACTIVE=api (default) or SPRING_PROFILES_ACTIVE=worker
ENV SPRING_PROFILES_ACTIVE=api

USER jababdihi
EXPOSE 8080
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "/app/backend.jar"]
