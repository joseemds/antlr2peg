local lpeg = require 'lpeglabel'
local re = require 'relabel'
local P,R,V = lpeg.P, lpeg.R, lpeg.V

local M = {}
local loc = lpeg.locale()

local ws = loc.space
local space0 = ws^0
local space1 = ws^1
local letter = loc.alpha
local digit = loc.digit
local EOF = P(-1)
local IDBegin = letter + "_"
local IDRest = letter + digit + "_"
local ID = IDBegin * IDRest^0 * space0

local quote = P('"')
local not_quote = 1 - quote
local STRING = quote * ((P"\\" * P(1)) + not_quote)^0 * quote * space0

local decimal = P(".") * loc.digit^1
local NUMBER = P("-")^-1 * (decimal + (loc.digit^1 * (P(".") * digit^0)^-1)) * space0

local function kw (s)
  return P(s) * -IDRest *space0
end

local function tk (s)
  return P(s) * space0
end

local not_close_tag = P(1) - P">"
local not_tag = P(1) - lpeg.S"<>"
local TAG = tk"<" * not_close_tag^0 * tk">"

local HTML_STRING = tk"<" * (TAG + not_tag)^0 * tk">"


local LINE_COMMENT = P"//" * (P(1) - P"\n")
local COMMENT = P"/*" * (P(1)^-1) * P"*/"


local G = {
 "start",
 start = space0 * V"graph" * EOF,
 graph =  (V"strict_kw")^-1 * (V"graph_kw" + V"digraph_kw") * V"id"^-1 * tk'{' * V"stmt_list" * tk'}',
 stmt_list = (V"stmt" * tk';'^-1)^0,
 stmt = (V"id" * tk'=' * V"id") +  V"edge_stmt" +  V"node_stmt" + V"attr_stmt" +  V"subgraph",
 node_stmt = V"node_id" * V"attr_list"^-1,
 edge_stmt = (V"node_id" + V"subgraph") * V"edgeRHS" * V"attr_list"^-1,
 subgraph = (V"subgraph_kw" * V"id"^-1)^-1 * tk"{" * V"stmt_list" * tk"}",
 edgeRHS = (V"edgeop"  * (V"node_id" + V"subgraph"))^1,
 edgeop = tk"->" + tk"--",

 node_id = V"id" * V"port"^-1 ,
 port = tk":" * V"id" * (tk":" * V"id")^-1,

 a_list = (V"id" * (tk"=" * V"id")^-1 * (tk";" + tk",")^-1)^1,
 attr_list = (tk"[" * V"a_list"^-1 * tk"]")^1,
 attr_stmt = (V"graph_kw" + V"node_kw" + V"edge_kw") *  V"attr_list",
 
 
 id = -V"Keyword_alt" * ID + STRING + NUMBER + HTML_STRING,
 
 Keyword = kw"strict" + kw"graph" + kw"digraph" + kw"node" + kw"edge" + kw"subgraph",
 Keyword_alt = (P"strict" + P"graph" + P"digraph" + P"node" + P"edge" + P"subgraph") * -IDRest,
 
 -- Tokens
 strict_kw = kw"strict",
 graph_kw = kw"graph",
 digraph_kw = kw"digraph",
 node_kw = kw"node",
 edge_kw = kw"edge",
 subgraph_kw = kw"subgraph",
}


G = P(G)


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
