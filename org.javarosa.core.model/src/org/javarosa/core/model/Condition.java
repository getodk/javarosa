package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.utils.ExternalizableHelper;
import org.javarosa.core.services.storage.utilities.Externalizable;


/**
 * A condition which is part of a rule. For definition of a rule, go to the Rule class.
 * E.g. If sex is Male. If age is greater than than 4. etc
 *
 *@author Daniel Kayiwa
 */
public class Condition implements Externalizable{
	
	/** The unique identifier of the question referenced by this condition. */
	private String questionId;
	
	/** The operator of the condition. Eg Equal to, Greater than, etc. */
	private int operator = Constants.OPERATOR_NULL;
	
	/** The value checked to see if the condition is true or false.
	 * For the above example, the value would be 4 or the id of the Male option.
	 * For a list of options this value is the option id, not the value or text value.
	 */
	private String value = Constants.EMPTY_STRING;
	
	/** The unique identifier of a condition. */
	private int id = Constants.NULL_ID;
	
	/** Creates a new condition object. */
	public Condition(){
		super();
	}
	
	/**
	 * Creates a new condition object from its parameters. 
	 * 
	 * @param id - the numeric identifier of the condition.
	 * @param questionId - the numeric identifier of the question.
	 * @param operator - the condition operator.
	 * @param value - the value to be equated to.
	 */
	public Condition(int id,String questionId, int operator, String value) {
		this();
		setQuestionId(questionId);
		setOperator(operator);
		setValue(value);
		setId(id);
	}
	
	public int getOperator() {
		return operator;
	}
	public void setOperator(int operator) {
		this.operator = operator;
	}
	public String getQuestionId() {
		return questionId;
	}
	public void setQuestionId(String questionId) {
		this.questionId = questionId;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public int getId() {
		return id;
	}
	public void setId(int conditionId) {
		this.id = conditionId;
	}
	
	/**
     * Test if a condition is true or false.
     */
	public boolean isTrue(FormData data){
		QuestionData qn= data.getQuestion(this.questionId);
		
		switch(qn.getDef().getDataType()){
			case Constants.DATATYPE_TEXT:
				return isTextTrue(qn);
			case Constants.DATATYPE_INTEGER:
				return isNumericTrue(qn);
			case Constants.DATATYPE_DATE:
				return isDateTrue(qn);
			case Constants.DATATYPE_DATE_TIME:
				return isDateTimeTrue(qn);
			case Constants.DATATYPE_DECIMAL:
				return isDecimalTrue(qn);
			case Constants.DATATYPE_LIST_EXCLUSIVE:
				return isListExclusiveTrue(qn);
			case Constants.DATATYPE_LIST_MULTIPLE:
				return isListMultipleTrue(qn);
			case Constants.DATATYPE_TIME:
				return isTimeTrue(qn);
			case Constants.DATATYPE_BOOLEAN:
				return isTextTrue(qn);
		}
		
		return true;
	}
	
	private boolean isNumericTrue(QuestionData data){
		return data.getTextAnswer().equals(this.value);
	}
	
	private boolean isTextTrue(QuestionData data){
		return data.getTextAnswer().equals(this.value);
	}
	
	/**
	 * Tests if the passed parameter date value is equal to the value of the condition.
	 * 
	 * @param data - passed parameter date value.
	 * @return - true when the two values are the same, else false.
	 */
	private boolean isDateTrue(QuestionData data){
		return data.getTextAnswer().equals(this.value);
	}
	
	private boolean isDateTimeTrue(QuestionData data){
		return data.getTextAnswer().equals(this.value);
	}
	
	private boolean isTimeTrue(QuestionData data){
		return data.getTextAnswer().equals(this.value);
	}
	
	private boolean isListMultipleTrue(QuestionData data){
		return data.getTextAnswer().equals(this.value);
	}
	
	private boolean isListExclusiveTrue(QuestionData data){
		
		//If any value is null, we assume false.
		//Therefore OPERATOR_NOT_EQUAL will always return true
		//while OPERATOR_EQUAL returns false.
		//This will help make conditions false when any value is not yet filled.
		if(data.getOptionAnswerIndices() == null || value == null)
			return operator != Constants.OPERATOR_EQUAL;
		
		//For the sake of performance, we dont compare the actual value.
		//We instead use the index.		
		int val1 = Integer.parseInt(data.getOptionAnswerIndices().toString());
		val1 += 1;
		
		int val2 = Integer.parseInt(value);

		switch(operator){
			case Constants.OPERATOR_EQUAL:
				return val1 == val2;
			case Constants.OPERATOR_NOT_EQUAL:
				return val1 != val2;
			default:
				return false;
		}
	}
	
	private boolean isDecimalTrue(QuestionData data){
		return data.getTextAnswer().equals(this.value);
	}
	
	/** 
	 * Reads the condition object from the supplied stream.
	 * 
	 * @param dis - the stream to read from.
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void readExternal(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		if(!ExternalizableHelper.isEOF(dis)){
			setId(dis.readByte());
			setQuestionId(dis.readUTF());
			setOperator(dis.readByte());
			setValue(dis.readUTF());
		}
	}

	/** 
	 * Writes the Condition object to the supplied stream.
	 * 
	 * @param dos - the stream to write to.
	 * @throws IOException
	 */
	public void writeExternal(DataOutputStream dos) throws IOException {
		dos.writeByte(getId());
		dos.writeUTF(getQuestionId());
		dos.writeByte(getOperator());
		dos.writeUTF(getValue());
	}
}