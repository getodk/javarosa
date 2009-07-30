/*
 * Copyright (C) 2009 JavaRosa
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

package org.javarosa.core.model.instance;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapList;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * This class holds a collection of study data.
 * 
 * @author Mark Gerard
 *
 */
public class StudyDataList implements Externalizable {

	/** Collection of studies. */
	private Vector studies;
	
	/** Constructs a new study data collection. */
	public StudyDataList(){
		super();
	}
	
	public StudyDataList(Vector studies){
		this();
		setStudies(studies);
	}
	
	public Vector getStudies() {
		return studies;
	}

	public void setStudies(Vector studies) {
		this.studies = studies;
	}
	
	public void addStudy(StudyData studyData){
		if(studies == null)
			studies = new Vector();
		studies.addElement(studyData);
	}
	
	public void addStudies(Vector studyList){
		if(studyList != null){
			if(studies == null)
				studies = studyList;
			else{
				for(byte i=0; i<studyList.size(); i++ )
					studies.addElement(studyList.elementAt(i));
			}
		}
	}
	
	/** 
	 * Reads the study collection object from the stream.
	 * 
	 * @param dis - the stream to read from.
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		setStudies(ExtUtil.nullIfEmpty((Vector)ExtUtil.read(in, new ExtWrapList(StudyData.class))));
		
	}

	/** 
	 * Writes the study collection object to the stream.
	 * 
	 * @param dos - the stream to write to.
	 * @throws IOException
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.write(out, new ExtWrapList(ExtUtil.emptyIfNull(getStudies())));
		
	}

}
