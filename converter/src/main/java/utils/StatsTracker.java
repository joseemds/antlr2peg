package utils;

public class StatsTracker {
  private int choiceAmbiguities = 0;
  private int repetitionsTransformed = 0;
  private int uniquePathSwaps = 0;

  public void bumpChoiceAmbiguites() {
    this.choiceAmbiguities++;
  }

  public void bumpRepetitionsTransformed() {
    this.repetitionsTransformed++;
  }

  public void bumpUniquePathSwaps() {
    this.uniquePathSwaps++;
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
}
