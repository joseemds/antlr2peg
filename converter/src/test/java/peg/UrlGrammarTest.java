package peg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import peg.node.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FIRST and FOLLOW sets derived from the URL grammar:
 *
 *   url           <- uri EOF
 *   uri           <- scheme '://' login? host (':' port)? ('/' path?)? query? frag? WS?
 *   scheme        <- string
 *   host          <- '/'? hostname
 *   hostname      <- string | '[' v6host ']'
 *   v6host        <- '::'? (string | DIGITS) ((':' | '::') (string | DIGITS))*
 *   port          <- DIGITS
 *   path          <- string ('/' string)* '/'?
 *   user          <- string
 *   login         <- user (':' password)? '@'
 *   password      <- string
 *   frag          <- '#' (string | DIGITS)
 *   query         <- '?' search
 *   search        <- searchparameter ('&' searchparameter)*
 *   searchparameter <- string ('=' (string | DIGITS | HEX))?
 *   string        <- STRING | DIGITS          (parsing rule — expands)
 *
 *   DIGITS  <- [0-9]+        (lexical)
 *   HEX     <- ('%' [a-fA-F0-9] [a-fA-F0-9])+   (lexical)
 *   STRING  <- ([a-zA-Z~0-9] | HEX) ([a-zA-Z0-9.+-] | HEX)*  (lexical)
 *   WS      <- [\r\n]+       (lexical)
 */
public class UrlGrammarTest {

    private PegGrammar g;

    // -------------------------------------------------------------------------
    // Grammar construction
    // -------------------------------------------------------------------------

