package org.javarosa.xpath.parser;

import java_cup.runtime.*;
import org.javarosa.xpath.expr.*;

%%
  
/* DON'T EDIT THIS FILE UNLESS YOU KNOW WHAT YOU'RE DOING */ 

/* this file isn't actually used, as the generated parser used too much memory,
 * however, our custom light-weight parser is based off it, and it is left here
 * for reference
 */

/* TODO:
 * need to catch syntax errors
 * need to explore directives that make compiled code smaller
 */

%public
%class XPathLexer
%unicode
%cup
%buffer 128
%pack

%{   
	private Symbol symbol(int type) {
		return new Symbol(type);
	}
    
	private Symbol symbol(int type, Object value) {
		return new Symbol(type, value);
	}

	private String trim (String str, int head, int tail) {
		return str.substring(head, str.length() - tail);
	}
%}

%init{
	yybegin(VAL_CONTEXT);
%init}

WhiteSpace = [ \n\t\r\f]+
Digit = [0-9]
Letter = [A-Za-z]
NameStartChar = {Letter}|_
NameTrailChar = {Letter}|{Digit}|[-._]
NCName = {NameStartChar}{NameTrailChar}*
QName = {NCName}(:{NCName})?

%xstate OP_CONTEXT, VAL_CONTEXT
      
%%

/* '*', 'div', 'mod', 'and', and 'or' are all ambiguous
 * '*' can either be the multiply operator or the path wildcard
 * 'div', 'mod', 'and', and 'or' can either be operators or identifiers
 *
 * These tokens, as operators, can only follow ')', ']', '.', '..', QNAME, VAR, NUM, STR, both wildcards
 *   ('*' and 'ns:*'), and '/'
 * As wildcards/identifiers, these tokens can only follow '::', '@', '/', '//', '[', '(', ',', all operators
 *   (including '*' as multiply), and the empty string (start of expression)
 *
 * Note: for the conflicting case of '/', precedence is given to the wildcard/identifier interpretation
 */

/* Node-types are handled here; axis specifiers will be validated in later phases */

<VAL_CONTEXT> {

"*"		{ yybegin(OP_CONTEXT);  return symbol(sym.WILDCARD); }
{QName}		{ yybegin(OP_CONTEXT);  return symbol(sym.QNAME, new XPathQName(yytext())); }
{NCName}:\*	{ yybegin(OP_CONTEXT);  return symbol(sym.NSWILDCARD, trim(yytext(), 0, 2)); }

}

<OP_CONTEXT> {

"*"		{ yybegin(VAL_CONTEXT); return symbol(sym.MULT); }
"and"		{ yybegin(VAL_CONTEXT); return symbol(sym.AND); }
"or"		{ yybegin(VAL_CONTEXT); return symbol(sym.OR); }
"div"		{ yybegin(VAL_CONTEXT); return symbol(sym.DIV); }
"mod"		{ yybegin(VAL_CONTEXT); return symbol(sym.MOD); }

}
   
<OP_CONTEXT, VAL_CONTEXT> {
   
"="		{ yybegin(VAL_CONTEXT); return symbol(sym.EQ); }
"!="		{ yybegin(VAL_CONTEXT); return symbol(sym.NEQ); }
"<"		{ yybegin(VAL_CONTEXT); return symbol(sym.LT); }
">"		{ yybegin(VAL_CONTEXT); return symbol(sym.GT); }
"<="		{ yybegin(VAL_CONTEXT); return symbol(sym.LTE); }
">="		{ yybegin(VAL_CONTEXT); return symbol(sym.GTE); }
"+"		{ yybegin(VAL_CONTEXT); return symbol(sym.PLUS); }
"-"		{ yybegin(VAL_CONTEXT); return symbol(sym.MINUS); }
"|"		{ yybegin(VAL_CONTEXT); return symbol(sym.UNION); }
"/"		{ yybegin(VAL_CONTEXT); return symbol(sym.SLASH); }
"//"		{ yybegin(VAL_CONTEXT); return symbol(sym.DBL_SLASH); }
"["		{ yybegin(VAL_CONTEXT); return symbol(sym.LBRACK); }
"]"		{ yybegin(OP_CONTEXT);  return symbol(sym.RBRACK); }
"("		{ yybegin(VAL_CONTEXT); return symbol(sym.LPAREN); }
")"		{ yybegin(OP_CONTEXT);  return symbol(sym.RPAREN); }
"."		{ yybegin(OP_CONTEXT);  return symbol(sym.DOT); }
".."		{ yybegin(OP_CONTEXT);  return symbol(sym.DBL_DOT); }
"@"		{ yybegin(VAL_CONTEXT); return symbol(sym.AT); }
"::"		{ yybegin(VAL_CONTEXT); return symbol(sym.DBL_COLON); }
","		{ yybegin(VAL_CONTEXT); return symbol(sym.COMMA); }

node/{WhiteSpace}?\(			{ return symbol(sym.NODETYPE_NODE); }
text/{WhiteSpace}?\(			{ return symbol(sym.NODETYPE_TEXT); }
comment/{WhiteSpace}?\(			{ return symbol(sym.NODETYPE_COMMENT); }
processing-instruction/{WhiteSpace}?\(	{ return symbol(sym.NODETYPE_PROCINSTR); }

\${QName}				{ yybegin(OP_CONTEXT); return symbol(sym.VAR, new XPathQName(trim(yytext(), 1, 0))); }
{Digit}+(\.{Digit}*)?|\.{Digit}+	{ yybegin(OP_CONTEXT); return symbol(sym.NUM, Double.valueOf(yytext())); }
\"[^\"]*\"|\'[^\']*\'			{ yybegin(OP_CONTEXT); return symbol(sym.STR, trim(yytext(), 1, 1)); }
  /* xpath strings have no escape mechanism */

{WhiteSpace}		{ /* ignore whitespace */ }

}