package peg.node;

public sealed interface Node
    permits Term, Ident, Sequence, OrderedChoice, Literal, Charset, Not, Empty, Wildcard, EOF {}
