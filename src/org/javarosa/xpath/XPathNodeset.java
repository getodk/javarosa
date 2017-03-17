package org.javarosa.xpath;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.expr.XPathPathExpr;

import java.util.List;

/**
 * Represents a set of XPath nodes returned from a path or other operation which acts on multiple
 * paths.
 *
 * Current encompasses two states.
 *
 * 1) A nodeset which references between 0 and N nodes which are known about (but, for instance,
 * don't match any predicates or are irrelevant). Some operations cannot be evaluated in this state
 * directly. If more than one node is referenced, it is impossible to return a normal evaluation, for
 * instance.
 *
 * 2) A nodeset which wasn't able to reference into any known model (generally a reference which is
 * written in error). In this state, the size of the nodeset can be evaluated, but the acual reference
 * cannot be returned, since it doesn't have any semantic value.
 *
 * (2) may be a deviation from normal XPath. This should be evaluated in the future.
 *
 * @author ctsims
 *
 */
public class XPathNodeset {

	private List<TreeReference> nodes;
	protected FormInstance instance;
	protected EvaluationContext ec;
	// these are purely for improved error messages
	private String pathEvaluated;
	private String originalPath;

	private XPathNodeset() {

	}

	/**
	 * for lazy evaluation
	 *
	 * @param instance
	 * @param ec
	 */
	protected XPathNodeset (FormInstance instance, EvaluationContext ec) {
		this.instance = instance;
		this.ec = ec;
	}


	/**
	 * Construct an XPath nodeset.
	 *
	 * @param nodes
	 * @param instance
	 * @param ec
	 */
	public XPathNodeset (List<TreeReference> nodes, FormInstance instance, EvaluationContext ec) {
		if(nodes == null) { throw new NullPointerException("Node list cannot be null when constructing a nodeset"); }
		this.nodes = nodes;
		this.instance = instance;
		this.ec = ec;
	}

	public static XPathNodeset ConstructInvalidPathNodeset(String pathEvaluated, String originalPath) {
		XPathNodeset nodeset = new XPathNodeset();
		nodeset.nodes = null;
		nodeset.instance = null;
		nodeset.ec = null;
		nodeset.pathEvaluated = pathEvaluated;
		nodeset.originalPath = originalPath;
		return nodeset;
	}

	protected void setReferences(List<TreeReference> nodes) {
		this.nodes = nodes;
	}

	protected List<TreeReference> getReferences() {
		return this.nodes;
	}


	/**
	 * @return The value represented by this xpath. Can only be evaluated when this xpath represents exactly one
	 * reference, or when it represents 0 references after a filtering operation (a reference which _could_ have
	 * existed, but didn't, rather than a reference which could not represent a real node).
	 */
	public Object unpack () {
		if(nodes == null) {
			throw getInvalidNodesetException();
		}

		if (size() == 0) {
			return XPathPathExpr.unpackValue(null);
		} else if (size() > 1) {
			throw new XPathTypeMismatchException("This field is repeated: \n\n" + nodeContents() + "\n\nYou may need to use the indexed-repeat() function to specify which value you want.");
		} else {
			return getValAt(0);
		}
	}

	public Object[] toArgList () {
		if(nodes == null) {
			throw getInvalidNodesetException();
		}

		Object[] args = new Object[size()];

		for (int i = 0; i < size(); i++) {
			Object val = getValAt(i);

			//sanity check
			if (val == null) {
				throw new RuntimeException("retrived a null value out of a nodeset! shouldn't happen!");
			}

			args[i] = val;
		}

		return args;
	}

	public int size () {
		if(nodes == null) {
			return 0;
		}
		return nodes.size();
	}

	public TreeReference getRefAt (int i) {
		if(nodes == null) {
			throw getInvalidNodesetException();
		}

		return nodes.get(i);
	}

	public Object getValAt (int i) {
		return XPathPathExpr.getRefValue(instance, ec, getRefAt(i));
	}

	protected XPathTypeMismatchException getInvalidNodesetException() {
		if(!pathEvaluated.equals(originalPath)) {
			throw new XPathTypeMismatchException("The path " + originalPath + " refers to the location " + pathEvaluated + " which was not found");
		} else {
			throw new XPathTypeMismatchException("Location " + pathEvaluated + " was not found");
		}
	}

	protected String nodeContents () {
		if(nodes == null) {
			return "Invalid Path: " + pathEvaluated;
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < nodes.size(); i++) {
			sb.append(nodes.get(i).toString());
			if (i < nodes.size() - 1) {
                sb.append(";");
			}
		}
		return sb.toString();
	}
}
