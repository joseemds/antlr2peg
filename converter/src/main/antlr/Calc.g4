grammar Calc;

program: expr EOF;

expr:
    term '+' expr
    | term '-' expr
    | term
    ;

term:
    factor '*' term
    | factor '/' term
		| factor
    ;

factor:
      INT
    | '-' factor
    | '(' expr ')'
    ;

INT : [0-9]+ ;
WS : [ \t\r\n]+ -> skip ;
