/**
 * 
 */
package org.javarosa.forms.review.util;

import java.io.IOException;
import java.util.Date;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.storage.DataModelTreeRMSUtility;
import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.services.storage.utilities.RMSUtility;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.entity.model.IEntity;

/**
 * @author ctsims
 *
 */
public class DataModelEntity implements IEntity {
	
	private String modelName;
	private Date dateSaved;
	private int recordId;
	private int formId;

	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#entityType()
	 */
	public String entityType() {
		return "Data Model";
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#factory(int)
	 */
	public IEntity factory(int recordID) {
		DataModelEntity entity = new DataModelEntity();
		entity.recordId = recordID;
		return entity;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#fetchRMS(org.javarosa.core.services.storage.utilities.RMSUtility)
	 */
	public Object fetchRMS(RMSUtility rmsu) {
		DataModelTreeRMSUtility rms = (DataModelTreeRMSUtility)rmsu;
		DataModelTree tree = new DataModelTree();
		try {
			rms.retrieveFromRMS(this.recordId, tree);
			return tree;
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Error loading DataModelTree from RMS to load as an Entity.");
		} catch (DeserializationException e) {
			e.printStackTrace();
			throw new RuntimeException("Error loading DataModelTree from RMS to load as an Entity.");
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#getHeaders(boolean)
	 */
	public String[] getHeaders(boolean detailed) {
		if(detailed) {
			return new String[] {JavaRosaServiceProvider.instance().localize("model.name.long"),
					JavaRosaServiceProvider.instance().localize("model.date.long"),
					JavaRosaServiceProvider.instance().localize("model.time.long")};
		} else {
			return new String[] {JavaRosaServiceProvider.instance().localize("name"),
								JavaRosaServiceProvider.instance().localize("date"),
								JavaRosaServiceProvider.instance().localize("time")};
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#getID()
	 */
	public String getID() {
		return String.valueOf(recordId);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#getLongFields(java.lang.Object)
	 */
	public String[] getLongFields(Object o) {
		DataModelTree tree = (DataModelTree)o;
		return new String[] {tree.getName(), DateUtils.getDifferenceFromToday(tree.getDateSaved()), DateUtils.formatTime(tree.getDateSaved(), DateUtils.FORMAT_HUMAN_READABLE_SHORT)};

	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#getName()
	 */
	public String getName() {
		return modelName;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#getRecordID()
	 */
	public int getRecordID() {
		return recordId;
	}
	
	public Date getDateSaved() {
		return new Date(dateSaved.getTime());
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#getShortFields()
	 */
	public String[] getShortFields() {
		return new String[] {modelName, DateUtils.getDifferenceFromToday(dateSaved), DateUtils.formatTime(dateSaved, DateUtils.FORMAT_HUMAN_READABLE_SHORT)};
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#matchID(java.lang.String)
	 */
	public boolean matchID(String key) {
		return matchName(key);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#matchName(java.lang.String)
	 */
	public boolean matchName(String key) {
		String[] fields = getShortFields();
		for(int i = 0; i < fields.length ; ++i) {
			if(fields[i].toLowerCase().startsWith(key.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#readEntity(java.lang.Object)
	 */
	public void readEntity(Object o) {
		DataModelTree tree = (DataModelTree)o;
		dateSaved = tree.getDateSaved();
		modelName = tree.getName();
		recordId = tree.getRecordId();
		formId = tree.getFormId();
	}

}
