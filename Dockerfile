FROM sapmachine

RUN apt-get -y update
RUN apt-get -y upgrade
RUN apt-get install -y make curl rlwrap

RUN curl -L -O https://github.com/clojure/brew-install/releases/latest/download/linux-install.sh \
    && chmod +x linux-install.sh \
    && ./linux-install.sh \
    && rm linux-install.sh

WORKDIR /home/janice

COPY deps.edn build.clj /home/janice/
RUN clojure -P && clojure -T:build help 2>/dev/null || true
COPY . /home/janice
RUN make test uber
CMD ["java", "-jar", "target/ballomud.jar"]
