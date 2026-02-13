FROM gcr.io/distroless/java25:nonroot
ENV TZ="Europe/Oslo"
ENV JAVA_TOOL_OPTIONS=-XX:+ExitOnOutOfMemoryError
WORKDIR /app
COPY build/libs/*.jar ./app.jar
USER nonroot
CMD ["app.jar"]
