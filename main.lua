local dot = require("dot")

local input_filename = arg[1]
local input_file

if input_filename then
    -- Attempt to open the specified file
    local file, err = io.open(input_filename, "r")
    if file then
        input_file = file
        print("Reading from file:", input_filename)
    else
        -- If file opening fails, print an error and fall back to stdin (or exit)
        io.stderr:write("Error: Could not open file '" .. input_filename .. "': " .. err .. "\n")
        print("Falling back to reading from stdin.")
        input_file = io.stdin -- Use standard input
    end
else
    -- No file specified, read from stdin
    print("No file specified. Reading from stdin.")
    input_file = io.stdin -- Use standard input
end

-- Now, 'input_file' will be either the opened file handle or io.stdin
-- You can use it to read data using the same methods (e.g., input_file:read(), input_file:lines())

print("\n--- Content being read ---")
for line in input_file:lines() do
    print(line)
end
print("--- End of content ---\n")

