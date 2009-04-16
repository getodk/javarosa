/**
 * 
 */
package org.javarosa.cases.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.model.util.restorable.Restorable;
import org.javarosa.core.model.util.restorable.RestoreUtils;
import org.javarosa.core.services.storage.utilities.IDRecordable;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapMapPoly;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.xform.util.XFormAnswerDataParser;
import org.javarosa.xform.util.XFormAnswerDataSerializer;

/**
 * @author Clayton Sims
 * @date Mar 19, 2009 
 *
 */
public class Case implements Externalizable, IDRecordable, Restorable {
	
	private String typeId;
	private String id;
	private String name;
	
	private boolean closed = false;
	
	private Date dateOpened;
	
	int recordId;

	Hashtable data = new Hashtable();
	
	/**
	 * NOTE: This constructor is for serialization only.
	 */
	public Case() {
		dateOpened = new Date();
	}
	
	public Case(String name, String typeId) {
		this.name = name;
		this.typeId = typeId;
		dateOpened = new Date();
	}
	
	/**
	 * @return the typeId
	 */
	public String getTypeId() {
		return typeId;
	}

	/**
	 * @param typeId the typeId to set
	 */
	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	/**
	 * @return The name of this case
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param The name of this case
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return True if this case is closed, false otherwise.
	 */
	public boolean isClosed() {
		return closed;
	}

	/**
	 * @param Whether or not this case should be recorded as closed
	 */
	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	/**
	 * @return the recordId
	 */
	public int getRecordId() {
		return recordId;
	}
	
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the dateOpened
	 */
	public Date getDateOpened() {
		return dateOpened;
	}

	/**
	 * @param dateOpened the dateOpened to set
	 */
	public void setDateOpened(Date dateOpened) {
		this.dateOpened = dateOpened;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#readExternal(java.io.DataInputStream, org.javarosa.core.util.externalizable.PrototypeFactory)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		typeId = in.readUTF();
		id = (String)ExtUtil.read(in, new ExtWrapNullable(String.class));
		name =in.readUTF();
		closed = in.readBoolean();
		dateOpened = new Date(in.readLong());
		recordId = in.readInt();
		data = (Hashtable)ExtUtil.read(in, new ExtWrapMapPoly(String.class, true));
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		out.writeUTF(typeId);
		ExtUtil.write(out, new ExtWrapNullable(id));
		out.writeUTF(name);
		out.writeBoolean(closed);
		out.writeLong(dateOpened.getTime());
		out.writeInt(recordId);
		ExtUtil.write(out, new ExtWrapMapPoly(data));

	}

	public void setRecordId(int recordId) {
		this.recordId = recordId;
	}
	
	public void setProperty(String key, Object value) {
		this.data.put(key, value);
	}
	
	public Object getProperty(String key) {
		return data.get(key);
	}

	public DataModelTree exportData() {
		DataModelTree dm = RestoreUtils.createDataModel(this);
		RestoreUtils.addData(dm, "case-id", id);
		RestoreUtils.addData(dm, "case-type-id", typeId);
		RestoreUtils.addData(dm, "name", name);
		RestoreUtils.addData(dm, "dateopened", dateOpened);
		RestoreUtils.addData(dm, "closed", new Boolean(closed));
		
		for (Enumeration e = data.keys(); e.hasMoreElements(); ) {
			String key = (String)e.nextElement();
			RestoreUtils.addData(dm, "other/" + key + "/type", new Integer(RestoreUtils.getDataType(data.get(key))));
			RestoreUtils.addData(dm, "other/" + key + "/data", data.get(key));
		}
				
		return dm;

	}

	public String getRestorableType() {
		return "case";
	}

	public void importData(DataModelTree dm) {
		id = (String)RestoreUtils.getValue("case-id", dm);
		typeId = (String)RestoreUtils.getValue("case-type-id", dm);		
		name = (String)RestoreUtils.getValue("name", dm);		
		dateOpened = (Date)RestoreUtils.getValue("dateopened", dm);		
        closed = ((Boolean)RestoreUtils.getValue("closed", dm)).booleanValue();			
        
        
        // Clayton Sims - Apr 14, 2009 : NOTE: this is unfortunate, but we need 
        // to be able to unparse.
        XFormAnswerDataSerializer s = new XFormAnswerDataSerializer();
        TreeElement e = dm.resolveReference(RestoreUtils.absRef("other", dm));
        for (int i = 0; i < e.getNumChildren(); i++) {
        	TreeElement child = (TreeElement)e.getChildren().elementAt(i);
        	String name = child.getName();
        	int dataType = ((Integer)RestoreUtils.getValue("other/"+name+"/type", dm)).intValue();
        	String value = (String)RestoreUtils.getValue("other/"+ name+"data", dm);
        	if(dataType == Constants.DATATYPE_CHOICE_LIST) {
        		//XFormAnswerDataParser.getAnswerData(value, dataType, q);
        	}
        }
	}

	public void templateData(DataModelTree dm, TreeReference parentRef) {
		RestoreUtils.applyDataType(dm, "case-id", parentRef, String.class);
		RestoreUtils.applyDataType(dm, "case-type-id", parentRef, String.class);
		RestoreUtils.applyDataType(dm, "name", parentRef, String.class);
		RestoreUtils.applyDataType(dm, "dateopened", parentRef, Date.class);
		RestoreUtils.applyDataType(dm, "closed", parentRef, Boolean.class);
		
		// other/* defaults to string
	}

}
