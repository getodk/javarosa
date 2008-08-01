package org.javarosa.xpath;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.parser.XPathLexer;
import org.javarosa.xpath.parser.XPathParser;

public class XPathTest {
	private static void testXPath (String expr) {
		System.out.println("Parsing [" + expr + "]");
		ByteArrayInputStream bais = new ByteArrayInputStream(expr.getBytes());
		
		//need to figure out how to catch syntax errors
		try {
			XPathParser p = new XPathParser(new XPathLexer(new InputStreamReader(bais)));
			XPathExpression root = (XPathExpression)p.parse().value;      
			root.printParseTree();
		} catch (Exception e) {
			System.err.println("   ...XPath Syntax Error");
		} catch (Error e) {
			System.err.println("   ...XPath Syntax Error");
		}
	}
	
	public static void test () {
		System.out.println("xpath tester here i am");
		
		testXPath("/path/to/a/node");
		testXPath("/patient/sex = 'male' and /patient/age > 15");
		testXPath("../jr:hist-data/labs[@type=\"cd4\"]");
		testXPath("function_call(26*(7+3), //*, /im/child::an/descendant-or-self::x/path[3][true()])");
		testXPath("~ lexical error");
		testXPath("syntax + ) error");
	}
}


