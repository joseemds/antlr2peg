local lpeg = require"lpeglabel"
local re = require"relabel"
local dot = require"grammars.dot"
local abnf = require"grammars.abnf"


local parse = function (input, lang)
		local l = require("grammars." .. lang) -- muita gambiarra
    local success, result, lab, errpos = pcall(lpeg.match, l.grammar, input)
    if success then
        if result == #input + 1 then
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

return {
	dot = dot,
	abnf = abnf,
	parse = parse
}
