package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

import org.javarosa.util.db.Persistent;
import org.javarosa.util.db.PersistentHelper;


/**
 * Represents data collected for a question.
 * In the MCV world, this is the model representing the question data.
 * 
 * @author Daniel Kayiwa
 *
 */
public class QuestionData implements Persistent{
	
	public static final String TRUE_VALUE = "true";
	public static final String FALSE_VALUE = "false";
	
	public static final String TRUE_DISPLAY_VALUE = "Yes";
	public static final String FALSE_DISPLAY_VALUE = "No";
	
	public static final String MULITPLE_SELECT_VALUE_SEPARATOR = " ";
	public static final String MULITPLE_SELECT_TEXT_SEPARATOR = ",";
	
	/** The answer of the question. */
	private Object answer;
	
	/** For Single Select, this is a zero based index of the selected option answer. This is just for increased performance.
	 * For Multiple Select, this is a list of indices for selected answers. This is for increased performance,
	 * and is particularly usefull for questions that expect multiple answers
	 * to be picked from a list.
	 */
	private Object optionAnswerIndices;
	
	/** Reference to the question definition for this data. */ 
	private QuestionDef def;
	
	/** The numeric unique identifier for the question that this data is collected for. */
	private byte id = EpihandyConstants.NULL_ID;
	
	private String dataDescription;
	
	/** Construct a new question data object. */
	public QuestionData(){
		super();
	}
	
	/** Copy constructor. */
	public QuestionData(QuestionData data){
		setId(data.getId());
		setDef(data.getDef());
		copyAnswersAndIndices(data);
	}
	
	/** 
	 * Constructs a new question data object from a definition. 
	 * 
	 * @param def - reference to the question definition.
	 */
	public QuestionData(QuestionDef def){
		this();
		setDef(def);
	}
	
	public byte getId() {
		return id;
	}
	
	public void setId(byte id) {
		this.id = id;
	}
	
	public Object getAnswer() {
		return answer;
	}
	
	public String getDataDescription() {
		return dataDescription;
	}

	public void setDataDescription(String dataDescription) {
		this.dataDescription = dataDescription;
	}

	public String getListExclusiveAnswer(){
		if(answer == null)
			return null;
		return ((OptionData)answer).getValue();  
	}
	
	public String getListMultileAnswers(){
		if(answer == null)
			return null;
		
		Vector list = (Vector)answer;
		String s = null;
		for(byte i=0; i<list.size(); i++){
			if(s!= null)
				s += ",";
			else
				s = "";
			s += ((OptionData)list.elementAt(i)).getValue();
		}
		
		return s;
	}
	
	public void setAnswer(Object answer) {
		this.answer = answer;
	}
	
	public boolean setOptionValueIfOne(){
		if(getDef().getType() != QuestionDef.QTN_TYPE_LIST_EXCLUSIVE)
			return false;
		
		Vector options = getDef().getOptions();
		if(options != null && options.size() == 1){
			setOptionAnswer(((OptionDef)options.elementAt(0)).getVariableName());
			return true;
		}
		return false;
	}
	
	public void setTextAnswer(String textAnswer){
		switch(getDef().getType()){
			case QuestionDef.QTN_TYPE_BOOLEAN:
				answer = fromString2Boolean(textAnswer);
				break;
			case QuestionDef.QTN_TYPE_DATE:
			case QuestionDef.QTN_TYPE_DATE_TIME:
			case QuestionDef.QTN_TYPE_TIME:
				answer = textAnswer;
				break;
			case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE:
				setOptionAnswer(textAnswer);
				break;
			case QuestionDef.QTN_TYPE_LIST_MULTIPLE:
				setOptionAnswers(split(textAnswer,OptionDef.SEPARATOR_CHAR));
				break;
			case QuestionDef.QTN_TYPE_DECIMAL:
			case QuestionDef.QTN_TYPE_NUMERIC:
			case QuestionDef.QTN_TYPE_TEXT:
				answer = textAnswer;
				break;
		}
	}
	
	public void setOptionAnswer(String textAnswer){
		for(byte i=0; i<getDef().getOptions().size(); i++){
			OptionDef optionDef = (OptionDef)getDef().getOptions().elementAt(i);
			if(optionDef.getVariableName().equals(textAnswer)){
				setAnswer(new OptionData(optionDef));
				setOptionAnswerIndices(new Byte(i));
				break;
			}
		}
	}
	
