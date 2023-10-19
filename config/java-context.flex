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

  "["                          { return new JavaContextSymbol(JavaContextSymbol.JavaContextSymbolType.LEFT_BRACKET, yyline, yycolumn); }
  "]"                          { return new JavaContextSymbol(JavaContextSymbol.JavaContextSymbolType.RIGHT_BRACKET, yyline, yycolumn); }
  ","                          { return new JavaContextSymbol(JavaContextSymbol.JavaContextSymbolType.COMMA, yyline, yycolumn); }
  ":"                          { return new JavaContextSymbol(JavaContextSymbol.JavaContextSymbolType.COLON, yyline, yycolumn); }
  ".."                         { return new JavaContextSymbol(JavaContextSymbol.JavaContextSymbolType.RANGE, yyline, yycolumn); }
  {StringLiteral}              { return new JavaContextSymbol(JavaContextSymbol.JavaContextSymbolType.STRING_VAL, yyline, yycolumn, yytext().substring(1, yytext().length() - 1).replaceAll("\\\\'", "'")); }
  {NumberLiteral}              { return new JavaContextSymbol(JavaContextSymbol.JavaContextSymbolType.NUMBER, yyline, yycolumn, Integer.valueOf(yytext())); }
  {VariableLiteral}            { return new JavaContextSymbol(JavaContextSymbol.JavaContextSymbolType.VARIABLE, yyline, yycolumn, yytext()); }

}

/* error fallback */
.|\n                           {  }
<<EOF>>                        { return null; }
