local lpeg = require "lpeglabel"
local re = require "relabel"
local P, S, V, R, utfR = lpeg.P, lpeg.S, lpeg.V, lpeg.R, lpeg.utfR
  local EMPTY = P''
local _idRest = R"az" + R"AZ" + R"09"
  local neg = function (pat)
   return P(1) - pat
  end
local regex = function (s)
	return re.compile(s)
end
local tk = function (s)
	return P(s) 
end

local keyword = function (s)
	return P(s) * -_idRest

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
    start_ =  V"pcre",
	pcre = (V"alternation")^-1 * V"EOF",
  alternation = V"expr" * (tk('|') * (V"expr")^-1)^0,
  expr = V"FixedRepetition_0",
  element = V"atom" * (V"quantifier")^-1,
  atom = V"character_class" + V"posix_character_class" + V"option_setting" + V"backtracking_control" + V"callout" + V"capture" + V"atomic_group" + V"lookaround" + V"backreference" + V"subroutine_reference" + V"conditional_pattern" + V"comment" + V"character" + V"character_type" + V"letter" + V"digit" + V"anchor" + V"match_point_reset" + V"quoting" + V"other",
  capture = tk('(') * (V"alternation" + (tk('?') * ((tk('<') * V"name" * tk('>') * V"alternation") + (tk('\'') * V"name" * tk('\'') * V"alternation") + (tk('P') * tk('<') * V"name" * tk('>') * V"alternation") + (((V"option_setting_flag")^1 * (tk('-') * (V"option_setting_flag")^1)^-1)^-1 * tk(':') * V"alternation") + (tk('|') * V"alternation")))) * tk(')'),
  atomic_group = tk('(') * tk('?') * tk('>') * V"alternation" * tk(')'),
  lookaround = tk('(') * tk('?') * (tk('=') + tk('!') + (tk('<') * tk('=')) + (tk('<') * tk('!'))) * V"alternation" * tk(')'),
  backreference = (tk('\\') * ((tk('g') * V"digits") + (tk('g') * tk('{') * (tk('-'))^-1 * V"digits" * tk('}')) + (tk('g') * tk('{') * V"name" * tk('}')) + (tk('k') * tk('<') * V"name" * tk('>')) + (tk('k') * tk('\'') * V"name" * tk('\'')) + (tk('k') * tk('{') * V"name" * tk('}')))) + (tk('(') * tk('?') * tk('P') * tk('=') * V"name" * tk(')')),
  subroutine_reference = (tk('(') * tk('?') * (tk('R') + ((tk('+') + tk('-'))^-1 * V"digits") + (tk('&') * V"name") + (tk('P') * tk('>') * V"name")) * tk(')')) + (tk('\\') * tk('g') * ((tk('<') * V"name" * tk('>')) + (tk('\'') * V"name" * tk('\'')) + (tk('<') * (tk('+') + tk('-'))^-1 * V"digits" * tk('>')) + (tk('\'') * (tk('+') + tk('-'))^-1 * V"digits" * tk('\'')))),
  conditional_pattern = tk('(') * tk('?') * ((tk('(') * (((tk('+') + tk('-'))^-1 * V"digits") + (tk('<') * V"name" * tk('>')) + (tk('\'') * V"name" * tk('\'')) + (tk('R') * (V"digits")^-1) + (tk('R') * tk('&') * V"name") + V"name") * tk(')')) + V"callout" + V"lookaround") * V"expr" * (tk('|') * V"expr")^-1 * tk(')'),
  comment = tk('(') * tk('?') * tk('#') * (neg(tk(')')))^1 * tk(')'),
  quantifier = ((tk('?') + tk('*') + tk('+')) * (tk('+') + tk('?'))^-1) + (tk('{') * V"digits" * (tk(',') * (V"digits")^-1)^-1 * tk('}') * (tk('+') + tk('?'))^-1),
  option_setting = tk('(') * ((tk('*') * ((V"utf" * (tk('8') + (tk('1') * tk('6')) + (tk('3') * tk('2')))^-1) + V"ucp" + V"no_auto_possess" + V"no_start_opt" + V"newline_conventions" + (V"limit_match" * tk('=') * V"digits") + (V"limit_recursion" * tk('=') * V"digits") + V"bsr_anycrlf" + V"bsr_unicode")) + (tk('?') * (((V"option_setting_flag")^1 * (tk('-') * (V"option_setting_flag")^1)^-1) + (tk('-') * (V"option_setting_flag")^1)))) * tk(')'),
  option_setting_flag = tk('i') + tk('J') + tk('m') + tk('s') + tk('U') + tk('x'),
  backtracking_control = tk('(') * tk('*') * (V"accept_" + V"fail" + ((V"mark")^-1 * tk(':') * V"name") + V"commit" + (V"prune" * (tk(':') * V"name")^-1) + (V"skip" * (tk(':') * V"name")^-1) + (V"then" * (tk(':') * V"name")^-1)) * tk(')'),
  callout = tk('(') * tk('?') * tk('C') * (V"digits")^-1 * tk(')'),
  newline_conventions = V"cr" + V"lf" + V"crlf" + V"anycrlf" + V"any",
  character = tk('\\') * (tk('a') + (tk('c') * P(1)) + tk('e') + tk('f') + tk('n') + tk('r') + tk('t') + (V"digit" * (V"digit" * (V"digit")^-1)^-1) + (tk('o') * tk('{') * V"digit" * V"digit" * (V"digit")^1 * tk('}')) + (tk('x') * V"hex" * V"hex") + (tk('x') * tk('{') * V"hex" * V"hex" * (V"hex")^1 * tk('}')) + (tk('u') * V"hex" * V"hex" * V"hex" * V"hex" * (V"hex" * V"hex" * V"hex" * V"hex")^-1)),
  character_type = tk('.') + (tk('\\') * (tk('C') + tk('d') + tk('D') + tk('h') + tk('H') + tk('N') + (tk('p') * tk('{') * (tk('^'))^-1 * V"name" * (tk('&'))^-1 * tk('}')) + (tk('P') * tk('{') * V"name" * (tk('&'))^-1 * tk('}')) + (tk('p') * V"letter" * (V"letter")^-1) + tk('R') + tk('s') + tk('S') + tk('v') + tk('V') + tk('w') + tk('W') + tk('X'))),
  character_class = (tk('[') * (tk('^'))^-1 * tk(']') * (V"character_class_atom")^0 * tk(']')) + (tk('[') * (tk('^'))^-1 * (V"character_class_atom")^1 * tk(']')),
  character_class_atom = V"posix_character_class" + V"character_class_range" + V"character" + V"character_type" + (tk('\\') * P(1)) + neg(tk('\\') + tk(']')),
  character_class_range = V"character_class_range_atom" * tk('-') * V"character_class_range_atom",
  character_class_range_atom = V"character" + (tk('\\') * P(1)) + neg(tk('\\') + tk(']')),
  posix_character_class = tk('[:') * (tk('^'))^-1 * V"letters" * tk(':]'),
  anchor = tk('$') + (tk('\\') * (tk('b') + tk('B') + tk('A') + tk('z') + tk('Z') + tk('G'))) + tk('^'),
  match_point_reset = tk('\\') * tk('K'),
  quoting = tk('\\') * ((tk('Q') * neg(tk('\\'))^0 * tk('\\') * tk('E')) + P(1)),
  digits = V"FixedRepetition_1",
  digit = V"D0" + V"D1" + V"D2" + V"D3" + V"D4" + V"D5" + V"D6" + V"D7" + V"D8" + V"D9",
  hex = V"digit" + tk('a') + tk('b') + tk('c') + tk('d') + tk('e') + tk('f') + tk('A') + tk('B') + tk('C') + tk('D') + tk('E') + tk('F'),
  letters = V"FixedRepetition_2",
  letter = tk('j') + tk('l') + tk('q') + tk('y') + tk('a') + tk('b') + tk('c') + tk('d') + tk('e') + tk('f') + tk('g') + tk('h') + tk('i') + tk('k') + tk('m') + tk('n') + tk('o') + tk('p') + tk('r') + tk('s') + tk('t') + tk('u') + tk('v') + tk('w') + tk('x') + tk('z') + tk('A') + tk('B') + tk('C') + tk('D') + tk('E') + tk('F') + tk('G') + tk('H') + tk('I') + tk('J') + tk('K') + tk('L') + tk('M') + tk('N') + tk('O') + tk('P') + tk('Q') + tk('R') + tk('S') + tk('T') + tk('U') + tk('V') + tk('W') + tk('X') + tk('Y') + tk('Z') + tk('_'),
  name = V"letter" * (V"letter" + V"digit")^0,
  other = tk('}') + tk(']') + tk(',') + tk('-') + tk('_') + tk('=') + tk('&') + tk('<') + tk('>') + tk('\'') + tk(':') + tk('#') + tk('!') + V"OTHER",
  utf = tk('U') * tk('T') * tk('F'),
  ucp = tk('U') * tk('C') * tk('P'),
  no_auto_possess = tk('N') * tk('O') * tk('_') * tk('A') * tk('U') * tk('T') * tk('O') * tk('_') * tk('P') * tk('O') * tk('S') * tk('S') * tk('E') * tk('S') * tk('S'),
  no_start_opt = tk('N') * tk('O') * tk('_') * tk('S') * tk('T') * tk('A') * tk('R') * tk('T') * tk('_') * tk('O') * tk('P') * tk('T'),
  cr = tk('C') * tk('R'),
  lf = tk('L') * tk('F'),
  crlf = tk('C') * tk('R') * tk('L') * tk('F'),
  anycrlf = tk('A') * tk('N') * tk('Y') * tk('C') * tk('R') * tk('L') * tk('F'),
  any = tk('A') * tk('N') * tk('Y'),
  limit_match = tk('L') * tk('I') * tk('M') * tk('I') * tk('T') * tk('_') * tk('M') * tk('A') * tk('T') * tk('C') * tk('H'),
  limit_recursion = tk('L') * tk('I') * tk('M') * tk('I') * tk('T') * tk('_') * tk('R') * tk('E') * tk('C') * tk('U') * tk('R') * tk('S') * tk('I') * tk('O') * tk('N'),
  bsr_anycrlf = tk('B') * tk('S') * tk('R') * tk('_') * tk('A') * tk('N') * tk('Y') * tk('C') * tk('R') * tk('L') * tk('F'),
  bsr_unicode = tk('B') * tk('S') * tk('R') * tk('_') * tk('U') * tk('N') * tk('I') * tk('C') * tk('O') * tk('D') * tk('E'),
  accept_ = tk('A') * tk('C') * tk('C') * tk('E') * tk('P') * tk('T'),
  fail = tk('F') * (tk('A') * tk('I') * tk('L'))^-1,
  mark = tk('M') * tk('A') * tk('R') * tk('K'),
  commit = tk('C') * tk('O') * tk('M') * tk('M') * tk('I') * tk('T'),
  prune = tk('P') * tk('R') * tk('U') * tk('N') * tk('E'),
  skip = tk('S') * tk('K') * tk('I') * tk('P'),
  ["then"] = tk('T') * tk('H') * tk('E') * tk('N'),
  BSlash = P('\\'),
  Dollar = P('$'),
  Dot = P('.'),
  OBrack = P('['),
  Caret = P('^'),
  Pipe = P('|'),
  QMark = P('?'),
  Star = P('*'),
  Plus = P('+'),
  OBrace = P('{'),
  CBrace = P('}'),
  OPar = P('('),
  CPar = P(')'),
  CBrack = P(']'),
  OPosixBrack = P('[:'),
  CPosixBrack = P(':]'),
  Comma = P(','),
  Dash = P('-'),
  UScore = P('_'),
  Eq = P('='),
  Amp = P('&'),
  Lt = P('<'),
  Gt = P('>'),
  Quote = P('\''),
  Col = P(':'),
  Hash = P('#'),
  Excl = P('!'),
  Au = P('A') * -((R('az') + R('09') + P('_'))),
  Bu = P('B') * -((R('az') + R('09') + P('_'))),
  Cu = P('C') * -((R('az') + R('09') + P('_'))),
  Du = P('D') * -((R('az') + R('09') + P('_'))),
  Eu = P('E') * -((R('az') + R('09') + P('_'))),
  Fu = P('F') * -((R('az') + R('09') + P('_'))),
  Gu = P('G') * -((R('az') + R('09') + P('_'))),
  Hu = P('H') * -((R('az') + R('09') + P('_'))),
  Iu = P('I') * -((R('az') + R('09') + P('_'))),
  Ju = P('J') * -((R('az') + R('09') + P('_'))),
  Ku = P('K') * -((R('az') + R('09') + P('_'))),
  Lu = P('L') * -((R('az') + R('09') + P('_'))),
  Mu = P('M') * -((R('az') + R('09') + P('_'))),
  Nu = P('N') * -((R('az') + R('09') + P('_'))),
  Ou = P('O') * -((R('az') + R('09') + P('_'))),
  Pu = P('P') * -((R('az') + R('09') + P('_'))),
  Qu = P('Q') * -((R('az') + R('09') + P('_'))),
  Ru = P('R') * -((R('az') + R('09') + P('_'))),
  Su = P('S') * -((R('az') + R('09') + P('_'))),
  Tu = P('T') * -((R('az') + R('09') + P('_'))),
  Uu = P('U') * -((R('az') + R('09') + P('_'))),
  Vu = P('V') * -((R('az') + R('09') + P('_'))),
  Wu = P('W') * -((R('az') + R('09') + P('_'))),
  Xu = P('X') * -((R('az') + R('09') + P('_'))),
  Yu = P('Y') * -((R('az') + R('09') + P('_'))),
  Zu = P('Z') * -((R('az') + R('09') + P('_'))),
  Al = P('a') * -((R('az') + R('09') + P('_'))),
  Bl = P('b') * -((R('az') + R('09') + P('_'))),
  Cl = P('c') * -((R('az') + R('09') + P('_'))),
  Dl = P('d') * -((R('az') + R('09') + P('_'))),
  El = P('e') * -((R('az') + R('09') + P('_'))),
  Fl = P('f') * -((R('az') + R('09') + P('_'))),
  Gl = P('g') * -((R('az') + R('09') + P('_'))),
  Hl = P('h') * -((R('az') + R('09') + P('_'))),
  Il = P('i') * -((R('az') + R('09') + P('_'))),
  Jl = P('j') * -((R('az') + R('09') + P('_'))),
  Kl = P('k') * -((R('az') + R('09') + P('_'))),
  Ll = P('l') * -((R('az') + R('09') + P('_'))),
  Ml = P('m') * -((R('az') + R('09') + P('_'))),
  Nl = P('n') * -((R('az') + R('09') + P('_'))),
  Ol = P('o') * -((R('az') + R('09') + P('_'))),
  Pl = P('p') * -((R('az') + R('09') + P('_'))),
  Ql = P('q') * -((R('az') + R('09') + P('_'))),
  Rl = P('r') * -((R('az') + R('09') + P('_'))),
  Sl = P('s') * -((R('az') + R('09') + P('_'))),
  Tl = P('t') * -((R('az') + R('09') + P('_'))),
  Ul = P('u') * -((R('az') + R('09') + P('_'))),
  Vl = P('v') * -((R('az') + R('09') + P('_'))),
  Wl = P('w') * -((R('az') + R('09') + P('_'))),
  Xl = P('x') * -((R('az') + R('09') + P('_'))),
  Yl = P('y') * -((R('az') + R('09') + P('_'))),
  Zl = P('z') * -((R('az') + R('09') + P('_'))),
  D0 = P('0'),
  D1 = P('1'),
  D2 = P('2'),
  D3 = P('3'),
  D4 = P('4'),
  D5 = P('5'),
  D6 = P('6'),
  D7 = P('7'),
  D8 = P('8'),
  D9 = P('9'),
  OTHER = P(1),
  FixedRepetition_0 = (V"element" * V"FixedRepetition_0") + #(tk('N') + tk('-') + tk('k') + tk('J') + tk(')') + tk('g') + tk('F') + tk('c') + tk('B') + tk('!') + tk('_') + tk('>') + tk('|') + tk('[') + tk(':') + tk('x') + tk('W') + tk('t') + tk('S') + tk('p') + tk('O') + tk('.') + tk('l') + tk('K') + tk('h') + tk('\'') + tk('G') + tk('&') + tk('d') + tk('C') + tk('}') + tk('\\') + tk('y') + tk('X') + tk('u') + tk('T') + tk('q') + tk('P') + tk('m') + tk('L') + tk('i') + tk('H') + tk('e') + tk('D') + tk('#') + tk('a') + tk(']') + tk('[:') + tk('<') + tk('z') + tk('Y') + tk('v') + tk('U') + tk('r') + V"EOF" + tk('Q') + tk('n') + tk('M') + tk(',') + tk('j') + tk('I') + tk('(') + tk('f') + tk('E') + tk('$') + tk('b') + V"D0" + tk('A') + V"D1" + V"D2" + V"D3" + V"D4" + tk('^') + V"OTHER" + V"D5" + tk('=') + V"D6" + V"D7" + tk('Z') + V"D8" + V"D9" + tk('w') + tk('V') + tk('s') + tk('R') + tk('o') + V"element"),
  FixedRepetition_1 = (V"digit" * V"FixedRepetition_1") + #(tk('N') + tk('-') + tk('k') + tk('J') + tk(')') + tk('g') + tk('F') + tk('c') + tk('B') + tk('!') + tk('_') + tk('>') + tk('|') + tk('[') + tk(':') + tk('x') + tk('W') + tk('t') + tk('S') + tk('p') + tk('O') + tk('.') + tk('l') + tk('K') + tk('*') + tk('h') + tk('\'') + tk('G') + tk('&') + tk('d') + tk('C') + tk('?') + tk('}') + tk('\\') + tk('y') + tk('X') + tk('u') + tk('T') + tk('q') + tk('P') + tk('m') + tk('L') + tk('+') + tk('i') + tk('H') + tk('e') + tk('D') + tk('#') + tk('a') + tk(']') + tk('[:') + tk('<') + tk('z') + tk('Y') + tk('v') + tk('U') + tk('r') + V"EOF" + tk('Q') + tk('n') + tk('M') + tk(',') + tk('j') + tk('I') + tk('(') + tk('f') + tk('E') + tk('$') + tk('b') + V"D0" + tk('A') + V"D1" + V"D2" + V"D3" + V"D4" + tk('^') + V"OTHER" + V"D5" + tk('=') + V"D6" + tk('{') + V"D7" + tk('Z') + V"D8" + V"D9" + tk('w') + tk('V') + tk('s') + tk('R') + tk('o') + V"digit"),
  FixedRepetition_2 = (V"letter" * V"FixedRepetition_2") + #(tk('N') + tk('-') + tk('k') + tk('J') + tk(')') + tk('g') + tk('F') + tk('c') + tk('B') + tk('!') + tk('_') + tk(':]') + tk('>') + tk('|') + tk('[') + tk(':') + tk('x') + tk('W') + tk('t') + tk('S') + tk('p') + tk('O') + tk('.') + tk('l') + tk('K') + tk('*') + tk('h') + tk('\'') + tk('G') + tk('&') + tk('d') + tk('C') + tk('?') + tk('}') + tk('\\') + tk('y') + tk('X') + tk('u') + tk('T') + tk('q') + tk('P') + tk('m') + tk('L') + tk('+') + tk('i') + tk('H') + tk('e') + tk('D') + tk('#') + tk('a') + tk(']') + tk('[:') + tk('<') + tk('z') + tk('Y') + tk('v') + tk('U') + tk('r') + V"EOF" + tk('Q') + tk('n') + tk('M') + tk(',') + tk('j') + tk('I') + tk('(') + tk('f') + tk('E') + tk('$') + tk('b') + V"D0" + tk('A') + V"D1" + V"D2" + V"D3" + V"D4" + tk('^') + V"OTHER" + V"D5" + tk('=') + V"D6" + tk('{') + V"D7" + tk('Z') + V"D8" + V"D9" + tk('w') + tk('V') + tk('s') + tk('R') + tk('o') + V"letter"),
	EOF = EOF,
    EMPTY = EMPTY,
    KEYWORDS = P'\\' + P'$' + P'.' + P'[' + P'^' + P'|' + P'?' + P'*' + P'+' + P'{' + P'}' + P'(' + P')' + P']' + P'[:' + P':]' + P',' + P'-' + P'_' + P'=' + P'&' + P'<' + P'>' + P'\'' + P':' + P'#' + P'!' + P'0' + P'1' + P'2' + P'3' + P'4' + P'5' + P'6' + P'7' + P'8' + P'9',
}

local parse = function (input)
  lpeg.setmaxstack(8000)
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
