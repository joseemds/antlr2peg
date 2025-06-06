LUA = lua
LUAROCKS = luarocks
EXAMPLES_DIR = examples
ANTLR_DIR = antlr
ANTLR = antlr4
ANTLR-PARSE = antlr4-parse

EXAMPLE_FILES = $(wildcard $(EXAMPLES_DIR)/*.dot)

test:
	@echo "--- Running validation for both Lua and ANTLR parsers ---"
	@for file in $(EXAMPLE_FILES); do \
		echo "  Testing $$file..."; \
		( $(LUA) main.lua $$file && \
		  echo "    - Lua: OK" && \
		  antlr4-parse $(ANTLR_DIR)/DOT.g4 graph -tree $$file > /dev/null && \
		  echo "    - ANTLR: OK" ); \
	done

clean:
	@rm -rf $(ANTLR_DIR)gen/
	@rm -f $(EXAMPLES_DIR)/*.lua.out $(EXAMPLES_DIR)/*.antlr.out

gen:
	@echo "Generating Antlr4 parser..."
	$(ANTLR) $(ANTLR_DIR)/DOT.g4 -o $(ANTLR_DIR)/gen
