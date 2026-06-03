# ANTLR2PEG

## Dependencies
- Lua > 5.3
- Java 21
- lpeglabel > 1.0
- grammarinator == 26.1
- antlr4

ANTLR2PEG source code is written in java and is available at the folder converter/

### Setup

To build ANTLR2PEG, run, on the root folder:

```bash
make deps && make jar
```

After that, you can use the helper `antlr2peg` to run the tool.

As input, an ANTLR grammar is required, which can be found at [ANTLR/grammars-v4](github.com/antlr/grammars-v4)

The syntax to run antlr2peg is:


```
# For a single file
./antlr2peg -i grammar.g4 -o grammar.lua

# For separated lexer and grammar
./antlr2peg -l lexer.g4 -g grammar.g4 -o grammar.lua
```


To run the generated test, it is required to have lua (>= 5.3) installed together with thelpeglabel library, which can be installe with:

```
luarocks install lpeglabel
```
