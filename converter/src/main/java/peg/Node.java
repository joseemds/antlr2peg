package peg;

public sealed interface Node permits Rule, Term, Ident, Sequence, OrderedChoice, Modifier {}
