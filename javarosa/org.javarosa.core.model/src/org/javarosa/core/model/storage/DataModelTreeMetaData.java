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

package org.javarosa.core.model.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

import org.javarosa.core.model.instance.BareBonesDataModelWrapper;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.services.storage.utilities.MetaDataObject;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * Serializable Meta Data object for Model Tree definition records that 
 * are saved in persistent storage.
 *  

 * @author Clayton Sims
 *
 */
public class DataModelTreeMetaData extends MetaDataObject{

	private String formName = ""; // the name of the FormData being referenced
	private int version = 0; // the version of the FormData
	private Date dateSaved;
	private int formIdReference;

	public String toString() {
		return new String(super.toString() + " name: " + this.formName
				+ " version: " + this.version);
	}

	/**
	 * Creates an Empty meta data object
	 */
	public DataModelTreeMetaData() {

	}

	/**
	 * Creates a meta data object for the form data object given.
	 * 
	 * @param form
	 *            The form whose meta data this object will become
	 */
	public DataModelTreeMetaData(DataModelTree data) {
		this.formName = data.getName() + data.getId();
		this.dateSaved = data.getDateSaved();
		this.formIdReference = data.getFormId();

	}

	/**
	 * @param name
	 *            Sets the name for the form this meta data object represents
	 */
	public void setName(String name) {
		this.formName = name;
	}

	/**
	 * @return the name of the form represented by this meta data
	 */
	public String getName() {
		return this.formName;
	}

	/**
	 * @return the RMS Storage Id for the form this meta data represents
	 */
	public int getFormIdReference() {
		return formIdReference;
	}

	/**
	 * @return The date the data represented by this object was taken
	 */
	public Date getDateSaved() {
		return dateSaved;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.javarosa.clforms.storage.MetaDataObject#readExternal(java.io.
	 * DataInputStream)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		super.readExternal(in, pf);
		this.formName = in.readUTF();
		this.version = in.readInt();
		this.dateSaved = new Date(in.readLong());
		this.formIdReference = in.readInt();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.javarosa.clforms.storage.MetaDataObject#writeExternal(java.io.
	 * DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		super.writeExternal(out);
		out.writeUTF(this.formName);
		out.writeInt(this.version);
		out.writeLong(this.dateSaved.getTime());
		out.writeInt(this.formIdReference);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.javarosa.core.services.storage.utilities.MetaDataObject#
	 * setMetaDataParameters(java.lang.Object)
	 */
	public void setMetaDataParameters(Object object) {
		DataModelTree data;
	//	if (object instanceof BareBonesDataModelWrapper) {
	//		data = ((BareBonesDataModelWrapper)object).getDataModel();
	//	} else {
			data = (DataModelTree) object;
	//	}
		
		this.formName = data.getName() + data.getId();
		this.dateSaved = data.getDateSaved();
		this.formIdReference = data.getFormId();
	}

}
