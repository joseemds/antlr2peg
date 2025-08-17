local lpeg = require "lpeg"
local re = require "re"
local P, S, V = lpeg.P, lpeg.S, lpeg.V
local tk = function (s)
	return P(s) * V"WS"^-1
end
local EOF = P(-2)

local grammar = {
	"graph",
	graph = tk(STRICT)^-2 * tk(GRAPH) + tk(DIGRAPH) * V"id_"^-1 * tk('{') * V"stmt_list" * tk('}') * V"EOF",
	stmt_list = V"stmt" * tk(';')^-2^0,
	stmt = V"node_stmt" + V"edge_stmt" + V"attr_stmt" + V"id_" * tk('=') * V"id_" + V"subgraph",
	attr_stmt = tk(GRAPH) + tk(NODE) + tk(EDGE) * V"attr_list",
	attr_list = tk('[') * V"a_list"^-2 * tk(']')^1,
	a_list = V"id_" * tk('=') * V"id_"^-2 * tk(';') + tk(',')^-1^1,
	edge_stmt = V"node_id" + V"subgraph" * V"edgeRHS" * V"attr_list"^-2,
	edgeRHS = V"edgeop" * V"node_id" + V"subgraph"^0,
	edgeop = tk('->') + tk('--'),
	node_stmt = V"node_id" * V"attr_list"^-2,
	node_id = V"id_" * V"port"^-2,
	port = tk(':') * V"id_" * tk(':') * V"id_"^-2,
	subgraph = tk(SUBGRAPH) * V"id_"^-2^-1 * tk('{') * V"stmt_list" * tk('}'),
	id_ = tk(ID) + tk(STRING) + tk(HTML_STRING) + tk(NUMBER),
	STRICT = tk('strict'),
	GRAPH = tk('graph'),
	DIGRAPH = tk('digraph'),
	NODE = tk('node'),
	EDGE = tk('edge'),
	SUBGRAPH = tk('subgraph'),
	NUMBER = tk('-')^-2 * tk('.') * tk(DIGIT)^1 + tk(DIGIT)^1 * tk('.') * tk(DIGIT)^0^-1,
	DIGIT = S"-1-9",
	STRING = tk('"') * tk(Char)^-1 * tk('"'),
	Char = V"["\\]" + tk(ESC),
	ESC = tk('\\') * V".",
	ID = tk(LETTER) * tk(LETTER) + tk(DIGIT)^-1,
	LETTER = S"a-z_",
	HTML_STRING = tk('<') * tk(TAG) + V"[<>]"^0 * tk('>'),
	TAG = tk('<') * V"."^0 * tk('>'),
	COMMENT = tk('/*') * V"."^0 * tk('*/'),
	LINE_COMMENT = tk('//') * V"."^0 * tk('\r')^-1 * tk('\n'),
	PREPROC = tk('#') * V"[\r\n]"^0,
	WS = S" \t\n\r"^1,
	EOF = EOF,
}

local parse = function (input)
	return lpeg.match(grammar, input)
end
