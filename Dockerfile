FROM adoptopenjdk:11-jre-hotspot

RUN apt-get -y update
RUN apt-get -y upgrade
RUN apt-get install -y make leiningen

WORKDIR /home/janice

COPY . /home/janice
RUN make test lint uberjar
