/*
 * FormDataMetaData.java
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
import java.util.Date;

import org.javarosa.core.model.FormData;
import org.javarosa.core.services.storage.utilities.MetaDataObject;

/**
 *
 * @author Clayton Sims
 */
public class FormDataMetaData extends MetaDataObject
{

    private String formName = ""; //the name of the FormData being referenced
    private int version = 0; //the version of the FormData
    private int type = 0; //the FormData Type
	private Date dateSaved;
	private int formIdReference;

    
    public String toString(){
    	return new String (super.toString()+" name: "+this.formName+" version: "+this.version
    			+ " type: "+this.type);
    }
    
    public FormDataMetaData()
    {
        
    }
    
    public FormDataMetaData(FormData form)
    {
        this.formName = form.getDef().getName() + form.getRecordId();
        this.dateSaved = form.getDateSaved();
        this.formIdReference = form.getRecordId();
        
    }
    
    public void setName(String name)
    {
        this.formName = name;
    }
    
    public String getName()
    {
       return this.formName; 
    }
    
    public int getFormIdReference() {
    	return formIdReference;
    }
    
    public Date getDateSaved() {
    	return dateSaved;
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
        this.dateSaved = new Date(in.readLong());
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
        out.writeLong(this.dateSaved.getTime());
    }

    public void setMetaDataParameters(Object object)
    {
        FormData form = (FormData)object;
        this.setName(form.getDef().getName() + form.getRecordId());
        
    }



}
