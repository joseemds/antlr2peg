LUA = lua
LUAROCKS = luarocks
EXAMPLES_DIR = examples/
ANTLR_DIR = antlr/
ANTLR = antlr4

gen:
	@echo "Generating Antlr4 parser..."
	$(ANTLR) $(ANTLR_DIR)/DOT.g4 -o $(ANTLR_DIR)/gen

clean:
	@rm -rf $(ANTLR_DIR)/gen/
	@rm -f $(EXAMPLES_DIR)*.lua.out $(EXAMPLES_DIR)*.antlr.out


test: test_all

test_all: gen test_lua test_antlr

test_lua:
	@echo "Running Lua LPEG parser on example files..."
	@for file in $(EXAMPLE_FILES); do \
		echo "  Running Lua parser on $$file..."; \
		$(LUA) main.lua $$file; \
	done

test_antlr: gen
	@echo "Running Antlr4 parser on example files..."
	@for file in $(EXAMPLE_FILES); do \
		echo "  Running Antlr4 parser on $$file..."; \
		$(ANTLR) $(ANTLR_DIR)/DOT.g4 -tree $$file > $$file.antlr.out; \
	done
