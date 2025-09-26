package peg;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import peg.node.*;
import transformation.Transformation;

public class PegGrammar {
  private List<Rule> rules = new ArrayList<>();

  public Term mkTerm(Node node, Optional<Operator> op) {
    return new Term(node, op);
  }

  public List<Rule> getRules() {
    return this.rules;
  }

  public Operator operatorOfString(String op) {
    switch (op) {
      case "?":
        return Operator.OPTIONAL;
      case "+":
        return Operator.PLUS;
      case "*":
        return Operator.STAR;
      default:
        throw new Error("Unexpected operator " + op);
    }
  }

  public Ident mkIdent(String name) {
    return new Ident(name);
  }

  public Literal mkLiteral(String content) {
    return new Literal(content);
  }

  public Charset mkCharset(String content) {
    return new Charset(content);
  }

  public Rule mkRule(String lhs, Node rhs) {
    return new Rule(lhs, rhs);
  }

  public OrderedChoice mkOrderedChoice(List<Node> nodes) {
    return new OrderedChoice(nodes);
  }

  public Sequence mkSequence(List<Node> nodes) {
    return new Sequence(nodes);
  }

  public Empty mkEmpty() {
    return new Empty();
  }

  public Not mkNot(Node node) {
    return new Not(node);
  }

  public Wildcard mkWildcard() {
    return new Wildcard();
  }

  public void addRule(Rule rule) {
    this.rules.add(rule);
  }

  public List<Rule> transform(Transformation transformation) {
    return this.rules.stream()
        .map(rule -> new Rule(rule.name(), transformation.apply(rule.rhs())))
        .toList();
  }
}
