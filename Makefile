LUA ?= lua
LUAROCKS ?= luarocks
EXAMPLES_DIR ?= examples
ANTLR_DIR ?= antlr
ANTLR ?= antlr4
ANTLR_PARSE ?= antlr4-parse
GRAMMARINATOR_GEN ?= grammarinator-generate

LANGUAGE ?= dot
START_RULE ?= graph


IS_TTY:=$(if $(MAKE_TERMOUT),1,0)

ifeq ($(IS_TTY),1)
    RED = $(shell tput setaf 1)
    GREEN = $(shell tput setaf 2)
    YELLOW = $(shell tput setaf 3)
    BLUE = $(shell tput setaf 4)
    NC = $(shell tput sgr0)
else
    RED = 
    GREEN = 
    YELLOW = 
    BLUE = 
    NC = 
endif

EXAMPLE_FILES := $(wildcard $(ANTLR_DIR)/$(LANGUAGE)/$(EXAMPLES_DIR)/*.$(LANGUAGE))
GENERATED_INPUTS := $(wildcard $(ANTLR_DIR)/$(LANGUAGE)/$(EXAMPLES_DIR)/gen/*.gen.$(LANGUAGE))
GRAMMAR_FILE := $(ANTLR_DIR)/$(LANGUAGE)/$(LANGUAGE).g4
GEN_DIR := $(ANTLR_DIR)/$(LANGUAGE)/$(EXAMPLES_DIR)/gen

.PHONY: all
all: test

.PHONY: test test-handcrafted test-generated

test: test-handcrafted test-generated
	@echo -e "$(GREEN)All tests completed$(NC)"

test-handcrafted:
	@echo -e "$(BLUE)--- Running validation for both Lua and ANTLR parsers on handcrafted files ---$(NC)"
	@for file in $(EXAMPLE_FILES); do \
		echo -e "$(YELLOW)Testing $$file...$(NC)"; \
		if $(ANTLR_PARSE) $(GRAMMAR_FILE) $(START_RULE) -tree $$file > /dev/null 2>&1; then \
			echo -e "  - ANTLR: $(GREEN)OK$(NC)"; \
			if $(LUA) main.lua $$file $(LANGUAGE) > /dev/null 2>&1; then \
				echo -e "  - Lua: $(GREEN)OK$(NC)"; \
			else \
				echo -e "  - Lua: $(RED)FAILED$(NC)"; \
			fi; \
		else \
			echo -e "  - ANTLR: $(RED)FAILED$(NC)"; \
		fi; \
	done

test-generated:
	@echo -e "$(BLUE)--- Running validation for both Lua and ANTLR parsers on generated files ---$(NC)"
	@for file in $(GENERATED_INPUTS); do \
		echo -e "$(YELLOW)Testing $$file...$(NC)"; \
		if $(ANTLR_PARSE) $(GRAMMAR_FILE) $(START_RULE) -tree $$file > /dev/null 2>&1; then \
			echo -e "  - ANTLR: $(GREEN)OK$(NC)"; \
			if $(LUA) main.lua $$file $(LANGUAGE) > /dev/null 2>&1; then \
				echo -e "  - Lua: $(GREEN)OK$(NC)"; \
			else \
				echo -e "  - Lua: $(RED)FAILED$(NC)"; \
			fi; \
		else \
			echo -e "  - ANTLR: $(RED)FAILED$(NC)"; \
		fi; \
	done

.PHONY: gen gen-all gen-antlr gen-grammarinator gen-grammarinator-tests

gen: gen-antlr gen-grammarinator

gen-all: gen-antlr gen-grammarinator gen-grammarinator-tests

gen-antlr:
	@echo -e "$(BLUE)Generating Antlr4 parser...$(NC)"
	@mkdir -p $(ANTLR_DIR)/$(LANGUAGE)/gen
	$(ANTLR) $(GRAMMAR_FILE) -o $(ANTLR_DIR)/$(LANGUAGE)/gen

gen-grammarinator:
	@echo -e "$(BLUE)Generating grammarinator files$(NC)"
	@mkdir -p $(GEN_DIR)
	grammarinator-process $(GRAMMAR_FILE) -o $(GEN_DIR)

gen-grammarinator-tests: gen-grammarinator
	@echo -e "$(BLUE)Generating grammarinator tests...$(NC)"
	@SEED=$$(od -An -N4 -tu4 < /dev/urandom | tr -d ' '); \
	echo $$SEED > $(GEN_DIR)/seed; \
	echo -e "$(YELLOW)Running with seed: $$SEED$(NC)"; \
	$(GRAMMARINATOR_GEN) $(LANGUAGE)Generator.$(LANGUAGE)Generator -d 20 -o antlr/$(LANGUAGE)/examples/gen/test_%d.gen.$(LANGUAGE) -n 100 \
	--random-seed $$SEED \
	-s grammarinator.runtime.simple_space_serializer \
	--sys-path antlr/$(LANGUAGE)/examples/gen/

.PHONY: clean
clean:
	@echo -e "$(BLUE)Cleaning generated files...$(NC)"
	@rm -rf $(ANTLR_DIR)/*/gen/
	@rm -f $(EXAMPLES_DIR)/*/*.lua.out $(EXAMPLES_DIR)/*/*.antlr.out

.PHONY: help
help:
	@echo -e "$(BLUE)Available targets:$(NC)"
	@echo -e "  all        - Run all tests (default target)"
	@echo -e "  test       - Run all tests"
	@echo -e "  test-handcrafted - Test handcrafted example files"
	@echo -e "  test-generated   - Test generated example files"
	@echo -e "  gen        - Generate parser and grammarinator files"
	@echo -e "  gen-all    - Generate everything including test files"
	@echo -e "  clean      - Clean generated files"
	@echo -e "  help       - Show this help message"
