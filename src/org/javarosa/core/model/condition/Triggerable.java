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

package org.javarosa.core.model.condition;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.List;
import java.util.Set;

import org.javarosa.core.model.QuickTriggerable;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapList;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.debug.EvaluationResult;
import org.javarosa.xpath.XPathException;

/**
 * A triggerable represents an action that should be processed based
 * on a value updating in a model. Trigerrables are comprised of two
 * basic components: An expression to be evaluated, and a reference
 * which represents where the resultant value will be stored.
 *
 * A triggerable will dispatch the action it's performing out to
 * all relevant nodes referenced by the context against thes current
 * models.
 *
 *
 * @author ctsims
 *
 */
public abstract class Triggerable implements Externalizable {
   public static final Comparator<Triggerable> triggerablesRootOrdering = new Comparator<Triggerable>() {
      @Override
      public int compare(Triggerable lhs, Triggerable rhs) {
        int cmp;
        cmp = lhs.contextRef.toString(false).compareTo(rhs.contextRef.toString(false));
        if ( cmp != 0 ) {
          return cmp;
        }
        cmp = lhs.originalContextRef.toString(false).compareTo(rhs.originalContextRef.toString(false));
        if ( cmp != 0 ) {
          return cmp;
        }
        
        // bias toward cascading targets....
        if ( lhs.isCascadingToChildren() ) {
          if (!rhs.isCascadingToChildren() ) {
            return -1;
          }
        } else if ( rhs.isCascadingToChildren() ) {
          return 1;
        }
        
        int lhsHash = lhs.hashCode();
        int rhsHash = rhs.hashCode();
        return (lhsHash < rhsHash) ? -1 : ((lhsHash == rhsHash) ? 0 : 1);
      }
   };
	/**
	 * The expression which will be evaluated to produce a result
	 */
	private IConditionExpr expr;

	/**
	 * References to all of the (non-contextualized) nodes which should be
	 * updated by the result of this triggerable
	 *
	 */
	private List<TreeReference> targets;

	/**
	 * Current reference which is the "Basis" of the trigerrables being evaluated. This is the highest
	 * common root of all of the targets being evaluated.
	 */
	private TreeReference contextRef;  //generic ref used to turn triggers into absolute references

	/**
	 * The first context provided to this triggerable before reducing to the common root.
	 */
   private TreeReference originalContextRef;

   private int waveCount = 0;
   
   private HashSet<QuickTriggerable> immediateCascades = null;
   
   public void setImmediateCascades(HashSet<QuickTriggerable> cascades) {
     immediateCascades = new HashSet<QuickTriggerable>(cascades);
   }
   
   public HashSet<QuickTriggerable> getImmediateCascades() {
     return immediateCascades;
   }
   
	public Triggerable () {

	}

   public Triggerable (IConditionExpr expr, TreeReference contextRef, ArrayList<TreeReference> targets) {
     this.expr = expr;
     this.contextRef = contextRef;
     this.originalContextRef = contextRef;
     this.targets = targets;
   }

   public Triggerable (IConditionExpr expr, TreeReference contextRef) {
     this(expr, contextRef, new ArrayList<TreeReference>(0));
   }

	protected abstract Object eval (FormInstance instance, EvaluationContext ec);

	protected abstract void apply (TreeReference ref, Object result, FormInstance mainInstance);

	public abstract boolean canCascade ();

	/**
	 * Not for re-implementation, dispatches all of the evaluation
	 * @param mainInstance
	 * @param parentContext
	 * @param context
	 */
	public final List<EvaluationResult> apply (FormInstance mainInstance, EvaluationContext parentContext, TreeReference context) {
		//The triggeringRoot is the highest level of actual data we can inquire about, but it _isn't_ necessarily the basis
		//for the actual expressions, so we need genericize that ref against the current context
		TreeReference ungenericised = originalContextRef.contextualize(context);
		EvaluationContext ec = new EvaluationContext(parentContext, ungenericised);

		Object result = eval(mainInstance, ec);

		List<EvaluationResult> affectedNodes = new ArrayList<EvaluationResult>(0);
		for (TreeReference target : targets) {
			TreeReference targetRef = target.contextualize(ec.getContextRef());
			List<TreeReference> v = ec.expandReference(targetRef);

			for (TreeReference affectedRef : v) {
				apply(affectedRef, result, mainInstance);

				affectedNodes.add(new EvaluationResult(affectedRef, result));
			}
		}

		return affectedNodes;
	}

	public IConditionExpr getExpr() {
		return expr;
	}
	
	public void addTarget (TreeReference target) {
		if (targets.indexOf(target) == -1) {
			targets.add(target);
		}
	}

	public List<TreeReference> getTargets () {
		return targets;
	}

	public void setWaveCount(int waveCount) {
	  this.waveCount = waveCount;
	}
	
