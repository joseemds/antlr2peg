package utils;

public class Utils {
  public static String sanitizeString(String s) {
    if (s == null) return null;
		s = sanitizeUnicode(s);
    String inner = s.substring(1, s.length() - 1);
    String escaped = inner.replace("\\'", "'").replace("'", "\\'");
    return "'" + escaped + "'";
  }

		public static String sanitizeUnicode(String s) {
			if (s.startsWith("\\u")) {
				return "\\u{" + s.substring(2) + "}";
			}
			return s;
		}
}
