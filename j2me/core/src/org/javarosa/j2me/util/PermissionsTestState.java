package org.javarosa.j2me.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;

import org.javarosa.core.api.State;
import org.javarosa.core.log.WrappedException;
import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.util.TrivialTransitions;
import org.javarosa.j2me.log.CrashHandler;
import org.javarosa.j2me.log.HandledCommandListener;
import org.javarosa.j2me.log.HandledThread;
import org.javarosa.j2me.view.J2MEDisplay;

/**
 * The PermissionsTestState is used to identify whether CommCare can properly access 
 * @author ctsims
 *
 */
public abstract class PermissionsTestState implements State, TrivialTransitions, HandledCommandListener {

	static final String URL = "http://www.google.com";
		
	Form view;
	Command exit;
	Date start = null;
	
	public PermissionsTestState () {
		
	}
	
	public void start () {
		view = new Form("Permissions test");
		exit = new Command("OK", Command.BACK, 0);
		view.setCommandListener(this);
		view.addCommand(exit);
		J2MEDisplay.setView(view);
		
		final PermissionsTestState parent = this;
		new HandledThread () {
			public void _run () {
				permissionsTest(parent);
			}
		}.start();
	}
	
	public void addLine (String line) {
		Date now = new Date();
		if (start == null)
			start = now;
		
		int diff = (int)(now.getTime() - start.getTime()) / 10;
		String sDiff = (diff / 100) + "." + DateUtils.intPad(diff % 100, 2);
		
		view.append(new StringItem("", sDiff + ": " + line));
	}
	
	public static void permissionsTest (PermissionsTestState parent) {
		HttpConnection conn = null;
		
		try{ 
			parent.addLine("Test #1: Network");
			parent.addLine("Opening Connection to (" + URL + ")");
			
			conn = (HttpConnection)Connector.open(URL);
			conn.setRequestMethod(HttpConnection.GET);

			parent.addLine("PASS: Connection Permitted.");
            
		} catch (SecurityException e) {
			parent.addLine("FAIL: Permission Not Granted!");
        } catch (IOException e) {
			parent.addLine("FAIL: IO Failure: " + WrappedException.printException(e));

		} finally {
        	try { if (conn != null) { conn.close(); } } catch (IOException ioe) { }
        }
		
		
		parent.addLine("Test #2: Read Files");
		
		String fileroot = null;
		
		//File Read
		//TODO: Wrap for preprocessing
		javax.microedition.io.file.FileConnection fcon = null;
		try{
			for(Enumeration en = javax.microedition.io.file.FileSystemRegistry.listRoots(); en.hasMoreElements(); ){
				fileroot = (String)en.nextElement();
			}
			if(fileroot != null) {
				fcon = (javax.microedition.io.file.FileConnection)Connector.open("file:///" + fileroot);
				fcon.list();
				parent.addLine("PASS: File Read Permissions Granted");
			} else {
				parent.addLine("SKIP: No file system found");
			}
		} catch(IOException e) {
			parent.addLine("FAIL: IO Exception!: " + WrappedException.printException(e));
		} catch(SecurityException se) {
			parent.addLine("FAIL: Permission Not Granted!");
		} finally {
			if(fcon != null) { try { fcon.close();} catch(IOException e) {} }
		}
		
		parent.addLine("Test #3: Write Files");
		String baseFileName = "CommCareWriteTest.file";
		//File Write
		//TODO: Wrap for preprocessing
		javax.microedition.io.file.FileConnection writecon = null;
		try{
			if(fileroot != null) {
				writecon = (javax.microedition.io.file.FileConnection)Connector.open("file:///" + fileroot + baseFileName);
				int ext = 0;
				while(writecon.exists()) {
					writecon.close();
					ext++;
					writecon = (javax.microedition.io.file.FileConnection)Connector.open("file:///" + fileroot + baseFileName + "." + ext); 
				}
				writecon.create();
				OutputStream temp = writecon.openOutputStream();
				parent.addLine("PASS: File Created. Deleting temp file");
				temp.close();
				writecon.delete();
			} else {
				parent.addLine("SKIP: No file system found");
			}
		} catch(IOException e) {
			parent.addLine("FAIL: IO Exception!: " + WrappedException.printException(e));
		} catch(SecurityException se) {
			parent.addLine("FAIL: Permission Not Granted!");
		} finally {
			if(writecon != null) { try { writecon.close();} catch(IOException e) {} }
		}
		//SMS Send
		
		parent.addLine("Test #4: SMS Sending");
		
		//#if polish.api.wmapi
		javax.wireless.messaging.MessageConnection mconn = null;
		try {
			// create a MessageConnection
			mconn = (javax.wireless.messaging.MessageConnection)Connector.open("sms://+15555555555");
			javax.wireless.messaging.TextMessage sms = (javax.wireless.messaging.TextMessage) mconn.newMessage(javax.wireless.messaging.MessageConnection.TEXT_MESSAGE);
			sms.setAddress("sms://+15555555555");
			sms.setPayloadText("dummy text");
			parent.addLine("PASS: Permission Granted (NOTE: This test doesn't send an actual message, you may still be prompted)");
		} catch(SecurityException se) {
			parent.addLine("FAIL: Permission Not Granted!");
		} catch (IOException e) {
			parent.addLine("FAIL: IO Exception!: " + WrappedException.printException(e));
		}
		//#else
		//# parent.addLine("SKIP: SMS is not available on this platform"); 
		//#endif

		
		//Multimedia recording
		
		//GPS
	}
	
	public void commandAction(Command c, Displayable d) {
		CrashHandler.commandAction(this, c, d);
	}
	
	public void _commandAction(Command c, Displayable d) {
		if (c == exit) { 
			done();
		}
	}
	
	//nokia s40 bug
	public abstract void done();
}
