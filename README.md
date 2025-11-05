### Dependencies
- Lua > 5.1
- lpeglabel > 1.0
- grammarinator > 23.7
- antlr4

### Setup

The project has a requirements.txt file with all python dependencies needed, to install and load it you can follow the steps:

```bash
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt
```
Besides the python dependencies, to install the `lpeglabel` and `lua`, you can use yours system package managar or use
`luarocks`.

```bash
luarocks install lpeglabel
```

### Running the project

To see a list of useful commands, run `make help`


To run a parser benchmark run (inside ./converter/):

```bash
./gradlew compareParser --args="Dot --use-gen"
```

To generate a parser run (inside ./converter/):

```bash
 ./gradlew run --args="-i src/main/antlr/Dot.g4 -o dot.lua"
```
