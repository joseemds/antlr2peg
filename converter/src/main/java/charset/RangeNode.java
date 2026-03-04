package charset;

// "a" .. "z"
public record RangeNode(String from, String to) implements CharacterSet {}
