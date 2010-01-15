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

public class SelectChoice implements Externalizable, Localizable {

	private String caption;
	private String captionID;
	private boolean captionLocalizable;
	private String value;
	private int index = -1;
	
	//for deserialization only
	public SelectChoice () {
	
	}
	
	public SelectChoice (String captionID, String value) {
		this(captionID, value, true);
	}
	
	public SelectChoice (String captionStr, String value, boolean captionLocalizable) {
		this.captionLocalizable = captionLocalizable;
		setCaptionStr(captionStr);
		this.value = value;
	}
	
	public void setIndex (int index) {
		this.index = index;
	}
	
	public String getCaption () {
		if (caption != null) {
			return caption;
		} else {
			System.err.println("attempted to retrieve caption but no locale set yet! [" + captionID + "]");
			return "[itext:" + captionID + "]";
		}
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
		if (captionLocalizable) {
			caption = localizer.getLocalizedText(captionID);
		}
	}
	
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		captionLocalizable = ExtUtil.readBool(in);
		setCaptionStr(ExtUtil.readString(in));
		value = ExtUtil.readString(in);
		//index will be set by questiondef
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.writeBool(out, captionLocalizable);
		ExtUtil.writeString(out, captionLocalizable ? captionID : caption);
		ExtUtil.writeString(out, value);
		//don't serialize index; it will be restored from questiondef
	}

	private void setCaptionStr (String captionStr) {
		if (captionLocalizable) {
			captionID = captionStr;
		} else {
			caption = captionStr;
		}
	}
	
	public Selection selection () {
		return new Selection(this);
	}
	
}
