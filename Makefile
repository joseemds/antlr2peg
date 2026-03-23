.PHONY: all build jar benchmark gen test gen-test clean

GRAMMAR   ?= 
EXT       ?= 
START     ?= 
N         ?= 50
OUT       ?= /tmp

all: build

build:
	cd converter && ./gradlew build -x shadowJar

jar:
	cd converter && ./gradlew shadowJar

benchmark:
	cd converter && ./gradlew benchmark

format:
	cd converter && ./gradlew spotlessApply

_check-grammar:
	@test -n "$(GRAMMAR)" || (echo "ERROR: GRAMMAR is required. Usage: make <target> GRAMMAR=foo.g4 EXT=foo START=rule"; exit 1)
	@test -n "$(EXT)"     || (echo "ERROR: EXT is required.";     exit 1)

gen: _check-grammar
	./scripts/gen_tests -g $(GRAMMAR) -e $(EXT) -n $(N) -o $(OUT)

test: _check-grammar
	./scripts/run_tests -g $(GRAMMAR) -e $(EXT) $(if $(START),-s $(START),)

gen-test: gen test

clean:
	cd converter && ./gradlew clean
	rm -rf _log