	public void setOptionAnswers(Vector vals){
		Vector optionAnswers = new Vector();
		Vector optionAnswerIndices = new Vector();
		for(byte j=0; j<vals.size(); j++){
			String strVal = (String)vals.elementAt(j);
			for(byte i=0; i<getDef().getOptions().size(); i++){
				OptionDef option = (OptionDef)getDef().getOptions().elementAt(i);
				if(option.getVariableName().equals(strVal)){
					optionAnswers.addElement(new OptionData(option));
					optionAnswerIndices.addElement(new Byte(i));
					break;
				}
			}
		}
		
		setAnswer(optionAnswers);
		setOptionAnswerIndices(optionAnswerIndices);
	}
	
	public Vector split(String contents, char separator){
		Vector ret = new Vector();
		
		int j=0, i = contents.indexOf(separator,j);
		if(i != -1){
			while(i>-1){
				ret.addElement(contents.substring(j, i));
				j = i+1;
				i = contents.indexOf(separator,j);
			}
			if(j > 0)
				ret.addElement(contents.substring(j, contents.length()));
		}
		else
			ret.addElement(contents); //one value found
		
		return ret;
	}

	public Object getOptionAnswerIndices() {
		return optionAnswerIndices;
	}

	public void setOptionAnswerIndices(Object optionAnswerIndices) {
		this.optionAnswerIndices = optionAnswerIndices;
	}

	public QuestionDef getDef() {
		return def;
	}

	public void setDef(QuestionDef def) {
		this.def = def;
		setId(def.getId());
		if(def.getDefaultValue() != null && getAnswer() == null)
			setTextAnswer(def.getDefaultValue());
	}
	
	private void copyAnswersAndIndices(QuestionData data){
		if(data.getAnswer() != null){
			if(getDef().getType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE){
				setAnswer(new OptionData((OptionData)data.getAnswer()));
				setOptionAnswerIndices(data.getOptionAnswerIndices());
			}
			else if(getDef().getType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE){
				Vector tempAnswer = new Vector();
				Vector ansrs  = (Vector)data.getAnswer();
				for(int i=0; i<ansrs.size(); i++)
					tempAnswer.addElement(new OptionData((OptionData)ansrs.elementAt(i)));
				setAnswer(tempAnswer);
				
				tempAnswer = new Vector();
				ansrs  = (Vector)data.getOptionAnswerIndices();
				for(int i=0; i<ansrs.size(); i++)
					tempAnswer.addElement(ansrs.elementAt(i));
				setOptionAnswerIndices(tempAnswer);
			}
			else
				setAnswer(data.getAnswer());
		}
	}

	/**
	 * Check to see if an answer is supplied for a question.
	 * 
	 * @return - true when answered, else false.
	 */
	public boolean isAnswered(){
		switch(getDef().getType()){
			case QuestionDef.QTN_TYPE_BOOLEAN:
			case QuestionDef.QTN_TYPE_DATE:
			case QuestionDef.QTN_TYPE_DATE_TIME:
			case QuestionDef.QTN_TYPE_TIME:
				return getAnswer() != null;
			case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE:
				return getAnswer() != null;
			case QuestionDef.QTN_TYPE_LIST_MULTIPLE:
				return getAnswer() != null && ((Vector)getAnswer()).size() > 0;
			case QuestionDef.QTN_TYPE_DECIMAL:
			case QuestionDef.QTN_TYPE_NUMERIC:
			case QuestionDef.QTN_TYPE_TEXT:
				return getAnswer() != null && this.getAnswer().toString().length() > 0;
		}
		//TODO need to handle other user defined types.
		return false;
	}
	
	/**
	 * Check whether a question's data is entered correctly. 
	 * No missing mandatory fields, not values out of range, etc.
	 * 
	 * @return - true if the data is correct, else false.
	 */
	public boolean isValid(){
		if(this.getDef().isMandatory() && !this.isAnswered())
			return false;
		return true;
	}
	
	//TODO This does not belong here.
	public static String DateToString(Date d){
		Calendar cd = Calendar.getInstance(EpihandyConstants.DEFAULT_TIME_ZONE);
		cd.setTime(d);
		String year = "" + cd.get(Calendar.YEAR);
		String month = "" + (cd.get(Calendar.MONTH)+1);
		String day = "" + cd.get(Calendar.DAY_OF_MONTH);
		
		if (month.length()<2)
			month = "0" + month;
		
		if (day.length()<2)
			day = "0" + day;
		
		//return day + "-" + month + "-" + year;
		//TODO The date format should be flexibly set by the user.
		return year + "-" + month + "-" + day;
	}
	