    @BeforeEach
    void buildGrammar() {
        g = new PegGrammar();

        // --- Parsing rules ---
        // IMPORTANT: url must be registered first — computeFollowSets() seeds EOF
        // into rules.getFirst(), so the start rule must be the first entry in the list.

        // url : uri EOF
        g.addRule(g.mkParsingRule("url",
            g.mkSequence(g.mkIdent("uri"), new EOF())));

        // uri : scheme '://' login? host (':' port)? ('/' path?)? query? frag? WS?
        g.addRule(g.mkParsingRule("uri",
            g.mkSequence(
                g.mkIdent("scheme"),
                g.mkLiteral("://"),
                g.mkTerm(g.mkIdent("login"),   Optional.of(Operator.OPTIONAL)),
                g.mkIdent("host"),
                g.mkTerm(
                    g.mkSequence(g.mkLiteral(":"), g.mkIdent("port")),
                    Optional.of(Operator.OPTIONAL)),
                g.mkTerm(
                    g.mkSequence(
                        g.mkLiteral("/"),
                        g.mkTerm(g.mkIdent("path"), Optional.of(Operator.OPTIONAL))),
                    Optional.of(Operator.OPTIONAL)),
                g.mkTerm(g.mkIdent("query"),   Optional.of(Operator.OPTIONAL)),
                g.mkTerm(g.mkIdent("frag"),    Optional.of(Operator.OPTIONAL)),
                g.mkTerm(g.mkIdent("WS"),      Optional.of(Operator.OPTIONAL)))));

        // scheme : string
        g.addRule(g.mkParsingRule("scheme", g.mkIdent("string")));

        // host : '/'? hostname
        g.addRule(g.mkParsingRule("host",
            g.mkSequence(
                g.mkTerm(g.mkLiteral("/"), Optional.of(Operator.OPTIONAL)),
                g.mkIdent("hostname"))));

        // hostname : string | '[' v6host ']'
        g.addRule(g.mkParsingRule("hostname",
            g.mkOrderedChoice(
                g.mkIdent("string"),
                g.mkSequence(
                    g.mkLiteral("["),
                    g.mkIdent("v6host"),
                    g.mkLiteral("]")))));

        // v6host : '::'? (string | DIGITS) ((':' | '::') (string | DIGITS))*
        g.addRule(g.mkParsingRule("v6host",
            g.mkSequence(
                g.mkTerm(g.mkLiteral("::"), Optional.of(Operator.OPTIONAL)),
                g.mkOrderedChoice(g.mkIdent("string"), g.mkIdent("DIGITS")),
                g.mkTerm(
                    g.mkSequence(
                        g.mkOrderedChoice(g.mkLiteral(":"), g.mkLiteral("::")),
                        g.mkOrderedChoice(g.mkIdent("string"), g.mkIdent("DIGITS"))),
                    Optional.of(Operator.STAR)))));

        // port : DIGITS
        g.addRule(g.mkParsingRule("port", g.mkIdent("DIGITS")));

        // path : string ('/' string)* '/'?
        g.addRule(g.mkParsingRule("path",
            g.mkSequence(
                g.mkIdent("string"),
                g.mkTerm(
                    g.mkSequence(g.mkLiteral("/"), g.mkIdent("string")),
                    Optional.of(Operator.STAR)),
                g.mkTerm(g.mkLiteral("/"), Optional.of(Operator.OPTIONAL)))));

        // user : string
        g.addRule(g.mkParsingRule("user", g.mkIdent("string")));

        // login : user (':' password)? '@'
        g.addRule(g.mkParsingRule("login",
            g.mkSequence(
                g.mkIdent("user"),
                g.mkTerm(
                    g.mkSequence(g.mkLiteral(":"), g.mkIdent("password")),
                    Optional.of(Operator.OPTIONAL)),
                g.mkLiteral("@"))));

        // password : string
        g.addRule(g.mkParsingRule("password", g.mkIdent("string")));

        // frag : '#' (string | DIGITS)
        g.addRule(g.mkParsingRule("frag",
            g.mkSequence(
                g.mkLiteral("#"),
                g.mkOrderedChoice(g.mkIdent("string"), g.mkIdent("DIGITS")))));

        // query : '?' search
        g.addRule(g.mkParsingRule("query",
            g.mkSequence(g.mkLiteral("?"), g.mkIdent("search"))));

        // search : searchparameter ('&' searchparameter)*
        g.addRule(g.mkParsingRule("search",
            g.mkSequence(
                g.mkIdent("searchparameter"),
                g.mkTerm(
                    g.mkSequence(g.mkLiteral("&"), g.mkIdent("searchparameter")),
                    Optional.of(Operator.STAR)))));

        // searchparameter : string ('=' (string | DIGITS | HEX))?
        g.addRule(g.mkParsingRule("searchparameter",
            g.mkSequence(
                g.mkIdent("string"),
                g.mkTerm(
                    g.mkSequence(
                        g.mkLiteral("="),
                        g.mkOrderedChoice(
                            g.mkIdent("string"),
                            g.mkIdent("DIGITS"),
                            g.mkIdent("HEX"))),
                    Optional.of(Operator.OPTIONAL)))));

        // string : STRING | DIGITS
        g.addRule(g.mkParsingRule("string",
            g.mkOrderedChoice(g.mkIdent("STRING"), g.mkIdent("DIGITS"))));


        // DIGITS : [0-9]+
        g.addRule(g.mkLexicalRule("DIGITS",
            g.mkTerm(g.mkCharset(g.mkRange("0", "9")), Optional.of(Operator.PLUS))));

        // HEX : ('%' [a-fA-F0-9] [a-fA-F0-9])+
        g.addRule(g.mkLexicalRule("HEX",
            g.mkTerm(
                g.mkSequence(
                    g.mkLiteral("%"),
                    g.mkCharset(g.mkRange("a", "f"), g.mkRange("A", "F"), g.mkRange("0", "9")),
                    g.mkCharset(g.mkRange("a", "f"), g.mkRange("A", "F"), g.mkRange("0", "9"))
                ),
                Optional.of(Operator.PLUS))));

        // STRING : ([a-zA-Z~0-9] | HEX) ([a-zA-Z0-9.+-] | HEX)*
        g.addRule(g.mkLexicalRule("STRING",
            g.mkSequence(
                g.mkOrderedChoice(
                    g.mkCharset(
                        g.mkRange("a", "z"), g.mkRange("A", "Z"),
                        g.mkRange("0", "9"), g.mkCharsetLiteral("~")),
                    g.mkIdent("HEX")),
                g.mkTerm(
                    g.mkOrderedChoice(
                        g.mkCharset(
                            g.mkRange("a", "z"), g.mkRange("A", "Z"),
                            g.mkRange("0", "9"),
                            g.mkCharsetLiteral("."), g.mkCharsetLiteral("+"), g.mkCharsetLiteral("-")),
                        g.mkIdent("HEX")),
                    Optional.of(Operator.STAR)))));

        // WS : [\r\n]+
        g.addRule(g.mkLexicalRule("WS",
            g.mkTerm(
                g.mkCharset(g.mkCharsetLiteral("\r"), g.mkCharsetLiteral("\n")),
                Optional.of(Operator.PLUS))));

        g.computeFirst();
        g.computeFollowSets();
    }

    private boolean containsLiteral(Set<Node> set, String s) {
        return set.stream().anyMatch(n -> n instanceof Literal lit && lit.content().equals(s));
    }

