package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.services.locale.Localizable;
import org.javarosa.core.services.locale.Localizer;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.xform.parse.XFormParseException;

public class SelectChoice implements Externalizable, Localizable {

	private String labelInnerText;
	private String textID;
	private boolean isLocalizable;
	private String value;
	private int index = -1;
	
	//for deserialization only
	public SelectChoice () {
	
	}
	
	public SelectChoice (String labelID, String value) {
		this(labelID,null,value,true);
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

	/**
	 * @deprecated
	 */
	public void localeChanged(String locale, Localizer localizer) {
//		if (captionLocalizable) {
//			caption = localizer.getLocalizedText(captionID);
//		}
	}
	
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		isLocalizable = ExtUtil.readBool(in);
		setLabelInnerText(ExtUtil.readString(in));
		value = ExtUtil.readString(in);
		//index will be set by questiondef
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.writeBool(out, isLocalizable);
		ExtUtil.writeString(out, isLocalizable ? textID : labelInnerText);
		ExtUtil.writeString(out, value);
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
		return (textID != null ? "{" + textID + "}" : "") + (labelInnerText != null ? labelInnerText : "") + " => " + value;
//		return ("{" + textID + ",innerText: " + getLabelInnerText() + "}");
	}

	public String getTextID() {
		return textID;
	}

	public void setTextID(String textID) {
		this.textID = textID;
	}
	
}
