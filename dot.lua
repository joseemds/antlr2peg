local lpeg = require 'lpeglabel'
local P,R,V = lpeg.P, lpeg.R, lpeg.V

--local G = {}
local M = {}
local loc = lpeg.locale()

local ws = loc.space
local space0 = ws^0
local space1 = ws^1
local letter = loc.alpha
local digit = loc.digit
local EOF = P(-1)
local IDBegin = letter
local IDRest = letter + digit
local ID = IDBegin * IDRest^0 * space0

local quote = P('"')
local not_quote = 1 - quote
--local STRING = quote * lpeg.C(not_quote^0) * quote * space0
local STRING = quote * not_quote^0 * quote * space0

local decimal = P(".") * loc.digit^1

local NUMBER = P("-")^-1 * (decimal + (loc.digit^1 * (P(".") * digit^0)^-1)) * space0

local function kw (s)
  return P(s) * -IDRest *space0
end

local function tk (s)
  return P(s) * space0
end


local G = {
 "start",
 start = space0 * V"graph_" * EOF,
 graph_ =  (V"strict")^-1 * (V"graph" + V"digraph") * tk'{' * V"stmt_list" * tk'}',
 stmt_list = (V"stmt" * tk';'^-1)^0,
 stmt = V"id" * tk'=' * V"id",
 a_list = (V"id" * (tk"=" * V"id"^-1) * (tk";" + tk",")^-1)^-1,
 attr_list = (tk"[" * V"a_list"^-1 * tk"]")^1,
 attr_stmt = (V"graph" + V"node" + V"edge") *  V"attr_list",
 
 id = ID + STRING + NUMBER,
 
 -- Tokens
 strict = kw"strict",
 graph = kw"graph",
 digraph = kw"digraph",
 node = kw"node",
 edge = kw"edge",
 subgraph = kw"subgraph",
}


G = P(G)


M.parse = function (input)
    local success, result, lab, errpos = pcall(lpeg.match, G, input)
    print("result", result)
    if success then
        if result == #input + 1 then
	    --print(result)
            return true, nil
        else
	    print("Errpos " .. errpos)
            return false, "LPEG parsing failed at label and pos " .. errpos .. " " .. lab
        end
    else
        return false, "LPEG internal error: " .. tostring(result)
    end
end

return M
