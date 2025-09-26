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
	"program",
	program = V"expr" * V"EOF",
  expr = (V"term" * tk('+') * V"expr") + (V"term" * tk('-') * V"expr") + V"term" + EMPTY,
  term = (V"factor" * tk('*') * V"term") + (V"factor" * tk('/') * V"term") + V"factor",
  factor = V"INT" + (tk('-') * V"factor") + (tk('(') * V"expr" * tk(')')),
  INT = (regex"[0-9]")^1,
  WS = (regex"[ \t\r\n]")^1,
	EOF = EOF,
    EMPTY = EMPTY,
}

local parse = function (input)
	return lpeg.match(grammar, input)
end

local input = io.read("*a")
print(parse(input))
