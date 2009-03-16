package org.javarosa.rmstest;

import java.util.Random;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.StringItem;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;
import javax.microedition.rms.RecordStoreNotOpenException;

/**
 * This is the starting point for the JavarosaDemo application
 * @author Brian DeRenzi
 *
 */
public class RMSTestMidlet extends MIDlet implements CommandListener {
	public Random rand;
	
	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
		deleteRecordStores();
	}

	protected void pauseApp() {
		// TODO Auto-generated method stub

	}

	protected void startApp() throws MIDletStateChangeException {
		rand = new Random();
		deleteRecordStores();
		
		initView();
	}

	private void initView () {
		List list = new List("RMS Tests", List.IMPLICIT);
	    list.setCommandListener(this);
	    
	    list.append("Fill RMS; small records", null);
	    list.append("Fill RMS; med records", null);
	    list.append("Fill RMS; big records", null);
	    list.append("Fill many RMSes", null);
	    
	    Display.getDisplay(this).setCurrent(list);
	}
	
	private void showResults (Vector log) {
		String[] lines = new String[log.size()];
		for (int i = 0; i < lines.length; i++) {
			lines[i] = (String)log.elementAt(i);
		}
		
		Form f = new Form("Test Result");
		
		for (int i = 0; i < lines.length; i++) {
			f.append(new StringItem(null, lines[i]));			
		}

		Display.getDisplay(this).setCurrent(f);
	}
	
	private void deleteRecordStores () {
		String[] rmses = RecordStore.listRecordStores();
		if (rmses == null)
			rmses = new String[0];
		
		for (int i = 0; i < rmses.length; i++) {
			deleteRecordStore(rmses[i]);
		}
	}
	
	private void deleteRecordStore (String rms) {
		try {
			RecordStore.deleteRecordStore(rms);
		} catch (RecordStoreNotFoundException e) {
			fail("deleteRecordStores", e);
		} catch (RecordStoreException e) {
			fail("deleteRecordStores", e);
		}
	}
	
	private boolean addRecord (RecordStore rms, byte[] data) {
		try {
			rms.addRecord(data, 0, data.length);
		} catch (RecordStoreNotOpenException e) {
			fail("addRecord", e);
		} catch (RecordStoreFullException e) {
			return false;
		} catch (RecordStoreException e) {
			fail("addRecord", e);
		}
		return true;
	}
	
	private int getRMSSize (RecordStore rms) {
		int n = -1;
		try {
			n = rms.getSize();
		} catch (RecordStoreNotOpenException e) {
			fail("getRMSSize", e);
		}
		return n;
	}
	
	private int getRMSAvailSpace (RecordStore rms) {
		int n = -1;
		try {
			n = rms.getSizeAvailable();
		} catch (RecordStoreNotOpenException e) {
			fail("getRMSAvailSpace", e);
		}
		return n;
	}
	
	private void fail (String prefix, Exception e) {
		throw new RuntimeException(prefix + ": " + e.getClass().getName());
	}
	
	public byte[] getData (int n) {
		byte[] data = new byte[n];
		
		int k = 0;
		for (int i = 0; i < data.length; i++) {
			if (i % 4 == 0)
				k = rand.nextInt();
				
			data[i] = (byte)((k >> (8 * (i % 4))) & 0xFF);
		}
		
		return data;
	}
	
	private boolean testFillRMS (int recSize, Vector log) {
		RecordStore rms = null;
		try {
			rms = RecordStore.openRecordStore("TEST_RMS_" + rand.nextInt(100000), true);
		} catch (RecordStoreFullException e) {
			return false;
		} catch (RecordStoreNotFoundException e) {
			fail("testFillRMS", e);
		} catch (RecordStoreException e) {
			fail("testFillRMS", e);
		}
		log.addElement("created RMS; cur size: " + getRMSSize(rms) + " avail size: " + getRMSAvailSpace(rms));
		
		boolean full = false;
		while (!full) {
			full = !addRecord(rms, getData(recSize));
		}
		
		log.addElement("RMS full; cur size: " + getRMSSize(rms) + " avail size: " + getRMSAvailSpace(rms));
		return true;
	}

	private void testFillRMSes (Vector log) {
		while (testFillRMS(200, log))
			;
		
		for (int i = 0; i < log.size(); i++) {
			log.setElementAt("" + (i / 2 + 1) + ": " + (String)log.elementAt(i), i);
		}
	}
	
	public void commandAction(Command c, Displayable d) {
		int choice = ((List)d).getSelectedIndex();
		
		Vector log = new Vector();
		d.setTitle("Working...");
		
		switch (choice) {
		case 0: testFillRMS(20, log); break;
		case 1: testFillRMS(200, log); break;
		case 2: testFillRMS(2000, log); break;
		case 3: testFillRMSes(log); break;
		}
		
		showResults(log);
	}
	
}
