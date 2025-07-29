grammar Calc;

program: expr EOF;


expr:
    | INT
    | '-' expr
    | '(' expr ')'
    | expr '/' expr
    | expr '*' expr
    | expr '+' expr
    | expr '-' expr
    ;


INT : [0-9]+ ;

WS : [ \t\r\n]+ -> skip ;
