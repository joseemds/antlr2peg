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
    start_ = V"WS"^0 * V"rulelist" * V"EOF",
	rulelist = (V"rule_")^0 * V"EOF",
  rule_ = V"ID" * tk('=') * (tk('/'))^-1 * V"elements",
  elements = V"alternation",
  alternation = V"concatenation" * (tk('/') * V"concatenation")^0,
  concatenation = (V"repetition")^1,
  repetition = (V"repeat_")^-1 * V"element",
  repeat_ = V"INT" + ((V"INT")^-1 * tk('*') * (V"INT")^-1),
  element = V"group" + V"option" + V"ID" + V"STRING" + V"NumberValue" + V"ProseValue",
  group = tk('(') * V"alternation" * tk(')'),
  option = tk('[') * V"alternation" * tk(']'),
  NumberValue = P('%') * (V"BinaryValue" + V"DecimalValue" + V"HexValue") * V"WS"^0,
  BinaryValue = P('b') * (V"BIT")^1 * ((P('.') * (V"BIT")^1)^1 + (P('-') * (V"BIT")^1))^-1,
  DecimalValue = P('d') * (V"DIGIT")^1 * ((P('.') * (V"DIGIT")^1)^1 + (P('-') * (V"DIGIT")^1))^-1,
  HexValue = P('x') * (V"HEX_DIGIT")^1 * ((P('.') * (V"HEX_DIGIT")^1)^1 + (P('-') * (V"HEX_DIGIT")^1))^-1,
  ProseValue = P('<') * (neg(P('>')))^0 * P('>') * V"WS"^0,
  ID = V"LETTER" * (V"LETTER" + V"DIGIT" + P('-'))^0 * V"WS"^0,
  INT = (regex"[0-9]")^1 * V"WS"^0,
  COMMENT = P(';') * (neg(P('\n') * P('\r')))^0 * (P('\r'))^-1 * P('\n') * V"WS"^0,
  WS = P(' ') + P('\t') + P('\r') + P('\n') * V"WS"^0,
  STRING = (P('%s') + P('%i'))^-1 * P('"') * (neg(P('"')))^0 * P('"') * V"WS"^0,
  LETTER = regex"[a-z]" + regex"[A-Z]",
  BIT = regex"[0-1]",
  DIGIT = regex"[0-9]",
  HEX_DIGIT = regex"[0-9]" + regex"[a-f]" + regex"[A-F]",
	EOF = EOF,
    EMPTY = EMPTY,
    
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
