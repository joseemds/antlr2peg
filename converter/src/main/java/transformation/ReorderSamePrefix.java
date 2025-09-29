package transformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import peg.node.*;

public class ReorderSamePrefix implements Transformation {

  private final Map<String, Set<Node>> firstSet;
  private final Map<String, Node> nonTerminals;

  public ReorderSamePrefix(Map<String, Set<Node>> firstSet, Map<String, Node> nonTerminals) {
    this.firstSet = firstSet;
    this.nonTerminals = nonTerminals;
  }

  @Override
  public Node apply(Node n) {
    return reorderNode(n);
  }

  private Node reorderNode(Node node) {
    return switch (node) {
      case OrderedChoice oc -> {
        List<Node> children =
            oc.nodes().stream()
                .map(this::reorderNode)
                .collect(Collectors.toCollection(ArrayList::new));

        reorderByPrefix(children);

        yield new OrderedChoice(children);
      }
      case Sequence seq -> new Sequence(
          seq.nodes().stream().map(this::reorderNode).collect(Collectors.toList()));

      case Not not -> new Not(reorderNode(not.node()));

      case Term term -> new Term(reorderNode(term.node()), term.op());

      default -> node;
    };
  }

  private void reorderByPrefix(List<Node> nodes) {
    for (int i = 0; i < nodes.size(); i++) {
      for (int j = i + 1; j < nodes.size(); j++) {
        Node nodeA = nodes.get(i);
        Node nodeB = nodes.get(j);

        List<Node> a = flattenSeqOrIdent(nodeA);
        List<Node> b = flattenSeqOrIdent(nodeB);
        int sharedLen = sharedPrefixLength(a, b);

        if (sharedLen > 0) {
          boolean aIsPrefix = sharedLen == a.size() && b.size() > a.size();

          if (aIsPrefix) {
            Collections.swap(nodes, i, j);
          }
        } else {
          Set<Node> firstA = getFirstSetForNode(nodeA);
          Set<Node> firstB = getFirstSetForNode(nodeB);

          Set<Node> intersection = new HashSet<>(firstA);
          intersection.retainAll(firstB);

          if (!intersection.isEmpty()) {

            boolean aIsIdent = nodeA instanceof Ident;
            boolean bIsSequence = nodeB instanceof Sequence;

            if (aIsIdent && bIsSequence) {
              Collections.swap(nodes, i, j);
            }
          }
        }
      }
    }
  }

  private Set<Node> getFirstSetForNode(Node node) {
    return switch (node) {
      case Literal lit -> Set.of(lit);
      case Charset cs -> Set.of(cs);

      case Ident id -> {
        Set<Node> first = firstSet.get(id.name());
        if (first == null) throw new Error("Unknown rule " + id.name());
        yield first;
      }

      case Sequence seq -> seq.nodes().isEmpty()
          ? Collections.emptySet()
          : getFirstSetForNode(seq.nodes().get(0));

      case Term t -> getFirstSetForNode(t.node());

      default -> Collections.emptySet();
    };
  }

  private List<Node> flattenSeqOrIdent(Node node) {
    return switch (node) {
      case Ident id -> {
        if (nonTerminals.containsKey(id.name())) {
          yield deepFlatten(nonTerminals.get(id.name()), new HashSet<>());
        } else {
          yield List.of(id);
        }
      }
      case Sequence seq -> seq.nodes().stream()
          .flatMap(n -> flattenSeqOrIdent(n).stream())
          .collect(Collectors.toList());

      case Literal lit -> List.of(lit);
      case Charset cs -> List.of(cs);
      case Term t -> flattenSeqOrIdent(t.node());
      default -> List.of(node);
    };
  }

  private int sharedPrefixLength(List<Node> a, List<Node> b) {
    int len = Math.min(a.size(), b.size());
    for (int i = 0; i < len; i++) {
      if (!a.get(i).equals(b.get(i))) {
        return i;
      }
    }
    return len;
  }

  private List<Node> deepFlatten(Node node, Set<String> visited) {
    return switch (node) {
      case Ident id -> {
        if (visited.contains(id.name())) {
          yield new ArrayList<>();
        }
        if (nonTerminals.containsKey(id.name())) {
          visited.add(id.name());
          yield deepFlatten(nonTerminals.get(id.name()), visited);
        } else {
          yield List.of(id);
        }
      }
      case Sequence seq -> seq.nodes().stream()
          .flatMap(child -> deepFlatten(child, visited).stream())
          .collect(Collectors.toList());

      case OrderedChoice oc -> oc.nodes().isEmpty()
          ? new ArrayList<>()
          : deepFlatten(oc.nodes().get(0), visited);

      case Literal lit -> List.of(lit);
      case Charset cs -> List.of(cs);
      case Term t -> deepFlatten(t.node(), visited);

      default -> new ArrayList<>();
    };
  }
}
