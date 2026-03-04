package peg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import charset.CharSetParser;
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

  // @Test
  // void testCharset() {
  //   Charset cs = new Charset("a");
  //   assertEquals(grammar.firstOf(cs), List.of(cs));
  // }

  @Test
  void testWildcard() {
    Wildcard w = new Wildcard();
    assertEquals(grammar.firstOf(w), List.of(w));
  }

  @Test
  void testTermWithoutOperator() {
    Literal a = new Literal("a");
    Term t = new Term(a, Optional.empty());
    assertEquals(grammar.firstOf(t), List.of(a));
  }

  @Test
  void testTermWithStar() {
    Literal a = new Literal("a");
    Term t = new Term(a, Optional.of(Operator.STAR));
    List<Node> result = grammar.firstOf(t);
    assertEquals(2, result.size());
    assertTrue(result.contains(a));
    assertTrue(result.stream().anyMatch(n -> n instanceof Empty));
  }

  @Test
  void testTermWithPlus() {
    Literal a = new Literal("a");
    Term t = new Term(a, Optional.of(Operator.PLUS));
    List<Node> result = grammar.firstOf(t);
    assertEquals(List.of(a), result);
  }

  @Test
  void testTermWithOptional() {
    Literal a = new Literal("a");
    Term t = new Term(a, Optional.of(Operator.OPTIONAL));
    List<Node> result = grammar.firstOf(t);
    assertEquals(2, result.size());
    assertTrue(result.contains(a));
    assertTrue(result.stream().anyMatch(n -> n instanceof Empty));
  }

  @Test
  void testSequenceStarFollowedByLiteral() {
     Literal a = new Literal("a");
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
     Literal a = new Literal("a");
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
     Literal a = new Literal("a");
    Term aOptional = new Term(a, Optional.of(Operator.OPTIONAL));
    Literal b  = new Literal("b");
    Term bStar = new Term(b, Optional.of(Operator.STAR));
    Sequence seq = new Sequence(List.of(aOptional, bStar));
    
    List<Node> result = grammar.firstOf(seq);
    assertTrue(result.contains(a));
    assertTrue(result.contains(b));
    assertTrue(result.stream().anyMatch(n -> n instanceof Empty));
  }

  @Test
  void testSequenceWithNot() {
     Literal a = new Literal("a");
    Not notA = new Not(a);
    Literal b = new Literal("b");
    Sequence seq = new Sequence(List.of(notA, b));
    
    List<Node> result = grammar.firstOf(seq);
    assertTrue(result.contains(b));
  }

  @Test
  void testOrderedChoiceWithTerms() {
     Literal a = new Literal("a");
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
     Literal a = new Literal("a");
    Literal b = new Literal("b");
    Sequence innerSeq = new Sequence(List.of(a, b));
    Term t = new Term(innerSeq, Optional.of(Operator.STAR));
    
    List<Node> result = grammar.firstOf(t);
    assertTrue(result.contains(a));
    assertTrue(result.stream().anyMatch(n -> n instanceof Empty));
  }

  @Test
  void testComplexSequence() {
     Literal a = new Literal("a");
    Term aStar = new Term(a, Optional.of(Operator.STAR));
    Literal b = new Literal("b");
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
