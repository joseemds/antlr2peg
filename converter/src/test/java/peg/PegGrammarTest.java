package peg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import peg.node.*;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PegGrammar FIRST and FOLLOW set computation.
 *
 * Grammar rules used throughout:
 *   - PARSING rules participate in FIRST/FOLLOW computation (isSyntacticRule == true)
 *   - LEXING / FRAGMENT rules do not; their Idents are treated as terminals
 */
public class PegGrammarTest {

    private PegGrammar g;

    @BeforeEach
    void setUp() {
        g = new PegGrammar();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Returns true if the set contains a Literal whose content equals `s`. */
    private boolean containsLiteral(Set<Node> set, String s) {
        return set.stream()
                  .anyMatch(n -> n instanceof Literal lit && lit.content().equals(s));
    }

    /** Returns true if the set contains an Empty node. */
    private boolean containsEmpty(Set<Node> set) {
        return set.stream().anyMatch(n -> n instanceof Empty);
    }

    /** Returns true if the set contains an EOF node. */
    private boolean containsEOF(Set<Node> set) {
        return set.stream().anyMatch(n -> n instanceof EOF);
    }

    /** Returns true if the set contains an Ident whose name equals `name`. */
    private boolean containsIdent(Set<Node> set, String name) {
        return set.stream()
                  .anyMatch(n -> n instanceof Ident id && id.name().equals(name));
    }

    // =========================================================================
    // FIRST SET TESTS
    // =========================================================================

    @Test
    @DisplayName("FIRST: single literal rule")
    void first_singleLiteral() {
        // rule: start <- 'a'
        g.addRule(g.mkParsingRule("start", g.mkLiteral("a")));
        g.computeFirst();

        Set<Node> first = g.getFirsts().get("start");
        assertNotNull(first);
        assertTrue(containsLiteral(first, "a"), "FIRST(start) should contain 'a'");
        assertEquals(1, first.size());
    }

    @Test
    @DisplayName("FIRST: ordered choice gives union of alternatives")
    void first_orderedChoice() {
        // rule: start <- 'a' / 'b' / 'c'
        g.addRule(g.mkParsingRule("start",
            g.mkOrderedChoice(
                g.mkLiteral("a"),
                g.mkLiteral("b"),
                g.mkLiteral("c")
            )));
        g.computeFirst();

        Set<Node> first = g.getFirsts().get("start");
        assertTrue(containsLiteral(first, "a"), "FIRST(start) should contain 'a'");
        assertTrue(containsLiteral(first, "b"), "FIRST(start) should contain 'b'");
        assertTrue(containsLiteral(first, "c"), "FIRST(start) should contain 'c'");
    }

    @Test
    @DisplayName("FIRST: sequence — only first element when non-nullable")
    void first_sequenceNonNullable() {
        // rule: start <- 'a' 'b'
        g.addRule(g.mkParsingRule("start",
            g.mkSequence(g.mkLiteral("a"), g.mkLiteral("b"))));
        g.computeFirst();

        Set<Node> first = g.getFirsts().get("start");
        assertTrue(containsLiteral(first, "a"), "FIRST(start) should contain 'a'");
        assertFalse(containsLiteral(first, "b"), "FIRST(start) should NOT contain 'b'");
        assertFalse(containsEmpty(first), "FIRST(start) should NOT contain ε");
    }

    @Test
    @DisplayName("FIRST: sequence — propagates through nullable prefix")
    void first_sequenceNullablePrefix() {
        // rules:
        //   start  <- opt 'b'
        //   opt    <- 'a'?
        Node optBody = g.mkTerm(g.mkLiteral("a"), Optional.of(Operator.OPTIONAL));
        g.addRule(g.mkParsingRule("opt", optBody));
        g.addRule(g.mkParsingRule("start",
            g.mkSequence(g.mkIdent("opt"), g.mkLiteral("b"))));
        g.computeFirst();

        Set<Node> first = g.getFirsts().get("start");
        // 'a' comes from opt when it matches
        assertTrue(containsLiteral(first, "a"), "FIRST(start) should contain 'a'");
        // 'b' comes from when opt is empty
        assertTrue(containsLiteral(first, "b"), "FIRST(start) should contain 'b'");
        // start itself is not nullable ('b' is mandatory)
        assertFalse(containsEmpty(first), "FIRST(start) should NOT contain ε");
    }

    @Test
    @DisplayName("FIRST: optional term adds ε to first set")
    void first_optionalTermIsNullable() {
        // rule: start <- 'a'?
        Node optA = g.mkTerm(g.mkLiteral("a"), Optional.of(Operator.OPTIONAL));
        g.addRule(g.mkParsingRule("start", optA));
        g.computeFirst();

        Set<Node> first = g.getFirsts().get("start");
        assertTrue(containsLiteral(first, "a"), "FIRST(start) should contain 'a'");
        assertTrue(containsEmpty(first), "FIRST(start) should contain ε (nullable)");
    }

    @Test
    @DisplayName("FIRST: star term adds ε to first set")
    void first_starTermIsNullable() {
        // rule: start <- 'x'*
        Node starX = g.mkTerm(g.mkLiteral("x"), Optional.of(Operator.STAR));
        g.addRule(g.mkParsingRule("start", starX));
        g.computeFirst();

        Set<Node> first = g.getFirsts().get("start");
        assertTrue(containsLiteral(first, "x"));
        assertTrue(containsEmpty(first), "FIRST(start) should contain ε because * can match zero times");
    }

    @Test
    @DisplayName("FIRST: plus term does NOT add ε")
    void first_plusTermNotNullable() {
        // rule: start <- 'x'+
        Node plusX = g.mkTerm(g.mkLiteral("x"), Optional.of(Operator.PLUS));
        g.addRule(g.mkParsingRule("start", plusX));
        g.computeFirst();

        Set<Node> first = g.getFirsts().get("start");
        assertTrue(containsLiteral(first, "x"));
        assertFalse(containsEmpty(first), "FIRST(start) should NOT contain ε because + requires at least one match");
    }

    @Test
    @DisplayName("FIRST: indirect non-terminal reference")
    void first_indirectNonTerminal() {
        // rules:
        //   start <- inner
        //   inner <- 'z'
        g.addRule(g.mkParsingRule("inner", g.mkLiteral("z")));
        g.addRule(g.mkParsingRule("start", g.mkIdent("inner")));
        g.computeFirst();

        Set<Node> startFirst = g.getFirsts().get("start");
        assertTrue(containsLiteral(startFirst, "z"),
            "FIRST(start) should contain 'z' via inner");
    }

    @Test
    @DisplayName("FIRST: lexical rule Ident is treated as terminal")
    void first_lexicalRuleIdentTreatedAsTerminal() {
        // A LEXING rule; its Ident should appear directly in FIRST of any
        // PARSING rule that references it — not be expanded.
        g.addRule(g.mkLexicalRule("NUM", g.mkLiteral("0")));
        g.addRule(g.mkParsingRule("start", g.mkIdent("NUM")));
        g.computeFirst();

        Set<Node> first = g.getFirsts().get("start");
        assertTrue(containsIdent(first, "NUM"),
            "FIRST(start) should contain Ident(NUM) because NUM is a lexical rule (terminal)");
        assertFalse(containsLiteral(first, "0"),
            "FIRST(start) should NOT expand the lexical rule body");
    }

    // =========================================================================
    // FOLLOW SET TESTS
    // =========================================================================

    @Test
    @DisplayName("FOLLOW: start rule gets EOF")
    void follow_startRuleGetsEOF() {
        // rule: start <- 'a'
        g.addRule(g.mkParsingRule("start", g.mkLiteral("a")));
        g.computeFirst();
        g.computeFollowSets();

        Set<Node> follow = g.getFollows().get("start");
        assertTrue(containsEOF(follow), "FOLLOW(start) must contain EOF");
    }

    @Test
    @DisplayName("FOLLOW: bracketed expr — FOLLOW(expr) contains ']'")
    void follow_bracketedExpr() {
        // rules:
        //   start <- '[' expr ']'
        //   expr  <- 'e'
        g.addRule(g.mkParsingRule("start",
            g.mkSequence(g.mkLiteral("["), g.mkIdent("expr"), g.mkLiteral("]"))));
        g.addRule(g.mkParsingRule("expr", g.mkLiteral("e")));
        g.computeFirst();
        g.computeFollowSets();

        Set<Node> follow = g.getFollows().get("expr");
        assertTrue(containsLiteral(follow, "]"),
            "FOLLOW(expr) should contain ']' because it appears immediately after expr");
    }

    @Test
    @DisplayName("FOLLOW: tail nullable — FOLLOW propagates from parent")
    void follow_tailNullablePropagatesFromParent() {
        // rules:
        //   start <- expr suffix
        //   expr  <- 'e'
        //   suffix <- ';'?      (nullable)
        Node optSemi = g.mkTerm(g.mkLiteral(";"), Optional.of(Operator.OPTIONAL));
        g.addRule(g.mkParsingRule("start",
            g.mkSequence(g.mkIdent("expr"), g.mkIdent("suffix"))));
        g.addRule(g.mkParsingRule("expr", g.mkLiteral("e")));
        g.addRule(g.mkParsingRule("suffix", optSemi));
        g.computeFirst();
        g.computeFollowSets();

        // suffix is nullable, so FOLLOW(expr) ⊇ FIRST(suffix) ∪ FOLLOW(start)
        Set<Node> followExpr = g.getFollows().get("expr");
        // FIRST(suffix) contains ';'
        assertTrue(containsLiteral(followExpr, ";"),
            "FOLLOW(expr) should contain ';' from FIRST(suffix)");
        // FOLLOW(start) contains EOF, which must propagate to expr since suffix is nullable
        assertTrue(containsEOF(followExpr),
            "FOLLOW(expr) should contain EOF because suffix is nullable so FOLLOW(start) propagates");
    }

    @Test
    @DisplayName("FOLLOW: recursive rule — FOLLOW(expr) includes its own recursive context")
    void follow_recursiveRule() {
        // Simulates: start <- '(' expr ')' / 'n'
        //            expr  <- start ('+' start)*
        Node plusStart = g.mkTerm(
            g.mkSequence(g.mkLiteral("+"), g.mkIdent("start")),
            Optional.of(Operator.STAR));
        g.addRule(g.mkParsingRule("start",
            g.mkOrderedChoice(
                g.mkSequence(g.mkLiteral("("), g.mkIdent("expr"), g.mkLiteral(")")),
                g.mkLiteral("n"))));
        g.addRule(g.mkParsingRule("expr",
            g.mkSequence(g.mkIdent("start"), plusStart)));
        g.computeFirst();
        g.computeFollowSets();

        Set<Node> followExpr = g.getFollows().get("expr");
        assertTrue(containsLiteral(followExpr, ")"),
            "FOLLOW(expr) should contain ')' from the bracketed alternative in start");
    }

    @Test
    @DisplayName("FOLLOW: alternative positions — A <- B | C B, FOLLOW(B) includes EOF and FOLLOW(A)")
    void follow_multiplePositions() {
        // rules:
        //   start <- B / 'c' B
        //   B     <- 'b'
        g.addRule(g.mkParsingRule("start",
            g.mkOrderedChoice(
                g.mkIdent("B"),
                g.mkSequence(g.mkLiteral("c"), g.mkIdent("B")))));
        g.addRule(g.mkParsingRule("B", g.mkLiteral("b")));
        g.computeFirst();
        g.computeFollowSets();

        Set<Node> followB = g.getFollows().get("B");
        // B appears at end of both alternatives → FOLLOW(B) ⊇ FOLLOW(start) = {EOF}
        assertTrue(containsEOF(followB),
            "FOLLOW(B) should contain EOF since B ends both alternatives and start is the root");
    }

    @Test
    @DisplayName("FOLLOW: non-terminal at end of sequence inherits parent follow")
    void follow_nonTerminalAtEnd() {
        // rules:
        //   start <- 'begin' body
        //   body  <- 'b'
        g.addRule(g.mkParsingRule("start",
            g.mkSequence(g.mkLiteral("begin"), g.mkIdent("body"))));
        g.addRule(g.mkParsingRule("body", g.mkLiteral("b")));
        g.computeFirst();
        g.computeFollowSets();

        Set<Node> followBody = g.getFollows().get("body");
        // body is last in start → FOLLOW(body) ⊇ FOLLOW(start) = {EOF}
        assertTrue(containsEOF(followBody),
            "FOLLOW(body) should contain EOF because body ends the start rule");
    }

    @Test
    @DisplayName("FOLLOW: star repetition — loop element sees its own first set in follow")
    void follow_starRepetitionSelfFollow() {
        // rules:
        //   start <- item*
        //   item  <- 'x'
        Node starItem = g.mkTerm(g.mkIdent("item"), Optional.of(Operator.STAR));
        g.addRule(g.mkParsingRule("start", starItem));
        g.addRule(g.mkParsingRule("item", g.mkLiteral("x")));
        g.computeFirst();
        g.computeFollowSets();

        Set<Node> followItem = g.getFollows().get("item");
        // Inside *, each iteration of item can be followed by another item → 'x' ∈ FOLLOW(item)
        assertTrue(containsLiteral(followItem, "x"),
            "FOLLOW(item) should contain 'x' because item* loops back on itself");
        // After all iterations, start ends → EOF propagates
        assertTrue(containsEOF(followItem),
            "FOLLOW(item) should contain EOF because star can exit and start is the root");
    }
}
