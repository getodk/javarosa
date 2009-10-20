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

import org.javarosa.xpath.expr.XPathQName;

public class Token {
	public static final int AND = 1;
	public static final int AT = 2;
	public static final int COMMA = 3;
	public static final int DBL_COLON = 4;
	public static final int DBL_DOT = 5;
	public static final int DBL_SLASH = 6;
	public static final int DIV = 7;
	public static final int DOT = 8;
	public static final int EQ = 9;
	public static final int GT = 10;
	public static final int GTE = 11;
	public static final int LBRACK = 12;
	public static final int LPAREN = 13;
	public static final int LT = 14;
	public static final int LTE = 15;
	public static final int MINUS = 16;
	public static final int MOD = 17;
	public static final int MULT = 18;
	public static final int NEQ = 19;
	public static final int NSWILDCARD = 20;
	public static final int NUM = 21;
	public static final int OR = 22;
	public static final int PLUS = 23;
	public static final int QNAME = 24;
	public static final int RBRACK = 25;
	public static final int RPAREN = 26;
	public static final int SLASH = 27;
	public static final int STR = 28;
	public static final int UMINUS = 29;
	public static final int UNION = 30;
	public static final int VAR = 31;
	public static final int WILDCARD = 32;
	
	public int type;
	public Object val;
	
	public Token (int type) {
		this(type, null);
	}
	
	public Token (int type, Object val) {
		this.type = type;
		this.val = val;
	}

	public String toString () {
		String s = null;
		
		switch (type) {
		case AND: s = "AND"; break;
		case AT: s = "AT"; break;
		case COMMA: s = "COMMA"; break;
		case DBL_COLON: s = "DBL_COLON"; break;
		case DBL_DOT: s = "DBL_DOT"; break;
		case DBL_SLASH: s = "DBL_SLASH"; break;
		case DIV: s = "DIV"; break;
		case DOT: s = "DOT"; break;
		case EQ: s = "EQ"; break;
		case GT: s = "GT"; break;
		case GTE: s = "GTE"; break;
		case LBRACK: s = "LBRACK"; break;
		case LPAREN: s = "LPAREN"; break;
		case LT: s = "LT"; break;
		case LTE: s = "LTE"; break;
		case MINUS: s = "MINUS"; break;
		case MOD: s = "MOD"; break;
		case MULT: s = "MULT"; break;
		case NEQ: s = "NEQ"; break;
		case NSWILDCARD: s = "NSWILDCARD(" + (String)val + ")"; break;
		case NUM: s = "NUM(" + ((Double)val).toString() + ")"; break;
		case OR: s = "OR"; break;
		case PLUS: s = "PLUS"; break;
		case QNAME: s = "QNAME(" + ((XPathQName)val).toString() + ")"; break;
		case RBRACK: s = "RBRACK"; break;
		case RPAREN: s = "RPAREN"; break;
		case SLASH: s = "SLASH"; break;
		case STR: s = "STR(" + (String)val + ")"; break;
		case UMINUS: s = "UMINUS"; break;
		case UNION: s = "UNION"; break;
		case VAR: s = "VAR(" + ((XPathQName)val).toString() + ")"; break;
		case WILDCARD: s = "WILDCARD"; break;		
		}
		
		return s;
	}
}
