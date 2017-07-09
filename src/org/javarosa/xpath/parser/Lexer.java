/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.xpath.parser;

import java.util.Vector;

import org.javarosa.xpath.expr.XPathQName;

public class Lexer {

	private static final int CONTEXT_LENGTH = 15;

	public static final int LEX_CONTEXT_VAL = 1;
	public static final int LEX_CONTEXT_OP = 2;

	public static Vector<Token> lex (String expr) throws XPathSyntaxException {
		Vector<Token> tokens = new Vector<Token>();

		int i = 0;
		int context = LEX_CONTEXT_VAL;

		while (i < expr.length()) {
			int c = expr.charAt(i);
			int d = getChar(expr, i + 1);

			Token token = null;
			int skip = 1;

			if (" \n\t\f\r".indexOf(c) >= 0) {
				/* whitespace; do nothing */
			} else if (c == '=') {
				token = new Token(Token.EQ);
			} else if (c == '!' && d == '=') {
				token = new Token(Token.NEQ);
				skip = 2;
			} else if (c == '<') {
				if (d == '=') {
					token = new Token(Token.LTE);
					skip = 2;
				} else {
					token = new Token(Token.LT);
				}
			} else if (c == '>') {
				if (d == '=') {
					token = new Token(Token.GTE);
					skip = 2;
				} else {
					token = new Token(Token.GT);
				}
			} else if (c == '+') {
				token = new Token(Token.PLUS);
			} else if (c == '-') {
				token = new Token(context == LEX_CONTEXT_VAL ? Token.UMINUS : Token.MINUS); //not sure this is entirely correct
			} else if (c == '*') {
				token = new Token(context == LEX_CONTEXT_VAL ? Token.WILDCARD : Token.MULT);
			} else if (c == '|') {
				token = new Token(Token.UNION);
			} else if (c == '/') {
				if (d == '/') {
					token = new Token(Token.DBL_SLASH);
					skip = 2;
				} else {
					token = new Token(Token.SLASH);
				}
			} else if (c == '[') {
				token = new Token(Token.LBRACK);
			} else if (c == ']') {
				token = new Token(Token.RBRACK);
			} else if (c == '(') {
				token = new Token(Token.LPAREN);
			} else if (c == ')') {
				token = new Token(Token.RPAREN);
			} else if (c == '.') {
				if (d == '.') {
					token = new Token(Token.DBL_DOT);
					skip = 2;
				} else if (isDigit(d)) {
					skip = matchNumeric(expr, i);
					token = new Token(Token.NUM, Double.valueOf(expr.substring(i, i + skip)));
				} else {
					token = new Token(Token.DOT);
				}
			} else if (c == '@') {
				token = new Token(Token.AT);
			} else if (c == ',') {
				token = new Token(Token.COMMA);
			} else if (c == ':' && d == ':') {
				token = new Token(Token.DBL_COLON);
				skip = 2;
			} else if (context == LEX_CONTEXT_OP && i + 3 <= expr.length() && "and".equals(expr.substring(i, i + 3))) {
				token = new Token(Token.AND);
				skip = 3;
			} else if (context == LEX_CONTEXT_OP && i + 2 <= expr.length() && "or".equals(expr.substring(i, i + 2))) {
				token = new Token(Token.OR);
				skip = 2;
			} else if (context == LEX_CONTEXT_OP && i + 3 <= expr.length() && "div".equals(expr.substring(i, i + 3))) {
				token = new Token(Token.DIV);
				skip = 3;
			} else if (context == LEX_CONTEXT_OP && i + 3 <= expr.length() && "mod".equals(expr.substring(i, i + 3))) {
				token = new Token(Token.MOD);
				skip = 3;
			} else if (c == '$') {
				int len = matchQName(expr, i + 1);
				if (len == 0) {
					badParse(expr, i, (char)c);
				} else {
					token = new Token(Token.VAR, new XPathQName(expr.substring(i + 1, i + len + 1)));
					skip = len + 1;
				}
			} else if (c == '\'' || c == '\"') {
				int end = expr.indexOf(c, i + 1);
				if (end == -1) {
					badParse(expr, i, (char)c);
				} else {
					token = new Token(Token.STR, expr.substring(i + 1, end));
					skip = (end - i) + 1;
				}
			} else if (isDigit(c)) {
				skip = matchNumeric(expr, i);
				token = new Token(Token.NUM, Double.valueOf(expr.substring(i, i + skip)));
			} else if (context == LEX_CONTEXT_VAL && (isAlpha(c) || c == '_')) {
				int len = matchQName(expr, i);
				String name = expr.substring(i, i + len);
				if (name.indexOf(':') == -1 && getChar(expr, i + len) == ':' && getChar(expr, i + len + 1) == '*') {
					token = new Token(Token.NSWILDCARD, name);
					skip = len + 2;
				} else {
					token = new Token(Token.QNAME, new XPathQName(name));
					skip = len;
				}
			} else {
				badParse(expr, i, (char)c);
			}
			if (token != null) {
				if (token.type == Token.WILDCARD ||
					token.type == Token.NSWILDCARD ||
					token.type == Token.QNAME ||
					token.type == Token.VAR ||
					token.type == Token.NUM ||
					token.type == Token.STR ||
					token.type == Token.RBRACK ||
					token.type == Token.RPAREN ||
					token.type == Token.DOT ||
					token.type == Token.DBL_DOT) {
					context = LEX_CONTEXT_OP;
				} else {
					context = LEX_CONTEXT_VAL;
				}

				tokens.addElement(token);
			}
			i += skip;
		}

		return tokens;
	}

