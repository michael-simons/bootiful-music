FROM openjdk:11-jre-slim

VOLUME /tmp

ARG JAR_FILE=knowledge.jar

COPY ${JAR_FILE} app.jar
COPY docker-entrypoint.sh /docker-entrypoint.sh

ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["knowledge"]