	/**
	 * Gets the answer of a question in string format regardless of the question type.
	 * 
	 * @return - the string value of the answer.
	 */
	public String getTextAnswer(){
		
		String val = null;
		
		if(getAnswer() != null){
			switch(getDef().getType()){
				case QuestionDef.QTN_TYPE_BOOLEAN:
					val = fromBoolean2DisplayString(getAnswer());
					break;
				case QuestionDef.QTN_TYPE_DECIMAL:
				case QuestionDef.QTN_TYPE_NUMERIC:
				case QuestionDef.QTN_TYPE_TEXT:
					val = getAnswer().toString();
					break;
				case QuestionDef.QTN_TYPE_DATE:
				case QuestionDef.QTN_TYPE_DATE_TIME:
				case QuestionDef.QTN_TYPE_TIME:{
					val = DateToString((Date)getAnswer());
					break;
				}
				case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE:
					val = ((OptionData)getAnswer()).toString();
					break;
				case QuestionDef.QTN_TYPE_LIST_MULTIPLE:
					String s = ""; Vector optionAnswers = (Vector)getAnswer();
					for(byte i=0; i<optionAnswers.size(); i++){
						if(s.length() != 0)
							s += MULITPLE_SELECT_TEXT_SEPARATOR;
						s += ((OptionData)optionAnswers.elementAt(i)).getDef().getText();
					}
					val = s;
					break;
				default:
					val = "Not Implemented yet.";
			}
		}
		return val;
	}
	
	public static String fromBoolean2DisplayString(Object boolVal){
		if(((Boolean)boolVal).booleanValue())
			return TRUE_DISPLAY_VALUE;
		return FALSE_DISPLAY_VALUE;
	}
	
	public static String fromBoolean2ValueString(Object boolVal){
		if(((Boolean)boolVal).booleanValue())
			return TRUE_VALUE;
		return FALSE_VALUE;
	}
	
	public static Boolean fromString2Boolean(String val){
		if(val.equals(TRUE_VALUE))
			return new Boolean(true);
		return new Boolean(false);
	}

	//TODO This method needs to be refactored with the getTextAnswer()
	/**
	 * Gets the answer of a question in string format regardless of the question type.
	 * The difference with this method and the getTextAnswer() is that this returns
	 * the underlying values instead of text for the select from list question types.
	 * 
	 * @return - the string value of the answer.
	 */
	public String getValueAnswer(){
		
		String val = null;
		
		if(getAnswer() != null){
			switch(getDef().getType()){
				case QuestionDef.QTN_TYPE_BOOLEAN:
					val = fromBoolean2ValueString(getAnswer());
					break;
				case QuestionDef.QTN_TYPE_DECIMAL:
				case QuestionDef.QTN_TYPE_NUMERIC:
				case QuestionDef.QTN_TYPE_TEXT:
					val = getAnswer().toString();
					break;
				case QuestionDef.QTN_TYPE_DATE:
				case QuestionDef.QTN_TYPE_DATE_TIME:
				case QuestionDef.QTN_TYPE_TIME:
					val = DateToString((Date)getAnswer());
					break;
				case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE:
					val = ((OptionData)getAnswer()).getValue();
					break;
				case QuestionDef.QTN_TYPE_LIST_MULTIPLE:
					String s = ""; Vector optionAnswers = (Vector)getAnswer();
					for(byte i=0; i<optionAnswers.size(); i++){
						if(s.length() != 0)
							s += MULITPLE_SELECT_VALUE_SEPARATOR;
						s += ((OptionData)optionAnswers.elementAt(i)).getValue();
					}
					val = s;
					break;
				default:
					val = "Not Implemented yet.";
			}
		}
		return val;
	}		

	
	public String toString() {
//TODO This method should be refactored with the one above.
		String val = getDef().getText();
		
		if(dataDescription != null && dataDescription.trim().length() > 0)
			val = dataDescription;
		
		if(getTextAnswer() != null && getTextAnswer().length() > 0)
			val += " {" + getTextAnswer() + "}";
		
		return val;
	}
	
