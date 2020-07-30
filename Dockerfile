FROM openjdk:8-alpine
ARG JAR_FILE
COPY /target/${JAR_FILE} /app/agent/${JAR_FILE}
COPY /target/dependency /app/agent
COPY /monitors /app/monitors
COPY start.sh /app/bin/start.sh
RUN chmod +rwx /app/bin/start.sh
ENTRYPOINT ["/app/bin/start.sh"]