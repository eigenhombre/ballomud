.PHONY: clean all test lint docker

JAR=target/uberjar/tableworld-0.1.0-SNAPSHOT-standalone.jar

all: uberjar

uberjar: ${JAR}

${JAR}: src/tableworld/*.clj resources/*
	lein uberjar

test:
	lein test

clean:
	rm -rf target

lint:
	lein kibit
	lein bikeshed

docker:
	docker build -t tableworld .

deploy: ${JAR}
	rsync -vurt ../tableworld tw:
	ssh tw 'killall java; sleep 1; cd tableworld; nohup ./tw 0.0.0.0 >/dev/null 2>&1 &'
