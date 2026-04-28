package benchmark;

import java.util.List;

public record GrammarEntry(
    String name, String parser, String lexer, String start, List<String> examples) {}
;
