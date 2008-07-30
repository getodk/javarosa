package org.javarosa.core.model;

import java.util.Date;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.utils.IDataModelVisitor;
import org.javarosa.core.util.Externalizable;

public interface IFormDataModel extends Externalizable {
	
	/**
	 * @return the name of this Model
	 */
	String getName();
	
	/**
	 * @return The Id of this particular Model
	 */
	int getId();	
	
	/**
	 * @return The id of the form that this is a model for.
	 */
	int getFormReferenceId();
	
	/**
	 * @return The date that this data model was filled out and saved
	 */
	Date getDateSaved();
	
	/**
	 * @param formIdReference The id of the form that this is a model for.
	 */
	public void setFormReferenceId(int formIdReference);

	
	/**
	 * Updates the data value associated with the reference that is passed in.
	 * 
	 * @param questionReference The reference that identifies the location that
	 * the data should be placed into. 
	 * @param value The value that the Data Value should be set to
	 * @return True if a value in this Model was updated, false otherwise
	 */
	boolean updateDataValue(IDataReference questionReference, IAnswerData value);
	
	/**
	 * Retrieves the data value associated with the reference that is passed in.
	 * @param questionReference The reference that identifies the location that
	 * data should be retrieved from
	 * @return The QuestionData object in this data model that corresponds to
	 * the reference passed in. Null if it is not present in this model.
	 */
	IAnswerData getDataValue(IDataReference questionReference);
	
	void accept(IDataModelVisitor visitor);
	
}
