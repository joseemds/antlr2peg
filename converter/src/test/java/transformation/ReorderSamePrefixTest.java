package transformation;

import org.junit.jupiter.api.Test;

import peg.node.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ReorderSamePrefixTest {

    @Test
    void testReorderSpecificBeforeGeneric() {
        // stmt = node_stmt / (id_ '=' id_)
        // node_stmt = node_id
        // node_id = id_

        Ident nodeStmt = new Ident("node_stmt");
        Sequence idEqualsId = new Sequence(List.of(
                new Ident("id_"),
                new Literal("="),
                new Ident("id_")
        ));

        OrderedChoice stmtChoices = new OrderedChoice(List.of(nodeStmt, idEqualsId));
        Rule stmtRule = new Rule("stmt", stmtChoices);

        Map<String, Node> nonTerminals = Map.of(
                "node_stmt", new Sequence(List.of(new Ident("node_id"))),
                "node_id", new Sequence(List.of(new Ident("id_")))
        );

        Set<Node> idFirstSet = Set.of(new Ident("ID"));
        Map<String, Set<Node>> firstSet = Map.of(
                "id_", idFirstSet,
                "node_id", idFirstSet,
                "node_stmt", idFirstSet
        );

        ReorderSamePrefix transformer = new ReorderSamePrefix(firstSet, nonTerminals);
        Node transformed = transformer.apply(stmtRule.rhs());

        assertInstanceOf(OrderedChoice.class, transformed);
        OrderedChoice resultChoice = (OrderedChoice) transformed;
        List<Node> reordered = resultChoice.nodes();

        assertEquals(2, reordered.size());

        assertTrue(reordered.get(0) instanceof Sequence);
        assertEquals(idEqualsId, reordered.get(0));

        assertTrue(reordered.get(1) instanceof Ident);
        assertEquals(nodeStmt, reordered.get(1));
    }
}
