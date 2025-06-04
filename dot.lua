local lpeg = require 'lpeglabel'
local P,R = lpeg.P, lpeg.R

local G = {}
local M = {}

G.strict = P("strict")
G.graph = P("graph")
G.digraph = P("digraph")
G.node = P("node")
G.edge = P("edge")
G.subgraph = P("subgraph")

local ws = lpeg.S(" \t\r\n")
local letter = R("az", "AZ")
local digit = R("09")

-- local ID = lpeg.C(letter + (letter+digit)^0)
-- local number = P"-"^-1 * (P"." * digit^1 + digit^1 * (P"." * digit^0)^-1)


G.graph = ws^0 * G.strict * (G.graph + G.digraph) * P('{') * P('}')

M.parse = function (input)
    local success, result = pcall(lpeg.match, G.graph, input)
    if success then
        if result then
            return result, nil
        else
            local err_pos = #input_string + 1
            for i = #input_string, 1, -1 do
                if not lpeg.match(G.dot_grammar, input_string:sub(1, i)) then
                    err_pos = i
                    break
                end
            end
            return nil, "Parsing failed near position " .. err_pos
        end
    else
        return nil, "LPEG internal error: " .. tostring(result)
    end
end

return {
    parse = parse,
}
