package peg;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

record Term(Node node, Optional<Operator> op) implements Node {}
record Rule(String name, Node rhs) implements Node {}
record Ident(String name) implements Node {}
record Sequence(List<Node> nodes) implements Node {}
record OrderedChoice(List<Node> nodes) implements Node {}
record Modifier() implements Node {}

public class PegAst {
	private List<Node> ast = new ArrayList<>();

	public Term mkTerm(Node node, Optional<Operator> op){
		return new Term(node, op);
	}

	public List<Node> getAst(){
		return this.ast;
	}

	public Operator operatorOfString(String op){
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

	public Ident mkIdent(String name){
		return new Ident(name);
	}

	public Rule mkRule(String lhs, Node rhs){
		return new Rule(lhs, rhs);
	}

	public OrderedChoice mkOrderedChoice(List<Node> nodes){
		return new OrderedChoice(nodes);
	}

	public Sequence mkSequence(List<Node> nodes){
   return new Sequence(nodes);
  }

	public void addNode(Node node){
		this.ast.add(node);
	}
}
