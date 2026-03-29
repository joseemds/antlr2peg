package utils;

public class Utils {
  public static String sanitizeString(String s) {
    if (s == null) return null;

    String inner = s.substring(1, s.length() - 1);
    String escaped = inner.replace("\\", "\\\\").replace("'", "\\'");
    return "'" + escaped + "'";
  }
}
