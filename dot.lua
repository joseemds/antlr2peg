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

G.strict = P("strict")
G.graph = P("graph")
G.digraph = P("digraph")
G.node = P("node")
G.edge = P("edge")
G.subgraph = P("subgraph")

G.id = ID + STRING

G.stmt = (G.id * space0 * P('=') * space0 * G.id)

G.stmt_list = (G.stmt * P(';')^-1 * space0)^0


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
