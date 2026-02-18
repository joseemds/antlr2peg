grammar LeftRecursive;

program: expr EOF;

expr:
     expr '+' term
    | expr '-' term
    | term
    ;

term:
    term '*' factor
    | term '/' factor
		| factor
    ;

factor:
      INT
    | '-' factor
    | '(' expr ')'
    ;

INT : [0-9]+ ;
WS : [ \t\r\n]+ -> skip ;
