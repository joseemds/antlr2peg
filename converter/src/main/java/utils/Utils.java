package utils;

import exception.*;

public class Utils {
  public static String sanitizeString(String s) {
    if (s == null) return null;

    if (s.startsWith("'") && s.endsWith("'") && s.length() >= 2) {
      s = s.substring(1, s.length() - 1);
    }

    if (s.startsWith("\\u")) return sanitizeUnicode(s);

    s = s.replace("\\'", "'");
    s = s.replace("'", "\\'");

    return "'" + s + "'";
  }

  public static String sanitizeUnicode(String s) {
    s = s.substring(2);
    if (s.length() > 4 || !s.matches("\\d+")) {
      throw new SemanticActionNotAllowedException("Unsupported u with > 4 chars or UTf8 marks");
    }

    return "\\u{" + s.substring(2) + "}";
  }

  public static String sanitizeChar(String ch) {
    if (ch == null) return null;
    if (ch.equals("'")) return "'\\\''";
    if (ch.equals("\\-")) ch = "-";
    if (ch.startsWith("\\u")) ch = "\\u{" + ch.substring(2) + "}";
    return "'" + ch + "'";
  }
}
