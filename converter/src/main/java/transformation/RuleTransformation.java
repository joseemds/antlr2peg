package transformation;

import peg.node.Rule;

@FunctionalInterface
public interface RuleTransformation {
  public Rule apply(Rule node);
}
