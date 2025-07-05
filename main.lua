local dot = require("grammars.dot")
local abnf = require("grammars.abnf")
local grammars = require"grammars"

local input_filename = arg[1]
local language = arg[2] or "dot"
local input_file

if input_filename then
    local file, err = io.open(input_filename, "r")
    if file then
        input_file = file
        print("Reading from file:", input_filename)
    else
        io.stderr:write("Error: Could not open file '" .. input_filename .. "': " .. err .. "\n")
        print("Falling back to reading from stdin.")
        input_file = io.stdin
    end
else
    print("No file specified. Reading from stdin.")
    input_file = io.stdin
end

local content = input_file:read("*all")
print(assert(grammars.parse(content, language)))
