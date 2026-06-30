SOURCES := $(shell find src -name '*.clj')
RESOURCES := $(wildcard resources/*)

default: uber

JAR=target/ballomud.jar

.PHONY: uber
uber: $(JAR)

$(JAR): $(SOURCES) $(RESOURCES) deps.edn build.clj
	clojure -T:build uber

.PHONY: deps
deps:
	clojure -P

.PHONY: test
test:
	clojure -M:test

.PHONY: clean
clean:
	rm -rf target/ .cpcache/

.PHONY: docker
docker:
	docker build -t ballomud .

.PHONY: install
install: $(JAR)
	@if [ ! -d "$${BINDIR:-$$HOME/bin}" ]; then \
		echo "Error: Install directory $${BINDIR:-$$HOME/bin} does not exist."; \
		echo "Please create it or set BINDIR to an existing directory."; \
		exit 1; \
	fi
	cp ballomud $${BINDIR:-$$HOME/bin}/
	cp $(JAR) $${BINDIR:-$$HOME/bin}/ballomud.jar

.PHONY: deploy
deploy: $(JAR)
	rsync -vurt ../ballomud tw:
	ssh tw 'killall java; sleep 1; cd ballomud; nohup ./ballomud 0.0.0.0 >/dev/null 2>&1 &'

.PHONY: docker-run
docker-run:
	docker run -it -p 9999:9999 ballomud

.PHONY: dockerpush
dockerpush:
	docker push -a eigenhombre/ballomud
