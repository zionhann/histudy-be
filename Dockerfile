FROM amazoncorretto:17 AS builder

ARG STATIC_PATH=src/main/resources/static
ARG API_DOCS=api-docs.yaml

WORKDIR /project

COPY . .

RUN chmod +x gradlew && \
    mkdir -p "$STATIC_PATH" && \
    mv "$API_DOCS" "$STATIC_PATH" && \
    ./gradlew bootJar --no-daemon

FROM amazoncorretto:17

WORKDIR /webapp

RUN groupadd -r app && \
    useradd -r -g app -d /webapp -s /sbin/nologin app && \
    mkdir -p image log && \
    chown -R app:app /webapp

COPY --from=builder --chown=app:app /project/build/libs/*.jar app.jar

EXPOSE 8080

USER app

ENTRYPOINT ["java", "-jar", "app.jar"]