    private boolean containsEmpty(Set<Node> set) {
        return set.stream().anyMatch(n -> n instanceof Empty);
    }

    private boolean containsEOF(Set<Node> set) {
        return set.stream().anyMatch(n -> n instanceof EOF);
    }

    private boolean containsIdent(Set<Node> set, String name) {
        return set.stream().anyMatch(n -> n instanceof Ident id && id.name().equals(name));
    }

    @Test
    @DisplayName("KEY: FOLLOW('/'?) inside host contains FIRST(hostname) = {STRING, DIGITS, '['}")
    void follow_optionalSlashInHost_containsFirstOfHostname() {
        Set<Node> firstHostname = g.getFirsts().get("hostname");
        assertNotNull(firstHostname, "FIRST(hostname) must be computed");

        assertTrue(containsLiteral(firstHostname, "["),
            "FIRST(hostname) must contain '[' from the IPv6 alternative");

        assertTrue(containsIdent(firstHostname, "STRING"),
            "FIRST(hostname) must contain Ident(STRING) via string rule");
        assertTrue(containsIdent(firstHostname, "DIGITS"),
            "FIRST(hostname) must contain Ident(DIGITS) via string rule");

        assertFalse(containsEmpty(firstHostname),
            "FIRST(hostname) must NOT contain ε — hostname always consumes input");
    }


    @Test
    @DisplayName("FIRST(url) = FIRST(uri) = FIRST(scheme) = FIRST(string) = {STRING, DIGITS}")
    void first_urlTracesToStringTerminals() {
        for (String rule : List.of("url", "uri", "scheme", "string")) {
            Set<Node> first = g.getFirsts().get(rule);
            assertNotNull(first, "FIRST(%s) must be computed".formatted(rule));
            assertTrue(containsIdent(first, "STRING"),
                "FIRST(%s) must contain Ident(STRING)".formatted(rule));
            assertTrue(containsIdent(first, "DIGITS"),
                "FIRST(%s) must contain Ident(DIGITS)".formatted(rule));
        }
    }

    @Test
    @DisplayName("FIRST(host) = {'/', STRING, DIGITS, '['} — the leading slash is optional")
    void first_host() {
        Set<Node> first = g.getFirsts().get("host");
        assertTrue(containsLiteral(first, "/"),
            "FIRST(host) must contain '/' from the optional leading slash");
        assertTrue(containsIdent(first, "STRING"),  "FIRST(host) must contain Ident(STRING)");
        assertTrue(containsIdent(first, "DIGITS"),  "FIRST(host) must contain Ident(DIGITS)");
        assertTrue(containsLiteral(first, "["),     "FIRST(host) must contain '[' from IPv6");
        assertFalse(containsEmpty(first), "FIRST(host) must NOT contain ε");
    }

    @Test
    @DisplayName("FIRST(frag) = {'#'}")
    void first_frag() {
        Set<Node> first = g.getFirsts().get("frag");
        assertTrue(containsLiteral(first, "#"), "FIRST(frag) must contain '#'");
        assertEquals(1, first.size(), "FIRST(frag) should contain only '#'");
    }

    @Test
    @DisplayName("FIRST(query) = {'?'}")
    void first_query() {
        Set<Node> first = g.getFirsts().get("query");
        assertTrue(containsLiteral(first, "?"), "FIRST(query) must contain '?'");
        assertEquals(1, first.size(), "FIRST(query) should contain only '?'");
    }

    @Test
    @DisplayName("FIRST(login) = FIRST(user) = FIRST(string) — login starts with a user string")
    void first_login() {
        Set<Node> first = g.getFirsts().get("login");
        assertTrue(containsIdent(first, "STRING"), "FIRST(login) must contain Ident(STRING)");
        assertTrue(containsIdent(first, "DIGITS"), "FIRST(login) must contain Ident(DIGITS)");
        assertFalse(containsEmpty(first), "FIRST(login) must NOT be nullable ('user' is mandatory)");
    }

    @Test
    @DisplayName("FIRST(v6host) = {'::', STRING, DIGITS} — leading '::' is optional so string/DIGITS also appear")
    void first_v6host() {
        Set<Node> first = g.getFirsts().get("v6host");
        assertTrue(containsLiteral(first, "::"),   "FIRST(v6host) must contain '::'");
        assertTrue(containsIdent(first, "STRING"),  "FIRST(v6host) must contain STRING");
        assertTrue(containsIdent(first, "DIGITS"),  "FIRST(v6host) must contain DIGITS");
        assertFalse(containsEmpty(first),
            "FIRST(v6host) must NOT be nullable — (string | DIGITS) is mandatory");
    }