	private static void badParse(String expr, int i, char c) throws XPathSyntaxException {

		String start = "\u034E" + c;
		String preContext =  (Math.max(0, i - CONTEXT_LENGTH) != 0 ? "..." : "") + expr.substring(Math.max(0, i - CONTEXT_LENGTH), Math.max(0, i)).trim();
		String postcontext = i == expr.length() - 1 ? "" :
				expr.substring(Math.min(i + 1, expr.length() - 1), Math.min(i + CONTEXT_LENGTH, expr.length())).trim() + (Math.min(i + CONTEXT_LENGTH, expr.length()) != expr.length() ? "..." : "");

		throw new XPathSyntaxException("Couldn't understand the expression starting at this point: " + (preContext + start + postcontext));
	}

	private static int matchNumeric (String expr, int i) {
		boolean seenDecimalPoint = false;
		int start = i;
		int c;

		for (; i < expr.length(); i++) {
			c = expr.charAt(i);

			if (!(isDigit(c) || (!seenDecimalPoint && c == '.')))
				break;

			if (c == '.')
				seenDecimalPoint = true;
		}

		return i - start;
	}

	private static int matchQName (String expr, int i) {
		int len = matchNCName(expr, i);

		if (len > 0 && getChar(expr, i + len) == ':') {
			int len2 = matchNCName(expr, i + len + 1);

			if (len2 > 0)
				len += len2 + 1;
		}

		return len;
	}

	private static int matchNCName (String expr, int i) {
		int start = i;
		int c;

		for (; i < expr.length(); i++) {
			c = expr.charAt(i);

			if (!(isAlpha(c) || c == '_' || (i > start && (isDigit(c) || c == '.' || c == '-'))))
				break;
		}

		return i - start;
	}

	//get char from string, return -1 for EOF
	private static int getChar (String expr, int i) {
		return (i < expr.length() ? expr.charAt(i) : -1);
	}

	private static boolean isDigit (int c) {
		return (c < 0 ? false : Character.isDigit((char)c));
	}

	private static boolean isAlpha (int c) {
		return (c < 0 ? false : Character.isLowerCase((char)c) || Character.isUpperCase((char)c));
	}
}
