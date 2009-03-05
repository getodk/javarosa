package org.javarosa.entity.model.view;



import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.javarosa.core.api.IView;
import org.javarosa.core.services.storage.utilities.RMSUtility;
import org.javarosa.entity.activity.EntitySelectActivity;
import org.javarosa.entity.model.IEntity;

import de.enough.polish.ui.Container;
import de.enough.polish.ui.Form;
import de.enough.polish.ui.StringItem;

public class EntitySelectDetailPopup extends Form implements IView, CommandListener {
	EntitySelectActivity psa;
	
	int recordID;
	String[] headers;
	String[] data;
	
	Command okCmd;
	Command backCmd;
	
	public EntitySelectDetailPopup (EntitySelectActivity psa, IEntity entity, RMSUtility entityRMS) {
		super(entity.entityType() + " Detail");
		
		this.psa = psa;
		
		recordID = entity.getRecordID();
		Object o = entity.fetchRMS(entityRMS);
		headers = entity.getHeaders(true);
		data = entity.getLongFields(o);
		
		okCmd = new Command("OK", Command.OK, 1);
		backCmd = new Command("Back", Command.BACK, 1);
		addCommand(okCmd);
		addCommand(backCmd);	
		setCommandListener(this);
		
		loadData();
	}
	
	public void loadData() {
		for (int i = 0; i < data.length; i++) {
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

	public Object getScreenObject() {
		return this;
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
