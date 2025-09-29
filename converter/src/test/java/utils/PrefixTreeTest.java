package utils;

import org.junit.jupiter.api.Test;
import peg.node.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PrefixTreeTest {
    
    @Test
    public void testDanglingElse() {
        // stmt ← "if" expr "then" stmt "else" stmt
        //      / "if" expr "then" stmt

        Node ifLiteral = new Literal("if");
        Node expr = new Literal("expr");
        Node thenLiteral = new Literal("then");
        Node elseLiteral = new Literal("else");
        Node stmt = new Literal("stmt");

        Sequence fullIfElse = new Sequence(List.of(
                ifLiteral,
                expr,
                thenLiteral,
                stmt,
                elseLiteral,
                stmt
        ));

        Sequence ifThen = new Sequence(List.of(
                ifLiteral,
                expr,
                thenLiteral,
                stmt
        ));

        OrderedChoice choice = new OrderedChoice(List.of(
                fullIfElse,
                ifThen
        ));

        Rule rule = new Rule("stmt", choice);

        PrefixTree tree = new PrefixTree(List.of(rule));


        String output = tree.prettyPrint();
        assertTrue(output.contains("if"));
        assertTrue(output.contains("then"));
        assertTrue(output.contains("else"));

        assertTrue(output.indexOf("else") > output.indexOf("then"));
    }

		@Test
    public void testDotNodeIdStmt(){
      // stmt → node_stmt | id_ '=' id_
      // node_stmt → node_id
      // node_id → id_
        Node id = new Literal("id");
        Node node_stmt_ref = new Ident("node_stmt");
        Node node_id_ref = new Ident("node_id");
        Sequence idSeq  = new Sequence(List.of(id));

        Node eq = new Literal("=");
        Sequence assignment = new Sequence(List.of(id, eq, id)); // id = id

        // stmt → node_stmt | (id = id)
        OrderedChoice stmtChoices = new OrderedChoice(List.of(
                node_stmt_ref, assignment
        ));

        Rule stmtRule = new Rule("stmt", stmtChoices);
        Rule nodeStmtRule = new Rule("node_stmt", node_id_ref);
        Rule nodeIdRule = new Rule("node_id", id);

        PrefixTree tree = new PrefixTree(List.of(stmtRule));

    }
}
