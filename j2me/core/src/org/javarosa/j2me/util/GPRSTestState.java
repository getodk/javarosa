package org.javarosa.j2me.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;

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

public abstract class GPRSTestState implements State, TrivialTransitions, HandledCommandListener {

	static final String DEFAULT_URL = "http://www.google.com";
	
	String url;
	
	Form view;
	Command exit;
	Date start = null;
	
	public GPRSTestState () {
		this(DEFAULT_URL);
	}
	
	public GPRSTestState (String url) {
		this.url = url;
	}
	
	public void start () {
		view = new Form("GPRS Test");
		exit = new Command("OK", Command.BACK, 0);
		view.setCommandListener(this);
		view.addCommand(exit);
		J2MEDisplay.setView(view);
		
		final GPRSTestState parent = this;
		new HandledThread () {
			public void _run () {
				networkTest(parent);
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
	
	public static void networkTest (GPRSTestState parent) {
		HttpConnection conn = null;
		InputStream is = null;
		
		try {
			parent.addLine("Beginning test... (" + parent.url + ")");
			
			conn = (HttpConnection)Connector.open(parent.url);
			conn.setRequestMethod(HttpConnection.GET);

			parent.addLine("Connection open and configured.");

			int code = conn.getResponseCode();
			parent.addLine("Received response code " + code);
			
			parent.addLine("Content Type: " + conn.getType());
			parent.addLine("Content Length: " + conn.getLength());
			
			byte[] data;
			is = conn.openInputStream();
			
            int len = (int)conn.getLength();
            if (len > 0) {
                int actual = 0;
                int bytesread = 0;
                data = new byte[len];
                while ((bytesread != len) && (actual != -1)) {
                    actual = is.read(data, bytesread, len - bytesread);
                    bytesread += actual;
                }
            } else {
    			ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int ch;
                while ((ch = is.read()) != -1) {
                     baos.write(ch);
                }
                data = baos.toByteArray();
            }
			
            parent.addLine("Received response (" + data.length + " bytes):");
            String body;
            try {
            	String encoding = conn.getEncoding();
           		body = new String(data, encoding != null ? encoding : "UTF-8");
            } catch (UnsupportedEncodingException uee) {
            	StringBuffer sb = new StringBuffer();
            	for (int i = 0; i < data.length; i++)
            		sb.append((char)data[i]);
            	body = sb.toString();
            }
            parent.addLine(body);
            
		} catch (Exception e) {
			parent.addLine("Failed: " + WrappedException.printException(e));
        } finally {
        	try {
        		if (is != null)
        			is.close();
        		if (conn != null)
        			conn.close();
        	} catch (IOException ioe) { }
        }
	}
	
	public void commandAction(Command c, Displayable d) {
		CrashHandler.commandAction(this, c, d);
	}
	
	public void _commandAction(Command c, Displayable d) {
		if (c == exit)
			done();
	}
	
	//nokia s40 bug
	public abstract void done();
}
