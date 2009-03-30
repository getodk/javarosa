/**
 * 
 */
package org.javarosa.cases.util;

import java.io.IOException;

import org.javarosa.cases.model.Case;
import org.javarosa.core.services.storage.utilities.RMSUtility;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.entity.model.IEntity;

/**
 * @author Clayton Sims
 * @date Mar 19, 2009 
 *
 */
public class CaseEntity implements IEntity {
	protected String name;
	protected String id;
	protected String type;
	
	protected int recordId;
	
	protected boolean closed;
	

	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#entityType()
	 */
	public String entityType() {
		return "Case";
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#factory(int)
	 */
	public IEntity factory(int recordID) {
		CaseEntity entity = new CaseEntity();
		entity.recordId = recordID;
		return entity;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#fetchRMS(org.javarosa.core.services.storage.utilities.RMSUtility)
	 */
	public Object fetchRMS(RMSUtility rmsu) {
		Case c = new Case();
		try {
			rmsu.retrieveFromRMS(recordId, c);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DeserializationException e) {
			e.printStackTrace();
		}
		return c;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#getID()
	 */
	public String getID() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#getHeaders(boolean)
	 */
	public String[] getHeaders(boolean detailed) {
		String [] headers;
		if(detailed) {
			headers = new String[] {"Name", "ID", "Date Opened", "Currently Open"};
		} else {
			headers = new String[] {"Name", "ID"};
		}
		return headers;
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#getLongFields(java.lang.Object)
	 */
	public String[] getLongFields(Object o) {
		Case c = (Case)o;
		String date;
		if(c.getDateOpened() == null) {
			date = "none";
		} else {
			date = c.getDateOpened().toString();
		}
		String open;
		if(c.isClosed()) {
			open = "No";
		} else {
			open = "Yes";
		}
		return new String[] {c.getName(), c.getId(), date, open};
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#getShortFields()
	 */
	public String[] getShortFields() {
		return new String[] {this.getName(), this.getID()};
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#getName()
	 */
	public String getName() {
		return name;
	}
	
	public boolean isClosed() {
		return closed;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#getRecordID()
	 */
	public int getRecordID() {
		return recordId;
	}
	/* (non-Javadoc)
	 * @see org.javarosa.patient.select.activity.IEntity#matchID(java.lang.String)
	 */
	public boolean matchID(String key) {
		//TODO: I don't really understand these methods. These should be matching
		//pretty broadly, but should be reevaluated once the method contract is clear.
		String[] fields = this.getShortFields();
		for(int i = 0; i < fields.length; ++i) {
			if(fields[i].indexOf(key) != -1) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.patient.select.activity.IEntity#matchName(java.lang.String)
	 */
	public boolean matchName(String key) {
		
		//TODO: I don't really understand these methods. These should be matching
		//pretty broadly, but should be reevaluated once the method contract is clear.
		return matchID(key);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#readEntity(java.lang.Object)
	 */
	public void readEntity(Object o) {
		Case c = (Case)o;
		this.name = c.getName();
		this.id = c.getId() == null? "" : c.getId();
		this.type = c.getTypeId();
		this.recordId = c.getRecordId();
		
		this.closed = c.isClosed();
	}

}
