FROM eclipse-temurin:21-jre-alpine
ARG ONTOLOGY_VERSION=2026.6.0
RUN adduser --disabled-password ontologyuser
COPY target/ontology-application-${ONTOLOGY_VERSION}.jar /ontology.jar
EXPOSE 8080
USER ontologyuser
ENTRYPOINT ["java","-jar","/ontology.jar"]
