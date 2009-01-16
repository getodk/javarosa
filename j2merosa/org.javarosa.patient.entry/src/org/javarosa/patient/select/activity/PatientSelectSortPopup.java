package org.javarosa.patient.select.activity;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import de.enough.polish.ui.ItemStateListener;

import org.javarosa.core.api.IView;

import de.enough.polish.ui.Choice;
import de.enough.polish.ui.ChoiceGroup;
import de.enough.polish.ui.Form;
import de.enough.polish.ui.Item;

public class PatientSelectSortPopup extends Form implements IView, CommandListener, ItemStateListener {
	private PatientSelectView psv;
	private PatientSelectActivity psa;
	
    private ChoiceGroup sortField;
    private Command cancelCmd;

    public PatientSelectSortPopup (PatientSelectView psv, PatientSelectActivity psa) {
		//#style patselSortPopup
		super("Sort by...");

		this.psv = psv;
		this.psa = psa;
		
		sortField = new ChoiceGroup("", Choice.EXCLUSIVE);
		sortField.append("Name", null);
		sortField.append("ID", null);
		sortField.setSelectedIndex(psv.sortByName ? 0 : 1, true);
		append(sortField);
		sortField.setItemStateListener(this);
		
		cancelCmd = new Command("Cancel", Command.CANCEL, 1);
		addCommand(cancelCmd);
		setCommandListener(this);
    }

	public Object getScreenObject() {
		return this;
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
			psv.changeSort(sortField.getSelectedIndex() == 0);
			psa.showList();
		}
	}
}