	/**
	 * This should return true if this triggerable's targets will implicity modify the
	 * value of their children. IE: if this triggerable makes a node relevant/irrelevant,
	 * expressions which care about the value of this node's children should be triggered.
	 *
	 * @return True if this condition should trigger expressions whose targets include
	 * nodes which are the children of this node's targets.
	 */
	public boolean isCascadingToChildren() {
		return false;
	}

	public Set<TreeReference> getTriggers () {
		Set<TreeReference> relTriggers = expr.getTriggers(null);  /// should this be originalContextRef???
		Set<TreeReference> absTriggers = new HashSet<TreeReference>();
		for (TreeReference r : relTriggers ) {
			absTriggers.add(r.anchor(originalContextRef));
		}
		return absTriggers;
	}

	Boolean evalPredicate(FormInstance model, EvaluationContext evalContext) {
		try {
			return new Boolean(expr.eval(model, evalContext));
		} catch (XPathException e) {
			e.setSource("Relevant expression for " + contextRef.toString(true));
			throw e;
		}
	}

	Object evalRaw(FormInstance model, EvaluationContext evalContext) {
		try {
			return expr.evalRaw(model, evalContext);
		} catch (XPathException e) {
			e.setSource("calculate expression for " + contextRef.toString(true));
			throw e;
		}
	}

	public void changeContextRefToIntersectWithTriggerable(Triggerable t) {
		contextRef = contextRef.intersect(t.contextRef);
	}

	public TreeReference contextualizeContextRef(TreeReference anchorRef) {
		// Contextualize the reference used by the triggerable against
		// the anchor
		return contextRef.contextualize(anchorRef);
	}

	public boolean equals (Object o) {
		if (o instanceof Triggerable) {
			Triggerable t = (Triggerable)o;
			if (this == t)
				return true;

			if (this.expr.equals(t.expr)) {

				// The original logic did not make any sense --
				// the
				try {
					// resolved triggers should match...
					Set<TreeReference> Atriggers = this.getTriggers();
					Set<TreeReference> Btriggers = t.getTriggers();

					return (Atriggers.size() == Btriggers.size()) &&
							Atriggers.containsAll(Btriggers);
				} catch (XPathException e) {
					return false;
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		expr = (IConditionExpr)ExtUtil.read(in, new ExtWrapTagged(), pf);
		contextRef = (TreeReference)ExtUtil.read(in, TreeReference.class, pf);
		originalContextRef = (TreeReference)ExtUtil.read(in, TreeReference.class, pf);
      List<TreeReference> tlist = (List<TreeReference>)ExtUtil.read(in, new ExtWrapList(TreeReference.class), pf);
		targets = new ArrayList<TreeReference>(tlist); 
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.write(out, new ExtWrapTagged(expr));
		ExtUtil.write(out, contextRef);
		ExtUtil.write(out, originalContextRef);
      List<TreeReference> tlist = new ArrayList<TreeReference>(targets);
		ExtUtil.write(out, new ExtWrapList(tlist));
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < targets.size(); i++) {
			sb.append(targets.get(i).toString());
			if (i < targets.size() - 1)
				sb.append(",");
		}
		return "trig[expr:" + expr.toString() + ";targets[" + sb.toString() + "]]";
	}

	public void print(OutputStreamWriter w) throws IOException {
		w.write("   waveCount: " + Integer.toString(waveCount) + "\n");
		w.write("   isCascading: "
				+ (isCascadingToChildren() ? "true" : "false") + "\n");
		w.write("   expr: " + expr.toString() + "\n");
		w.write("   contextRef: "
				+ ((contextRef != null) ? contextRef.toString(true) : "null")
				+ "\n");
		w.write("   originalContextRef: "
				+ ((originalContextRef != null) ? originalContextRef
						.toString(true) : "null") + "\n");
		int j;
		for (j = 0; j < getTargets().size(); ++j) {
			TreeReference r = getTargets().get(j);
			w.write("   targets[" + Integer.toString(j) + "] :"
					+ r.toString(true) + "\n");
		}
	}

	/**
	 * Searches in the triggers of this Triggerable, trying to find the ones that are
	 * contained in the given list of contextualized refs.
	 *
	 * @param firedAnchorsMap a map of absolute refs
	 * @return a list of affected nodes.
	 */
	public List<TreeReference> findAffectedTriggers(Map<TreeReference, List<TreeReference>> firedAnchorsMap) {
		List<TreeReference> affectedTriggers = new ArrayList<TreeReference>(0);

		Set<TreeReference> triggers = this.getTriggers();
		for (TreeReference trigger : triggers) {
			List<TreeReference> firedAnchors = firedAnchorsMap.get(trigger.genericize());
			if (firedAnchors == null) {
				continue;
			}
			
			affectedTriggers.addAll(firedAnchors);
		}

		return affectedTriggers;
	}
}
