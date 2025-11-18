# Build stage
FROM gradle:9.1.0-jdk25-alpine AS build
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
COPY src ./src

# Primero limpiamos cualquier build anterior para evitar problemas de caché
RUN gradle clean --no-daemon

# 'build' ejecuta 'test' (informe JUnit)
# 'jacocoTestReport' genera el informe de cobertura
# 'jacocoTestCoverageVerification' verifica la cobertura mínima (65%)
# ':dokkaGeneratePublicationHtml' genera la documentación Dokka en formato HTML
# '--no-build-cache' evita problemas con archivos de assets en caché
RUN gradle build jacocoTestReport jacocoTestCoverageVerification :dokkaGeneratePublicationHtml --no-daemon --no-build-cache

# Artifacts stage for reports
FROM alpine:latest AS artifacts
WORKDIR /artifacts
COPY --from=build /app/build/reports/tests/test /artifacts/test-reports
COPY --from=build /app/build/docs/dokka /artifacts/doc-reports
COPY --from=build /app/build/reports/jacoco /artifacts/coverage-reports

# Runtime stage
FROM eclipse-temurin:25-jre-alpine AS runtime
WORKDIR /app

# Copiar el JAR
COPY --from=build /app/build/libs/*.jar app.jar

# Copiar los reportes generados
COPY --from=artifacts /artifacts/test-reports ./reports/test
COPY --from=artifacts /artifacts/doc-reports ./reports/doc
COPY --from=artifacts /artifacts/coverage-reports ./reports/coverage

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]