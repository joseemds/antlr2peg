grammar Calc;

program: expr EOF;

expr:
    term '+' term
    | term '-' term
    | term
    ;

term:
    factor '*' factor
    | factor '/' factor
		| factor
    ;

factor:
      INT
    | '-' factor
    | '(' expr ')'
    ;

INT : [0-9]+ ;
WS : [ \t\r\n]+ -> skip ;
