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

package org.javarosa.entity.model.view;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.javarosa.core.services.storage.Persistable;
import org.javarosa.entity.api.EntitySelectController;
import org.javarosa.entity.model.Entity;

import de.enough.polish.ui.Choice;
import de.enough.polish.ui.ChoiceGroup;
import de.enough.polish.ui.Form;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.ItemStateListener;

public class EntitySelectSortPopup<E extends Persistable> extends Form implements CommandListener, ItemStateListener {
	private EntitySelectView<E> psv;
	private EntitySelectController<E> psa;
	private Entity<E> entityPrototype;
	
    private ChoiceGroup sortField;
    private Command cancelCmd;

    public EntitySelectSortPopup (EntitySelectView<E> psv, EntitySelectController<E> psa, Entity<E> entityPrototype) {
		//#style patselSortPopup
		super("Sort by...");

		this.psv = psv;
		this.psa = psa;
		this.entityPrototype = entityPrototype;
		
		sortField = new ChoiceGroup("", Choice.EXCLUSIVE);

		String[] sortFields = entityPrototype.getSortFields();
		String[] sortFieldNames = entityPrototype.getSortFieldNames();
		for (int i = 0; i < sortFieldNames.length; i++) {
			sortField.append(sortFieldNames[i], null);
			if (sortFields[i].equals(psv.getSortField())) {
				sortField.setSelectedIndex(i, true);
			}
		}
		
		append(sortField);
		sortField.setItemStateListener(this);
		
		cancelCmd = new Command("Cancel", Command.CANCEL, 1);
		addCommand(cancelCmd);
		setCommandListener(this);
    }
    
    public void show () {
    	psa.setView(this);
    }
	
	public void commandAction(Command cmd, Displayable d) {
		if (d == this) {
			if (cmd == cancelCmd) {
				psa.showList();
			}
		}
	}

	public void itemStateChanged(Item item) {
		if (item == sortField) {
			psv.changeSort(entityPrototype.getSortFields()[sortField.getSelectedIndex()]);
			psa.showList();
		}
	}
}