	public String getText(){
		String val = getDef().getText();
		
		if(dataDescription != null && dataDescription.trim().length() > 0)
			val = dataDescription;
		
		return val;
	}
	
	/** 
	 * Reads the question data object from the supplied stream.
	 * 
	 * @param dis - the stream to read from.
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void read(DataInputStream dis) throws IOException, IllegalAccessException, InstantiationException{
		if(!PersistentHelper.isEOF(dis)){
			setId(dis.readByte());	
			readAnswer(dis,dis.readByte());
		}
	}
	
	/** 
	 * Reads an answer from the stream. 
	 * 
	 * @param dis
	 * @param type
	 * @throws IOException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public void readAnswer(DataInputStream dis, byte type) throws IOException, IllegalAccessException, InstantiationException{
		switch(type){
		case QuestionDef.QTN_TYPE_BOOLEAN:
			setAnswer(PersistentHelper.readBoolean(dis));
			break;
		case QuestionDef.QTN_TYPE_TEXT:
		case QuestionDef.QTN_TYPE_DECIMAL:
		case QuestionDef.QTN_TYPE_NUMERIC:
			setAnswer(PersistentHelper.readUTF(dis));
			break;
		case QuestionDef.QTN_TYPE_DATE:
		case QuestionDef.QTN_TYPE_DATE_TIME:
		case QuestionDef.QTN_TYPE_TIME:
			setAnswer(PersistentHelper.readDate(dis));
			break;
		case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE:
			if(dis.readBoolean()){
				OptionData option = new OptionData();
				option.read(dis);
				setAnswer(option);	
				
				setOptionAnswerIndices(new Byte(dis.readByte()));
			}
			break;
		case QuestionDef.QTN_TYPE_LIST_MULTIPLE:
			if(dis.readBoolean()){
				setAnswer(PersistentHelper.read(dis, new OptionData().getClass()));
				
				byte count = dis.readByte(); //should always be greater than zero
				Vector col = new Vector();
				for(byte i=0; i<count; i++)
					col.addElement(new Byte(dis.readByte()));
				setOptionAnswerIndices(col);
			}
			break;
		}
	}

	/** 
	 * Writes the question data object to the supplied stream.
	 * 
	 * @param dos - the stream to write to.
	 * @throws IOException
	 */
	public void write(DataOutputStream dos) throws IOException {
		dos.writeByte(getId());
		
		//This type is only used when reading data back from storage.
		//Otherwise it is not kept in memory because it can be got from the QuestionDef.
		dos.writeByte(getDef().getType());
		writeAnswer(dos);
	}
	
	private void writeAnswer(DataOutputStream dos) throws IOException {	
		switch(getDef().getType()){
			case QuestionDef.QTN_TYPE_BOOLEAN:
				PersistentHelper.writeBoolean(dos, (Boolean)getAnswer());
				break;
			case QuestionDef.QTN_TYPE_TEXT:
			case QuestionDef.QTN_TYPE_DECIMAL:
			case QuestionDef.QTN_TYPE_NUMERIC:
				PersistentHelper.writeUTF(dos, getTextAnswer());
				break;
			case QuestionDef.QTN_TYPE_DATE:
			case QuestionDef.QTN_TYPE_DATE_TIME:
			case QuestionDef.QTN_TYPE_TIME:
				PersistentHelper.writeDate(dos, (Date)getAnswer());
				break;
			case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE:
				if(getAnswer() != null){
					dos.writeBoolean(true);
					((OptionData)getAnswer()).write(dos);
					dos.writeByte(((Byte)getOptionAnswerIndices()).byteValue());
				}
				else
					dos.writeBoolean(false);
				break;
			case QuestionDef.QTN_TYPE_LIST_MULTIPLE:
				if(getAnswer() != null){
					dos.writeBoolean(true);
					PersistentHelper.write((Vector)getAnswer(), dos);
					Vector col = (Vector)getOptionAnswerIndices();
					dos.writeByte(col.size());
					for(byte i=0; i<col.size(); i++)
						dos.writeByte(((Byte)col.elementAt(i)).byteValue());
				}
				else
					dos.writeBoolean(false);
				break;
			case QuestionDef.QTN_TYPE_REPEAT:
				if(getAnswer() != null){
					dos.writeBoolean(true);
					((Persistent)getAnswer()).write(dos);
				}
				else
					dos.writeBoolean(false);
				break;
		}
	}
}

