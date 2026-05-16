package utils;

public class Utils {
  public static String sanitizeString(String s) {
    if (s == null) return null;
    s = sanitizeUnicode(s);

    if (s.startsWith("'") && s.endsWith("'") && s.length() >= 2) {
      s = s.substring(1, s.length() - 1);
    }

    s = s.replace("\\'", "'");
    s = s.replace("'", "\\'");

    return "'" + s + "'";
  }

  public static String sanitizeUnicode(String s) {
    if (s.startsWith("\\u")) {
      return "\\u{" + s.substring(2) + "}";
    }
    return s;
  }
}
