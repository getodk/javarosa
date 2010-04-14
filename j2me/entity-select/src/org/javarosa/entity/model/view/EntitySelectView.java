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

import java.io.IOException;
import java.util.Date;
import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.services.Logger;
import org.javarosa.core.services.storage.Persistable;
import org.javarosa.entity.api.EntitySelectController;
import org.javarosa.entity.model.Entity;
import org.javarosa.j2me.log.CrashHandler;
import org.javarosa.j2me.log.HandledCommandListener;
import org.javarosa.j2me.log.HandledPItemStateListener;

import de.enough.polish.ui.Container;
import de.enough.polish.ui.FramedForm;
import de.enough.polish.ui.ImageItem;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.TextField;

public class EntitySelectView<E extends Persistable> extends FramedForm implements HandledPItemStateListener, HandledCommandListener {
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
	public boolean wrapAround = false; //TODO: support this
	public int newMode = NEW_IN_LIST;
		
	private EntitySelectController<E> controller;
	private Entity<E> entityPrototype;
	private String baseTitle;
	
	private TextField tf;
	private Command exitCmd;
	private Command sortCmd;
    private Command newCmd;
	
	private int firstIndex;
	private int selectedIndex;
	private String sortField;
	
	private Style headerStyle;
	private Style rowStyle;
	
	private Vector<Integer> rowIDs; //index into data corresponding to current matches
	
	public EntitySelectView (EntitySelectController<E> controller, Entity<E> entityPrototype, String title, int newMode) {
		super(title);
		this.baseTitle = title;
		
		this.controller = controller;
		this.entityPrototype = entityPrototype;
		this.newMode = newMode;
		
		this.sortField = getDefaultSortField();
		
		tf = new TextField("Find:  ", "", 20, TextField.ANY);
		tf.setInputMode(TextField.MODE_UPPERCASE);
		tf.setItemStateListener(this);
				
        append(Graphics.BOTTOM, tf);
        
        exitCmd = new Command("Cancel", Command.CANCEL, 4);
        sortCmd = new Command("Sort", Command.SCREEN, 3);
        addCommand(exitCmd);
        if (getNumSortFields() > 1) {
        	addCommand(sortCmd);
        }
        if (newMode == NEW_IN_MENU) {
        	newCmd = new Command("New " + entityPrototype.entityType(), Command.SCREEN, 4);
        	addCommand(newCmd);
        }
        this.setCommandListener(this);
        
        rowIDs = new Vector<Integer>();
        
        this.setScrollYOffset(0, false);
	}
	
