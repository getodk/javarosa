package org.javarosa.xform.util;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import org.javarosa.core.model.IAnswerDataSerializer;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.Selection;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.utils.DateUtils;

/**
 * The XFormAnswerDataSerializer takes in AnswerData objects, and provides
 * an XForms compliant (String or Element) representation of that AnswerData.
 * 
 * By default, this serializer can properly operate on StringData, DateData
 * SelectMultiData, and SelectOneData AnswerData objects. This list can be
 * extended by registering appropriate XForm serializing AnswerDataSerializers
 * with this class.
 * 
 * @author Clayton Sims
 *
 */
public class XFormAnswerDataSerializer implements IAnswerDataSerializer {
	
	Vector additionalSerializers = new Vector();
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.IAnswerDataSerializer#registerAnswerSerializer(org.javarosa.core.model.IAnswerDataSerializer)
	 */
	public void registerAnswerSerializer(IAnswerDataSerializer ads) {
		additionalSerializers.addElement(ads);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.IAnswerDataSerializer#canSerialize(org.javarosa.formmanager.model.temp.AnswerData)
	 */
	public boolean canSerialize(IAnswerData data) {
		if (data.getClass() == StringData.class
				|| data.getClass() == DateData.class
				|| data.getClass() == SelectMultiData.class
				|| data.getClass() == SelectOneData.class) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * @param data The AnswerDataObject to be serialized
	 * @return A String which contains the given answer
	 */
	public Object serializeAnswerData(StringData data) {
		return (String)data.getValue();
	}
	
	/**
	 * @param data The AnswerDataObject to be serialized
	 * @return A String which contains a date in xsd:date
	 * formatting
	 */
	public Object serializeAnswerData(DateData data) {
		return DateUtils.getXMLStringValue((Date)data.getValue());
	}
	
	/**
	 * @param data The AnswerDataObject to be serialized
	 * @return A string containing the xforms compliant format
	 * for a <select> tag, a string containing a list of answers
	 * separated by space characters.
	 */
	public Object serializeAnswerData(SelectMultiData data) {
		Vector selections = (Vector)data.getValue();
		Enumeration en = selections.elements();
		String selectString = "";
		
		while(en.hasMoreElements()) {
			Selection selection = (Selection)en.nextElement();
			selectString = selectString + " " + selection.getText();
		}
		//As Crazy, and stupid, as it sounds, this is the XForms specification
		//for storing multiple selections.	
		return selectString;
	}
	
	/**
	 * @param data The AnswerDataObject to be serialized
	 * @return A String which contains the value of a selection
	 */
	public Object serializeAnswerData(SelectOneData data) {
		return ((Selection)data.getValue()).getText();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.IAnswerDataSerializer#serializeAnswerData(org.javarosa.formmanager.model.temp.AnswerData)
	 */
	public Object serializeAnswerData(IAnswerData data) {
		if(data.getClass() == StringData.class) {
			return serializeAnswerData((StringData)data);
		} else if(data.getClass() == DateData.class) {
			return serializeAnswerData((DateData)data);
		} else if(data.getClass() == SelectMultiData.class) {
			return serializeAnswerData((SelectMultiData)data);
		} else if(data.getClass() == SelectOneData.class) {
			return serializeAnswerData((SelectOneData)data);
		} else {
			Enumeration en = additionalSerializers.elements();
			while(en.hasMoreElements()) {
				IAnswerDataSerializer serializer = (IAnswerDataSerializer)en.nextElement();
				if(serializer.canSerialize(data)) {
					return serializer.serializeAnswerData(data);
				}
			}
		}
		return null;
	}
}
