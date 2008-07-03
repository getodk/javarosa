/*
 * FormDefMetaData.java
 * 
 * Created on 2007/10/31, 11:31:57
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.javarosa.core.model.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.services.storage.utilities.MetaDataObject;

/**
 *
 * @author Clayton Sims
 */
public class FormDefMetaData extends MetaDataObject
{

    private String formName = ""; //the name of the FormDef being referenced
    private int version = 0; //the version of the FormDef
    private int type = 0; //the FormDef Type
    
    public String toString(){
    	return new String (super.toString()+" name: "+this.formName+" version: "+this.version
    			+ " type: "+this.type);
    }
    
    public FormDefMetaData()
    {
        
    }
    
    public FormDefMetaData(FormDef form)
    {
        this.formName = form.getName();
        
    }
    
    public void setName(String name)
    {
        this.formName = name;
    }
    
    public String getName()
    {
       return this.formName; 
    }
    
    /* (non-Javadoc)
     * @see org.javarosa.clforms.storage.MetaDataObject#readExternal(java.io.DataInputStream)
     */
    public void readExternal(DataInputStream in) throws IOException
    {
    	super.readExternal(in);
        this.formName = in.readUTF();
        this.version = in.readInt();
        this.type = in.readInt();
    }

   
    /* (non-Javadoc)
     * @see org.javarosa.clforms.storage.MetaDataObject#writeExternal(java.io.DataOutputStream)
     */
    public void writeExternal(DataOutputStream out) throws IOException
    {
    	super.writeExternal(out);
        out.writeUTF(this.formName);
        out.writeInt(this.version);
        out.writeInt(type);
    }

    public void setMetaDataParameters(Object object)
    {
        FormDef form = (FormDef)object;
        this.setName(form.getName());
        
    }



}
