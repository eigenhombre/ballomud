.PHONY: clean all test

JAR=target/uberjar/tableworld-0.1.0-SNAPSHOT-standalone.jar

all: uberjar

uberjar: ${JAR}

${JAR}: src/tableworld/*.clj
	lein uberjar

test:
	lein test

clean:
	rm -rf target
