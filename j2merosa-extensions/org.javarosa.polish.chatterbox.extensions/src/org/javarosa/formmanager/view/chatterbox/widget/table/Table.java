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


package org.javarosa.formmanager.view.chatterbox.widget.table;
/**
 *  
 * @author Brotecs
 * @date 
 **/

import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;

import javax.microedition.lcdui.Canvas;
import de.enough.polish.ui.Command;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import org.javarosa.formmanager.view.chatterbox.widget.util.ExtensionConstants;
import org.javarosa.patient.model.data.ImmunizationData;
import org.javarosa.patient.model.data.ImmunizationRow;

import de.enough.polish.ui.Container;
import de.enough.polish.ui.CustomItem;
import de.enough.polish.ui.DateField;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.ItemCommandListener;
import de.enough.polish.ui.StringItem;

public class Table extends CustomItem implements ItemCommandListener {
    
	private static final Command CMD_EDIT = new Command("OK", Command.OK, 1);
	///private static final Command CMD_DATE_OK = new Command("OK", Command.OK, 1);

	private static final int UPPER = 0;
    private static final int IN = 1;
    private static final int LOWER = 2;
    private int rows = 6;
    private int cols = 6;
    private int dx = 36;
    private int dy = 20;
    private int location = IN;
    private int currentX = 0;
    private int currentY = 0;
    public String[][] data = new String[rows][cols];
    public static long[][] recorddate = new long[6][6];
    public static int[][] selectedindex= new int [6][6];
    public static boolean[][] availablecells = new boolean [6][6];
    // Traversal stuff     
    // indicating support of horizontal traversal internal to the CustomItem
    boolean horz;

    // indicating support for vertical traversal internal to the CustomItem.
    boolean vert;

    //to get the  child information
    private StringItem option = new StringItem("", "Press 1 To 4 Key for choose Option");
    private StringItem question1 = new StringItem("", "1:Given. ");
    private StringItem question2 = new StringItem("", "2:No vaccination. ");
    private StringItem question3=  new StringItem("", "3:Unknown. ");
    private StringItem question4 = new StringItem("", "4:Specify Date Given: ");
    public DateField datefield = new DateField("", DateField.DATE);
    
    private Container container;
    
    private boolean isStringitem=false;
    private boolean isDatefield=false;
    
    //private int currentrow;
    //private int currentcol;
    
    public int length=25;
    
    public Table(String title) {
    	this(title, null);
    }
    
    public Table(String title, Container container) {
        super(title);
        this.container = container;
        datefield.addCommand(CMD_EDIT);
        //setDefaultCommand(CMD_EDIT);
        //setItemCommandListener(this);
        int interactionMode = getInteractionModes();
        horz = ((interactionMode & CustomItem.TRAVERSE_HORIZONTAL) != 0);
        vert = ((interactionMode & CustomItem.TRAVERSE_VERTICAL) != 0);
       // getRecord();
        datefield.setDate(new Date());
    }
    
    public void setData(ImmunizationData data) 
    {
    	Enumeration en = data.getImmunizationRows().elements();
    	int currentRow = 0;
    	
    	while(en.hasMoreElements()) {
    		ImmunizationRow row = (ImmunizationRow)en.nextElement();
    		for(int i = 0; i < 5 ; ++i ) {
    			if(row.getStatus(i) != -1) {
    			selectedindex[currentRow+1][i+1] = row.getStatus(i);
    			if(row.getStatus(i) == ExtensionConstants.VACCINATION_GIVEN_ON_DATE) {
    				recorddate[currentRow+1][i+1] = row.getDate(i).getTime();
    			}
    			//This may seem backwards, but it's because the function goes X,Y, not row,column
    			setText(" X",i+1,currentRow+1);
    			}
    			if(i <= data.getLargestAgeColumn() ) {
    				availablecells[currentRow+1][i+1] = row.getCellEnabled(i);
    			}
    			else {
    				availablecells[currentRow+1][i+1] = false;
    			}
    		}
    		currentRow++;
    	}
    }
    
    public ImmunizationData getData() {
    	ImmunizationData data = new ImmunizationData();
    	
    	//First row is for labels
    	for(int i = 1 ; i < rows ; ++i ) {
    		ImmunizationRow row = new ImmunizationRow(this.data[i][0]);
    		for(int j = 1 ; j < cols ; j++) {
    			if(selectedindex[i][j] == ExtensionConstants.VACCINATION_GIVEN_ON_DATE) {
    				row.setDose(j-1, selectedindex[i][j], new Date(recorddate[i][j]));
    			} else {
        			row.setVaccinationDose(j-1, selectedindex[i][j]);	
    			}
    		}
    		data.addRow(row);
    	}
    	
    	return data;
    }
    
    
    protected int getMinContentHeight() {
        return (rows * dy) + 1;
    }

