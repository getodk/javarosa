package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.model.condition.IConditionExpr;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.services.locale.Localizable;
import org.javarosa.core.services.locale.Localizer;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class ItemsetBinding implements Externalizable, Localizable {
	
	public IConditionExpr nodeset;
	public IConditionExpr label;
	public boolean labelIsItext;
	public boolean copyMode; //true = copy subtree; false = copy string value
	public TreeReference copyRef;
	public IConditionExpr value;
	
	private Vector<SelectChoice> choices; //dynamic choices
	
	public Vector<SelectChoice> getChoices () {
		return choices;
	}
	
	public void setChoices (Vector<SelectChoice> choices, Localizer localizer) {
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
				choices.elementAt(i).localeChanged(locale, localizer);
			}
		}
	}
	
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		nodeset = (IConditionExpr)ExtUtil.read(in, new ExtWrapTagged(), pf);
		label = (IConditionExpr)ExtUtil.read(in, new ExtWrapTagged(), pf);
		value = (IConditionExpr)ExtUtil.read(in, new ExtWrapNullable(new ExtWrapTagged()), pf);
		copyRef = (TreeReference)ExtUtil.read(in, new ExtWrapNullable(TreeReference.class), pf);
		labelIsItext = ExtUtil.readBool(in);
		copyMode = ExtUtil.readBool(in);
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.write(out, new ExtWrapTagged(nodeset));
		ExtUtil.write(out, new ExtWrapTagged(label));
		ExtUtil.write(out, new ExtWrapNullable(value == null ? null : new ExtWrapTagged(value)));
		ExtUtil.write(out, new ExtWrapNullable(copyRef));
		ExtUtil.writeBool(out, labelIsItext);
		ExtUtil.writeBool(out, copyMode);
	}

}
