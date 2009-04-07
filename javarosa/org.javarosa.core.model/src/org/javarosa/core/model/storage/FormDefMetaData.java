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

import org.javarosa.core.model.FormDef;
import org.javarosa.core.services.storage.utilities.MetaDataObject;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * Serializable Meta Data object for form definition records that 
 * are saved in persistent storage.
 *  
 * @author Clayton Sims
 */
public class FormDefMetaData extends MetaDataObject
{
	private String formTitle = ""; //the title of the FormDef being referenced

	private String formName = ""; //the name of the FormDef being referenced
    private int version = 0; //the version of the FormDef
    private int type = 0; //the FormDef Type
    
    public String toString(){
    	return new String (super.toString()+" name: "+this.formName+" title: " + this.formTitle + " version: "+this.version
    			+ " type: "+this.type);
    }
    
    public FormDefMetaData()
    {
        
    }
    
    /**
     * Creates a new meta data entry for a form definition
     * @param form The form def whose metadata is to be captured
     */
    public FormDefMetaData(FormDef form)
    {
        this.formName = form.getTitle();
        
    }
    
    /**
     * Sets the name of the form definition that this meta data represents
     * @param name The name to be used
     */
    public void setName(String name)
    {
        this.formName = name;
    }
    
    /**
     * @return the name of the form definition that this meta data represents
     */
    public String getName()
    {
       return this.formName; 
    }
    
    /**
	 * @return the formTitle
	 */
	public String getTitle() {
		return formTitle;
	}

	/**
	 * @param formTitle the formTitle to set
	 */
	public void setTitle(String formTitle) {
		this.formTitle = formTitle;
	}
    
    /* (non-Javadoc)
     * @see org.javarosa.clforms.storage.MetaDataObject#readExternal(java.io.DataInputStream)
     */
    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
    	super.readExternal(in, pf);
        this.formName = (String)ExtUtil.read(in, new ExtWrapNullable(String.class));
        this.formTitle = ExtUtil.nullIfEmpty(ExtUtil.readString(in));
        this.version = in.readInt();
        this.type = in.readInt();
    }

   
    /* (non-Javadoc)
     * @see org.javarosa.clforms.storage.MetaDataObject#writeExternal(java.io.DataOutputStream)
     */
    public void writeExternal(DataOutputStream out) throws IOException
    {
    	super.writeExternal(out);
		ExtUtil.write(out, new ExtWrapNullable(this.formName));
        ExtUtil.writeString(out,ExtUtil.emptyIfNull(this.formTitle));
        out.writeInt(this.version);
        out.writeInt(type);
    }

    /*
     * (non-Javadoc)
     * @see org.javarosa.core.services.storage.utilities.MetaDataObject#setMetaDataParameters(java.lang.Object)
     */
    public void setMetaDataParameters(Object object)
    {
        FormDef form = (FormDef)object;
        this.setName(form.getName());
        this.setTitle(form.getTitle());
    }



}
