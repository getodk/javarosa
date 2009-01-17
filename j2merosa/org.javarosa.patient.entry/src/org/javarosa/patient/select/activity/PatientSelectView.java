package org.javarosa.patient.select.activity;

import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;

import org.javarosa.core.api.IView;

import de.enough.polish.ui.Container;
import de.enough.polish.ui.FramedForm;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.ItemStateListener;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.TextField;

public class PatientSelectView extends FramedForm implements IView, ItemStateListener, CommandListener {
	//#if polish.ScreenWidth > 128 || device.identifier == 'Generic/DefaultColorPhone'
	private static final int MAX_ROWS_ON_SCREEN = 11;
	private static final int SCROLL_INCREMENT = 5;	
	//#else
	private static final int MAX_ROWS_ON_SCREEN = 5;
	private static final int SCROLL_INCREMENT = 3;	
	//#endif
	
	private static final boolean ALLOW_NEW = true;
	private static final int INDEX_NEW = -1;
	
	private PatientSelectActivity controller;
	public String entityType;
	
	private TextField tf;
	private Container list;
	private Command exitCmd;
	private Command sortCmd;
	
	private int firstIndex;
	private int selectedIndex;
	
	public boolean sortByName = true; //if false, sort by ID
	public boolean wrapAround = false; //TODO: support this
	
	private Vector rowIDs; //index into data corresponding to current matches
		
	public PatientSelectView(PatientSelectActivity controller, String title) {
		super(title);
		this.controller = controller;

		tf = new TextField("Find:  ", "", 20, TextField.ANY);
		tf.setInputMode(TextField.MODE_UPPERCASE);
		tf.setItemStateListener(this);
		
		list = new Container(false);
		
        append(Graphics.BOTTOM, tf);
        append(Graphics.VCENTER, list);
                
        exitCmd = new Command("Cancel", Command.CANCEL, 4);
        sortCmd = new Command("Sort", Command.SCREEN, 3);
        addCommand(exitCmd);
        addCommand(sortCmd);
        this.setCommandListener(this);
        
        rowIDs = new Vector();
	}

	public void init () {
        selectedIndex = 0;
        firstIndex = 0;

        refresh();
	}
	
	public void refresh () {
		refresh(-1);
	}
	
	public void refresh (int selectedEntity) {
		if (selectedEntity == -1)
			selectedEntity = getSelectedEntity();
		
        getMatches(tf.getText());
        selectEntity(selectedEntity);
        refreshList();
	}
	
	public void show () {
		controller.setView(this);
	}
	
	public Object getScreenObject() {
		return this;
	}

	private void getMatches (String key) {
		rowIDs = controller.search(key);
		sortRows();
		if (ALLOW_NEW) {
			rowIDs.addElement(new Integer(INDEX_NEW));
		}
	}

	private void stepIndex (boolean increment) {
		selectedIndex += (increment ? 1 : -1);
		if (selectedIndex < 0) {
			selectedIndex = 0;
		} else if (selectedIndex >= rowIDs.size()) {
			selectedIndex = rowIDs.size() - 1;
		}
		
		if (selectedIndex < firstIndex) {
			firstIndex -= SCROLL_INCREMENT;
			if (firstIndex < 0)
				firstIndex = 0;
		} else if (selectedIndex >= firstIndex + MAX_ROWS_ON_SCREEN) {
			firstIndex += SCROLL_INCREMENT;
			//don't believe i need to do any clipping in this case
		}
	}
		
	private int getSelectedEntity () {
		int selectedEntityID = -1;
		
		//save off old selected item
		if (!listIsEmpty()) {
			int rowID = rowID(selectedIndex);
			if (rowID != INDEX_NEW) {	
				selectedEntityID = controller.getRecordID(rowID(selectedIndex));
			}
		}

		return selectedEntityID;
	}
	
	private boolean listIsEmpty () {
		return rowIDs.size() == 0 || (rowIDs.size() == 1 && ALLOW_NEW);
	}
	
