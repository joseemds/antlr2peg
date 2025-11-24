package peg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import peg.node.*;

public class FirstSetTests {
  PegGrammar grammar;

  @BeforeEach
  void setup() {
    grammar = new PegGrammar();
  }

  @Test
  void testEmpty() {
    Empty e = new Empty();
    assertEquals(grammar.firstOf(e), List.of(e));
  }

  @Test
  void testLiteral() {
    Literal lit = new Literal("a");
    assertEquals(grammar.firstOf(lit), List.of(lit));
  }

  @Test
  void testCharset() {
    Charset cs = new Charset("a");
    assertEquals(grammar.firstOf(cs), List.of(cs));
  }

  @Test
  void testWildcard() {
    Wildcard w = new Wildcard();
    assertEquals(grammar.firstOf(w), List.of(w));
  }

  @Test
  void testTermWithoutOperator() {
    Charset cs = new Charset("a");
    Term t = new Term(cs, Optional.empty());
    assertEquals(grammar.firstOf(t), List.of(cs));
  }

  @Test
  void testTermWithStar() {
    Charset cs = new Charset("a");
    Term t = new Term(cs, Optional.of(Operator.STAR));
    List<Node> result = grammar.firstOf(t);
    assertEquals(2, result.size());
    assertTrue(result.contains(cs));
    assertTrue(result.stream().anyMatch(n -> n instanceof Empty));
  }

  @Test
  void testTermWithPlus() {
    Charset cs = new Charset("a");
    Term t = new Term(cs, Optional.of(Operator.PLUS));
    List<Node> result = grammar.firstOf(t);
    assertEquals(List.of(cs), result);
  }

  @Test
  void testTermWithOptional() {
    Charset cs = new Charset("a");
    Term t = new Term(cs, Optional.of(Operator.OPTIONAL));
    List<Node> result = grammar.firstOf(t);
    assertEquals(2, result.size());
    assertTrue(result.contains(cs));
    assertTrue(result.stream().anyMatch(n -> n instanceof Empty));
  }

  @Test
  void testSequenceStarFollowedByLiteral() {
    Charset a = new Charset("a");
    Term aStar = new Term(a, Optional.of(Operator.STAR));
    Literal b = new Literal("b");
    Sequence seq = new Sequence(List.of(aStar, b));
    
    List<Node> result = grammar.firstOf(seq);
    assertTrue(result.contains(a));
    assertTrue(result.contains(b));
    assertTrue(result.stream().noneMatch(n -> n instanceof Empty));
  }

  @Test
  void testSequenceOptionalFollowedByLiteral() {
    Charset a = new Charset("a");
    Term aOptional = new Term(a, Optional.of(Operator.OPTIONAL));
    Literal b = new Literal("b");
    Sequence seq = new Sequence(List.of(aOptional, b));
    
    List<Node> result = grammar.firstOf(seq);
    assertTrue(result.contains(a));
    assertTrue(result.contains(b));
    assertTrue(result.stream().noneMatch(n -> n instanceof Empty));
  }

  @Test
  void testSequenceAllOptional() {
    Charset a = new Charset("a");
    Term aOptional = new Term(a, Optional.of(Operator.OPTIONAL));
    Charset b = new Charset("b");
    Term bStar = new Term(b, Optional.of(Operator.STAR));
    Sequence seq = new Sequence(List.of(aOptional, bStar));
    
    List<Node> result = grammar.firstOf(seq);
    assertTrue(result.contains(a));
    assertTrue(result.contains(b));
    assertTrue(result.stream().anyMatch(n -> n instanceof Empty));
  }

  @Test
  void testSequenceWithNot() {
    Charset a = new Charset("a");
    Not notA = new Not(a);
    Literal b = new Literal("b");
    Sequence seq = new Sequence(List.of(notA, b));
    
    List<Node> result = grammar.firstOf(seq);
    assertTrue(result.contains(b));
  }

  @Test
  void testOrderedChoiceWithTerms() {
    Charset a = new Charset("a");
    Term aStar = new Term(a, Optional.of(Operator.STAR));
    Literal b = new Literal("b");
    OrderedChoice oc = new OrderedChoice(List.of(aStar, b));
    
    List<Node> result = grammar.firstOf(oc);
    assertTrue(result.contains(a));
    assertTrue(result.contains(b));
    assertTrue(result.stream().anyMatch(n -> n instanceof Empty));
  }

  @Test
  void testNestedTermWithStar() {
    Charset a = new Charset("a");
    Literal b = new Literal("b");
    Sequence innerSeq = new Sequence(List.of(a, b));
    Term t = new Term(innerSeq, Optional.of(Operator.STAR));
    
    List<Node> result = grammar.firstOf(t);
    assertTrue(result.contains(a));
    assertTrue(result.stream().anyMatch(n -> n instanceof Empty));
  }

  @Test
  void testComplexSequence() {
    Charset a = new Charset("a");
    Term aStar = new Term(a, Optional.of(Operator.STAR));
    Charset b = new Charset("b");
    Term bStar = new Term(b, Optional.of(Operator.STAR));
    Literal c = new Literal("c");
    Sequence seq = new Sequence(List.of(aStar, bStar, c));
    
    List<Node> result = grammar.firstOf(seq);
    assertTrue(result.contains(a));
    assertTrue(result.contains(b));
    assertTrue(result.contains(c));
    assertTrue(result.stream().noneMatch(n -> n instanceof Empty));
  }

  @Test
  void testEmptyInSequence() {
    Empty e = new Empty();
    Literal a = new Literal("a");
    Sequence seq = new Sequence(List.of(e, a));
    
    List<Node> result = grammar.firstOf(seq);
    assertTrue(result.contains(a));
    assertTrue(result.stream().noneMatch(n -> n instanceof Empty));
  }
}
