package org.javarosa.xpath.expr;

public class XPathStep {
	public static final int AXIS_CHILD = 0;
	public static final int AXIS_DESCENDANT = 1;
	public static final int AXIS_PARENT = 2;
	public static final int AXIS_ANCESTOR = 3;
	public static final int AXIS_FOLLOWING_SIBLING = 4;
	public static final int AXIS_PRECEDING_SIBLING = 5;
	public static final int AXIS_FOLLOWING = 6;
	public static final int AXIS_PRECEDING = 7;
	public static final int AXIS_ATTRIBUTE = 8;
	public static final int AXIS_NAMESPACE = 9;
	public static final int AXIS_SELF = 10;
	public static final int AXIS_DESCENDANT_OR_SELF = 11;
	public static final int AXIS_ANCESTOR_OR_SELF = 12;

	public static final int TEST_NAME = 0;
	public static final int TEST_NAME_WILDCARD = 1;
	public static final int TEST_NAMESPACE_WILDCARD = 2;
	public static final int TEST_TYPE_NODE = 3;
	public static final int TEST_TYPE_TEXT = 4;
	public static final int TEST_TYPE_COMMENT = 5;
	public static final int TEST_TYPE_PROCESSING_INSTRUCTION = 6;

	public static XPathStep ABBR_SELF () {
		return new XPathStep(AXIS_SELF, TEST_TYPE_NODE);
	}

	public static XPathStep ABBR_PARENT () {
		return new XPathStep(AXIS_PARENT, TEST_TYPE_NODE);
	}

	public static XPathStep ABBR_DESCENDANTS () {
		return new XPathStep(AXIS_DESCENDANT_OR_SELF, TEST_TYPE_NODE);
	}

	public int axis;
	public int test;
	public XPathExpression[] predicates;

	//test-dependent variables
	public XPathQName name; //TEST_NAME only
	public String namespace; //TEST_NAMESPACE_WILDCARD only
	public String literal; //TEST_TYPE_PROCESSING_INSTRUCTION only

	public XPathStep (int axis, int test) {
		this.axis = axis;
		this.test = test;
		this.predicates = new XPathExpression[0];
	}

	public XPathStep (int axis, XPathQName name) {
		this(axis, TEST_NAME);
		this.name = name;
	}

	public XPathStep (int axis, String namespace) {
		this(axis, TEST_NAMESPACE_WILDCARD);
		this.namespace = namespace;
	}
}
