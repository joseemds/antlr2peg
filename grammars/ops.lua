local lpeg = require "lpeg"
local re = require "re"
local P, S, V = lpeg.P, lpeg.S, lpeg.V
  local EMPTY = P''
  local neg = function (pat)
   return P(1) - pat
  end
local regex = function (s)
	return re.compile(s)
end
local rule = function (s)
	return V(s) * V"WS"^0
end
local tk = function (s)
	return P(s) * V"WS"^0
end
local EOF = P(-1)

local grammar = {
	"graph",
	graph = (V"STRICT")^-1 * (V"GRAPH" + V"DIGRAPH") * (V"id_")^-1 * tk('{') * V"stmt_list" * tk('}') * V"EOF",
  stmt_list = (V"stmt" * (tk(';'))^-1)^0,
  stmt = V"node_stmt" + V"edge_stmt" + V"attr_stmt" + (V"id_" * tk('=') * V"id_") + V"subgraph",
  attr_stmt = (V"GRAPH" + V"NODE" + V"EDGE") * V"attr_list",
  attr_list = (tk('[') * (V"a_list")^-1 * tk(']'))^1,
  a_list = (V"id_" * (tk('=') * V"id_")^-1 * (tk(';') + tk(','))^-1)^1,
  edge_stmt = (V"node_id" + V"subgraph") * V"edgeRHS" * (V"attr_list")^-1,
  edgeRHS = (V"edgeop" * (V"node_id" + V"subgraph"))^1,
  edgeop = tk('->') + tk('--'),
  node_stmt = V"node_id" * (V"attr_list")^-1,
  node_id = V"id_" * (V"port")^-1,
  port = tk(':') * V"id_" * (tk(':') * V"id_")^-1,
  subgraph = (V"SUBGRAPH" * (V"id_")^-1)^-1 * tk('{') * V"stmt_list" * tk('}'),
  id_ = V"ID" + V"STRING" + V"HTML_STRING" + V"NUMBER",
  STRICT = tk('strict'),
  GRAPH = tk('graph'),
  DIGRAPH = tk('digraph'),
  NODE = tk('node'),
  EDGE = tk('edge'),
  SUBGRAPH = tk('subgraph'),
  NUMBER = (tk('-'))^-1 * ((tk('.') * (V"DIGIT")^1) + ((V"DIGIT")^1 * (tk('.') * (V"DIGIT")^0)^-1)),
  DIGIT = regex"[0-9]",
  STRING = tk('"') * (V"Char")^0 * tk('"'),
  Char = V"ESC" + neg(regex"[\"\\]"),
  ESC = tk('\\') * P(1),
  ID = V"LETTER" * (V"LETTER" + V"DIGIT")^0,
  LETTER = regex"[a-z_]",
  HTML_STRING = tk('<') * (V"TAG" + neg(regex"[<>]"))^0 * tk('>'),
  TAG = tk('<') * (P(1))^0 * tk('>'),
  COMMENT = tk('/*') * (P(1))^0 * tk('*/'),
  LINE_COMMENT = tk('//') * (P(1))^0 * (tk('\r'))^-1 * tk('\n'),
  PREPROC = tk('#') * (neg(regex"[\r\n]"))^0,
  WS = (regex"[ \t\n\r]")^1,
	EOF = EOF,
    EMPTY = EMPTY,
}

local parse = function (input)
	return lpeg.match(grammar, input)
end

local input = io.read("*a")
print(parse(input))
