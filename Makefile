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

EXAMPLE_FILES = $(wildcard $(EXAMPLES_DIR)/*.dot)
GENERATED_EXAMPLES = $(wildcard $(EXAMPLES_DIR)/gen/*.gen.dot)

test: test-handcrafted test-generated

test-generated:
	@echo "--- Running validation for both Lua and ANTLR parsers on generated files ---"
	@for file in $(GENERATED_EXAMPLES); do \
		echo "  Testing $$file..."; \
		( antlr4-parse $(ANTLR_DIR)/DOT.g4 graph -tree $$file > /dev/null && \
		  echo -e "    - ANTLR: $(GREEN)OK$(NC)" && \
			$(LUA) main.lua $$file && \
		  echo -e "    - Lua: $(GREEN)OK$(NC)" );\
	done

test-handcrafted:
	@echo "--- Running validation for both Lua and ANTLR parsers on handcrafted files---"
	@for file in $(EXAMPLE_FILES); do \
		echo "  Testing $$file..."; \
		( antlr4-parse $(ANTLR_DIR)/DOT.g4 graph -tree $$file > /dev/null && \
		  echo -e "    - ANTLR: $(GREEN)OK$(NC)" && \
			$(LUA) main.lua $$file && \
		  echo -e "    - Lua: $(GREEN)OK$(NC)" );\
	done

clean:
	@rm -rf $(ANTLR_DIR)gen/
	@rm -f $(EXAMPLES_DIR)/*.lua.out $(EXAMPLES_DIR)/*.antlr.out

gen:
	@echo "Generating Antlr4 parser..."
	$(ANTLR) $(ANTLR_DIR)/DOT.g4 -o $(ANTLR_DIR)/gen

gen-grammarinator:
	@echo "Generating grammarinator files"
	grammarinator-process antlr/DOT.g4 -o examples/gen/   

gen-grammarinator-tests: gen-grammarinator
	@echo "Generating gen-grammarinator tests..."
	$(GRAMMARINATOR_GEN) DOTGenerator.DOTGenerator -r graph -d 20 -o examples/gen/test_%d.gen.dot -n 100 \
  -s grammarinator.runtime.simple_space_serializer \
	--sys-path examples/gen/ \

