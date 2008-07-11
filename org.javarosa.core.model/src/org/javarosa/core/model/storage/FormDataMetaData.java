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
 * FormData RMS serialization meta data object.
 * 
 * Contains the form's name the date it was saved, and 
 * the Id of the form data in RMS
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
    
    /**
     * Creates an Empty meta data object
     */
    public FormDataMetaData()
    {
        
    }
    
    /**
     * Creates a meta data object for the form data object given.
     * 
     * @param form The form whose meta data this object will become
     */
    public FormDataMetaData(FormData form)
    {
        this.formName = form.getDef().getName() + form.getRecordId();
        this.dateSaved = form.getDateSaved();
        this.formIdReference = form.getRecordId();
        
    }
    
    /**
     * @param name Sets the name for the form this meta data object represents
     */
    public void setName(String name)
    {
        this.formName = name;
    }
    
    /**
     * @return the name of the form represented by this meta data
     */
    public String getName()
    {
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

    /*
     * (non-Javadoc)
     * @see org.javarosa.core.services.storage.utilities.MetaDataObject#setMetaDataParameters(java.lang.Object)
     */
    public void setMetaDataParameters(Object object)
    {
        FormData form = (FormData)object;
        this.setName(form.getDef().getName() + form.getRecordId());
        
    }



}
