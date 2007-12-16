/****************************************************************************
 * MIDPLogViewer.java
 *
 * Copyright (C) Martin Woolley 2001
 *
 * This file is part of MIDPLogViewer.
 *
 * MIDPLogViewer is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * MIDPLogViewer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MIDPLogViewer; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * @author martin.woolley@woolleynet.com
 * http://www.woolleynet.com
 * http://mobilelandscape.co.uk
 *
 ****************************************************************************/

package org.celllife.clforms.logger;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.*;
import java.util.*;
import java.io.*;

public class MIDPLogViewer extends MIDlet implements CommandListener
{
    private static final String version = "1.00";
    public Display display; 
       
    private Form logForm = null;
    private Form about = null;
    private TextBox entryNumber = new TextBox("Go to line:", "", 6, TextField.NUMERIC);
    private StringItem logEntry;
    public static final String msg = 
                 "MIDPLogViewer 1.0.5 Copyright (C) 2002 Martin Woolley\n"+
                 "comes with ABSOLUTELY NO WARRANTY. "+
                 "This is free software, and you are welcome to redistribute it under "+
                 "certain conditions; Read the GNU GPL license at http://www.gnu.org/licenses/gpl.html "+
                 "for details. martin@woolleynet.com. http://www.mobilelandscape.co.uk";

    private static final Command nextCommand    = new Command("Next",  Command.SCREEN,   1);
    private static final Command prevCommand    = new Command("Prev",  Command.SCREEN, 2);
    private static final Command clearCommand   = new Command("Clear",   Command.SCREEN,   3);
    private static final Command gotoCommand    = new Command("Go to",   Command.SCREEN,   4);
    private static final Command aboutCommand    = new Command("About",  Command.SCREEN,   5);
    private static final Command exitCommand     = new Command("Exit",   Command.SCREEN,   6);

    private static final Command doneCommand    = new Command("Done",  Command.SCREEN,   1);
    private static final Command okCommand      = new Command("OK",  Command.SCREEN,   1);


    private static final String logFile = "MIDPLog";
    private RecordStore db;
    private int recno;
    private int numRecords;
    
    public MIDPLogViewer() throws RecordStoreNotFoundException, RecordStoreException
    {
       display = Display.getDisplay(this);
       db = openLocalDB();
    }

