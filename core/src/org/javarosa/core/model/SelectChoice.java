package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.services.locale.Localizable;
import org.javarosa.core.services.locale.Localizer;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.xform.parse.XFormParseException;

public class SelectChoice implements Externalizable, Localizable {

	private String labelInnerText;
	private String textID;
	private boolean isLocalizable;
	private String value;
	private int index = -1;
	
	public TreeElement copyNode; //if this choice represents part of an <itemset>, and the itemset uses 'copy'
	                             //answer mode, this points to the node to be copied if this selection is chosen
								 //this field only has meaning for dynamic choices, thus is unserialized
	
	//for deserialization only
	public SelectChoice () {
	
	}
	
	public SelectChoice (String labelID, String value) {
		this(labelID, null, value, true);
	}
	
	/**
	 * 
	 * @param labelID can be null
	 * @param labelInnerText can be null
	 * @param value should not be null
	 * @param isLocalizable
	 * @throws XFormParseException if value is null
	 */
	public SelectChoice (String labelID, String labelInnerText, String value, boolean isLocalizable) {
		this.isLocalizable = isLocalizable;
		this.textID = labelID;
		this.labelInnerText = labelInnerText;
		if(value != null){
			this.value = value;
		}else{
			throw new XFormParseException("SelectChoice{id,innerText}:{"+labelID+","+labelInnerText+"}, has null Value!");
		}
	}
	
	public SelectChoice(String labelOrID,String Value, boolean isLocalizable){
		this(isLocalizable ? labelOrID : null,
			 isLocalizable ? null : labelOrID,
			 Value,isLocalizable);
	}
	
	public void setIndex (int index) {
		this.index = index;
	}
	

	public String getLabelInnerText () {
		return labelInnerText;
	}
	
	public String getValue () {
		return value;
	}
	
	public int getIndex () {
		if (index == -1) {
			throw new RuntimeException("trying to access choice index before it has been set!");
		}
		
		return index;
	}


	public void localeChanged(String locale, Localizer localizer) {
//		if (captionLocalizable) {
//			caption = localizer.getLocalizedText(captionID);
//		}
	}
	
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		isLocalizable = ExtUtil.readBool(in);
		setLabelInnerText(ExtUtil.nullIfEmpty(ExtUtil.readString(in)));
		setTextID(ExtUtil.nullIfEmpty(ExtUtil.readString(in)));
		value = ExtUtil.nullIfEmpty(ExtUtil.readString(in));
		//index will be set by questiondef
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.writeBool(out, isLocalizable);
		ExtUtil.writeString(out, ExtUtil.emptyIfNull(labelInnerText));
		ExtUtil.writeString(out, ExtUtil.emptyIfNull(textID));
		ExtUtil.writeString(out, ExtUtil.emptyIfNull(value));
		//don't serialize index; it will be restored from questiondef
	}

	private void setLabelInnerText (String labelInnerText) {
		this.labelInnerText = labelInnerText;
	}
	
	public Selection selection () {
		return new Selection(this);
	}
	
	public boolean isLocalizable(){
		return isLocalizable;
	}
	
	public void setLocalizable(boolean localizable){
		this.isLocalizable = localizable;
	}
	
	public String toString () {
		return ((textID != null && textID.length() > 0) ? "{" + textID + "}" : "") + (labelInnerText != null ? labelInnerText : "") + " => " + value;
	}

	public String getTextID() {
		return textID;
	}

	public void setTextID(String textID) {
		this.textID = textID;
	}
	
}
