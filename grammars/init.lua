local lpeg = require"lpeglabel"
local re = require"relabel"
local dot = require"dot"
local abnf = require"abnf"

local parse = function (input)
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

return {
	dot = dot,
	abnf = abnf,
	parse = parse
}
