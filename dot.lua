local lpeg = require 'lpeg'
local P,R = lpeg.P, lpeg.R

local G = {}
local M = {}

local ws = lpeg.S(" \t\r\n")
local space0 = ws^0
local space1 = ws^1
local letter = R("az", "AZ")
local digit = R("09")
local EOF = P(-1)

G.strict = P("strict")
G.graph = P("graph")
G.digraph = P("digraph")
G.node = P("node")
G.edge = P("edge")
G.subgraph = P("subgraph")


G.graph_ = space0 * (G.strict * space1)^-1 * (G.graph + G.digraph) * space1 * P('{') * space0 * P('}')
G.graph = space0 * G.graph_ * space0 * -1

M.parse = function (input)
    local success, result = pcall(lpeg.match, G.graph, input)
    if success then
        if result then
						print(result)
            return true, nil
        else
            return false, "LPEG parsing failed"
        end
    else
        return false, "LPEG internal error: " .. tostring(result)
    end
end

return M
