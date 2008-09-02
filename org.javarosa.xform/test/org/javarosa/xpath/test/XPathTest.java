package org.javarosa.xpath.test;

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

import org.javarosa.xpath.XPathParseTool;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.parser.XPathSyntaxException;


public class XPathTest extends TestCase {
	public XPathTest(String name, TestMethod rTestMethod) {
		super(name, rTestMethod);
	}
	
	public XPathTest(String name) {
		super(name);
	}
	
	public XPathTest() {
		super();
	}	
	
	public Test suite() {
		TestSuite aSuite = new TestSuite();
		aSuite.addTest(new XPathTest("XPath Parsing Tests", new TestMethod() {
			public void run (TestCase tc) {
				((XPathTest)tc).testParseSamples();
			}
		}));
		return aSuite;
	}
	
	private void testXPathValid (String expr, String expected) {
		//debug
		//System.out.println("+[ " + expr + " ]");
		
		try {
			XPathExpression xpe = XPathParseTool.parseXPath(expr);
			String result = (xpe != null ? xpe.toString() : null);
			
			//debug
			//System.out.println("[ " + result + " ]");
			
			if (result == null || !result.equals(expected)) {
				this.fail("XPath Parse Failed! Incorrect parse tree." +
							"\n    expression:[" + expr + "]" +
							"\n    expected:[" + expected + "]" +
							"\n    result:[" + result + "]");
			}
		} catch (XPathSyntaxException xse) {
			this.fail("XPath Parse Failed! Unexpected syntax error." + 
						"\n    expression:[" + expr + "]");
		}
	}
	
	private void testXPathInvalid (String expr) {
		//debug
		//System.out.println("+[ " + expr + " ]");
		
		try {
			XPathExpression xpe = XPathParseTool.parseXPath(expr);
			String result = (xpe != null ? xpe.toString() : null);
			
			this.fail("XPath Parse Failed! Did not get syntax error as expected." + 
						"\n    expression:[" + expr + "]" +
						"\n    result:[" + (result == null ? "(null)" : result) + "]");
		} catch (XPathSyntaxException xse) {
			//success: syntax error as expected
		}
	}

