/**
 * 
 */
package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.util.Utilities;
import org.javarosa.util.db.PersistentHelper;


/**
 * 
 */
public class XPathBinding implements IBinding {

	private String id;
	private String nodeset;
	private String type;
	private String relevancy;
	private boolean required = false;

	public String preload;
	public String preloadParams;
	
	private String xpathBinding;
	private String relevantString;
	
	/**
	 * 
	 */
	public XPathBinding() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getId() {
		return this.id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getNodeset() {
		return this.nodeset;
	}
	public void setNodeset(String nodeset) {
		this.nodeset = nodeset;
	}
	public String getType() {
		return this.type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getRelevancy() {
		return this.relevancy;
	}
	public void setRelevancy(String relevancy) {
		this.relevancy = relevancy;
	}

	public String toString() {
		return "id:"+id + " nodset: " + nodeset + " rel: " + relevancy + " req: "+required+" type " + type;
	}

	public void setRequired(String attributeValue) throws Exception {
		System.out.println("setting bind "+this.id+ " req="+attributeValue);
		this.required = Utilities.getBoolean(attributeValue);
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public boolean isRequired() {
		return required;
	}
	
	/**
	 * Reads the object from stream.
	 */
	public void read(DataInputStream dis) throws IOException, IllegalAccessException, InstantiationException{
		if(!PersistentHelper.isEOF(dis)){
			setId(dis.readUTF());
			setNodeset(dis.readUTF());
			setType(dis.readUTF());
			setRelevancy(dis.readUTF());
			setRequired(dis.readBoolean());
			preload = dis.readUTF();
			preloadParams = dis.readUTF();
		}
	}

	/**
	 * Write the object to stream.
	 */
	public void write(DataOutputStream dos) throws IOException {
		dos.writeUTF(getId());
		dos.writeUTF(getNodeset());
		dos.writeUTF(getType());
		dos.writeUTF(getRelevancy());
		dos.writeBoolean(isRequired());
		
		dos.writeUTF(preload);
		dos.writeUTF(preloadParams);
	}

}
