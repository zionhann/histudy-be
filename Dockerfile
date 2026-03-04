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

RUN mkdir -p image log

COPY --from=builder /project/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
