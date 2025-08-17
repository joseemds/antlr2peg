package peg.node;
import java.util.Optional;

public record Term(Node node, Optional<Operator> op) implements Node {}
