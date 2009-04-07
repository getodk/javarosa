/*
 * Copyright (C) 2009 JavaRosa-Core Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.core.model;

import java.util.Date;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.utils.IDataModelVisitor;
import org.javarosa.core.util.externalizable.Externalizable;

/**
 * An IFormDataModel stores the values underlying a form.
 * 
 * Answer data values are set, updated, and retrieved based
 * on implementation specific IDataReferences.
 * 
 * Elements in implementation specific data models should
 * be walked using IDataModelVisitors if necessary.
 * 
 * @author Clayton Sims
 *
 */
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
	int getFormId();
	
	/**
	 * @return The date that this data model was filled out and saved
	 */
	Date getDateSaved();
	
	/**
	 * @param formIdReference The id of the form that this is a model for.
	 */
	public void setFormId(int formId);

	
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
	
	/**
	 * Dispatches the visitor to any child elements that exist in this model.
	 * 
	 * @param visitor
	 */
	void accept(IDataModelVisitor visitor);
	
}
