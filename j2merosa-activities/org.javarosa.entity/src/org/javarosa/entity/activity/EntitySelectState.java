package org.javarosa.entity.activity;
/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import java.util.Vector;

import javax.microedition.lcdui.Displayable;

import org.javarosa.core.api.State;
import org.javarosa.core.services.storage.utilities.IRecordStoreEnumeration;
import org.javarosa.core.services.storage.utilities.RMSUtility;
import org.javarosa.core.services.storage.utilities.RecordStorageException;
import org.javarosa.entity.model.IEntity;
import org.javarosa.entity.model.view.EntitySelectDetailPopup;
import org.javarosa.entity.model.view.EntitySelectView;
import org.javarosa.entity.util.IEntityFilter;
import org.javarosa.j2me.view.J2MEDisplay;

public class EntitySelectState implements State<EntitySelectTransitions> {
	private EntitySelectTransitions transitions;
	
	private Displayable activeView;
	private EntitySelectView selView;
	
	private RMSUtility entityRMS;
	private IEntity entityPrototype;
	
	boolean immediatelySelectNewlyCreated;
	boolean bailOnEmpty;
	IEntityFilter filter;
	
	Vector entities;
	
	public EntitySelectState (String title, RMSUtility entityRMS, IEntity entityPrototype) {
		this(title, entityRMS, entityPrototype, true, EntitySelectView.NEW_IN_LIST, false, null, null);
	}
	
	public EntitySelectState (String title, RMSUtility entityRMS, IEntity entityPrototype,
			boolean immediatelySelectNewlyCreated, int newMode, boolean bailOnEmpty, IEntityFilter filter, String styleKey) {
		this.entityRMS = entityRMS;
		this.entityPrototype = entityPrototype;

		this.immediatelySelectNewlyCreated = immediatelySelectNewlyCreated;
		this.bailOnEmpty = bailOnEmpty;
		this.filter = filter;

		selView = new EntitySelectView(this, title, entityPrototype.entityType(), newMode);
		selView.setStyleKey(styleKey); //droos: what is this?
	}

	public void enter (EntitySelectTransitions transitions) {
		this.transitions = transitions;
	}
	
	public void start () {
		loadEntities();

		if(entities.isEmpty() && bailOnEmpty && selView.newMode == EntitySelectView.NEW_DISALLOWED) {
			transitions.empty();
			return;
		} 
		
		selView.init();
		showList();
	}

	private void loadEntities () {
		entities = new Vector();
		
		IRecordStoreEnumeration recenum = entityRMS.enumerateMetaData();
		while (recenum.hasNextElement()) {
			try {
				loadEntity(recenum.nextRecordId());
			} catch (RecordStorageException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void loadEntity (int recordID) {
		IEntity entity = entityPrototype.factory(recordID);
		entity.readEntity(entity.fetchRMS(entityRMS));
		if(filter == null || filter.isPermitted(entity)) {
			entities.addElement(entity);		
		}
	}
	
	public void setView (Displayable view) {
		activeView = view;
		J2MEDisplay.setView(view);
	}

	public void newEntity (int newEntityID) {
		if (immediatelySelectNewlyCreated) {
			entityChosen(newEntityID);
		} else {
			loadEntity(newEntityID);
			selView.refresh(newEntityID);
			showList();
		}
	}
	
	public Vector search (String key) {
		Vector matches = new Vector();
		
		if (key == null || key.equals("")) {
			for (int i = 0; i < entities.size(); i++)
				matches.addElement(new Integer(i));
		} else {
			for (int i = 0; i < entities.size(); i++) {
				IEntity entity = (IEntity)entities.elementAt(i);
				
				if (entity.matchID(key) || entity.matchName(key)) {
					matches.addElement(new Integer(i));
				}
			}
		}
		
		return matches;
	}
	
	public void showList () {
		selView.show();
	}
	
	public void itemSelected (int i) {
		IEntity entity = (IEntity)entities.elementAt(i);
		EntitySelectDetailPopup psdp = new EntitySelectDetailPopup(this, entity, entityPrototype, entityRMS);
		psdp.show();
	}
	
	public void entityChosen (int entityID) {
		transitions.entitySelected(entityID);
	}
	
	public void newEntity () {
		transitions.newEntity();
	}
	
	public void exit () {
		transitions.cancel();
	}
	
	public String[] getDataFields (int i) {
		return ((IEntity)entities.elementAt(i)).getShortFields();
	}
	
	public String[] getTitleData () {
		return entityPrototype.getHeaders(false);
	}
	
	public String getDataName (int i) {
		return ((IEntity)entities.elementAt(i)).getName();
	}
	
	public String getDataID (int i) {
		return ((IEntity)entities.elementAt(i)).getID();
	}	

	public int getRecordID (int i) {
		return ((IEntity)entities.elementAt(i)).getRecordID();
	}
	
	public String getEntityType() {
		return entityPrototype.entityType();
	}

}
