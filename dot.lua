local lpeg = require 'lpeglabel'
local P,R = lpeg.P, lpeg.R

local G = {}
local M = {}
local loc = lpeg.locale()

local ws = loc.space
local space0 = ws^0
local space1 = ws^1
local letter = loc.alpha
local digit = loc.digit
local EOF = P(-1)
local ID = letter * (letter + digit)^0

local quote = P('"')
local not_quote = 1 - quote
local STRING = quote * lpeg.C(not_quote^0) * quote

local decimal = P(".") * loc.digit^1

local NUMBER = P("-")^-1 * (decimal + (loc.digit^1 * (P(".") * digit^0)^-1))

G.strict = P("strict")
G.graph = P("graph")
G.digraph = P("digraph")
G.node = P("node")
G.edge = P("edge")
G.subgraph = P("subgraph")

G.id = ID + STRING + NUMBER

G.stmt = (G.id * space0 * P('=') * space0 * G.id)

G.stmt_list = (G.stmt * P(';')^-1 * space0)^0


G.a_list = (G.id * (space1 * P"=" * space1 * G.id^-1) *space1 * (P";" + P",")^-1)^-1

G.attr_list = (P"[" * space0 * G.a_list^-1 * space0 * P"]")^1

G.attr_stmt = (G.graph + G.node + G.edge) * space0  * G.attr_list

G.graph_ = space0 * (G.strict * space1)^-1 * (G.graph + G.digraph) * space1 * P('{') * space0 * G.stmt_list * space0 * P('}')
G.graph = space0 * G.graph_ * space0 * EOF

M.parse = function (input)
    local success, result, lab, errpos = pcall(lpeg.match, G.graph, input)
    if success then
        if result then
						print(result)
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
