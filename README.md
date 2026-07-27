# ANTLR2PEG

ANTLR2PEG is a tool that converts ANTLR4 grammars into PEG grammars usable with
`lpeg`

## Paper

This artifact accompanies the paper *ANTLR2PEG: A tool to convert ANTLR grammars into PEGs
grammars*, accepted at
CBSoft 2026 / SBLP 2026. The paper is available at: [./paper/antlr2peg.pdf].

## Repository Structure
```
├─ converter/        # ANTLR2PEG source code (Java)
├─ scripts/ 
├─ grammars/ 
├─ LICENSE
├─ Dockerfile
└─ README.md
```

- `converter/` — ANTLR2PEG source code, written in Java.
- `grammars/` — 10 grammars used for deeper analysis, together with their respective examples for testing
- `scripts/` — Helpers to generate and run tests using grammarinator, written in Python 

## Requirements

### Software environment

ANTLR2PEG was tested using Linux, we expect it to work flawless on other UNIX environments.

ANTLR2PEG dependencies are listed below. We also provide a Dockerfile that setups a working environment to run
the tool and all benchmarks

### Dependencies

- Java 21 (Required to build and run ANTLR2PEG)
- Lua >= 5.3 (Required to run the generated PEG parser)
- make (Required to build ANTLR2PEG more easily)
- lpeglabel > 1.0 (Required to run the generated PEG parser)
- grammarinator == 26.1 (Only required to generated new tests)
- python > 3.11 (Required to run scripts for benchmarks and testing)

## Installation

To simply run and build the tool, only Java is enough 

### Build

To build ANTLR2PEG run on the root folder:

```bash
make deps && make jar
```
After that, you can use the helper `antlr2peg` to run the tool.


### Running the tool

As input, an ANTLR grammar is required, the grammars used for deeper analysis are avaible under (./grammars/)[./grammars/] and can be used for testing purposes

The syntax to run `antlr2peg` is:

```bash
# For a single file
./antlr2peg -i grammar.g4 -o grammar.lua

# For separated lexer and grammar
./antlr2peg -l lexer.g4 -g grammar.g4 -o grammar.lua
```

### Running the generated parser

To run the generated test, it is required to have Lua (>= 5.3) installed together
with the `lpeglabel` library, which can be installed with:

```bash
luarocks install lpeglabel
```

First, we need to generate a PEG parser using the command stated before, left use the `abnf` grammar available under (./grammars/abnf/Abnf.g4)[./grammars/abnf/Abnf.g4]

```
./antlr2peg -i ./grammars/abnf/Abnf.g4 -o abnf.lua
lua abnf.lua < ./grammars/abnf/examples/iri.abnf
# Expected: Parsed: 1881
```

### Using Docker

Instead of downloading all dependencies and building the project locally, ANTLR2PEG can also be used using Docker, by running:

```
docker build --target dev antlr2peg .
```


## License
This project is licensed under the MIT.
See [LICENSE](./LICENSE) for details.
