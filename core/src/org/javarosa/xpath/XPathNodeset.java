package org.javarosa.xpath;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.expr.XPathPathExpr;

import java.util.Vector;

public class XPathNodeset {

	Vector<TreeReference> nodes;
	FormInstance instance;
	EvaluationContext ec;
	
	public XPathNodeset (Vector<TreeReference> nodes, FormInstance instance, EvaluationContext ec) {
		this.nodes = nodes;
		this.instance = instance;
		this.ec = ec;
	}

	public Object unpack () {
		if (size() == 0) {
			return XPathPathExpr.unpackValue(null);
		} else if (size() > 1) {
			throw new XPathTypeMismatchException("This field is repeated: \n\n" + nodeContents() + "\n\nYou may need to use the indexed-repeat() function to specify which value you want.");
		} else {
			return getValAt(0);
		}
	}

	public Object[] toArgList () {
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
		return nodes.size();
	}
	
	public TreeReference getRefAt (int i) {
		return nodes.elementAt(i);
	}
	
	public Object getValAt (int i) {
		return XPathPathExpr.getRefValue(instance, ec, getRefAt(i));
	}
	
	private String nodeContents () {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < nodes.size(); i++) {
			sb.append(nodes.elementAt(i).toString());
			if (i < nodes.size() - 1) {
                sb.append("\n");
			}
		}
		return sb.toString();
	}
}
