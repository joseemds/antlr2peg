package charset;

import java.util.*;

public class CharSetParser {

  private static boolean isUnicodeEscape(String token) {
    return token.startsWith("\\u");
  }

  private static int tokenEnd(String input, int i) {
    if (input.charAt(i) == '\\') {
      return (input.charAt(i + 1) == 'u') ? i + 6 : i + 2;
    }
    int cp = input.codePointAt(i);
    return i + Character.charCount(cp);
  }

  public static List<CharacterSet> parseCharSet(String input) {
    if (input.startsWith("[") && input.endsWith("]"))
      input = input.substring(1, input.length() - 1);
    List<CharacterSet> nodes = new ArrayList<>();
    int i = 0;
    while (i < input.length()) {
      int fromEnd = tokenEnd(input, i);
      String from = input.substring(i, fromEnd);
      i = fromEnd;
      if (i < input.length()
          && input.charAt(i) == '-'
          && i + 1 < input.length()
          && input.charAt(i + 1) != ']') {
        i++;
        int toEnd = tokenEnd(input, i);
        String to = input.substring(i, toEnd);
        i = toEnd;
        if (isUnicodeEscape(from) || isUnicodeEscape(to)) {
          nodes.add(new UTF8RangeNode(from, to));
        } else {
          nodes.add(new RangeNode(from, to));
        }
      } else {
        nodes.add(new LiteralNode(from));
      }
    }
    return nodes;
  }

  private static String stripQuotes(String s) {
    s = s.trim();
    if (s.startsWith("'") && s.endsWith("'") && s.length() >= 2)
      return s.substring(1, s.length() - 1);
    return s;
  }

  public static CharacterSet parseCharacterRange(String input) {
    String[] parts = input.split("\\.\\.", 2);
    if (parts.length != 2)
      throw new IllegalArgumentException("Expected format: x..y, got: " + input);
    String from = stripQuotes(parts[0]);
    String to = stripQuotes(parts[1]);
    if (isUnicodeEscape(from) || isUnicodeEscape(to)) {
      return new UTF8RangeNode(from, to);
    }
    return new RangeNode(from, to);
  }
}
