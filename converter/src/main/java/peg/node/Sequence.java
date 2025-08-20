package peg.node;

import java.util.List;

public record Sequence(List<Node> nodes) implements Node {}
