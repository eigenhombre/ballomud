FROM sapmachine

RUN apt-get -y update
RUN apt-get -y upgrade
RUN apt-get install -y make leiningen

WORKDIR /home/janice

COPY project.clj /home/janice
RUN lein deps
COPY . /home/janice
RUN make test lint uberjar
CMD ["./mud"]

