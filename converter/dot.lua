local lpeg = require "lpeglabel"
local re = require "relabel"
local P, S, V = lpeg.P, lpeg.S, lpeg.V
  local EMPTY = P''
  local neg = function (pat)
   return P(1) - pat
  end
local regex = function (s)
	return re.compile(s)
end
local tk = function (s)
	return P(s) * V"WS"^0
end
local EOF = P(-1)

  local ci =  function (s)
    local pat = P""
    for i = 1, #s do
      local ch = s:sub(i, i)
      local lower = ch:lower()
      local upper = ch:upper()
      if lower == upper then
       pat = pat * P(ch)
      else
       pat = pat * S(lower .. upper)
      end
    end
    return pat
  end

local grammar = {
	"start_",
    start_ = V"WS"^0 * V"graph" * V"EOF",
	graph = (V"STRICT")^-1 * (V"GRAPH" + V"DIGRAPH") * (V"id_")^-1 * tk(ci('{')) * V"stmt_list" * tk(ci('}')) * V"EOF",
  stmt_list = (V"stmt" * (tk(ci(';')))^-1)^0,
  stmt = V"edge_stmt" + V"attr_stmt" + V"node_stmt" + (V"id_" * tk(ci('=')) * V"id_") + V"subgraph",
  attr_stmt = (V"GRAPH" + V"NODE" + V"EDGE") * V"attr_list",
  attr_list = (tk(ci('[')) * (V"a_list")^-1 * tk(ci(']')))^1,
  a_list = (V"id_" * (tk(ci('=')) * V"id_")^-1 * (tk(ci(';')) + tk(ci(',')))^-1)^1,
  edge_stmt = (V"node_id" + V"subgraph") * V"edgeRHS" * (V"attr_list")^-1,
  edgeRHS = (V"edgeop" * (V"node_id" + V"subgraph"))^1,
  edgeop = tk(ci('->')) + tk(ci('--')),
  node_stmt = V"node_id" * (V"attr_list")^-1,
  node_id = V"id_" * (V"port")^-1,
  port = tk(ci(':')) * V"id_" * (tk(ci(':')) * V"id_")^-1,
  subgraph = (V"SUBGRAPH" * (V"id_")^-1)^-1 * tk(ci('{')) * V"stmt_list" * tk(ci('}')),
  id_ = V"ID" + V"STRING" + V"HTML_STRING" + V"NUMBER",
  STRICT = P(ci('strict')) * V"WS"^0,
  GRAPH = P(ci('graph')) * V"WS"^0,
  DIGRAPH = P(ci('digraph')) * V"WS"^0,
  NODE = P(ci('node')) * V"WS"^0,
  EDGE = P(ci('edge')) * V"WS"^0,
  SUBGRAPH = P(ci('subgraph')) * V"WS"^0,
  NUMBER = (P(ci('-')))^-1 * ((P(ci('.')) * (V"DIGIT")^1) + ((V"DIGIT")^1 * (P(ci('.')) * (V"DIGIT")^0)^-1)) * V"WS"^0,
  DIGIT = regex"[0-9]",
  STRING = P(ci('"')) * (V"Char")^0 * P(ci('"')) * V"WS"^0,
  Char = V"ESC" + neg(regex"[\"\\]"),
  ESC = P(ci('\\')) * P(1),
  ID = V"LETTER" * (V"LETTER" + V"DIGIT")^0 * V"WS"^0,
  LETTER = regex"[a-z_]",
  HTML_STRING = P(ci('<')) * (V"TAG" + neg(regex"[<>]"))^0 * P(ci('>')) * V"WS"^0,
  TAG = P(ci('<')) * neg(P(ci('>')))^0 * P(ci('>')),
  COMMENT = P(ci('/*')) * neg(P(ci('*/')))^0 * P(ci('*/')) * V"WS"^0,
  LINE_COMMENT = P(ci('//')) * neg((P(ci('\r')))^-1)^0 * (P(ci('\r')))^-1 * P(ci('\n')) * V"WS"^0,
  PREPROC = P(ci('#')) * (neg(regex"[\r\n]"))^0 * V"WS"^0,
  WS = (regex"[ \t\n\r]")^1 * V"WS"^0,
	EOF = EOF,
    EMPTY = EMPTY,
    KEYWORDS = P'strict' + P'graph' + P'digraph' + P'node' + P'edge' + P'subgraph',
}

local parse = function (input)
	local result, label, errpos = lpeg.match(grammar, input)
	if result then
		print("Parsed: ", result)
	else
      local line, col = re.calcline(input, errpos)
		print("LPEG Parsing failed at " .. line .. ":" .. col)
		os.exit(1)
	end
	return lpeg.match(grammar, input)
end

local input = io.read("*a")
print(parse(input))