	private int rowID (int i) {
		return ((Integer)rowIDs.elementAt(i)).intValue();
	}
		
	private void selectEntity (int entityID) {
		//if old selected item is in new search result, select it, else select first match
		selectedIndex = 0;
		if (entityID != -1) {
			for (int i = 0; i < rowIDs.size(); i++) {
				int rowID = rowID(i);
				if (rowID != INDEX_NEW) {
					if (controller.getRecordID(rowID) == entityID) {
						selectedIndex = i;
					}
				}
			}
		}
		//position selected item in center of visible list
		firstIndex = selectedIndex - MAX_ROWS_ON_SCREEN / 2;
		if (firstIndex < 0)
			firstIndex = 0;
	}
	
	private void refreshList () {
		list.clear();
		
		if (listIsEmpty()) {
			list.add(new StringItem("", "(No matches)"));
		}
		
		for (int i = firstIndex; i < rowIDs.size() && i < firstIndex + MAX_ROWS_ON_SCREEN; i++) {
			Container row;
			int rowID = rowID(i);
			
			if (i == selectedIndex) {
				//#style patselSelectedRow
				row = new Container(false);			
			} else if (i % 2 == 0) {
				//#style patselEvenRow
				row = new Container(false);
			} else {
				//#style patselOddRow
				row = new Container(false);
			}
			
			if (rowID == INDEX_NEW) {
				row.add(new StringItem("", "Add New " + entityType));
			} else {
				String[] rowData = controller.getDataFields(rowID);
				
				for (int j = 0; j < rowData.length; j++) {
					//#style patselCell
					StringItem str = new StringItem("", rowData[j]);
					row.add(str);
				}
			}
			list.add(row);
		}
		
		setActiveFrame(Graphics.BOTTOM);
	}
	
	protected boolean handleKeyReleased(int keyCode, int gameAction) {
		boolean ret = super.handleKeyReleased(keyCode, gameAction);
		
		if (gameAction == Canvas.UP && keyCode != Canvas.KEY_NUM2) {
			stepIndex(false);
			refreshList();
			return true;
		} else if (gameAction == Canvas.DOWN && keyCode != Canvas.KEY_NUM8) {
			stepIndex(true);
			refreshList();
			return true;
		} else if (gameAction == Canvas.FIRE && keyCode != Canvas.KEY_NUM5) {
			int rowID = rowID(selectedIndex);
			if (rowID == INDEX_NEW) {
				controller.newEntity();
			} else {
				controller.itemSelected(rowID);
			}
			return true;
		}
		
		return ret;
	}
	
	public void itemStateChanged (Item item) {
		if (item == tf) {
			refresh();
		}
	}	
	
	public void changeSort (boolean sortByName) {
		this.sortByName = sortByName;
		refresh();
	}
	
	//can't believe i'm writing a fucking sort function
	private void sortRows () {
		for (int i = rowIDs.size() - 1; i > 0; i--) {
			for (int j = 0; j < i; j++) {
				int rowA = rowID(j);
				int rowB = rowID(j + 1);
				String keyA, keyB;
				if (sortByName) {
					keyA = controller.getDataName(rowA);
					keyB = controller.getDataName(rowB);
				} else {
					keyA = controller.getDataID(rowA);
					keyB = controller.getDataID(rowB);
				}
				if (keyA.compareTo(keyB) > 0) {
					rowIDs.setElementAt(new Integer(rowB), j);
					rowIDs.setElementAt(new Integer(rowA), j + 1);
				}
			}
		}
	}

	public void commandAction(Command cmd, Displayable d) {
		if (d == this) {
			if (cmd == exitCmd) {
				controller.exit();
			} else if (cmd == sortCmd) {
				PatientSelectSortPopup pssw = new PatientSelectSortPopup(this, controller);
				pssw.show();
			}
		}
	}	
}	
