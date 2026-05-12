package utils;

public class StatsTracker {
  private int choiceAmbiguities = 0;
  private int repetitionsTransformed = 0;
  private int uniquePathSwaps = 0;
  private int prefixesReordered = 0;
  private int emptyRulesMoved = 0;

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
}
