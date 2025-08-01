/*
 [The "BSD licence"]
 Copyright (c) 2013 Terence Parr
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
 3. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
/** Derived from http://www.graphviz.org/doc/info/lang.html.
    Comments pulled from spec.
 */
 /*
  Modified by: Andrzej Borucki (2025) : character set
 */

// $antlr-format alignTrailingComments true, columnLimit 150, minEmptyLines 1, maxEmptyLinesToKeep 1, reflowComments false, useTab false
// $antlr-format allowShortRulesOnASingleLine false, allowShortBlocksOnASingleLine true, alignSemicolons hanging, alignColons hanging
// Andrzej Borucki: characters

grammar Dot;

options {
    caseInsensitive = true;
}

graph
    : STRICT? (GRAPH | DIGRAPH) id_? '{' stmt_list '}' EOF
    ;

stmt_list
    : (stmt ';'?)*
    ;

stmt
    : node_stmt
    | edge_stmt
    | attr_stmt
    | id_ '=' id_
    | subgraph
    ;

attr_stmt
    : (GRAPH | NODE | EDGE) attr_list
    ;

attr_list
    : ('[' a_list? ']')+
    ;

a_list
    : (id_ ( '=' id_)? (';' | ',')?)+
    ;

edge_stmt
    : (node_id | subgraph) edgeRHS attr_list?
    ;

edgeRHS
    : (edgeop ( node_id | subgraph))+
    ;

edgeop
    : '->'
    | '--'
    ;

node_stmt
    : node_id attr_list?
    ;

node_id
    : id_ port?
    ;

port
    : ':' id_ (':' id_)?
    ;

subgraph
    : (SUBGRAPH id_?)? '{' stmt_list '}'
    ;

id_
    : ID
    | STRING
    | HTML_STRING
    | NUMBER
    ;

// "The keywords node, edge, graph, digraph, subgraph, and strict are
// case-independent"
STRICT
    : 'strict'
    ;

GRAPH
    : 'graph'
    ;

DIGRAPH
    : 'digraph'
    ;

NODE
    : 'node'
    ;

EDGE
    : 'edge'
    ;

SUBGRAPH
    : 'subgraph'
    ;

/** "a numeral [-]?(.[0-9]+ | [0-9]+(.[0-9]*)? )" */
NUMBER
    : '-'? ('.' DIGIT+ | DIGIT+ ( '.' DIGIT*)?)
    ;

fragment DIGIT
    : [0-9]
    ;

/** "any double-quoted string ("...") possibly containing escaped quotes" */
STRING
    : '"' Char* '"'
    ;

fragment Char
    : ~["\\]
    | ESC
    ;

fragment ESC
    : '\\' .
    ;

/** "Any string of alphabetic ([a-zA-Z\200-\377]) characters, underscores
 *  ('_') or digits ([0-9]), not beginning with a digit"
 */
ID
    : LETTER (LETTER | DIGIT)*
    ;

fragment LETTER
    : [a-z_]  // caseInsensitive = true
    ;

/** "HTML strings, angle brackets must occur in matched pairs, and
 *  unescaped newlines are allowed."
 */
HTML_STRING
    : '<' (TAG | ~ [<>])* '>'
    ;

fragment TAG
    : '<' .*? '>'
    ;

COMMENT
    : '/*' .*? '*/' -> skip
    ;

LINE_COMMENT
    : '//' .*? '\r'? '\n' -> skip
    ;

/** "a '#' character is considered a line output from a C preprocessor (e.g.,
 *  # 34 to indicate line 34 ) and discarded"
 */
PREPROC
    : '#' ~[\r\n]* -> skip
    ;

WS
    : [ \t\n\r]+ -> skip
    ;
