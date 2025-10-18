package peg.node;

public record Rule(String name, Node rhs, RuleKind kind) {}
