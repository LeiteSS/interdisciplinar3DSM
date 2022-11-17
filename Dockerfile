FROM eclipse-temurin:11
MAINTAINER fateczl.gov.sp.br
COPY target/grupo3-0.0.1-SNAPSHOT.jar grupo3-0.0.1-SNAPSHOT.jar
ADD data.mv.db  data.mv.db
ENTRYPOINT ["java","-jar","/grupo3-0.0.1-SNAPSHOT.jar"]
