FROM ubuntu:16.04

MAINTAINER Valitov Rishat

RUN apt-get -y update
RUN apt-get install -y tzdata
RUN echo "Europe/Moscow" > /etc/timezone
RUN rm /etc/localtime
RUN dpkg-reconfigure -f noninteractive tzdata
ENV PGVER 9.5
RUN apt-get install -y postgresql-$PGVER
RUN apt-get install -y openjdk-8-jdk-headless
RUN apt-get install -y maven
USER postgres
RUN /etc/init.d/postgresql start && \
     psql --command "ALTER USER postgres WITH PASSWORD 'technogeek';" && \
     /etc/init.d/postgresql stop
RUN echo "host all  all    0.0.0.0/0  md5" >> /etc/postgresql/$PGVER/main/pg_hba.conf
RUN echo "listen_addresses='*'" >> /etc/postgresql/$PGVER/main/postgresql.conf
EXPOSE 5432
VOLUME  ["/etc/postgresql", "/var/log/postgresql", "/var/lib/postgresql"]
USER root
ENV WORK /opt
ADD . $WORK/java-spring/
WORKDIR $WORK/java-spring
RUN mvn package
EXPOSE 5000
USER postgres
CMD service postgresql start && \
    psql --command "UPDATE pg_database SET datistemplate = FALSE WHERE datname = 'template1';" && \
    psql --command "DROP DATABASE template1;" && \
    psql --command "CREATE DATABASE template1 WITH TEMPLATE = template0 ENCODING = 'UNICODE';" && \
    psql --command "UPDATE pg_database SET datistemplate = TRUE WHERE datname = 'template1';" && \
    psql --command "\c template1" && \
    psql --command "VACUUM FREEZE;" && \
    \
    psql --command "CREATE DATABASE technopark WITH ENCODING 'UTF8';" && \
    psql -f $WORK/java-spring/config/docker_initdb.sql technopark postgres && \
    java -jar $WORK/java-spring/target/Functional_test_db-1.0-SNAPSHOT.jar