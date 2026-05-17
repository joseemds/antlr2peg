package peg.node;

public sealed interface Node
    permits Repetition,
        Ident,
        Sequence,
        OrderedChoice,
        Literal,
        Charset,
        Not,
        And,
        Empty,
        Wildcard,
        EOF {}
