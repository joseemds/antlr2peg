package peg.node;
import java.util.List;

public record OrderedChoice(List<Node> nodes) implements Node {}
