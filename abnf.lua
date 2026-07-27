local lpeg = require "lpeglabel"
local re = require "relabel"
local P, S, V, R, utfR = lpeg.P, lpeg.S, lpeg.V, lpeg.R, lpeg.utfR
  local EMPTY = P''
  local neg = function (pat)
   return P(1) - pat
  end
local regex = function (s)
	return re.compile(s)
end
local tk = function (s)
	return P(s) * V"SKIP_"^0
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
    start_ = V"SKIP_"^0 *  V"rulelist",
	rulelist = (V"rule_")^0 * V"EOF",
  rule_ = V"ID" * tk('=') * (tk('/'))^-1 * V"elements",
  elements = V"alternation",
  alternation = V"concatenation" * (tk('/') * V"concatenation")^0,
  concatenation = V"FixedRepetition_0",
  repetition = (V"repeat_")^-1 * V"element",
  repeat_ = ((V"INT")^-1 * tk('*') * (V"INT")^-1) + V"INT",
  element = V"group" + V"option" + V"ID" + V"STRING" + V"NumberValue" + V"ProseValue",
  group = tk('(') * V"alternation" * tk(')'),
  option = tk('[') * V"alternation" * tk(']'),
  NumberValue = P('%') * (V"BinaryValue" + V"DecimalValue" + V"HexValue") * V"SKIP_"^0,
  BinaryValue = P('b') * (V"BIT")^1 * ((P('.') * (V"BIT")^1)^1 + (P('-') * (V"BIT")^1))^-1,
  DecimalValue = P('d') * (V"DIGIT")^1 * ((P('.') * (V"DIGIT")^1)^1 + (P('-') * (V"DIGIT")^1))^-1,
  HexValue = P('x') * (V"HEX_DIGIT")^1 * ((P('.') * (V"HEX_DIGIT")^1)^1 + (P('-') * (V"HEX_DIGIT")^1))^-1,
  ProseValue = P('<') * (neg(P('>')))^0 * P('>') * V"SKIP_"^0,
  ID = V"LETTER" * (V"LETTER" + V"DIGIT" + P('-'))^0 * V"SKIP_"^0,
  INT = (R('09'))^1 * V"SKIP_"^0,
  COMMENT = P(';') * (neg(P('\n') + P('\r')))^0 * (P('\r'))^-1 * P('\n') * V"SKIP_"^0,
  WS = P(' ') + P('\t') + P('\r') + P('\n') * V"SKIP_"^0,
  STRING = (P('%s') + P('%i'))^-1 * P('"') * (neg(P('"')))^0 * P('"') * V"SKIP_"^0,
  LETTER = R('az') + R('AZ'),
  BIT = R('01'),
  DIGIT = R('09'),
  HEX_DIGIT = R('09') + R('af') + R('AF'),
  FixedRepetition_0 = (V"repetition" * V"FixedRepetition_0") + #(tk(']') + tk('[') + tk('*') + V"NumberValue" + tk(')') + tk('(') + V"STRING" + V"ID" + V"ProseValue" + V"EOF" + V"INT" + tk('/') + V"repetition"),
  SKIP_ = (V"WS" + V"COMMENT")^1,
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
