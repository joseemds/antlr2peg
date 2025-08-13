grammar Simple;

program: expr EOF;


expr:
    'a';


WS : [ \t\r\n]+ -> skip ;
