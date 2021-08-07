FROM openjdk:11-slim
ARG JAR_NAME
ENV JAR_NAME_ENV=$JAR_NAME
COPY ./jar/$JAR_NAME /
EXPOSE 8080
ENTRYPOINT java -Dapplication.properties.path=codenames.properties -jar /$JAR_NAME_ENV
