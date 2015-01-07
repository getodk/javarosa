package org.javarosa.xpath;

import java.util.List;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.expr.XPathPathExpr;

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
public class XPathLazyNodeset extends XPathNodeset {

	Boolean evaluated = Boolean.FALSE;
	TreeReference unExpandedRef;

	/**
	 * Construct an XPath nodeset.
	 *
	 * @param nodes
	 * @param instance
	 * @param ec
	 */
	public XPathLazyNodeset (TreeReference unExpandedRef, FormInstance instance, EvaluationContext ec) {
		super(instance, ec);
		this.unExpandedRef = unExpandedRef;
	}


	private void performEvaluation() {
		synchronized(evaluated) {
			if(evaluated.booleanValue()) {
				return;
			}
         List<TreeReference> nodes = ec.expandReference(unExpandedRef);

			//to fix conditions based on non-relevant data, filter the nodeset by relevancy
			for (int i = 0; i < nodes.size(); i++) {
				if (!instance.resolveReference(nodes.get(i)).isRelevant()) {
					nodes.remove(i);
					i--;
				}
			}
			this.setReferences(nodes);
			evaluated = true;
		}
	}


	/**
	 * @return The value represented by this xpath. Can only be evaluated when this xpath represents exactly one
	 * reference, or when it represents 0 references after a filtering operation (a reference which _could_ have
	 * existed, but didn't, rather than a reference which could not represent a real node).
	 */
	public Object unpack () {
		synchronized(evaluated) {
			if(evaluated.booleanValue()) {
				return super.unpack();
			}

			//this element is the important one. For Basic nodeset evaluations (referring to one node with no
			//multiplicites) we should be able to do this without doing the expansion

			//first, see if this treeref is usable without expansion
			int size = unExpandedRef.size();
			boolean safe = true;;
			for(int i = 0 ;  i < size ; ++i) {
				//We can't evaluated any predicates for sure
				if(unExpandedRef.getPredicate(i) != null) {
					safe = false;
					break;
				}
				int mult = unExpandedRef.getMultiplicity(i);
				if(!(mult >= 0 || mult == TreeReference.INDEX_UNBOUND)) {
					safe = false;
					break;
				}
			}
			if(!safe) {
				performEvaluation();
				return super.unpack();
			}

			//TOOD: Evaluate error fallbacks, here. I don't know whether this handles the 0 case
			//the same way, although invalid multiplicities should be fine.
			try {
				//TODO: This doesn't handle templated nodes (repeats which may exist in the future)
				//figure out if we can roll that in easily. For now the catch handles it
				return XPathPathExpr.getRefValue(instance, ec, unExpandedRef);
			} catch(XPathException xpe) {
				//This isn't really a best effort attempt, so if we can, see if evaluating cleany works.
				performEvaluation();
				return super.unpack();
			}
		}
	}

	public Object[] toArgList () {
		performEvaluation();
		return super.toArgList();
	}

	protected List<TreeReference> getReferences() {
		performEvaluation();
		return super.getReferences();
	}

	public int size () {
		performEvaluation();
		return super.size();
	}

	public TreeReference getRefAt (int i) {
		performEvaluation();
		return super.getRefAt(i);
	}

	public Object getValAt (int i) {
		performEvaluation();
		return super.getValAt(i);
	}

	protected String nodeContents () {
		performEvaluation();
		return super.nodeContents();
	}
}