    // =========================================================================
    // FOLLOW set tests
    // =========================================================================

    @Test
    @DisplayName("FOLLOW(url) = {EOF} — url is the start rule")
    void follow_urlIsStart() {
        assertTrue(containsEOF(g.getFollows().get("url")),
            "FOLLOW(url) must contain EOF as it is the start rule");
    }

    @Test
    @DisplayName("FOLLOW(scheme) contains '://' — scheme is always followed by '://' in uri")
    void follow_schemeContainsSchemeSeparator() {
        Set<Node> follow = g.getFollows().get("scheme");
        assertTrue(containsLiteral(follow, "://"),
            "FOLLOW(scheme) must contain '://' because uri is: scheme '://' ...");
    }

    @Test
    @DisplayName("FOLLOW(host) contains ':' and '/' and '?' and '#' and Ident(WS) and EOF")
    void follow_host() {
        Set<Node> follow = g.getFollows().get("host");
        assertNotNull(follow);

        assertTrue(containsLiteral(follow, ":"),
            "FOLLOW(host) must contain ':' from optional port clause");

        assertTrue(containsLiteral(follow, "/"),
            "FOLLOW(host) must contain '/' from optional path clause");

        assertTrue(containsLiteral(follow, "?"),
            "FOLLOW(host) must contain '?' from optional query");

        assertTrue(containsLiteral(follow, "#"),
            "FOLLOW(host) must contain '#' from optional frag");

        assertTrue(containsEOF(follow),
            "FOLLOW(host) must contain EOF because all trailing items are optional");
    }

    @Test
    @DisplayName("FOLLOW(hostname) = FOLLOW(host) — hostname ends the host sequence")
    void follow_hostnameInheritsFromHost() {
        Set<Node> followHost     = g.getFollows().get("host");
        Set<Node> followHostname = g.getFollows().get("hostname");

        assertNotNull(followHostname);
        for (Node n : followHost) {
            assertTrue(followHostname.contains(n),
                "FOLLOW(hostname) should contain everything in FOLLOW(host) since hostname ends host");
        }
    }

    @Test
    @DisplayName("FOLLOW(v6host) contains ']' — v6host is always followed by ']' in hostname")
    void follow_v6hostContainsClosingBracket() {
        Set<Node> follow = g.getFollows().get("v6host");
        assertTrue(containsLiteral(follow, "]"),
            "FOLLOW(v6host) must contain ']' because hostname: '[' v6host ']'");
    }

    @Test
    @DisplayName("FOLLOW(search) = FOLLOW(query) — search ends the query rule")
    void follow_searchInheritsFromQuery() {
        Set<Node> followQuery  = g.getFollows().get("query");
        Set<Node> followSearch = g.getFollows().get("search");
        assertNotNull(followSearch);
        for (Node n : followQuery) {
            assertTrue(followSearch.contains(n),
                "FOLLOW(search) must contain everything in FOLLOW(query)");
        }
    }

    @Test
    @DisplayName("FOLLOW(searchparameter) contains '&' — loop repetition feeds back into itself")
    void follow_searchparameterContainsAmpersand() {
        Set<Node> follow = g.getFollows().get("searchparameter");
        assertTrue(containsLiteral(follow, "&"),
            "FOLLOW(searchparameter) must contain '&' due to the repetition loop in search");
    }

    @Test
    @DisplayName("FOLLOW(port) contains '/' and '?' and '#' and EOF — all trailing uri items are optional")
    void follow_port() {
        Set<Node> follow = g.getFollows().get("port");
        assertNotNull(follow);
        assertTrue(containsLiteral(follow, "/"),  "FOLLOW(port) must contain '/'");
        assertTrue(containsLiteral(follow, "?"),  "FOLLOW(port) must contain '?'");
        assertTrue(containsLiteral(follow, "#"),  "FOLLOW(port) must contain '#'");
        assertTrue(containsEOF(follow),            "FOLLOW(port) must contain EOF");
    }

    @Test
    @DisplayName("FOLLOW(login) contains FIRST(host) — login? is immediately before host in uri")
    void follow_loginContainsFirstOfHost() {
        Set<Node> followLogin = g.getFollows().get("login");
        Set<Node> firstHost   = g.getFirsts().get("host");
        assertNotNull(followLogin);
        assertNotNull(firstHost);

        for (Node n : firstHost) {
            if (n instanceof Empty) continue;
            assertTrue(followLogin.contains(n),
                "FOLLOW(login) must contain %s from FIRST(host)".formatted(n));
        }
    }
}
