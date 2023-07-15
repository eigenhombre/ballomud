.PHONY: clean all test lint docker

JAR=target/uberjar/ballomud-0.1.0-SNAPSHOT-standalone.jar

all: lint test uberjar

uberjar: ${JAR}

${JAR}: src/ballomud/*.clj resources/*
	lein uberjar

test:
	lein test

clean:
	rm -rf target

lint:
	lein kibit

docker:
	docker build -t ballomud .

deploy: ${JAR}
	rsync -vurt ../ballomud tw:
	ssh tw 'killall java; sleep 1; cd ballomud; nohup ./tw 0.0.0.0 >/dev/null 2>&1 &'
