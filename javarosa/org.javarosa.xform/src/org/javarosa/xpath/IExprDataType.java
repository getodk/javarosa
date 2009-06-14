package org.javarosa.xpath;

public interface IExprDataType {
	
	//return null if object cannot be converted to data type
	
	Boolean toBoolean();
	Double toNumeric();
	String toString();
}
