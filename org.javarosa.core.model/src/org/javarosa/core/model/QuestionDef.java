package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.model.utils.ExternalizableHelper;
import org.javarosa.core.services.storage.utilities.Externalizable;
import org.javarosa.core.util.SimpleOrderedHashtable;


/** 
 * This is the question definition properties.
 * 
 * @author Daniel Kayiwa
 *
 */
public class QuestionDef implements Externalizable{
	/** The prompt text. The text the user sees. */
	private String longText = Constants.EMPTY_STRING;
	
	/** The prompt text. The text the user sees in short modes. */
	private String shortText = Constants.EMPTY_STRING;
	
	/** The locale id. Will be used to regionalize strings. */
	private String localeId;
	
	/** The help text. */
	private String helpText = Constants.EMPTY_STRING;
	
	/** A flag to tell whether the question is to be answered or is optional. */
	private boolean mandatory = false;
	
	/** The type of question. eg Numeric,Date,Text etc. */
	private byte type = Constants.QTN_TYPE_TEXT;
	
	/** The type of widget. eg TextInput,Slider,List etc. */
	private byte controlType = Constants.QTN_TYPE_TEXT;
	
	/** A flag to tell whether the question should be shown or not. */
	private boolean visible = true;
	
	/** A flag to tell whether the question should be enabled or disabled. */
	private boolean enabled = true;
	
	/** A flag to tell whether a question is to be locked or not. A locked question 
	 * is one which is visible, enabled, but cannot be edited.
	 */
	private boolean locked = false;
	
	/** The text identifier of the question. This is used by the users of the questionnaire 
	 * but in code we use the dynamically generated numeric id for speed. 
	 */
	private String variableName = Constants.EMPTY_STRING;
	
	/** The allowed set of values (OptionDef) for an answer of the question. */
	private Vector options;
	
	/** The identifier of a question. */
	private String id;
	
	/** The value supplied as answer if the user has not supplied one. */
	private Object defaultValue;
	
	/** A Binding to an external data value */
	private IBinding bind;
	
	//TODO Add some way to link a set of visual display options
	
	/** This constructor is used mainly during deserialization. */
	public QuestionDef(){
		super();
	}
	
	/**
	 * Constructs a new question definition object from the supplied parameters.
	 * For String type parameters, they should NOT be NULL. They should instead be empty,
	 * for the cases of missing values.
	 * 
	 * @param id
	 * @param text
	 * @param helpText - The hint or help text. Should NOT be NULL.
	 * @param mandatory
	 * @param type
	 * @param defaultValue
	 * @param visible
	 * @param enabled
	 * @param locked
	 * @param variableName
	 * @param options
	 */
	public QuestionDef(String id,String longText, String shortText, String helpText, boolean mandatory, byte type, String defaultValue, boolean visible, boolean enabled, boolean locked, String variableName, Vector options) {
		this();
		setId(id);
		setLongText(longText);
		setShortText(shortText);
		setHelpText(helpText);
		setMandatory(mandatory);
		setType(type);
		setDefaultValue(defaultValue);
		setVisible(visible);
		setEnabled(enabled);
		setLocked(locked);
		setVariableName(variableName);
		setOptions(options);
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public Object getDefaultValue() {
		return defaultValue;
	}
	
	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public String getHelpText() {
		return helpText;
	}
	
	public void setHelpText(String helpText) {
		this.helpText = helpText;
	}
	
	public boolean isLocked() {
		return locked;
	}
	
	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	
	public boolean isMandatory() {
		return mandatory;
	}
	
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}
	
	public Vector getOptions() {
		return options;
	}
	
	public void setOptions(Vector options) {
		this.options = options;
	}
	
	public String getLongText() {
		return longText;
	}
	
	public void setLongText(String longText) {
		this.longText = longText;
	}
	
	public String getShortText() {
		return shortText;
	}
	
	public void setShortText(String shortText) {
		this.shortText = shortText;
	}
	
	public String getLocaleId() {
		return localeId;
	}
	
	public void setLocaleId(String localeId) {
		this.localeId = localeId;
	}
	
	public byte getType() {
		return type;
	}
	
	public void setType(byte type) {
		this.type = type;
	}
	
	public byte getControlType() {
		return controlType;
	}
	
	public void setControlType(byte controlType) {
		this.controlType = controlType;
	}
	
	public String getVariableName() {
		return variableName;
	}
	
	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public void addOption(OptionDef optionDef){
		if(options == null)
			options = new Vector();
		options.addElement(optionDef);
	}
	
	public IBinding getBind() {
		return bind;
	}
	
	public void setBind(IBinding bind) {
		this.bind = bind;
	}
	
	
	public String serializedDefaultValue() {
		if(this.defaultValue != null) {
			return this.defaultValue.toString();
		}
		else return null;
	}

	/**
	 * Reads the object from stream.
	 */
	public void readExternal(DataInputStream dis) throws IOException, IllegalAccessException, InstantiationException{
		if(!ExternalizableHelper.isEOF(dis)){
			setId(dis.readUTF());
			
			setLongText(ExternalizableHelper.readUTF(dis));
			setShortText(ExternalizableHelper.readUTF(dis));
			setLocaleId(ExternalizableHelper.readUTF(dis));
			setHelpText(ExternalizableHelper.readUTF(dis));
			setMandatory(dis.readBoolean());
			setType(dis.readByte());
			setControlType(dis.readByte());
			setVisible(dis.readBoolean());
			setEnabled(dis.readBoolean());
			setLocked(dis.readBoolean());
			setVariableName(ExternalizableHelper.readUTF(dis));
			//TODO Note that this sucks, because bind has to know its type. 
			//We need to deal with that. Should Binding be a class that
			//has a header for its type? Should we be writing that binding
			//here manually? We could also make bind serialize to a string.
			bind.readExternal(dis);
			setDefaultValue(ExternalizableHelper.readUTF(dis));
			
			setOptions(ExternalizableHelper.readExternal(dis,new OptionDef().getClass()));
		}
	}

	/**
	 * Write the object to stream.
	 */
	public void writeExternal(DataOutputStream dos) throws IOException {
		dos.writeUTF(getId());

		ExternalizableHelper.writeUTF(dos, getLongText());
		ExternalizableHelper.writeUTF(dos, getShortText());
		ExternalizableHelper.writeUTF(dos, getLocaleId());
		ExternalizableHelper.writeUTF(dos, getHelpText());
		dos.writeBoolean(isMandatory());
		dos.writeByte(getType());
		dos.writeByte(getControlType());
		dos.writeBoolean(isVisible());
		dos.writeBoolean(isEnabled());
		dos.writeBoolean(isLocked());
		ExternalizableHelper.writeUTF(dos, getVariableName());
		bind.writeExternal(dos);
		
		ExternalizableHelper.writeUTF(dos, serializedDefaultValue());
		
		ExternalizableHelper.writeExternal(getOptions(), dos);
	}
	
	public String toString() {
		return getLongText();
	}
	
	public SimpleOrderedHashtable getSelectItems() {
		//Super stub method that Drew is going to fill in.
		return new SimpleOrderedHashtable();
	}
}

