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

/**
 * 
 */
package org.javarosa.cases.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.cases.model.Case;
import org.javarosa.core.services.storage.utilities.MetaDataObject;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * @author Clayton Sims
 * @date Mar 19, 2009 
 *
 */
public class CaseMetaDataObject extends MetaDataObject {
	
	//private int recordId;
	private int caseId;
	
	private String caseName;
	
    public String toString(){
    	return new String (super.toString()+" name: "+this.caseName + " caseId: " + caseId);
    }

	
	public CaseMetaDataObject() {
		
	}

	public CaseMetaDataObject(Case c) {
		setMetaDataParameters(c);
	}
	
	public String getCaseName() {
		return caseName;
	}
	
	public int getCaseId() {
		return caseId;
	}
	
    /* (non-Javadoc)
     * @see org.javarosa.clforms.storage.MetaDataObject#readExternal(java.io.DataInputStream)
     */
    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
    	super.readExternal(in, pf);
    	
        this.caseId = in.readInt();
        
        this.caseName = in.readUTF();
    }

   
    /* (non-Javadoc)
     * @see org.javarosa.clforms.storage.MetaDataObject#writeExternal(java.io.DataOutputStream)
     */
    public void writeExternal(DataOutputStream out) throws IOException
    {
    	super.writeExternal(out);
    	
    	out.writeInt(this.caseId);
    	
    	out.writeUTF(this.caseName);
    }
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.MetaDataObject#setMetaDataParameters(java.lang.Object)
	 */
	public void setMetaDataParameters(Object originalObject) {
		Case c = (Case)originalObject;
		caseId = c.getRecordId();
		caseName = c.getName();
	}

}
