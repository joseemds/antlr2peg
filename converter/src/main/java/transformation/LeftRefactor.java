package transformation;

import java.util.*;
import peg.node.*;

public class LeftRefactor implements Transformation {

  @Override
  public Node apply(Node node) {
    return refactor(node);
  }

  private Node refactor(Node node) {
    return switch (node) {
      case OrderedChoice oc -> leftFactorChoice(oc);
      case Sequence seq -> new Sequence(seq.nodes().stream().map(this::refactor).toList());
      case Term t -> new Term(refactor(t.node()), t.op());
      case Not n -> new Not(refactor(n.node()));
      default -> node;
    };
  }

  private Node leftFactorChoice(OrderedChoice oc) {
    List<Node> alts = oc.nodes().stream().map(this::refactor).toList();

    if (alts.size() <= 1) return new OrderedChoice(alts);

    List<Node> prefix = longestCommonPrefix(alts);

    if (prefix.isEmpty()) {
      return new OrderedChoice(alts);
    }

    List<Node> newAlts = new ArrayList<>();
    for (Node alt : alts) {
      List<Node> flattened = flattenSeq(alt);
      List<Node> suffix = flattened.subList(Math.min(prefix.size(), flattened.size()), flattened.size());

      if (suffix.isEmpty()) {
        newAlts.add(new Empty());
      } else if (suffix.size() == 1) {
        newAlts.add(suffix.get(0));
      } else {
        newAlts.add(new Sequence(suffix));
      }
    }

    Node factoredAlt;
    if (newAlts.size() == 1) {
      factoredAlt = newAlts.get(0);
    } else {
      factoredAlt = new OrderedChoice(newAlts);
    }

    List<Node> combined = new ArrayList<>(prefix);
    combined.add(factoredAlt);

    Node result = new Sequence(combined);

    return refactor(result);
  }

  private List<Node> longestCommonPrefix(List<Node> alternatives) {
    if (alternatives.isEmpty()) return List.of();

    List<List<Node>> flatAlts = alternatives.stream()
        .map(this::flattenSeq)
        .toList();

    List<Node> prefix = new ArrayList<>();
    int minLen = flatAlts.stream().mapToInt(List::size).min().orElse(0);
    for (int i = 0; i < minLen; i++) {
      final int idx = i;
      Node first = flatAlts.get(0).get(i);
      boolean allSame = flatAlts.stream().allMatch(a -> nodesEqual(first, a.get(idx)));
      if (allSame) {
        prefix.add(first);
      } else {
        break;
      }
    }

    return prefix;
  }

  private List<Node> flattenSeq(Node node) {
    if (node instanceof Sequence seq) {
      List<Node> res = new ArrayList<>();
      for (Node n : seq.nodes()) {
        res.addAll(flattenSeq(n));
      }
      return res;
    }
    return List.of(node);
  }

  private boolean nodesEqual(Node a, Node b) {
    if (a.getClass() != b.getClass()) return false;
    return switch (a) {
      case Literal litA -> ((Literal) b).content().equals(litA.content());
      case Charset csA -> ((Charset) b).content().equals(csA.content());
      case Ident idA -> ((Ident) b).name().equals(idA.name());
      case Wildcard w -> true;
      case Empty e -> true;
      default -> a.equals(b);
    };
  }
}
