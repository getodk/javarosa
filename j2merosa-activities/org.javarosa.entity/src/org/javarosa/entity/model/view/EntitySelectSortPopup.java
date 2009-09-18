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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.javarosa.entity.api.EntitySelectState;
import org.javarosa.entity.util.IEntityComparator;

import de.enough.polish.ui.Choice;
import de.enough.polish.ui.ChoiceGroup;
import de.enough.polish.ui.Form;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.ItemStateListener;

public class EntitySelectSortPopup extends Form implements CommandListener, ItemStateListener {
	private EntitySelectView psv;
	private EntitySelectState psa;
	
    private ChoiceGroup sortField;
    private Command cancelCmd;
    private Hashtable map;

    public EntitySelectSortPopup (EntitySelectView psv, EntitySelectState psa, Vector comparators) {
		//#style patselSortPopup
		super("Sort by...");

		this.psv = psv;
		this.psa = psa;
		
		sortField = new ChoiceGroup("", Choice.EXCLUSIVE);
		map = new Hashtable();
		
		for(Enumeration en = comparators.elements(); en.hasMoreElements();) {
			IEntityComparator comp = (IEntityComparator)en.nextElement();
			int index = sortField.append(comp.getName(), null);
			map.put(new Integer(index), comp);
			if(psv.currentSort.equals(comp)) {
				sortField.setSelectedIndex(index, true);
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
			System.out.println(sortField.getSelectedIndex());
			psv.changeSort((IEntityComparator)map.get(new Integer(sortField.getSelectedIndex())));
			psa.showList();
		}
	}
}
