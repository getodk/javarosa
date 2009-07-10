/**
 * 
 */
package org.javarosa.forms.review.util;

import java.util.Date;

import org.javarosa.entity.model.IEntity;
import org.javarosa.entity.util.IEntityFilter;

/**
 * @author ctsims
 *
 */
public class DataModelDateFilter implements IEntityFilter {
	
	private Date startDate;
	private Date endDate;
	
	public DataModelDateFilter(Date startDate, Date endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.util.IEntityFilter#isPermitted(org.javarosa.entity.model.IEntity)
	 */
	public boolean isPermitted(IEntity entity) {
		if(!(entity instanceof DataModelEntity)) {
			return false;
		}
		
		DataModelEntity dme = (DataModelEntity)entity;
		Date modelDate = dme.getDateSaved();
		
		if(modelDate.getTime() < startDate.getTime()) {
			return false;
		} else if(modelDate.getTime() > endDate.getTime()) {
			return false;
		}
		return true;
	}

}
