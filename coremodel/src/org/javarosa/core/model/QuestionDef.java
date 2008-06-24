package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

import org.javarosa.util.db.Persistent;
import org.javarosa.util.db.PersistentHelper;


/** 
 * This is the question definition properties.
 * 
 * @author Daniel Kayiwa
 *
 */
public class QuestionDef implements Persistent{
	/** The prompt text. The text the user sees. */
	private String text = EpihandyConstants.EMPTY_STRING;
	
	/** The help text. */
	private String helpText = EpihandyConstants.EMPTY_STRING;
	
	/** A flag to tell whether the question is to be answered or is optional. */
	private boolean mandatory = false;
	
	/** The type of question. eg Numeric,Date,Text etc. */
	private byte type = QTN_TYPE_TEXT;
	
	/** The value supplied as answer if the user has not supplied one. */
	private String defaultValue;
	
	/** A flag to tell whether the question should be shown or not. */
	private boolean visible = true;
	
	/** A flag to tell whether the question should be enabled or disabled. */
	private boolean enabled = true;
	
	/** A flag to tell whether a question is to be locked or not. A locked question 
	 * is one which is visible, enabled, but cannot be edited.
	 */
	private boolean locked = false;
	
	/** The text indentifier of the question. This is used by the users of the questionaire 
	 * but in code we use the dynamically generated numeric id for speed. 
	 */
	private String variableName = EpihandyConstants.EMPTY_STRING;
	
	/** The allowed set of values (OptionDef) for an answer of the question. */
	private Vector options;
	
	/** The numeric identifier of a question. When a form definition is being built, each question is 
	 * given a unique (on a form) id starting from 1 up to 127. The assumption is that one will never need to have
	 * a form with more than 127 questions for a mobile device (It would be too big).
	 */
	private byte id = EpihandyConstants.NULL_ID;
	
	/** Text question type. */
	public static final byte QTN_TYPE_TEXT = 1;
	
	/** Numeric question type. These are numbers without decimal points*/
	public static final byte QTN_TYPE_NUMERIC = 2;
	
	/** Decimal question type. These are numbers with decimals */
	public static final byte QTN_TYPE_DECIMAL = 3;
	
	/** Date question type. This has only date component without time. */
	public static final byte QTN_TYPE_DATE = 4;
	
	/** Time question type. This has only time element without date*/
	public static final byte QTN_TYPE_TIME = 5;
	
	/** This is a question with alist of options where not more than one option can be selected at a time. */
	public static final byte QTN_TYPE_LIST_EXCLUSIVE = 6;
	
	/** This is a question with alist of options where more than one option can be selected at a time. */
	public static final byte QTN_TYPE_LIST_MULTIPLE = 7;
	
	/** Date and Time question type. This has both the date and time components*/
	public static final byte QTN_TYPE_DATE_TIME = 8;
	
	/** Question with true and false answers. */
	public static final byte QTN_TYPE_BOOLEAN = 9;
	
	/** Question with repeat sets of questions. */
	public static final byte QTN_TYPE_REPEAT = 10;
		
	
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
	public QuestionDef(byte id,String text, String helpText, boolean mandatory, byte type, String defaultValue, boolean visible, boolean enabled, boolean locked, String variableName, Vector options) {
		this();
		setId(id);
		setText(text);
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
	
	public byte getId() {
		return id;
	}
	
	public void setId(byte id) {
		this.id = id;
	}
	
	public String getDefaultValue() {
		return defaultValue;
	}
	
	public void setDefaultValue(String defaultValue) {
		if(defaultValue != null && defaultValue.trim().length() > 0)
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
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public byte getType() {
		return type;
	}
	
	public void setType(byte type) {
		this.type = type;
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

	/**
	 * Reads the object from stream.
	 */
	public void read(DataInputStream dis) throws IOException, IllegalAccessException, InstantiationException{
		if(!PersistentHelper.isEOF(dis)){
			setId(dis.readByte());
			
			setText(dis.readUTF());
			setHelpText(dis.readUTF());
			setMandatory(dis.readBoolean());
			setType(dis.readByte());
			//setDefaultValue(dis.readUTF());
			setDefaultValue(PersistentHelper.readUTF(dis));
			setVisible(dis.readBoolean());
			setEnabled(dis.readBoolean());
			setLocked(dis.readBoolean());
			setVariableName(dis.readUTF());
			
			setOptions(PersistentHelper.read(dis,new OptionDef().getClass()));
		}
	}

	/**
	 * Write the object to stream.
	 */
	public void write(DataOutputStream dos) throws IOException {
		dos.writeByte(getId());

		dos.writeUTF(getText());
		dos.writeUTF(getHelpText());
		dos.writeBoolean(isMandatory());
		dos.writeByte(getType());
		//dos.writeUTF(getDefaultValue());
		PersistentHelper.writeUTF(dos, getDefaultValue());
		dos.writeBoolean(isVisible());
		dos.writeBoolean(isEnabled());
		dos.writeBoolean(isLocked());
		dos.writeUTF(getVariableName());
		
		PersistentHelper.write(getOptions(), dos);
	}
	
	public String toString() {
		return getText();
	}
}

