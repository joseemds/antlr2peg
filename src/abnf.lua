local lpeg = require 'lpeglabel'
local re = require 'relabel'
local P,R,V = lpeg.P, lpeg.R, lpeg.V

local loc = lpeg.locale()

local ws = loc.space
local space0 = ws^0
local space1 = ws^1
local letter = loc.alpha
local digit = loc.digit


local IDBegin = letter * IDRest
local IDRest = letter + digit + P"-"
local ID = IDBegin * IDRest*^0 * space0


local function tk (s)
  return P(s) * space0
end


local BIT = lpeg.S"01" * space0
local HEX_DIGIT = (R"09" + R"af" + R"AF") * space0
local INT = digit^1 * space0

local quote = P('"')
local not_quote = 1 - quote

local STRING = quote * not_quote * quote * space0


local EOF = P(-1)


local G = P{
	"rulelist",
	rulelist = V"rule_" * EOF,
	rule = ID * tk"=" * tk"/"^-1 V"elements",
	elements = V"alternation",
	alternation = V"concatenation" * (tk"/" * V"concatenation")^0,
	concatenation = V"repetition"^1,
	repetition = V"repeat_"^-1 * V"element",
	repeat_ = (INT^-1 * tk"*" * INT^-1) + INT,
	element = V"id"
}
