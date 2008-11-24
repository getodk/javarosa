package org.javarosa.xform.util;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.DataBinding;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.IAnswerDataSerializer;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.Selection;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.QuestionDataElement;
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
	
	public final static String DELIMITER = "|";
	
	Vector additionalSerializers = new Vector();
	
	public void registerAnswerSerializer(IAnswerDataSerializer ads) {
		additionalSerializers.addElement(ads);
	}
	
	public boolean canSerialize(QuestionDataElement element) {
		if (element.getValue() instanceof StringData || element.getValue() instanceof DateData ||
				element.getValue() instanceof SelectMultiData || element.getValue() instanceof SelectOneData) {
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
		StringBuffer selectString = new StringBuffer();
		
		while(en.hasMoreElements()) {
			Selection selection = (Selection)en.nextElement();
			if (selectString.length() > 0)
				selectString.append(DELIMITER);
			selectString.append(selection.getValue());
		}
		//As Crazy, and stupid, as it sounds, this is the XForms specification
		//for storing multiple selections.	
		return selectString.toString();
	}
	
	/**
	 * @param data The AnswerDataObject to be serialized
	 * @return A String which contains the value of a selection
	 */
	public Object serializeAnswerData(SelectOneData data) {
		return ((Selection)data.getValue()).getValue();
	}
	
	public Object serializeAnswerData(IntegerData data) {
		return ((Integer)data.getValue()).toString();
	}
	

	public Object serializeAnswerData(QuestionDataElement element, FormDef formDef) {
		if(element == null || element.getValue() == null) { 
			return "";
		}
		if(element.getValue() instanceof DateData) {
			DataBinding binding = getBinding(element, formDef);
			if(binding == null){
				return serializeAnswerData((DateData) element.getValue());
			} else {
				if(Constants.DATATYPE_DATE_TIME == binding.getDataType()){
					return DateUtils.formatDateToTimeStamp((Date)element.getValue().getValue());
				} else {
					return serializeAnswerData((DateData) element.getValue());
				}
			}
		}
		else {
			Object data = serializeAnswerData(element.getValue());
			if(data == null) {
				Enumeration en = additionalSerializers.elements();
				while(en.hasMoreElements()) {
					IAnswerDataSerializer serializer = (IAnswerDataSerializer)en.nextElement();
					if(serializer.canSerialize(element)) {
						return serializer.serializeAnswerData(element, formDef);
					}
				}
			}
		}
		return null;
	}

	private DataBinding getBinding(QuestionDataElement element, FormDef formDef) {
		return formDef == null ? null : formDef.getBinding(element.getReference());
	}

	public Object serializeAnswerData(IAnswerData data) {
		if(data instanceof StringData) {
			return serializeAnswerData((StringData)data);
		} else if(data instanceof SelectMultiData) {
			return serializeAnswerData((SelectMultiData)data);
		} else if(data instanceof SelectOneData) {
			return serializeAnswerData((SelectOneData)data);
		} else if(data instanceof IntegerData){
			return serializeAnswerData((IntegerData)data);
		}
		
		return null;
	}
}
