package exception;

public class SemanticActionNotAllowedException extends RuntimeException {
  public SemanticActionNotAllowedException(String message) {
    super(message);
  }
}
