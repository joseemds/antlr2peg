package exception;

public class WrongStartRuleException extends RuntimeException {
  public WrongStartRuleException(String message) {
    super(message);
  }
}
