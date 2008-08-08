package org.javarosa.xpath;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.parser.XPathLexer;
import org.javarosa.xpath.parser.XPathParser;
import org.javarosa.xpath.parser.XPathSyntaxException;

/* if you get build errors about missing XPathParser and XPathLexer, run the ant target 'build-xpath-parser'
 * in the build.xml file for this project
 */

public class XPathTest {
	public static XPathExpression parseXPath (String xpath) throws XPathSyntaxException {
		XPathExpression root;
		ByteArrayInputStream bais = new ByteArrayInputStream(xpath.getBytes());
		try {
			XPathParser p = new XPathParser(new XPathLexer(new InputStreamReader(bais)));
			root = (XPathExpression)p.parse().value;
			
		} catch (Exception e) {
			throw new XPathSyntaxException();
		} catch (Error e) {
			throw new XPathSyntaxException();
		}
		
		return root;
	}
	
	private static void testXPath (String expr) {
		System.out.println("Parsing [" + expr + "]");
		try {
			parseXPath(expr).printParseTree();
		} catch (XPathSyntaxException xse) {
			System.out.println("  ... syntax error!");
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


