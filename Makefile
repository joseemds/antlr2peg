LUA = lua
LUAROCKS = luarocks
EXAMPLES_DIR = examples
ANTLR_DIR = antlr
ANTLR = antlr4
ANTLR-PARSE = antlr4-parse
GRAMMARINATOR_GEN = grammarinator-generate
RED=\033[0;31m
GREEN=\033[0;32m
NC=\033[0m


LANGUAGE?=dot
START_RULE?=graph

EXAMPLE_FILES = $(wildcard $(ANTLR_DIR)/$(LANGUAGE)/$(EXAMPLES_DIR)/*.$(LANGUAGE))
GENERATED_INPUTS = $(wildcard $(ANTLR_DIR)/$(LANGUAGE)/$(EXAMPLES_DIR)/gen/*.gen.$(LANGUAGE))

test: test-handcrafted test-generated

test-generated:
	@echo "--- Running validation for both Lua and ANTLR parsers on generated files ---"
	@for file in $(GENERATED_INPUTS); do \
		echo "  Testing $$file..."; \
		( antlr4-parse $(ANTLR_DIR)/$(LANGUAGE)/$(LANGUAGE).g4 $(START_RULE) -tree $$file > /dev/null && \
		  echo -e "    - ANTLR: $(GREEN)OK$(NC)" && \
			$(LUA) main.lua $$file && \
		  echo -e "    - Lua: $(GREEN)OK$(NC)" );\
	done

test-handcrafted:
	@echo "--- Running validation for both Lua and ANTLR parsers on handcrafted files---"
	@for file in $(EXAMPLE_FILES); do \
		echo "  Testing $$file..."; \
		( antlr4-parse $(ANTLR_DIR)/$(LANGUAGE)/$(LANGUAGE).g4 $(START_RULE) -tree $$file > /dev/null && \
		  echo -e "    - ANTLR: $(GREEN)OK$(NC)" && \
			$(LUA) main.lua ./$$file && \
		  echo -e "    - Lua: $(GREEN)OK$(NC)" );\
	done

clean:
	@rm -rf $(ANTLR_DIR)/**/gen/
	@rm -f $(EXAMPLES_DIR)/**/*.lua.out $(EXAMPLES_DIR)/**/*.antlr.out

gen: gen-antlr gen-grammarinator

gen-all: gen-antlr gen-grammarinator gen-grammarinator-tests

gen-antlr:
	@echo "Generating Antlr4 parser..."
	$(ANTLR) $(ANTLR_DIR)/$(LANGUAGE)/$(LANGUAGE).g4 -o $(ANTLR_DIR)/$(LANGUAGE)/gen

gen-grammarinator:
	@echo "Generating grammarinator files"
	grammarinator-process $(ANTLR_DIR)/$(LANGUAGE)/$(LANGUAGE).g4 -o $(ANTLR_DIR)/$(LANGUAGE)/examples/gen/   
gen-grammarinator-tests: gen-grammarinator
	@echo "Generating gen-grammarinator tests..."
	@SEED=$$(od -An -N4 -tu4 < /dev/urandom | tr -d ' '); \
	echo $$SEED > antlr/$(LANGUAGE)/examples/gen/seed; \
	echo "Running with seed: $$SEED"; \
	$(GRAMMARINATOR_GEN) $(LANGUAGE)Generator.$(LANGUAGE)Generator -d 20 -o antlr/$(LANGUAGE)/examples/gen/test_%d.gen.$(LANGUAGE) -n 100 \
	--random-seed $$SEED \
	-s grammarinator.runtime.simple_space_serializer \
	--sys-path antlr/$(LANGUAGE)/examples/gen/
