package org.javarosa.core.model.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.services.storage.utilities.MetaDataObject;

/**
 * Serializable Meta Data object for form definition records that 
 * are saved in persistent storage.
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
    
    /**
     * Creates a new meta data entry for a form definition
     * @param form The form def whose metadata is to be captured
     */
    public FormDefMetaData(FormDef form)
    {
        this.formName = form.getName();
        
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

    /*
     * (non-Javadoc)
     * @see org.javarosa.core.services.storage.utilities.MetaDataObject#setMetaDataParameters(java.lang.Object)
     */
    public void setMetaDataParameters(Object object)
    {
        FormDef form = (FormDef)object;
        this.setName(form.getName());
        
    }



}
