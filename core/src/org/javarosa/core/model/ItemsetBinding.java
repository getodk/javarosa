package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import org.javarosa.core.model.condition.IConditionExpr;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.model.util.restorable.RestoreUtils;
import org.javarosa.core.services.locale.Localizable;
import org.javarosa.core.services.locale.Localizer;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.model.xform.XPathReference;
import org.javarosa.xpath.XPathConditional;
import org.javarosa.xpath.expr.XPathPathExpr;

public class ItemsetBinding implements Externalizable, Localizable {

	/**
	 * note that storing both the ref and expr for everything is kind of redundant, but we're forced
	 * to since it's nearly impossible to convert between the two w/o having access to the underlying
	 * xform/xpath classes, which we don't from the core model project
	 */
	
	public TreeReference nodesetRef;   //absolute ref of itemset source nodes
	public IConditionExpr nodesetExpr; //path expression for source nodes; may be relative, may contain predicates
	public TreeReference contextRef;   //context ref for nodesetExpr; ref of the control parent (group/formdef) of itemset question
	   //note: this is only here because its currently impossible to both (a) get a form control's parent, and (b)
       //convert expressions into refs while preserving predicates. once these are fixed, this field can go away
	
	public TreeReference labelRef;     //absolute ref of label
	public IConditionExpr labelExpr;   //path expression for label; may be relative, no predicates  
	public boolean labelIsItext;       //if true, content of 'label' is an itext id
	
	public boolean copyMode;           //true = copy subtree; false = copy string value
	public IConditionExpr copyExpr;    // path expression for copy; may be relative, no predicates
	public TreeReference copyRef;      //absolute ref to copy
	public TreeReference valueRef;     //absolute ref to value
	public IConditionExpr valueExpr;   //path expression for value; may be relative, no predicates (must be relative if copy mode)

	private TreeReference destRef; //ref that identifies the repeated nodes resulting from this itemset
								   //not serialized -- set by QuestionDef.setDynamicChoices()
	private List<SelectChoice> choices; //dynamic choices -- not serialized, obviously
	
	public List<SelectChoice> getChoices () {
		return choices;
	}
	
	public void setChoices (List<SelectChoice> choices, Localizer localizer) {
		if (this.choices != null) {
			System.out.println("warning: previous choices not cleared out");
			clearChoices();
		}
		this.choices = choices;
		
		//init localization
		if (localizer != null) {
			String curLocale = localizer.getLocale();
			if (curLocale != null) {
				localeChanged(curLocale, localizer);
			}
		}
	}
	
	public void clearChoices () {
		this.choices = null;
	}
	
	public void localeChanged(String locale, Localizer localizer) {
		if (choices != null) {
			for (int i = 0; i < choices.size(); i++) {
				choices.get(i).localeChanged(locale, localizer);
			}
		}
	}
	
	public TreeReference getDestRef () {
		return destRef;
	}
	
	public IConditionExpr getRelativeValue () {
		TreeReference relRef = null;
		
		if (copyRef == null) {
			relRef = valueRef; //must be absolute in this case
		} else if (valueRef != null) {
			relRef = valueRef.relativize(copyRef);
		}
		
		return relRef != null ? RestoreUtils.xfFact.refToPathExpr(relRef) : null;
	}

	public void initReferences(QuestionDef q) {
		// To construct the xxxRef, we need the full model, which wasn't available before now. 
		// Compute the xxxRefs now.
		nodesetRef = FormInstance.unpackReference(FormDef.getAbsRef(new XPathReference(
				((XPathPathExpr) ((XPathConditional) nodesetExpr).getExpr()).getReference(true)), contextRef));
		if ( labelExpr != null ) {
			labelRef = FormInstance.unpackReference(FormDef.getAbsRef(new XPathReference(((XPathPathExpr) ((XPathConditional) labelExpr).getExpr())),
					nodesetRef));
		}
		if ( copyExpr != null ) {
			copyRef = FormInstance.unpackReference(FormDef.getAbsRef(new XPathReference(((XPathPathExpr) ((XPathConditional) copyExpr).getExpr())),
					nodesetRef));
		}
		if ( valueExpr != null ) {
			valueRef = FormInstance.unpackReference(FormDef.getAbsRef(new XPathReference(((XPathPathExpr) ((XPathConditional) valueExpr).getExpr())),
					nodesetRef));
		}

		if ( q != null ) {
			// When loading from XML, the first time through, during verification, q will be null.
			// The second time through, q will be non-null.
			// Otherwise, when loading from binary, this will be called only once with a non-null q.
			destRef = FormInstance.unpackReference(q.getBind()).clone();
			if (copyMode) {
				destRef.add(copyRef.getNameLast(), TreeReference.INDEX_UNBOUND);
			}
		}
	}

	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		nodesetExpr = (IConditionExpr)ExtUtil.read(in, new ExtWrapTagged(), pf);
		contextRef = (TreeReference)ExtUtil.read(in, TreeReference.class, pf);
		labelExpr = (IConditionExpr)ExtUtil.read(in, new ExtWrapTagged(), pf);
		valueExpr = (IConditionExpr)ExtUtil.read(in, new ExtWrapNullable(new ExtWrapTagged()), pf);
		copyExpr = (IConditionExpr)ExtUtil.read(in, new ExtWrapNullable(new ExtWrapTagged()), pf);
		labelIsItext = ExtUtil.readBool(in);
		copyMode = ExtUtil.readBool(in);
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.write(out, new ExtWrapTagged(nodesetExpr));
		ExtUtil.write(out, contextRef);
		ExtUtil.write(out, new ExtWrapTagged(labelExpr));
		ExtUtil.write(out, new ExtWrapNullable(valueExpr == null ? null : new ExtWrapTagged(valueExpr)));
		ExtUtil.write(out, new ExtWrapNullable(copyExpr == null ? null : new ExtWrapTagged(copyExpr)));
		ExtUtil.writeBool(out, labelIsItext);
		ExtUtil.writeBool(out, copyMode);
	}

}
