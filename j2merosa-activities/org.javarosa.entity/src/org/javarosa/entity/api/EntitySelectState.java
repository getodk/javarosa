package org.javarosa.entity.api;
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
import org.javarosa.core.services.storage.IStorageIterator;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.core.services.storage.Persistable;
import org.javarosa.entity.api.transitions.EntitySelectTransitions;
import org.javarosa.entity.model.Entity;
import org.javarosa.entity.model.view.EntitySelectDetailPopup;
import org.javarosa.entity.model.view.EntitySelectView;
import org.javarosa.entity.util.IEntityFilter;
import org.javarosa.j2me.view.J2MEDisplay;

public class EntitySelectState <E extends Persistable> implements State<EntitySelectTransitions> {
	private EntitySelectTransitions transitions;
	
	private EntitySelectView<E> selView;
	
	private IStorageUtility entityStorage;
	private Entity<E> entityPrototype;
	
	boolean immediatelySelectNewlyCreated;
	boolean bailOnEmpty;
	
	Vector<Entity<E>> entities;
	
	public EntitySelectState (String title, IStorageUtility entityStorage, Entity<E> entityPrototype) {
		this(title, entityStorage, entityPrototype, EntitySelectView.NEW_IN_LIST, true, false);
	}
	
	public EntitySelectState (String title, IStorageUtility entityStorage, Entity<E> entityPrototype, int newMode, boolean immediatelySelectNewlyCreated) {
		this(title, entityStorage, entityPrototype, newMode, immediatelySelectNewlyCreated, false);
	}

	public EntitySelectState (String title, IStorageUtility entityStorage, Entity<E> entityPrototype,
			int newMode, boolean immediatelySelectNewlyCreated, boolean bailOnEmpty) {
		this.entityStorage = entityStorage;
		this.entityPrototype = entityPrototype;

		this.immediatelySelectNewlyCreated = immediatelySelectNewlyCreated;
		this.bailOnEmpty = bailOnEmpty;

		selView = new EntitySelectView<E>(this, entityPrototype, title, newMode);
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
		entities = new Vector<Entity<E>>();
		IEntityFilter<? super E> filter = entityPrototype.getFilter();
		
		IStorageIterator ei = entityStorage.iterate();
		while (ei.hasMore()) {
			E obj = (E)ei.nextRecord();
			
			if (filter == null || filter.isPermitted(obj)) {
				loadEntity(obj);
			}
		}
	}
	
	private void loadEntity (E obj) {
		Entity<E> entity = entityPrototype.factory();
		entity.readEntity(obj);
		entities.addElement(entity);
	}
	
	public void setView (Displayable view) {
		J2MEDisplay.setView(view);
	}

	public void newEntity (int newEntityID) {
		//note: it is assumed that the newly created entity satisfies any filters in effect
		if (immediatelySelectNewlyCreated) {
			entityChosen(newEntityID);
		} else {
			E obj = (E)entityStorage.read(newEntityID);
			loadEntity(obj);
			selView.refresh(newEntityID);
			showList();
		}
	}
	
	public Vector<Integer> search (String key) {
		Vector<Integer> matches = new Vector<Integer>();
		
		if (key == null || key.equals("")) {
			for (int i = 0; i < entities.size(); i++)
				matches.addElement(new Integer(i));
		} else {
			for (int i = 0; i < entities.size(); i++) {
				Entity<E> entity = entities.elementAt(i);
				if (entity.match(key)) {
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
		Entity<E> entity = entities.elementAt(i);
		EntitySelectDetailPopup<E> psdp = new EntitySelectDetailPopup<E>(this, entity, entityStorage);
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
		return entities.elementAt(i).getShortFields();
	}
	
	public String[] getTitleData () {
		return entityPrototype.getHeaders(false);
	}
	
	public Entity<E> getEntity (int i) {
		return entities.elementAt(i);
	}	

	public int getRecordID (int i) {
		return entities.elementAt(i).getRecordID();
	}
}
