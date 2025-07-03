local lpeg = require 'lpeglabel'
local re = require 'relabel'
local P,R,V = lpeg.P, lpeg.R, lpeg.V

local loc = lpeg.locale()

local ws = loc.space
local space0 = ws^0
local space1 = ws^1
local letter = loc.alpha
local digit = loc.digit


local IDBegin = letter
local IDRest = letter + digit + P"-"
local ID = IDBegin * IDRest^0 * space0


local function tk (s)
  return P(s) * space0
end


local BIT = lpeg.S"01" * space0
local HEX_DIGIT = R("09", "af", "AF") * space0
local INT = digit^1 * space0

local quote = P('"')
local not_quote = P(1) - quote


local marker = P"%" * lpeg.S"si"

local STRING = marker^-1 * quote * not_quote^0 * quote * space0

local BINARY_VALUE = tk"b" * BIT^1 * ((tk"." * BIT^1)^1 + (tk"-" * BIT^1))^-1
local DECIMAL_VALUE = tk"d" * digit^1 * ((tk"." * digit^1)^1 + (tk"-" * digit^1))^-1
local HEX_VALUE = tk"x" * HEX_DIGIT^0 * ((tk"." * HEX_DIGIT^1)^1 + (tk"-" * HEX_DIGIT^1))^-1
local NUMBER_VALUE = tk"%" * (BINARY_VALUE + DECIMAL_VALUE + HEX_VALUE) * space0

local not_close = 1 - P">"
local PROSE_VALUE =  tk"<" * not_close^0 * tk">"


local EOF = P(-1)


local G = P{
	"rulelist",
	rulelist = V"rule_"^0 * EOF,
	rule_ = ID * tk"=" * tk"/"^-1 * V"elements",
	elements = V"alternation",
	alternation = V"concatenation" * (tk"/" * V"concatenation")^0,
	concatenation = V"repetition"^1,
	repetition = V"repeat_"^-1 * V"element",
	repeat_ = (INT^-1 * tk"*" * INT^-1) + INT,
	element =  (ID * - tk"=") + V"group" + V"option" + STRING + NUMBER_VALUE + PROSE_VALUE,
	group = tk"[" * V"alternation" * tk"]",
	option = tk"(" * V"alternation" * tk")",

}


local M = {}


M.parse = function (input)
    local success, result, lab, errpos = pcall(lpeg.match, G, input)
    -- print("result", result)
    if success then
        if result == #input + 1 then
	    --print(result)
            return true, nil
        else
            local line, col = re.calcline(input, errpos)
	    print("Errpos " .. errpos)
            return false, "LPEG parsing failed at line " .. line .. ", col " .. col .. " with label " .. lab
        end
    else
        return false, "LPEG internal error: " .. tostring(result)
    end
end

return M


