local lpeg = require "lpeglabel"
local re = require "relabel"
local P, S, V, R, utfR = lpeg.P, lpeg.S, lpeg.V, lpeg.R, lpeg.utfR
  local EMPTY = P''
  local neg = function (pat)
   return P(1) - pat
  end
local regex = function (s)
	return re.compile(s)
end
local tk = function (s)
	return P(s) * V"SKIP_"^0
end
local EOF = P(-1)

  local ci =  function (s)
    local pat = P""
    for i = 1, #s do
      local ch = s:sub(i, i)
      local lower = ch:lower()
      local upper = ch:upper()
      if lower == upper then
       pat = pat * P(ch)
      else
       pat = pat * S(lower .. upper)
      end
    end
    return pat
  end

local grammar = {
	"start_",
    start_ = V"SKIP_"^0 *  V"document",
	document = V"file_identifier" * (V"using_import")^0 * (V"namespace_")^-1 * (V"document_content")^0 * V"EOF",
  file_identifier = V"FILE_ID" * tk(';'),
  using_import = tk('using') * (V"NAME" * tk('='))^-1 * tk('import') * V"TEXT" * (tk('.') * V"NAME")^-1 * tk(';'),
  namespace_ = tk('$') * V"NAME" * tk('.namespace') * tk('(') * V"TEXT" * tk(')') * tk(';'),
  document_content = V"interface_def" + V"annotation_def" + V"const_def" + V"enum_def" + V"struct_def" + V"function_def",
  struct_def = tk('struct') * V"type_" * (V"annotation_reference")^-1 * tk('{') * (V"struct_content")^0 * tk('}'),
  struct_content = V"enum_def" + V"named_union_def" + V"unnamed_union_def" + V"interface_def" + V"annotation_def" + V"group_def" + V"const_def" + V"field_def" + V"struct_def" + V"inner_using",
  interface_def = tk('interface') * V"type_" * (tk('extends') * tk('(') * V"type_" * tk(')'))^-1 * tk('{') * (V"interface_content")^0 * tk('}'),
  interface_content = V"enum_def" + V"named_union_def" + V"unnamed_union_def" + V"interface_def" + V"field_def" + V"struct_def" + V"function_def",
  field_def = V"NAME" * V"LOCATOR" * tk(':') * V"type_" * (tk('=') * V"const_value")^-1 * tk(';'),
  type_ = V"NAME" * V"FixedRepetition_0" * (tk('.') * V"type_")^-1,
  inner_type = tk('(') * V"type_" * (V"inner_type")^-1 * (tk(',') * V"type_" * (V"inner_type")^-1)^0 * tk(')'),
  enum_def = tk('enum') * V"NAME" * (V"annotation_reference")^-1 * tk('{') * (V"enum_content")^0 * tk('}'),
  annotation_reference = tk('$') * V"type_" * (tk('.ann'))^-1 * tk('(') * V"TEXT" * tk(')'),
  enum_content = V"NAME" * V"LOCATOR" * (V"annotation_reference")^-1 * tk(';'),
  named_union_def = V"NAME" * (V"LOCATOR")^-1 * tk(':union') * tk('{') * (V"union_content")^0 * tk('}'),
  unnamed_union_def = tk('union') * tk('{') * (V"union_content")^0 * tk('}'),
  union_content = V"group_def" + V"unnamed_union_def" + V"named_union_def" + V"field_def",
  group_def = V"NAME" * tk(':group') * tk('{') * (V"group_content")^0 * tk('}'),
  group_content = V"unnamed_union_def" + V"named_union_def" + V"field_def",
  function_def = V"NAME" * (V"LOCATOR")^-1 * (V"generic_type_parameters")^-1 * (V"function_parameters" + V"type_") * (tk('->') * (V"function_parameters" + V"type_"))^-1 * tk(';'),
  generic_type_parameters = tk('[') * V"NAME" * (tk(',') * V"NAME")^0 * tk(']'),
  function_parameters = tk('(') * (V"NAME" * tk(':') * V"type_" * (tk('=') * V"const_value")^-1 * (tk(',') * V"NAME" * tk(':') * V"type_" * (tk('=') * V"const_value")^-1)^0)^-1 * tk(')'),
  annotation_def = tk('annotation') * V"type_" * (V"annotation_parameters")^-1 * tk(':') * V"type_" * tk(';'),
  annotation_parameters = tk('(') * tk('struct') * tk(')'),
  const_def = tk('const') * V"NAME" * tk(':') * V"type_" * tk('=') * V"const_value" * tk(';'),
  const_value = ((tk('-'))^-1 * (tk('.'))^-1 * V"NAME" * (tk('.') * V"NAME")^-1) + V"HEXADECIMAL" + V"FLOAT" + V"INTEGER" + V"TEXT" + V"BOOLEAN"  + V"VOID" + V"literal_list" + V"literal_union" + V"literal_bytes",
  literal_union = tk('(') * V"NAME" * tk('=') * V"union_mapping" * (tk(',') * V"NAME" * tk('=') * V"union_mapping")^0 * tk(')'),
  literal_list = tk('[') * V"const_value" * (tk(',') * V"const_value")^0 * tk(']'),
  literal_bytes = tk('0x') * V"TEXT",
  union_mapping = (tk('(') * V"NAME" * tk('=') * V"const_value" * tk(')')) + V"const_value",
  inner_using = tk('using') * V"NAME" * (tk('.') * V"NAME")^0 * (tk('=') * V"type_")^-1 * tk(';'),
  DIGIT = R('09'),
  HEX_DIGIT = V"DIGIT" + R('AF') + R('af'),
  LOCATOR = P('@') * (V"DIGIT")^1 * (P('!'))^-1 * V"SKIP_"^0,
  TEXT = P('"') * (neg(P('"'))) * P('"') * V"SKIP_"^0,
  INTEGER = (P('-'))^-1 * (V"DIGIT")^1 * V"SKIP_"^0,
  FLOAT = (P('-'))^-1 * (V"DIGIT")^1 * (P('.') * (V"DIGIT")^1)^-1 * (P('e') * (P('-'))^-1 * (V"DIGIT")^1)^-1 * V"SKIP_"^0,
  HEXADECIMAL = (P('-'))^-1 * P('0x') * (V"HEX_DIGIT")^1 * V"SKIP_"^0,
  FILE_ID = P('@') * V"HEXADECIMAL" * V"SKIP_"^0,
  BOOLEAN = P('true') + P('false') * V"SKIP_"^0,
  VOID = P('void') * -((R('az') + R('09') + P('_'))) * V"SKIP_"^0,
  NAME = (R('az') + R('AZ')) * ((R('az') + R('AZ') + R('09')))^0 * V"SKIP_"^0,
  COMMENT = P('#') * (neg(P('\n')))^0 * V"SKIP_"^0,
  WHITESPACE = (P(' ') + P('\t') + P('\r') + P('\n')) * V"SKIP_"^0,
  FixedRepetition_0 = (V"inner_type" * #(tk('.') + tk('extends') + tk('=') + tk(',') + tk('{') + tk(';') + tk(':') + tk(')') + tk('(') + tk('$') + tk('->') + tk('.ann'))) + EMPTY,
  SKIP_ = (V"WHITESPACE" + V"COMMENT")^1,
	EOF = EOF,
    EMPTY = EMPTY,
    
}

local parse = function (input)
	local result, label, errpos = lpeg.match(grammar, input)
	if result then
		print("Parsed: ", result)
	else
      local line, col = re.calcline(input, errpos)
		print("LPEG Parsing failed at " .. line .. ":" .. col)
		os.exit(1)
	end
	return lpeg.match(grammar, input)
end

local input = io.read("*a")
print(parse(input))
