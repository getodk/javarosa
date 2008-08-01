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
		
		try {
			XPathParser p = new XPathParser(new XPathLexer(new InputStreamReader(bais)));
			XPathExpression root = (XPathExpression)p.parse().value;      
			root.printParseTree();
		} catch (Exception e) {
			System.err.println("   ...XPath Parsing Exception");
		}
	}
	
	public static void test () {
		System.out.println("xpath tester here i am");
		
		testXPath("2+3+4+5");
	}
}