	public void init () {
        selectedIndex = 0;
        firstIndex = 0;
        
        calculateStyles();
        
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
	
	private void calculateStyles() {
		headerStyle = genStyleFromHints(entityPrototype.getStyleHints(true));
		
		rowStyle = genStyleFromHints(entityPrototype.getStyleHints(false));
	}
	
	private Style genStyleFromHints(int[] hints) {
		
		int screenwidth = 240;
		
		//#ifdef javarosa.patientselect.screenwidth:defined
		//#= screenwidth = ${ javarosa.patientselect.screenwidth };
		//#elifdef polish.ScreenWidth:defined
		//#= screenwidth = ${ polish.ScreenWidth };
		//#else
		//# screenwidth = this.getWidth();
		//#endif
		
		Style style = new Style();
		style.addAttribute("columns", new Integer(hints.length));
		
		int fullSize = 100;
		int sharedBetween = 0;
		for(int hint : hints) {
			if(hint != -1) {
				fullSize -= hint;
			} else {
				sharedBetween ++;
			}
		}
		
		double average = ((double)fullSize) / (double)sharedBetween;
		int averagePixels = (int)(Math.floor((average / 100.0) * screenwidth));
		
		String columnswidth = "";
		for(int hint : hints) {
			int width = hint == -1? averagePixels : 
				(int)Math.floor((((double)hint)/100.0)*screenwidth);
			columnswidth += width + ",";
		}
		columnswidth = columnswidth.substring(0, columnswidth.lastIndexOf(','));
		
		style.addAttribute("columns-width", columnswidth);
		return style;
	}
	
	public void show () {
		this.setActiveFrame(Graphics.BOTTOM);
		controller.setView(this);
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
	
	private int numMatches () {
		return rowIDs.size() - (newMode == NEW_IN_LIST ? 1 : 0);	
	}
	
	private boolean listIsEmpty () {
		return numMatches() <= 0;
	}
	
	private int rowID (int i) {
		return rowIDs.elementAt(i).intValue();
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

		this.setTitle(baseTitle + " (" + numMatches() + ")");
		
		//#style patselTitleRow
		Container title = new Container(false);
		applyStyle(title, STYLE_TITLE);
		
		String[] titleData = controller.getTitleData();
		for (int j = 0; j < titleData.length; j++) {
			//#style patselCell
			StringItem str = new StringItem("", titleData[j]);
			applyStyle(str, STYLE_CELL);
			title.add(str);
		}
		this.append(title);
		
		if (listIsEmpty()) {
			this.append( new StringItem("", "(No matches)"));
		}
		
		String[] colFormat = controller.getColumnFormat(false);
		
		for (int i = firstIndex; i < rowIDs.size() && i < firstIndex + MAX_ROWS_ON_SCREEN; i++) {
			Container row;
			int rowID = rowID(i);
			
			if (i == selectedIndex) {
				//#style patselSelectedRow
				row = new Container(false);
				applyStyle(row, STYLE_SELECTED);
			} else if (i % 2 == 0) {
				//#style patselEvenRow
				row = new Container(false);
				applyStyle(row, STYLE_EVEN);
			} else {
				//#style patselOddRow
				row = new Container(false);
				applyStyle(row, STYLE_ODD);
			}
			
			if (rowID == INDEX_NEW) {
				row.add(new StringItem("", "Add New " + entityPrototype.entityType()));
			} else {
				String[] rowData = controller.getDataFields(rowID);
				
				for (int j = 0; j < rowData.length; j++) {
					if(colFormat[j] == null) {
						//#style patselCell
						StringItem str = new StringItem("", rowData[j]);
						applyStyle(str, STYLE_CELL);
						row.add(str);
					}
					else if ("image".equals(colFormat[j])) {
							String uri = rowData[j];
							if(uri == null || uri.equals("")) {
								//#style patselCell
								StringItem str = new StringItem("", rowData[j]);
								applyStyle(str, STYLE_CELL);
								row.add(str);
							} else {
								try {
							//#style patselCell
							ImageItem img = new ImageItem("", Image.createImage(ReferenceManager._().DeriveReference(uri).getStream()),ImageItem.LAYOUT_CENTER,"img");
							applyStyle(img, STYLE_CELL);
							row.add(img);
							} catch (IOException e) {
							e.printStackTrace();
							throw new RuntimeException("IO Exception while reading an image item for Entity Select");
						} catch (InvalidReferenceException e) {
							e.printStackTrace();
							throw new RuntimeException("Invalid reference while trying to create an image for Entity Select: " + e.getReferenceString());
						}
						}
					}
				}
			}

			append(row);
		}

		setActiveFrame(Graphics.BOTTOM);
	}

	private static final int STYLE_TITLE = 0;
	private static final int STYLE_CELL = 1;
	private static final int STYLE_EVEN = 2;
	private static final int STYLE_ODD = 3;
	private static final int STYLE_SELECTED = 4;
	
	private void applyStyle(Item i, int type) {
		
		if(type == STYLE_TITLE) {
			i.getStyle().addAttribute("columns",  headerStyle.getIntProperty("columns"));
			i.getStyle().addAttribute("columns-width", headerStyle.getProperty("columns-width"));
		} else {
			i.getStyle().addAttribute("columns",  rowStyle.getIntProperty("columns"));
			i.getStyle().addAttribute("columns-width", rowStyle.getProperty("columns-width"));
		}
	}
		
	//needs no exception wrapping
	protected boolean handleKeyPressed(int keyCode, int gameAction) {
		//Supress these actions, letting the propogates screws up scrolling on some platforms.
		if (gameAction == Canvas.UP && keyCode != Canvas.KEY_NUM2) {
			return true;
		} else if (gameAction == Canvas.DOWN && keyCode != Canvas.KEY_NUM8) {
			return true;
		}
		return super.handleKeyPressed(keyCode, gameAction);
	}

	protected boolean handleKeyReleased(int keyCode, int gameAction) {
		try {
		
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

		} catch (Exception e) {
			Logger.die("gui-keyup", e);
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
	
	public void itemStateChanged(Item i) {
		CrashHandler.itemStateChanged(this, i);
	}  

	public void _itemStateChanged(Item item) {
		if (item == tf) {
			refresh();
		}
	}	
	
	public void changeSort (String sortField) {
		this.sortField = sortField;
		refresh();
	}
	
	public String getSortField () {
		return sortField;
	}
	
	//can't believe i'm writing a .. sort function
	private void sortRows () {
		for (int i = rowIDs.size() - 1; i > 0; i--) {
			for (int j = 0; j < i; j++) {
				int rowA = rowID(j);
				int rowB = rowID(j + 1);
				if (compare(controller.getEntity(rowA), controller.getEntity(rowB)) > 0) {
					rowIDs.setElementAt(new Integer(rowB), j);
					rowIDs.setElementAt(new Integer(rowA), j + 1);
				}
			}
		}
	}

	private int compare (Entity<E> eA, Entity<E> eB) {
		if (sortField == null) {
			return 0;
		}
		
		Object valA = eA.getSortKey(sortField);
		Object valB = eB.getSortKey(sortField);
		
		return compareVal(valA, valB);
	}
	
	private int compareVal (Object valA, Object valB) {
		if (valA == null && valB == null) {
			return 0;
		} else if (valA == null) {
			return 1;
		} else if (valB == null) {
			return -1;
		}
		
		if (valA instanceof Integer) {
			return compareInt(((Integer)valA).intValue(), ((Integer)valB).intValue());
		} else if (valA instanceof Long) {
			return compareInt(((Long)valA).longValue(), ((Long)valB).longValue());
		} else if (valA instanceof Double) {
			return compareFloat(((Double)valA).doubleValue(), ((Double)valB).doubleValue());
		} else if (valA instanceof String) {
			return compareStr((String)valA, (String)valB);
		} else if (valA instanceof Date) {
			return compareInt((int)DateUtils.daysSinceEpoch((Date)valA), (int)DateUtils.daysSinceEpoch((Date)valB));
		} else if (valA instanceof Object[]) {
			Object[] arrA = (Object[])valA;
			Object[] arrB = (Object[])valB;
			
			for (int i = 0; i < arrA.length && i < arrB.length; i++) {
				int cmp = compareVal(arrA[i], arrB[i]);
				if (cmp != 0)
					return cmp;
			}
			return compareInt(arrA.length, arrB.length);
		} else {
			throw new RuntimeException ("Don't know how to order type [" + valA.getClass().getName() + "]; only int, long, double, string, and date are supported");
		}
	}
	
	private int compareInt (long a, long b) {
		return (a == b ? 0 : (a < b ? -1 : 1));
	}
	
	private int compareFloat (double a, double b) {
		return (a == b ? 0 : (a < b ? -1 : 1));
	}
	
	private int compareStr (String a, String b) {
		return a.compareTo(b);
	}
	
	private int getNumSortFields () {
		String[] fields = entityPrototype.getSortFields();
		return (fields == null ? 0 : fields.length);
	}
	
	private String getDefaultSortField () {
		return (getNumSortFields() == 0 ? null : entityPrototype.getSortFields()[0]);
	}
	
	public void commandAction(Command c, Displayable d) {
		CrashHandler.commandAction(this, c, d);
	}  

	public void _commandAction(Command cmd, Displayable d) {
		if (d == this) {
			if (cmd == exitCmd) {
				controller.exit();
			} else if (cmd == sortCmd) {
				EntitySelectSortPopup<E> pssw = new EntitySelectSortPopup<E>(this, controller, entityPrototype);
				pssw.show();
			} else if (cmd == newCmd) {
				controller.newEntity();
			}
		}
	}
	
//#if polish.hasPointerEvents
//#
//#	private int selectedIndexFromScreen (int i) {
//#		return firstIndex + i;
//#	}
//#	
//#	protected boolean handlePointerPressed (int x, int y) {
//#		boolean handled = false;
//#		
//#		try {
//#	
//#			int screenIndex = 0;
//#			for (int i = 0; i < this.container.size(); i++) {
//#				Item item = this.container.getItems()[i];
//#				if (item instanceof Container) {
//#					if (this.container.isInItemArea(x - this.container.getAbsoluteX(), y - this.container.getAbsoluteY(), item)) {
//#						selectedIndex = selectedIndexFromScreen(screenIndex);
//#						refreshList();
//#						processSelect();
//#					
//#						handled = true;
//#						break;
//#					}
//#				
//#					screenIndex++;
//#				}
//#			}
//#			
//#		} catch (Exception e) {
//#			IncidentLogger.die("gui-ptrdown", e);
//#		}
//#		
//#		if (handled) {
//#			return true;
//#		} else {
//#			return super.handlePointerPressed(x, y);
//#		}
//#	}
//#	
//#endif

}	
