local dot = require("dot")

local input_filename = arg[1]
local input_file

if input_filename then
    local file, err = io.open(input_filename, "r")
    if file then
        input_file = file
        -- print("Reading from file:", input_filename)
    else
        io.stderr:write("Error: Could not open file '" .. input_filename .. "': " .. err .. "\n")
        print("Falling back to reading from stdin.")
        input_file = io.stdin
    end
else
    print("No file specified. Reading from stdin.")
    input_file = io.stdin
end

-- Now, 'input_file' will be either the opened file handle or io.stdin
-- You can use it to read data using the same methods (e.g., input_file:read(), input_file:lines())

local content = input_file:read("*all")
--print(content)


-- print(assert(dot.parse(content)))
assert(dot.parse(content))
