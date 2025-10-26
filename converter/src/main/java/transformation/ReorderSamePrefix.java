package transformation;

import java.util.*;
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
        List<Node> children = new ArrayList<>();
        for (Node child : oc.nodes()) {
          children.add(reorderNode(child));
        }
        reorderByPrefix(children);
        yield new OrderedChoice(children);
      }
      case Sequence seq -> {
        List<Node> children = new ArrayList<>();
        for (Node child : seq.nodes()) {
          children.add(reorderNode(child));
        }
        yield new Sequence(children);
      }
      case Not not -> new Not(reorderNode(not.node()));
      case Term term -> new Term(reorderNode(term.node()), term.op());
      default -> node;
    };
  }

  private void reorderByPrefix(List<Node> alternatives) {
    if (alternatives.size() <= 1) return;

    List<AlternativeInfo> altInfos = new ArrayList<>();
    for (Node alt : alternatives) {
      altInfos.add(new AlternativeInfo(alt));
    }

    altInfos.sort(
        (a, b) -> {
          int prefixComparison = compareByPrefix(a, b);
          if (prefixComparison != 0) return prefixComparison;

          int specificityComparison = compareBySpecificity(a, b);
          if (specificityComparison != 0) return specificityComparison;

          return compareByFirstSetConflict(a, b);
        });

    alternatives.clear();
    for (AlternativeInfo info : altInfos) {
      alternatives.add(info.node);
    }
  }

  private int compareByPrefix(AlternativeInfo a, AlternativeInfo b) {
    List<Node> flatA = a.flattened;
    List<Node> flatB = b.flattened;

    int sharedLen = sharedPrefixLength(flatA, flatB);

    if (sharedLen > 0) {
      boolean aIsPrefixOfB = sharedLen == flatA.size() && flatB.size() > flatA.size();
      boolean bIsPrefixOfA = sharedLen == flatB.size() && flatA.size() > flatB.size();

      if (aIsPrefixOfB) return 1; // b before a
      if (bIsPrefixOfA) return -1; // a before b

      return compareSpecificityAfterPrefix(flatA, flatB, sharedLen);
    }

    return 0;
  }

  private int compareBySpecificity(AlternativeInfo a, AlternativeInfo b) {
    boolean aIsSequence = a.node instanceof Sequence;
    boolean bIsSequence = b.node instanceof Sequence;

    if (aIsSequence && !bIsSequence) return -1;
    if (!aIsSequence && bIsSequence) return 1;

    int lengthCompare = Integer.compare(b.flattened.size(), a.flattened.size());
    if (lengthCompare != 0) return lengthCompare;

    int terminalCompare = Integer.compare(b.terminalCount, a.terminalCount);
    if (terminalCompare != 0) return terminalCompare;

    return 0;
  }

  private int compareByFirstSetConflict(AlternativeInfo a, AlternativeInfo b) {
    Set<Node> intersection = new HashSet<>(a.firstSet);
    intersection.retainAll(b.firstSet);

    if (!intersection.isEmpty()) {
      boolean aIsIdent = a.node instanceof Ident;
      boolean bIsSequence = b.node instanceof Sequence;

      if (aIsIdent && bIsSequence) return 1;

      boolean bIsIdent = b.node instanceof Ident;
      boolean aIsSequence = a.node instanceof Sequence;

      if (bIsIdent && aIsSequence) return -1;
    }

    return 0;
  }

  private int compareSpecificityAfterPrefix(List<Node> a, List<Node> b, int prefixLen) {
    if (a.size() <= prefixLen || b.size() <= prefixLen) return 0;

    Node nextA = a.get(prefixLen);
    Node nextB = b.get(prefixLen);

    // Prefer terminals over non-terminals
    boolean aIsTerminal = isTerminal(nextA);
    boolean bIsTerminal = isTerminal(nextB);

    if (aIsTerminal && !bIsTerminal) return -1;
    if (!aIsTerminal && bIsTerminal) return 1;

    return 0;
  }

  private boolean isTerminal(Node node) {
    return node instanceof Literal || node instanceof Charset;
  }

  private int sharedPrefixLength(List<Node> a, List<Node> b) {
    int len = Math.min(a.size(), b.size());
    for (int i = 0; i < len; i++) {
      if (!nodesEqual(a.get(i), b.get(i))) {
        return i;
      }
    }
    return len;
  }

  private boolean nodesEqual(Node a, Node b) {
    if (a.getClass() != b.getClass()) return false;

    return switch (a) {
      case Literal litA -> {
        Literal litB = (Literal) b;
        yield litA.content().equals(litB.content());
      }
      case Charset csA -> {
        Charset csB = (Charset) b;
        yield csA.equals(csB); // Implement proper Charset equality
      }
      case Ident idA -> {
        Ident idB = (Ident) b;
        yield idA.name().equals(idB.name());
      }
      default -> a.equals(b);
    };
  }

  private class AlternativeInfo {
    final Node node;
    final List<Node> flattened;
    final Set<Node> firstSet;
    final int terminalCount;

    AlternativeInfo(Node node) {
      this.node = node;
      this.flattened = flattenSeqOrIdent(node);
      this.firstSet = getFirstSetForNode(node);
      this.terminalCount = countTerminals(flattened);
    }
  }

  private int countTerminals(List<Node> nodes) {
    return (int) nodes.stream().filter(this::isTerminal).count();
  }

  private Set<Node> getFirstSetForNode(Node node) {
    return switch (node) {
      case Literal lit -> Set.of(lit);
      case Charset cs -> Set.of(cs);
      case Ident id -> firstSet.getOrDefault(id.name(), Set.of());
      case Sequence seq -> seq.nodes().isEmpty()
          ? Set.of()
          : getFirstSetForNode(seq.nodes().get(0));
      case Term t -> getFirstSetForNode(t.node());
      default -> Set.of();
    };
  }

  private List<Node> flattenSeqOrIdent(Node node) {
    return flattenSeqOrIdent(node, new HashSet<>());
  }

  private List<Node> flattenSeqOrIdent(Node node, Set<String> visited) {
    return switch (node) {
      case Ident id -> {
        if (visited.contains(id.name())) {
          yield List.of(id);
        }
        visited.add(id.name());
        Node definition = nonTerminals.get(id.name());
        if (definition != null) {
          yield flattenSeqOrIdent(definition, visited);
        } else {
          yield List.of(id);
        }
      }
      case Sequence seq -> {
        List<Node> result = new ArrayList<>();
        for (Node child : seq.nodes()) {
          result.addAll(flattenSeqOrIdent(child, new HashSet<>(visited)));
        }
        yield result;
      }
      case Literal lit -> List.of(lit);
      case Charset cs -> List.of(cs);
      case Term t -> flattenSeqOrIdent(t.node(), visited);
      default -> List.of(node);
    };
  }
}
