package charset;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CharSetParserTest {

    @Test
    void parseCharSet_singleRange() {
        List<CharacterSet> nodes = CharSetParser.parseCharSet("a-z");

        assertEquals(1, nodes.size());
        assertRange("a", "z", nodes.get(0));
    }

    @Test
    void parseCharSet_singleLiteral() {
        List<CharacterSet> nodes = CharSetParser.parseCharSet("_");

        assertEquals(1, nodes.size());
        assertLiteral("_", nodes.get(0));
    }

    @Test
    void parseCharSet_mixedRangeAndLiteral() {
        List<CharacterSet> nodes = CharSetParser.parseCharSet("a-z_");

        assertEquals(2, nodes.size());
        assertRange("a", "z", nodes.get(0));
        assertLiteral("_", nodes.get(1));
    }

    @Test
    void parseCharSet_multipleRanges() {
        List<CharacterSet> nodes = CharSetParser.parseCharSet("a-z0-9");

        assertEquals(2, nodes.size());
        assertRange("a", "z", nodes.get(0));
        assertRange("0", "9", nodes.get(1));
    }

    @Test
    void parseCharSet_multipleLiterals() {
        List<CharacterSet> nodes = CharSetParser.parseCharSet("abc");

        assertEquals(3, nodes.size());
        assertLiteral("a", nodes.get(0));
        assertLiteral("b", nodes.get(1));
        assertLiteral("c", nodes.get(2));
    }

    @Test
    void parseCharSet_unicodeEscapeAsRangeStart() {
        List<CharacterSet> nodes = CharSetParser.parseCharSet("\\u0041-Z");

        assertEquals(1, nodes.size());
        assertRange("\\u0041", "Z", nodes.get(0));
    }

    @Test
    void parseCharSet_unicodeEscapeAsRangeEnd() {
        List<CharacterSet> nodes = CharSetParser.parseCharSet("A-\\u005A");

        assertEquals(1, nodes.size());
        assertRange("A", "\\u005A", nodes.get(0));
    }

    @Test
    void parseCharSet_unicodeEscapeLiteral() {
        List<CharacterSet> nodes = CharSetParser.parseCharSet("\\u005F");

        assertEquals(1, nodes.size());
        assertLiteral("\\u005F", nodes.get(0));
    }

    @Test
    void parseCharSet_otherEscapeSequence() {
        List<CharacterSet> nodes = CharSetParser.parseCharSet("\\n");

        assertEquals(1, nodes.size());
        assertLiteral("\\n", nodes.get(0));
    }

    @Test
    void parseCharSet_emptyInput() {
        List<CharacterSet> nodes = CharSetParser.parseCharSet("");
        assertTrue(nodes.isEmpty());
    }


    @Test
    void parseCharacterRange_quotedTokens() {
        CharacterSet node = CharSetParser.parseCharacterRange("'a'..'z'");
        assertRange("a", "z", node);
    }

    @Test
    void parseCharacterRange_unquotedTokens() {
        CharacterSet node = CharSetParser.parseCharacterRange("a..z");
        assertRange("a", "z", node);
    }

    @Test
    void parseCharacterRange_withWhitespace() {
        CharacterSet node = CharSetParser.parseCharacterRange("'a' .. 'z'");
        assertRange("a", "z", node);
    }

    @Test
    void parseCharacterRange_digits() {
        CharacterSet node = CharSetParser.parseCharacterRange("'0'..'9'");
        assertRange("0", "9", node);
    }

    private void assertRange(String from, String to, CharacterSet node) {
        assertInstanceOf(RangeNode.class, node);
        RangeNode range = (RangeNode) node;
        assertEquals(from, range.from(), "range.from");
        assertEquals(to,   range.to(),   "range.to");
    }

    private void assertLiteral(String ch, CharacterSet node) {
        assertInstanceOf(LiteralNode.class, node);
        assertEquals(ch, ((LiteralNode) node).ch());
    }
}
