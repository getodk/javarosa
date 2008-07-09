package org.javarosa.clforms.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;


public class ModelMetaData extends MetaDataObject {

	private String name = "";
	private int xformReference = 0;
	private Date dateSaved;


	public ModelMetaData() {

		// TODO Auto-generated constructor stub
	}

	public ModelMetaData(Model  model)
	{
		setMetaDataParameters(model);
	}
	public void setMetaDataParameters(Object originalObject) {
		Model model =(Model)originalObject;
		//model.loadName();
		this.name = model.getName();
		//System.out.println("ModelMETADATA setting xref to: "+model.getXformReference());
		this.xformReference = model.getXformReference();
		this.dateSaved = model.getDateSaved();
	}

	public void readExternal(DataInputStream in) throws IOException
    {
        this.name = in.readUTF();
        this.xformReference = in.readInt();
        this.dateSaved = new Date(in.readLong());
    }

    public void writeExternal(DataOutputStream out) throws IOException
    {
    	out.writeUTF(this.name);
        out.writeInt(this.xformReference);
        out.writeLong(this.dateSaved.getTime());

    }

    public int getXformReference() {
		return xformReference;
	}

	public void setXformReference(int xformReference) {
		this.xformReference = xformReference;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName()
    {
    	return this.name;
    }

    public Date getDateSaved() {
		return dateSaved;
	}

	public void setDateSaved(Date dateSaved) {
		this.dateSaved = dateSaved;
	}

	public String toString(){
    	return new String(super.toString()+"Name: "+this.getName()+"XformRef :"+this.getXformReference());
    }


}
