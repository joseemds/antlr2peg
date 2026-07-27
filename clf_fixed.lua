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
    start_ = V"SKIP_"^0 *  V"log",
	log = V"FixedRepetition_0" * (V"line")^-1 * V"EOF",
  line = V"host" * V"logname" * V"username" * V"datetimetz" * V"request" * V"statuscode" * V"bytes" * (V"referer" * V"useragent")^-1,
  host = V"STRING" + V"IP",
  logname = V"STRING",
  username = V"STRING",
  datetimetz = tk('[') * V"DATE" * tk(':') * V"TIME" * V"TZ" * tk(']'),
  DATE = (R('09'))^1 * P('/') * V"STRING" * P('/') * (R('09'))^1 * V"SKIP_"^0,
  TIME = (R('09'))^1 * P(':') * (R('09'))^1 * P(':') * (R('09'))^1 * V"SKIP_"^0,
  TZ = P('-') * (R('09'))^1 * V"SKIP_"^0,
  referer = V"LITERAL",
  request = V"LITERAL",
  useragent = V"LITERAL",
  statuscode = V"STRING",
  bytes = V"STRING",
  LITERAL = P('"') * (neg(P('"')))^0 * P('"') * V"SKIP_"^0,
  IP = (R('09'))^1 * P('.') * (R('09'))^1 * P('.') * (R('09'))^1 * P('.') * (R('09'))^1 * V"SKIP_"^0,
  STRING = ((R('az') + R('AZ') + R('09') + P('(') + P(')') + P(';') + P('.') + P('_') + P('-')))^1 * V"SKIP_"^0,
  EOL = (P('\r'))^-1 * P('\n') * V"SKIP_"^0,
  WS = (P(' ') + P('\t')) * V"SKIP_"^0,
  FixedRepetition_0 = (((V"line")^-1 * V"EOL") * V"FixedRepetition_0") + #(V"IP" + V"STRING" + V"EOF" + ((V"line")^-1 * V"EOL")),
  SKIP_ = (V"WS")^1,
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
