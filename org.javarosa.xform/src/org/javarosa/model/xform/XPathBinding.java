/**
 * 
 */
package org.javarosa.model.xform;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.IBinding;
import org.javarosa.core.model.utils.ExternalizableHelper;
import org.javarosa.core.util.Utilities;
import org.javarosa.core.util.db.PersistentHelper;


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
		//#if debug.output==verbose
		System.out.println("setting bind "+this.id+ " req="+attributeValue);
		//#endif
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
	public void readExternal(DataInputStream dis) throws IOException, IllegalAccessException, InstantiationException{
		if(!PersistentHelper.isEOF(dis)){
			setId(ExternalizableHelper.readUTF(dis));
			setNodeset(ExternalizableHelper.readUTF(dis));
			setType(ExternalizableHelper.readUTF(dis));
			setRelevancy(ExternalizableHelper.readUTF(dis));
			setRequired(dis.readBoolean());
			preload = ExternalizableHelper.readUTF(dis);
			preloadParams = ExternalizableHelper.readUTF(dis);
		}
	}

	/**
	 * Write the object to stream.
	 */
	public void writeExternal(DataOutputStream dos) throws IOException {
		ExternalizableHelper.writeUTF(dos, getId());
		ExternalizableHelper.writeUTF(dos, getNodeset());
		ExternalizableHelper.writeUTF(dos, getType());
		ExternalizableHelper.writeUTF(dos, getRelevancy());
		dos.writeBoolean(isRequired());
		
		ExternalizableHelper.writeUTF(dos, preload);
		ExternalizableHelper.writeUTF(dos, preloadParams);
	}

}
