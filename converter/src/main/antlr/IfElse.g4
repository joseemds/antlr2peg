grammar IfElse;

program: expr EOF;


if:
    'if' expr 'then' expr
    | 'if' expr 'then' expr 'else' expr;


expr: 'true' | 'false';




WS : [ \t\r\n]+ -> skip ;
