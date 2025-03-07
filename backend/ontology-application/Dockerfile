FROM eclipse-temurin:17-jre-alpine-3.20
RUN adduser --disabled-password ontologyuser
COPY target/ontology-application*[^sources].jar /ontology.jar
EXPOSE 8080
USER ontologyuser
ENTRYPOINT ["java","-jar","/ontology.jar"]