    protected int getMinContentWidth() {
        return (cols * dx) + 1;
    }

    protected int getPrefContentHeight(int width) {
        return (rows * dy) + 1;
    }

    protected int getPrefContentWidth(int height) {
        return (cols * dx) + 1;
    }

    protected void paint(Graphics g, int w, int h) {
    	this.border = null;
        for (int i = 0; i <= rows; i++) {
            g.drawLine(0, i * dy, cols * dx, i * dy);
        }

        for (int i = 0; i <= cols; i++) {
            g.drawLine(i * dx, 0, i * dx, rows * dy);
        }
        int oldColor = g.getColor();
        g.setColor(0x00D0D0D0);
        g.fillRect((currentX * dx) + 1, (currentY * dy) + 1, dx - 1, dy - 1);
        g.setColor(oldColor);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
            	if(!availablecells[i][j] && (i != 0 && j != 0)) {
            		oldColor = g.getColor();
            		if(i == currentY && j == currentX) {
            			g.setColor(0x00545454);
            		} else {
            			g.setColor(0x00040404);
            		}
            		g.fillRect((j * dx) + 1, i * dy+1, dx - 2, dy - 2);
            		g.setColor(oldColor);
            	}
            	else if (data[i][j] != null) {
                    // store clipping properties
                    int oldClipX = g.getClipX();
                    int oldClipY = g.getClipY();
                    int oldClipWidth = g.getClipWidth();
                    int oldClipHeight = g.getClipHeight();
                    g.setClip((j * dx) + 1, i * dy, dx - 1, dy - 1);
                    g.drawString(data[i][j], (j * dx) + 2, ((i + 1) * dy) - 2,
                        Graphics.BOTTOM | Graphics.LEFT);
                    // restore clipping properties
                    g.setClip(oldClipX, oldClipY, oldClipWidth, oldClipHeight);
                }
            }
        }
        
        
       
    }
    
    

    protected boolean traverse(int dir, int viewportWidth, int viewportHeight, int[] visRect_inout) {
    	
    	if (horz && vert) {
            switch (dir) {
            case Canvas.DOWN:

                if (location == UPPER) {
                    location = IN;
                } else {
                    if (currentY < (rows - 1)) {
                        currentY++;
                        repaint(currentX * dx, (currentY - 1) * dy, dx, dy);
                        repaint(currentX * dx, currentY * dy, dx, dy);
                        
                    } else {
                        location = LOWER;

                        return false;
                    }
                }

                break;

            case Canvas.UP:

                if (location == LOWER) {
                    location = IN;
                } else {
                    if (currentY > 0) {
                        currentY--;
                        repaint(currentX * dx, (currentY + 1) * dy, dx, dy);
                        repaint(currentX * dx, currentY * dy, dx, dy);
                    } else {
                        location = UPPER;

                        return false;
                    }
                }

                break;

            case Canvas.LEFT:

                if (currentX > 0) {
                    currentX--;
                    repaint((currentX + 1) * dx, currentY * dy, dx, dy);
                    repaint(currentX * dx, currentY * dy, dx, dy);
                }

                break;

            case Canvas.RIGHT:

                if (currentX < (cols - 1)) {
                    currentX++;
                    repaint((currentX - 1) * dx, currentY * dy, dx, dy);
                    repaint(currentX * dx, currentY * dy, dx, dy);
                }
            }
        } else if (horz || vert) {
            switch (dir) {
            case Canvas.UP:
            case Canvas.LEFT:

                if (location == LOWER) {
                    location = IN;
                } else {
                    if (currentX > 0) {
                        currentX--;
                        repaint((currentX + 1) * dx, currentY * dy, dx, dy);
                        repaint(currentX * dx, currentY * dy, dx, dy);
                    } else if (currentY > 0) {
                        currentY--;
                        repaint(currentX * dx, (currentY + 1) * dy, dx, dy);
                        currentX = cols - 1;
                        repaint(currentX * dx, currentY * dy, dx, dy);
                    } else {
                        location = UPPER;

                        return false;
                    }
                }

                break;

            case Canvas.DOWN:
            case Canvas.RIGHT:

                if (location == UPPER) {
                    location = IN;
                } else {
                    if (currentX < (cols - 1)) {
                        currentX++;
                        repaint((currentX - 1) * dx, currentY * dy, dx, dy);
                        repaint(currentX * dx, currentY * dy, dx, dy);
                    } else if (currentY < (rows - 1)) {
                        currentY++;
                        repaint(currentX * dx, (currentY - 1) * dy, dx, dy);
                        currentX = 0;
                        repaint(currentX * dx, currentY * dy, dx, dy);
                    } else {
                        location = LOWER;

                        return false;
                    }
                }
            }
        } else {
            // In case of no Traversal at all: (horz|vert) == 0
        }

       setInformation();
 
       visRect_inout[0] = currentX;
       visRect_inout[1] = currentY;
       visRect_inout[2] = dx;
       visRect_inout[3] = dy; 
       return true;
    }
    private void setInformation()
    {
    	 data[0][0]="Vaccine";
         data[0][1]="birth";
         data[0][2]="4 wk";
         data[0][3]="8 wk";
         data[0][4]="12 wk";
         data[0][5]="9 mo";
         data[1][0]="BCG";
         data[2][0]="OPV";
         data[3][0]="DPT";
         data[4][0]="Hep B";
         data[5][0]="Measles";
         if(!(currentX==0 || currentY==0))
     	{
        	 setOptions();
     	}
    }
    
    public void removeStringItem()
    {
    	container.remove(option);
    	container.remove(question1);
    	container.remove(question2);
    	container.remove(question3);
    	container.remove(question4);
    }
    public void getdate()
    {
    	datefield.getDate();
    }
    public void checkdatefield()
    {
    	if(isDatefield==true)
    	{
    		//chatScreen.removeItem(datefield);
    		isDatefield=false;
    	}
    }

    public void setText(String text,int x,int y) {
    	currentX=x;
    	currentY=y;
        data[currentY][currentX] = text;
        //repaint(currentY * dx, currentX * dy, dx, dy);
    }
    public void keyPressed(int code) {
    	if(availablecells[currentY][currentX]) {
    		switch(code)
    		{
    			case Canvas.KEY_NUM1:
    				question1.setLabel("");
    				question1.setText("1:Given. ");
    				question1.setFont(Font.getFont(Font.FACE_SYSTEM,Font.STYLE_BOLD,Font.SIZE_MEDIUM)); //get specific Font for ur 'text'
    				question2.setFont(Font.getFont(Font.FACE_SYSTEM,Font.STYLE_PLAIN,Font.SIZE_MEDIUM)); //get specific Font for ur 'text'
    				question3.setFont(Font.getFont(Font.FACE_SYSTEM,Font.STYLE_PLAIN,Font.SIZE_MEDIUM)); //get specific Font for ur 'text'
    				question4.setFont(Font.getFont(Font.FACE_SYSTEM,Font.STYLE_PLAIN,Font.SIZE_MEDIUM)); //get specific Font for ur 'text'
    				setText("X",currentX,currentY);
    				selectedindex[currentY][currentX] = 1;
    				checkdatefield();
    				break;
            
    			case Canvas.KEY_NUM2:
    				question2.setLabel("");
    				question2.setText("2:No vaccination. ");
    				question1.setFont(Font.getFont(Font.FACE_SYSTEM,Font.STYLE_PLAIN,Font.SIZE_MEDIUM)); //get specific Font for ur 'text'
    				question3.setFont(Font.getFont(Font.FACE_SYSTEM,Font.STYLE_PLAIN,Font.SIZE_MEDIUM)); //get specific Font for ur 'text'
    				question4.setFont(Font.getFont(Font.FACE_SYSTEM,Font.STYLE_PLAIN,Font.SIZE_MEDIUM)); //get specific Font for ur 'text'
    				question2.setFont(Font.getFont(Font.FACE_SYSTEM,Font.STYLE_BOLD,Font.SIZE_MEDIUM)); //get specific Font for ur 'text'
    				setText("X",currentX,currentY);
    				selectedindex[currentY][currentX] = 2;
    				checkdatefield();
    				break;
    			case Canvas.KEY_NUM3:
    				question3.setLabel("");
    				question3.setText("3:Unknown. ");
    				question1.setFont(Font.getFont(Font.FACE_SYSTEM,Font.STYLE_PLAIN,Font.SIZE_MEDIUM)); //get specific Font for ur 'text'
    				question2.setFont(Font.getFont(Font.FACE_SYSTEM,Font.STYLE_PLAIN,Font.SIZE_MEDIUM)); //get specific Font for ur 'text'
    				question4.setFont(Font.getFont(Font.FACE_SYSTEM,Font.STYLE_PLAIN,Font.SIZE_MEDIUM)); //get specific Font for ur 'text'
    				question3.setFont(Font.getFont(Font.FACE_SYSTEM,Font.STYLE_BOLD,Font.SIZE_MEDIUM)); //get specific Font for ur 'text'
    				setText("X",currentX,currentY);
    				selectedindex[currentY][currentX] = 3;
    				checkdatefield();
    				break;
    			case Canvas.KEY_NUM4:
    				isDatefield=true;
    				question4.setLabel("");
    				question4.setText("4:Specify Date Given: ");
    				question1.setFont(Font.getFont(Font.FACE_SYSTEM,Font.STYLE_PLAIN,Font.SIZE_MEDIUM)); //get specific Font for ur 'text'
    				question2.setFont(Font.getFont(Font.FACE_SYSTEM,Font.STYLE_PLAIN,Font.SIZE_MEDIUM)); //get specific Font for ur 'text'
    				question3.setFont(Font.getFont(Font.FACE_SYSTEM,Font.STYLE_PLAIN,Font.SIZE_MEDIUM)); //get specific Font for ur 'text'
    				question4.setFont(Font.getFont(Font.FACE_SYSTEM,Font.STYLE_BOLD,Font.SIZE_MEDIUM)); //get specific Font for ur 'text'
    				Calendar calendar = Calendar.getInstance();
  	    	  		calendar.setTime(new Date());  
  	    	  		calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)-6);
  	    	  		calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
  	    	  		calendar.set(Calendar.DAY_OF_MONTH,calendar.get(Calendar.DAY_OF_MONTH));
  	    	  		datefield.setDate(calendar.getTime());
  	    	  		//chatScreen.insert(chatScreen.getCurrentIndex()+1,datefield);
  	    	  		setText("X",currentX,currentY);
  	    	  		selectedindex[currentY][currentX] = 4;
  	    	  		recorddate[currentY][currentX] = calendar.getTime().getTime();
  	    	  		//chatScreen.append(datefield);
  	    	  		//chatScreen.focus(datefield);
  	    	  		break;
    		}
    	}
    }
    public void addOptions()
    {
    	if (container != null) {
			container.add(option);
			container.add(question1);
			container.add(question2);
			container.add(question3);
			container.add(question4);
		}
    	//set default
    	question1.setLabel("");
		question1.setText("1:Given. ");
		question1.setFont(Font.getFont(Font.FACE_SYSTEM,Font.STYLE_PLAIN,Font.SIZE_MEDIUM)); //get specific Font for ur 'text'
		question2.setFont(Font.getFont(Font.FACE_SYSTEM,Font.STYLE_PLAIN,Font.SIZE_MEDIUM)); //get specific Font for ur 'text'
		question3.setFont(Font.getFont(Font.FACE_SYSTEM,Font.STYLE_PLAIN,Font.SIZE_MEDIUM)); //get specific Font for ur 'text'
		question4.setFont(Font.getFont(Font.FACE_SYSTEM,Font.STYLE_PLAIN,Font.SIZE_MEDIUM)); //get specific Font for ur 'text'
		
		switch(selectedindex[currentY][currentX]) {
		case 1:
			question1.setFont(Font.getFont(Font.FACE_SYSTEM,Font.STYLE_BOLD,Font.SIZE_MEDIUM)); //get specific Font for ur 'text'
			break;
		case 2:
			question2.setFont(Font.getFont(Font.FACE_SYSTEM,Font.STYLE_BOLD,Font.SIZE_MEDIUM)); //get specific Font for ur 'text'
			break;
		case 3:
			question3.setFont(Font.getFont(Font.FACE_SYSTEM,Font.STYLE_BOLD,Font.SIZE_MEDIUM)); //get specific Font for ur 'text'
			break;
		case 4:
			question4.setFont(Font.getFont(Font.FACE_SYSTEM,Font.STYLE_BOLD,Font.SIZE_MEDIUM)); //get specific Font for ur 'text'
			break;
			
		}
    }
    public void setOptions()
    {
      	 if(isStringitem ==true)
    	 {
         	removeStringItem();
         	checkdatefield();
         	addOptions();
         	//isStringitem=false;
    	 }
    	 else{
    		addOptions();
        	isStringitem=true;
    	 }

    }

    public void commandAction(Command c, Item i) {
        if (c == CMD_EDIT) {
    }
   }
    public String[][] getText()
    {
    	return data;
    }
    public int getcurrentrow()
    {
    	return currentX;
    }
    public int getcurrentcolumn()
    {
    	return currentY;
    }
}
