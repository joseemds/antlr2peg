package peg.node;

public record Rule(String name, Node rhs) implements Node {}
