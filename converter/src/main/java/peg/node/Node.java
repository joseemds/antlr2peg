package peg.node;

public sealed interface Node permits Rule, Term, Ident, Sequence, OrderedChoice, Literal, Charset {}
