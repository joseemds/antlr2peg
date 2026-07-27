.PHONY: all build jar benchmark gen test gen-test clean download-grammars

GRAMMAR   ?= 
EXT       ?= 
START     ?= 
N         ?= 50
OUT       ?= /tmp

all: build

download-grammars:
	@if [ ! -d /tmp/grammars ]; then \
		git clone https://github.com/antlr/grammars-v4.git /tmp/grammars && \
		cd /tmp/grammars && \
		git checkout 55d2bd37ca9b4271ff1f5cb3868e7d58e68f5a0f; \
	else \
		echo "/tmp/grammars already exists; skipping clone."; \
	fi

deps:
	cd converter && ./gradlew dependencies

build:
	cd converter && ./gradlew build -x shadowJar

jar:
	cd converter && ./gradlew shadowJar

benchmark: download-grammars
	cd converter && ./gradlew benchmark --args="grammars.json"

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
