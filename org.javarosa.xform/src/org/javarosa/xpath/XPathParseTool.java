package org.javarosa.xpath;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.parser.XPathLexer;
import org.javarosa.xpath.parser.XPathParser;
import org.javarosa.xpath.parser.XPathSyntaxException;

public class XPathParseTool {
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
}