    private RecordStore openLocalDB() throws RecordStoreNotFoundException, RecordStoreException
    {
        RecordStore db = null;
        db = RecordStore.openRecordStore(logFile, true); 
        numRecords = db.getNumRecords();
        System.out.println("Num records="+numRecords);
        return db;
    }

    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
      try {
        db.closeRecordStore();
      }
      catch (Exception e) {}
    }

    protected  void pauseApp() {
    }

    protected void startApp() 
    {        
        logForm = new Form("");
        logForm.addCommand(nextCommand);
        logForm.addCommand(prevCommand);
        logForm.addCommand(clearCommand);
        logForm.addCommand(gotoCommand);
        logForm.addCommand(aboutCommand);
        logForm.addCommand(exitCommand);
        logForm.setCommandListener(this);
        logEntry = new StringItem("","");
        logForm.append(logEntry);
        
        about = new Form("MIDPLogViewer");
        about.addCommand(doneCommand);
        about.setCommandListener(this);
        about.append(msg);
        
        entryNumber.addCommand(okCommand);
        entryNumber.setCommandListener(this);
        
        showFirstRecord();
        display.setCurrent(logForm);
        
    }

    private void showFirstRecord()
    {
        logForm.setTitle(numRecords+" entries in log");
        String firstRec;
        if (numRecords > 0) {
          firstRec = getFirstRecord();
          logEntry.setText(recno+":"+firstRec);
        } else {
          firstRec = "No log entries to display";
          logEntry.setText(0+":"+firstRec);
        }          
    }

    private void showNextRecord()
    {
        if (hasNextRecord()) {
          String rec = getNextRecord();
          logEntry.setText(recno+":"+rec);
          display.setCurrent(display.getCurrent());
        }
    }

    private void showPrevRecord()
    {
        if (hasPrevRecord()) {
          String rec = getPrevRecord();
          logEntry.setText(recno+":"+rec);        
          display.setCurrent(display.getCurrent());
        }
    }

    private void showAbout()
    {
        display.setCurrent(about);
    }
    
    public String getFirstRecord()
    {
        byte[] rec = null;
        recno = 1;
        try
        {
           rec = db.getRecord(recno);
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
        return decodeByteArray(rec);
    }    
    
    public String getNextRecord()
    {
        byte [] rec = null;
        if (recno < numRecords)
        {
            try
            {
               recno++;
               rec = db.getRecord(recno);
            }
            catch (Exception e)
            {
              e.printStackTrace();
            }                            
        }
        return decodeByteArray(rec);
    }    

    public String getPrevRecord()
    {
        byte [] rec = null;
        if (recno > 0)
        {
            try
            {
               recno--;
               rec = db.getRecord(recno);
            }
            catch (Exception e)
            {
              e.printStackTrace();
            }                            
        }
        return decodeByteArray(rec);
    }

    public String getNthRecord() throws InvalidRecordIDException, RecordStoreException, RecordStoreNotOpenException
    {
        byte [] rec = null;
        if (recno > numRecords) {
            rec = db.getRecord(numRecords-1);
        } else {
            rec = db.getRecord(recno);
        }
        return decodeByteArray(rec);
    }
    
    public boolean hasNextRecord()
    {
        if (recno < numRecords)
        {
                return true;
        }
        else
        {
                return false;
        }
    }

    public boolean hasPrevRecord()
    {
        if (recno > 0)
        {
                return true;
        }
        else
        {
                return false;
        }
    }
    
    protected void close()
    {
      try {
        db.closeRecordStore();
      }
      catch (Exception e) {};
    }
     
    
    private void deleteRecord(int recno)
    {
      try
      {
        db.deleteRecord(recno);
      }
      catch (Exception e)
      {
        System.out.println("Exception thrown whilst deleting record");
        e.printStackTrace();
      }
        
    }        

    private void deleteAllRecords()
    {
        try
        {
          db.closeRecordStore();
          db.deleteRecordStore(logFile);       
          db = openLocalDB();
          numRecords = db.getNumRecords();
        }
        catch (Exception e)
        {
          System.out.println("Failed to delete RecordStore");
          e.printStackTrace();
        }
    }        

    protected String decodeByteArray(byte [] rawdata)
    {
      String rec = null;
      try
      {
        ByteArrayInputStream bais = new ByteArrayInputStream(rawdata);
        DataInputStream dis = new DataInputStream(bais);
        rec = dis.readUTF(); 
      }
      catch (Exception e)
      {
        System.out.println("Exception whilst decoding record");
        e.printStackTrace();
      }
      return rec;
    }
    
    private void getEntryNumber()
    {
        display.setCurrent(entryNumber);
    }

    private void showNthEntry()
    {
        int rowNumber = 0;
        try {
          rowNumber = Integer.parseInt(entryNumber.getString());
          recno = rowNumber;
        }
        catch (Exception e) {} 
        try {
          String rec = getNthRecord();
          logEntry.setText(recno+":"+rec);
        }
        catch (InvalidRecordIDException irie) {
          logEntry.setText(recno+": No such record");
        }
        catch (Exception e) {}
        display.setCurrent(logForm);
    }
    
    public void commandAction(Command c, Displayable d) 
    {
        String label = c.getLabel();

        if (label.equals("Next")) {
            showNextRecord();
            return;
        }

        if (label.equals("Prev")) {
            showPrevRecord();
            return;
        }

        if (label.equals("Clear")) {                
            deleteAllRecords();
            showFirstRecord();
            return;       
        }

        if (label.equals("Go to")) {
            getEntryNumber();
            return;
        }

        if (label.equals("About")) {
            showAbout();
            return;
        }
         
        if (label.equals("Exit")) 
        {
           try 
           {
                destroyApp(true);
                notifyDestroyed();
           }
           catch (MIDletStateChangeException msce) 
           {
                // shouldn't ever happen since we called destroyApp with unconditional = true
           }
           return;
        }        
        
        if (label.equals("Done")) {
            display.setCurrent(logForm);
            return;
        }
        
        if (label.equals("OK")) {
            showNthEntry();
            return;
        }

    }
}
