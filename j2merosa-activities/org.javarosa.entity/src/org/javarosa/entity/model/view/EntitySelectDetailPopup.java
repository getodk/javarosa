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



import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.core.services.storage.Persistable;
import org.javarosa.entity.api.EntitySelectController;
import org.javarosa.entity.model.Entity;

import de.enough.polish.ui.Container;
import de.enough.polish.ui.Form;
import de.enough.polish.ui.StringItem;

public class EntitySelectDetailPopup<E extends Persistable> extends Form implements CommandListener {
	EntitySelectController<E> psa;
	
	int recordID;
	String[] headers;
	String[] data;
	
	Command okCmd;
	Command backCmd;
	
	public EntitySelectDetailPopup (EntitySelectController<E> psa, Entity<E> entity, IStorageUtility storage) {
		super(entity.entityType() + " Detail");
		
		this.psa = psa;
		
		recordID = entity.getRecordID();
		headers = entity.getHeaders(true);
		data = entity.getLongFields((E)storage.read(recordID));
		
		okCmd = new Command("OK", Command.OK, 1);
		backCmd = new Command("Back", Command.BACK, 1);
		addCommand(okCmd);
		addCommand(backCmd);	
		setCommandListener(this);
		
		loadData();
	}
	
	public void loadData() {
		for (int i = 0; i < data.length; i++) {
			//TODO: make these styles dynamically configurable as well (like on the main list view)
			//#style patselDetailRow
			Container c = new Container(false);
			c.add(new StringItem("", headers[i] + ":"));
			c.add(new StringItem("", data[i]));
			append(c);
		}
	}
	
	public void show () {
		psa.setView(this);
	}

	public void commandAction(Command cmd, Displayable d) {
		if (d == this) {
			if (cmd == okCmd) {
				psa.entityChosen(recordID);
			} else if (cmd == backCmd) {
				psa.showList();
			}
		}		
	}
	
	public boolean handleKeyReleased (int keyCode, int gameAction) {
		boolean ret = super.handleKeyReleased(keyCode, gameAction);
		
		if (gameAction == Canvas.FIRE) {
			commandAction(okCmd, this);
			return true;
		}
		
		return ret;
	}
}
