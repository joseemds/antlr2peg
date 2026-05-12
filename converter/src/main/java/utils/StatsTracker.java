package utils;

public class StatsTracker {
  private int choiceAmbiguities = 0;
  private int repetitionsTransformed = 0;
  private int uniquePathSwaps = 0;
  private int rulesBefore = 0;
  private int rulesAfter = 0;
  private int prefixesReordered = 0;
  private int emptyRulesMoved = 0;
  private int choiceOfLiteralsReordered = 0;

  public void bumpChoiceAmbiguities() {
    this.choiceAmbiguities++;
  }

  public void bumpRepetitionsTransformed() {
    this.repetitionsTransformed++;
  }

  public void bumpUniquePathSwaps() {
    this.uniquePathSwaps++;
  }

  public void bumpPrefixReorder() {
    this.prefixesReordered++;
  }

  public void bumpEmptyRule() {
    this.emptyRulesMoved++;
  }

  public void bumpChoiceOfLiteralsReordered() {
    this.choiceOfLiteralsReordered++;
  }

  public int getChoiceAmbiguities() {
    return choiceAmbiguities;
  }

  public int getRepetitionsTransformed() {
    return repetitionsTransformed;
  }

  public int getUniquePathSwaps() {
    return uniquePathSwaps;
  }

  public int getPrefixReordered() {
    return prefixesReordered;
  }

  public int getEmptyRulesMoved() {
    return emptyRulesMoved;
  }

  public int getLiteralsReorder() {
    return this.choiceOfLiteralsReordered;
  }

  public int getRulesBefore() {
    return this.rulesBefore;
  }

  public int getRulesAFter() {
    return this.rulesAfter;
  }

  public void setRulesBefore(int rulesBefore) {

    this.rulesBefore = rulesBefore;
  }

  public void setRulesAfter(int rulesAfter) {
    this.rulesAfter = rulesAfter;
  }
}
