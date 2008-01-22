/*
 * XFormMetaData.java
 * 
 * Created on 2007/10/31, 11:31:57
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.javarosa.clforms.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.clforms.api.Form;

/**
 *
 * @author Munier
 */
/**
 * @author Ulrike Rivett
 *
 */
public class XFormMetaData extends MetaDataObject
{

    private String xformName = ""; //the name of the XForm being referenced
    private int version = 0; //the version of the XForm
    private int type = 0; //the XForm Type
    
    public String toString(){
    	return new String (super.toString()+" name: "+this.xformName+" version: "+this.version
    			+ " type: "+this.type);
    }
    
    public XFormMetaData()
    {
        
    }
    
    public XFormMetaData(Form form)
    {
        this.xformName = form.getName();
        
    }
    
    public void setName(String name)
    {
        this.xformName = name;
    }
    
    public String getName()
    {
       return this.xformName; 
    }
    
    /* (non-Javadoc)
     * @see org.javarosa.clforms.storage.MetaDataObject#readExternal(java.io.DataInputStream)
     */
    public void readExternal(DataInputStream in) throws IOException
    {
    	super.readExternal(in);
        this.xformName = in.readUTF();
        this.version = in.readInt();
        this.type = in.readInt();
    }

   
    /* (non-Javadoc)
     * @see org.javarosa.clforms.storage.MetaDataObject#writeExternal(java.io.DataOutputStream)
     */
    public void writeExternal(DataOutputStream out) throws IOException
    {
    	super.writeExternal(out);
        out.writeUTF(this.xformName);
        out.writeInt(this.version);
        out.writeInt(type);
    }

    public void setMetaDataParameters(Object object)
    {
        Form form = (Form)object;
        this.setName(form.getName());
        
    }



}
