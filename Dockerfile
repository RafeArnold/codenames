FROM openjdk:11-slim
ARG JAR_NAME
COPY ./jar/$JAR_NAME /
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/$JAR_NAME"]
