package org.javarosa.entity.model.view;

import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;

import org.javarosa.core.api.IView;
import org.javarosa.entity.activity.EntitySelectActivity;

import de.enough.polish.ui.Container;
import de.enough.polish.ui.FramedForm;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.ItemStateListener;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.TextField;

public class EntitySelectView extends FramedForm implements IView, ItemStateListener, CommandListener {
	//#if javarosa.patientselect.formfactor == nokia-s40 or javarosa.patientselect.formfactor == sony-k610i
	//# private static final int MAX_ROWS_ON_SCREEN = 5;
	//# private static final int SCROLL_INCREMENT = 4;	
	//#else
	private static final int MAX_ROWS_ON_SCREEN = 10;
	private static final int SCROLL_INCREMENT = 5;	
	//#endif
	
	public static final int NEW_DISALLOWED = 0;
	public static final int NEW_IN_LIST = 1;
	public static final int NEW_IN_MENU = 2;
	
	private static final int INDEX_NEW = -1;
	
	//behavior configuration options
	public boolean sortByName = true; //if false, sort by ID
	public boolean wrapAround = false; //TODO: support this
	public int newMode = NEW_IN_LIST;
	
	private EntitySelectActivity controller;
	public String entityType;
	
	private TextField tf;
	private Command exitCmd;
	private Command sortCmd;
    private Command newCmd;
	
	private int firstIndex;
	private int selectedIndex;
		
	private Vector rowIDs; //index into data corresponding to current matches
	
	public EntitySelectView(EntitySelectActivity controller, String title) {
		super(title);
		
		this.controller = controller;

		tf = new TextField("Find:  ", "", 20, TextField.ANY);
		tf.setInputMode(TextField.MODE_UPPERCASE);
		tf.setItemStateListener(this);
				
        append(Graphics.BOTTOM, tf);
        
        exitCmd = new Command("Cancel", Command.CANCEL, 4);
        sortCmd = new Command("Sort", Command.SCREEN, 3);
        addCommand(exitCmd);
        addCommand(sortCmd);
        this.setCommandListener(this);
        
        rowIDs = new Vector();
        
        this.setScrollYOffset(0, false);
	}

	public void init () {
        selectedIndex = 0;
        firstIndex = 0;

        //can't go in constructor, as entityType is not set there yet
        if (newMode == NEW_IN_MENU) {
        	newCmd = new Command("New " + entityType, Command.SCREEN, 4);
        	addCommand(newCmd);
        }
        
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
		this.setActiveFrame(Graphics.BOTTOM);
		controller.setView(this);
	}
	
	public Object getScreenObject() {
		return this;
	}

	private void getMatches (String key) {
		rowIDs = controller.search(key);
		sortRows();
		if (newMode == NEW_IN_LIST) {
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
		return rowIDs.size() == 0 || (rowIDs.size() == 1 && newMode == NEW_IN_LIST);
	}
	
	private int rowID (int i) {
		return ((Integer)rowIDs.elementAt(i)).intValue();
	}

	private int selectedIndexFromScreen (int i) {
		return firstIndex + i;
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
		container.clear();
		
		//#style patselTitleRow
		Container title = new Container(false);
		
		String[] titleData = controller.getTitleData();
		for (int j = 0; j < titleData.length; j++) {
			//#style patselCell
			StringItem str = new StringItem("", titleData[j]);
			title.add(str);
		}
		//#style patselCell
		StringItem number = new StringItem("","(" + String.valueOf(rowIDs.size()) + ")");
		title.add(number);
		this.append(title);
		
		if (listIsEmpty()) {
			this.append( new StringItem("", "(No matches)"));
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

			append(row);
		}

		setActiveFrame(Graphics.BOTTOM);
	}

	protected boolean handleKeyPressed(int keyCode, int gameAction) {
		//Supress these actions, letting the propogates screws up scrolling
		//on some platforms.
		if (gameAction == Canvas.UP && keyCode != Canvas.KEY_NUM2) {
			return true;
		} else if (gameAction == Canvas.DOWN && keyCode != Canvas.KEY_NUM8) {
			return true;
		}
		return super.handleKeyPressed(keyCode, gameAction);
	}

	
	protected boolean handleKeyReleased(int keyCode, int gameAction) {
		if (gameAction == Canvas.UP && keyCode != Canvas.KEY_NUM2) {
			stepIndex(false);
			refreshList();
			return true;
		} else if (gameAction == Canvas.DOWN && keyCode != Canvas.KEY_NUM8) {
			stepIndex(true);
			refreshList();
			return true;
		} else if (gameAction == Canvas.FIRE && keyCode != Canvas.KEY_NUM5) {
			processSelect();
			return true;
		}
		
		return super.handleKeyReleased(keyCode, gameAction);
	}

	private void processSelect() {
		if (rowIDs.size() > 0) {
			int rowID = rowID(selectedIndex);
			if (rowID == INDEX_NEW) {
				controller.newEntity();
			} else {
				controller.itemSelected(rowID);
			}
		}
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
				EntitySelectSortPopup pssw = new EntitySelectSortPopup(this, controller);
				pssw.show();
			} else if (cmd == newCmd) {
				controller.newEntity();
			}
		}
	}
	
	//#if polish.hasPointerEvents
	
	/* (non-Javadoc)
	 * @see de.enough.polish.ui.Container#handlePointerPressed(int, int)
	 */
	protected boolean handlePointerPressed(int x, int y) {
		boolean handled = false;
		
		int screenIndex = 0;
		for (int i = 0; i < this.container.size(); i++) {
			Item item = this.container.getItems()[i];
			if (item instanceof Container) {
				if (this.container.isInItemArea(x - this.container.getAbsoluteX(), y - this.container.getAbsoluteY(), item)) {
					selectedIndex = selectedIndexFromScreen(screenIndex);
					refreshList();
					processSelect();
					
					handled = true;
					break;
				}
				
				screenIndex++;
			}
		}
		
		if (handled){
			return true;
		} else {
			return super.handlePointerPressed(x, y);
		}
	}
	
	//#endif

}	
