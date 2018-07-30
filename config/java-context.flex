/*
  Flex definition of the java context syntax.

  After creating the class, add the following to the top:
     @SuppressWarnings("ALL")
*/

package com.imsweb.validation.internal.context;

%%

%class JavaContextLexer
%public
%line
%column
%cup
%ignorecase


StringLiteral = '([^']|\\')*'
NumberLiteral = \-?[0-9]+
VariableLiteral = [A-Za-z0-9_\-]+

%%

<YYINITIAL> {

  "["                          { return new Symbol(Symbol.SymbolType.LEFT_BRACKET, yyline, yycolumn); }
  "]"                          { return new Symbol(Symbol.SymbolType.RIGHT_BRACKET, yyline, yycolumn); }
  ","                          { return new Symbol(Symbol.SymbolType.COMMA, yyline, yycolumn); }
  ":"                          { return new Symbol(Symbol.SymbolType.COLON, yyline, yycolumn); }
  ".."                         { return new Symbol(Symbol.SymbolType.RANGE, yyline, yycolumn); }
  {StringLiteral}              { return new Symbol(Symbol.SymbolType.STRING, yyline, yycolumn, yytext().substring(1, yytext().length() - 1).replaceAll("\\\\'", "'")); }
  {NumberLiteral}              { return new Symbol(Symbol.SymbolType.NUMBER, yyline, yycolumn, Integer.valueOf(yytext())); }
  {VariableLiteral}            { return new Symbol(Symbol.SymbolType.VARIABLE, yyline, yycolumn, yytext()); }

}

/* error fallback */
.|\n                           {  }
<<EOF>>                        { return null; }
