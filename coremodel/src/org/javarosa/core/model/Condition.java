package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.util.db.Persistent;
import org.javarosa.util.db.PersistentHelper;


/**
 * A condition which is part of a rule. For definition of a rule, go to the Rule class.
 * E.g. If sex is Male. If age is greater than than 4. etc
 *
 *@author Daniel Kayiwa
 */
public class Condition implements Persistent{
	
	/** The unique identifier of the question referenced by this condition. */
	private byte questionId = EpihandyConstants.NULL_ID;
	
	/** The operator of the condition. Eg Equal to, Greater than, etc. */
	private byte operator = EpihandyConstants.OPERATOR_NULL;
	
	/** The value checked to see if the condition is true or false.
	 * For the above example, the value would be 4 or the id of the Male option.
	 * For a list of options this value is the option id, not the value or text value.
	 */
	private String value = EpihandyConstants.EMPTY_STRING;
	
	/** The unique identifier of a condition. */
	private byte id = EpihandyConstants.NULL_ID;
	
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
	public Condition(byte id,byte questionId, byte operator, String value) {
		this();
		setQuestionId(questionId);
		setOperator(operator);
		setValue(value);
		setId(id);
	}
	
	public byte getOperator() {
		return operator;
	}
	public void setOperator(byte operator) {
		this.operator = operator;
	}
	public byte getQuestionId() {
		return questionId;
	}
	public void setQuestionId(byte questionId) {
		this.questionId = questionId;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public byte getId() {
		return id;
	}
	public void setId(byte conditionId) {
		this.id = conditionId;
	}
	
	/**
     * Test if a condition is true or false.
     */
	public boolean isTrue(FormData data){
		QuestionData qn= data.getQuestion(this.questionId);
		
		switch(qn.getDef().getType()){
			case QuestionDef.QTN_TYPE_TEXT:
				return isTextTrue(qn);
			case QuestionDef.QTN_TYPE_NUMERIC:
				return isNumericTrue(qn);
			case QuestionDef.QTN_TYPE_DATE:
				return isDateTrue(qn);
			case QuestionDef.QTN_TYPE_DATE_TIME:
				return isDateTimeTrue(qn);
			case QuestionDef.QTN_TYPE_DECIMAL:
				return isDecimalTrue(qn);
			case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE:
				return isListExclusiveTrue(qn);
			case QuestionDef.QTN_TYPE_LIST_MULTIPLE:
				return isListMultipleTrue(qn);
			case QuestionDef.QTN_TYPE_TIME:
				return isTimeTrue(qn);
			case QuestionDef.QTN_TYPE_BOOLEAN:
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
			return operator != EpihandyConstants.OPERATOR_EQUAL;
		
		//For the sake of performance, we dont compare the actual value.
		//We instead use the index.		
		byte val1 = Byte.parseByte(data.getOptionAnswerIndices().toString());
		val1 += 1;
		
		byte val2 = Byte.parseByte(value);

		switch(operator){
			case EpihandyConstants.OPERATOR_EQUAL:
				return val1 == val2;
			case EpihandyConstants.OPERATOR_NOT_EQUAL:
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
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		if(!PersistentHelper.isEOF(dis)){
			setId(dis.readByte());
			setQuestionId(dis.readByte());
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
	public void write(DataOutputStream dos) throws IOException {
		dos.writeByte(getId());
		dos.writeByte(getQuestionId());
		dos.writeByte(getOperator());
		dos.writeUTF(getValue());
	}
}