	public void testParseSamples () {
		testXPathInvalid("");
		testXPathInvalid("     ");
		testXPathInvalid("  \t \n  \r ");
		testXPathValid("10", "{num:10.0}");
		testXPathValid("123.", "{num:123.0}");
		testXPathValid("734.04", "{num:734.04}");
		testXPathValid("0.12345", "{num:0.12345}");
		testXPathValid(".666", "{num:0.666}");
		testXPathValid("00000333.3330000", "{num:333.333}");
		testXPathValid("0", "{num:0.0}");
		testXPathValid("0.", "{num:0.0}");
		testXPathValid(".0", "{num:0.0}");
		testXPathValid("0.0", "{num:0.0}");
		testXPathValid("\"\"", "{str:''}");
		testXPathValid("\"   \"", "{str:'   '}");
		testXPathValid("''", "{str:''}");
		testXPathValid("'\"'", "{str:'\"'}");
		testXPathValid("\"'\"", "{str:'''}");
		testXPathValid("'mary had a little lamb'", "{str:'mary had a little lamb'}");
		testXPathValid("$var", "{var:var}");
		testXPathValid("$qualified:name", "{var:qualified:name}");
		testXPathInvalid("$x:*");
		testXPathInvalid("$");
		testXPathInvalid("$$asdf");
		testXPathValid("(5)", "{num:5.0}");
		testXPathValid("(( (( (5 )) )))  ", "{num:5.0}");
		testXPathInvalid(")");
		testXPathInvalid("(");
		testXPathInvalid("()");
		testXPathInvalid("(((3))");
		testXPathValid("(5)", "{num:5.0}");
		testXPathValid("5 + 5", "{binop-expr:+,{num:5.0},{num:5.0}}");
		testXPathValid("-5", "{unop-expr:num-neg,{num:5.0}}");
		testXPathValid("- 5", "{unop-expr:num-neg,{num:5.0}}");
		testXPathValid("----5", "{unop-expr:num-neg,{unop-expr:num-neg,{unop-expr:num-neg,{unop-expr:num-neg,{num:5.0}}}}}");
		testXPathValid("6 * - 7", "{binop-expr:*,{num:6.0},{unop-expr:num-neg,{num:7.0}}}");
		testXPathValid("0--0", "{binop-expr:-,{num:0.0},{unop-expr:num-neg,{num:0.0}}}");		
		testXPathInvalid("+-");
		testXPathValid("5 * 5", "{binop-expr:*,{num:5.0},{num:5.0}}");
		testXPathValid("5 div 5", "{binop-expr:/,{num:5.0},{num:5.0}}");
		testXPathInvalid("5/5");
		testXPathValid("5 mod 5", "{binop-expr:%,{num:5.0},{num:5.0}}");
		testXPathInvalid("5%5");
		testXPathValid("3mod4", "{binop-expr:%,{num:3.0},{num:4.0}}");
		testXPathValid("5 divseparate-token", "{binop-expr:/,{num:5.0},{path-expr:rel,{{step:child,separate-token}}}}"); //not quite sure if this is legal xpath or not, but it *can* be parsed unambiguously
		testXPathValid("5 = 5", "{binop-expr:==,{num:5.0},{num:5.0}}");
		testXPathValid("5 != 5", "{binop-expr:!=,{num:5.0},{num:5.0}}");
		testXPathInvalid("5 == 5");
		testXPathInvalid("5 <> 5");
		testXPathValid("5 < 5", "{binop-expr:<,{num:5.0},{num:5.0}}");
		testXPathValid("5 <= 5", "{binop-expr:<=,{num:5.0},{num:5.0}}");
		testXPathValid("5 > 5", "{binop-expr:>,{num:5.0},{num:5.0}}");
		testXPathValid("5 >= 5", "{binop-expr:>=,{num:5.0},{num:5.0}}");
		testXPathInvalid(">=");
		testXPathInvalid("'asdf'!=");
		testXPathValid("5 and 5", "{binop-expr:and,{num:5.0},{num:5.0}}");
		testXPathValid("5 or 5", "{binop-expr:or,{num:5.0},{num:5.0}}");
		testXPathValid("5 | 5", "{binop-expr:union,{num:5.0},{num:5.0}}");
		testXPathValid("1 or 2 or 3", "{binop-expr:or,{num:1.0},{binop-expr:or,{num:2.0},{num:3.0}}}");
		testXPathValid("1 and 2 and 3", "{binop-expr:and,{num:1.0},{binop-expr:and,{num:2.0},{num:3.0}}}");
		testXPathValid("1 = 2 != 3 != 4 = 5", "{binop-expr:==,{binop-expr:!=,{binop-expr:!=,{binop-expr:==,{num:1.0},{num:2.0}},{num:3.0}},{num:4.0}},{num:5.0}}");
		testXPathValid("1 < 2 >= 3 <= 4 > 5", "{binop-expr:>,{binop-expr:<=,{binop-expr:>=,{binop-expr:<,{num:1.0},{num:2.0}},{num:3.0}},{num:4.0}},{num:5.0}}");
		testXPathValid("1 + 2 - 3 - 4 + 5", "{binop-expr:+,{binop-expr:-,{binop-expr:-,{binop-expr:+,{num:1.0},{num:2.0}},{num:3.0}},{num:4.0}},{num:5.0}}");
		testXPathValid("1 mod 2 div 3 div 4 * 5", "{binop-expr:*,{binop-expr:/,{binop-expr:/,{binop-expr:%,{num:1.0},{num:2.0}},{num:3.0}},{num:4.0}},{num:5.0}}");
		testXPathValid("1|2|3", "{binop-expr:union,{binop-expr:union,{num:1.0},{num:2.0}},{num:3.0}}");
		testXPathValid("1 < 2 = 3 > 4 and 5 <= 6 != 7 >= 8 or 9 and 10",
				"{binop-expr:or,{binop-expr:and,{binop-expr:==,{binop-expr:<,{num:1.0},{num:2.0}},{binop-expr:>,{num:3.0},{num:4.0}}},{binop-expr:!=,{binop-expr:<=,{num:5.0},{num:6.0}},{binop-expr:>=,{num:7.0},{num:8.0}}}},{binop-expr:and,{num:9.0},{num:10.0}}}");
		testXPathValid("1 * 2 + 3 div 4 < 5 mod 6 | 7 - 8",
				"{binop-expr:<,{binop-expr:+,{binop-expr:*,{num:1.0},{num:2.0}},{binop-expr:/,{num:3.0},{num:4.0}}},{binop-expr:-,{binop-expr:%,{num:5.0},{binop-expr:union,{num:6.0},{num:7.0}}},{num:8.0}}}");
		testXPathValid("- 4 * 6", "{binop-expr:*,{unop-expr:num-neg,{num:4.0}},{num:6.0}}");
		testXPathInvalid("8|-9"); //seems to be disallowed by the xpath spec
		testXPathValid("6*(3+4)and(5or2)", "{binop-expr:and,{binop-expr:*,{num:6.0},{binop-expr:+,{num:3.0},{num:4.0}}},{binop-expr:or,{num:5.0},{num:2.0}}}");
		testXPathValid("function()", "{func-expr:function,{}}");
		testXPathValid("func:tion()", "{func-expr:func:tion,{}}");
		testXPathValid("function(   )", "{func-expr:function,{}}");
		testXPathValid("function (5)", "{func-expr:function,{{num:5.0}}}");
		testXPathValid("function   ( 5, 'arg', 4 * 12)", "{func-expr:function,{{num:5.0},{str:'arg'},{binop-expr:*,{num:4.0},{num:12.0}}}}");
		testXPathInvalid("function ( 4, 5, 6 ");
		testXPathValid("4andfunc()", "{binop-expr:and,{num:4.0},{func-expr:func,{}}}");		
		testXPathValid("node()", "{path-expr:rel,{{step:child,node()}}}");
		testXPathValid("text()", "{path-expr:rel,{{step:child,text()}}}");
		testXPathValid("comment()", "{path-expr:rel,{{step:child,comment()}}}");
		testXPathValid("processing-instruction()", "{path-expr:rel,{{step:child,proc-instr()}}}");
		testXPathValid("processing-instruction('asdf')", "{path-expr:rel,{{step:child,proc-instr('asdf')}}}");
		testXPathInvalid("node(5)");
		testXPathInvalid("text('str')");
		testXPathInvalid("comment(name)");
		testXPathInvalid("processing-instruction(5)");
		testXPathInvalid("processing-instruction('asdf','qwer')");
		testXPathValid("bunch-o-nodes()[3]", "{filt-expr:{func-expr:bunch-o-nodes,{}},{{num:3.0}}}");
		testXPathValid("bunch-o-nodes()[3]['predicates'!='galore']", "{filt-expr:{func-expr:bunch-o-nodes,{}},{{num:3.0},{binop-expr:!=,{str:'predicates'},{str:'galore'}}}}");
		testXPathValid(".", "{path-expr:rel,{{step:self,node()}}}");
		testXPathValid("..", "{path-expr:rel,{{step:parent,node()}}}");
		testXPathValid("name", "{path-expr:rel,{{step:child,name}}}");
		testXPathValid("qual:name", "{path-expr:rel,{{step:child,qual:name}}}");
		testXPathValid("namespace:*", "{path-expr:rel,{{step:child,namespace:*}}}");
		testXPathValid("*", "{path-expr:rel,{{step:child,*}}}");
		testXPathValid("*****", "{binop-expr:*,{binop-expr:*,{path-expr:rel,{{step:child,*}}},{path-expr:rel,{{step:child,*}}}},{path-expr:rel,{{step:child,*}}}}");
		testXPathValid("_rea--ll:y.funk..y_N4M3", "{path-expr:rel,{{step:child,_rea--ll:y.funk..y_N4M3}}}");
		testXPathInvalid("a:b:c");
		testXPathInvalid("inv#lid_N~AME");
		testXPathInvalid(".abc");
		testXPathInvalid("5abc");
		testXPathValid("child::*", "{path-expr:rel,{{step:child,*}}}");
		testXPathValid("parent::*", "{path-expr:rel,{{step:parent,*}}}");
		testXPathValid("descendant::*", "{path-expr:rel,{{step:descendant,*}}}");
		testXPathValid("ancestor::*", "{path-expr:rel,{{step:ancestor,*}}}");
		testXPathValid("following-sibling::*", "{path-expr:rel,{{step:following-sibling,*}}}");
		testXPathValid("preceding-sibling::*", "{path-expr:rel,{{step:preceding-sibling,*}}}");
		testXPathValid("following::*", "{path-expr:rel,{{step:following,*}}}");
		testXPathValid("preceding::*", "{path-expr:rel,{{step:preceding,*}}}");
		testXPathValid("attribute::*", "{path-expr:rel,{{step:attribute,*}}}");
		testXPathValid("namespace::*", "{path-expr:rel,{{step:namespace,*}}}");
		testXPathValid("self::*", "{path-expr:rel,{{step:self,*}}}");
		testXPathValid("descendant-or-self::*", "{path-expr:rel,{{step:descendant-or-self,*}}}");
		testXPathValid("ancestor-or-self::*", "{path-expr:rel,{{step:ancestor-or-self,*}}}");
		testXPathInvalid("bad-axis::*");
		testXPathInvalid("::*");
		testXPathInvalid("child::.");
		testXPathInvalid("..[4]");
		testXPathValid("@attr", "{path-expr:rel,{{step:attribute,attr}}}");
		testXPathValid("@*", "{path-expr:rel,{{step:attribute,*}}}");
		testXPathValid("@ns:*", "{path-expr:rel,{{step:attribute,ns:*}}}");
		testXPathInvalid("@attr::*");
		testXPathInvalid("child::func()");
		testXPathValid("descendant::node()[@attr='blah'][4]", "{path-expr:rel,{{step:descendant,node(),{{binop-expr:==,{path-expr:rel,{{step:attribute,attr}}},{str:'blah'}},{num:4.0}}}}}");
		testXPathValid("rel/ative/path", "{path-expr:rel,{{step:child,rel},{step:child,ative},{step:child,path}}}");
		testXPathInvalid("rel/ative/path/");
		testXPathValid("/abs/olute/path['etc']", "{path-expr:abs,{{step:child,abs},{step:child,olute},{step:child,path,{{str:'etc'}}}}}");
		testXPathValid("filter()/expr/path", "{path-expr:{filt-expr:{func-expr:filter,{}},{}},{{step:child,expr},{step:child,path}}}");
		testXPathValid("/", "{path-expr:abs,{}}");
		testXPathInvalid("//");
		testXPathValid("//all", "{path-expr:abs,{{step:descendant-or-self,node()},{step:child,all}}}");
		testXPathValid("a/.//../z", "{path-expr:rel,{{step:child,a},{step:self,node()},{step:descendant-or-self,node()},{step:parent,node()},{step:child,z}}}");
		
		testXPathValid("/patient/sex = 'male' and /patient/age > 15",
				"{binop-expr:and,{binop-expr:==,{path-expr:abs,{{step:child,patient},{step:child,sex}}},{str:'male'}},{binop-expr:>,{path-expr:abs,{{step:child,patient},{step:child,age}}},{num:15.0}}}");
		testXPathValid("../jr:hist-data/labs[@type=\"cd4\"]",
				"{path-expr:rel,{{step:parent,node()},{step:child,jr:hist-data},{step:child,labs,{{binop-expr:==,{path-expr:rel,{{step:attribute,type}}},{str:'cd4'}}}}}}");
		testXPathValid("function_call(26*(7+3), //*, /im/child::an/ancestor::x[3][true()]/path)",
				"{func-expr:function_call,{{binop-expr:*,{num:26.0},{binop-expr:+,{num:7.0},{num:3.0}}},{path-expr:abs,{{step:descendant-or-self,node()},{step:child,*}}},{path-expr:abs,{{step:child,im},{step:child,an},{step:ancestor,x,{{num:3.0},{func-expr:true,{}}}},{step:child,path}}}}}");
	}	
}


