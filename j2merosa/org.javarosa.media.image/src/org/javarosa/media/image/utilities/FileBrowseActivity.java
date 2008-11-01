package org.javarosa.media.image.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IDisplay;
import org.javarosa.core.api.IShell;
import org.javarosa.j2me.view.DisplayViewFactory;

public class FileBrowseActivity implements IActivity, CommandListener {

	private String currDirName;

	private Command view = new Command("View", Command.ITEM, 1);
	private Command back = new Command("Back", Command.BACK, 2);
	private Command exit = new Command("Exit", Command.EXIT, 3);

	private IShell shell;
	private IDisplay display;


	private final static String UP_DIRECTORY = "/";
	private final static String MEGA_ROOT = "/";
	private final static String SEP_STR = "/";
	private final static char SEP = '/';

	public FileBrowseActivity(IShell shell) {
		this.shell = shell;
		this.display = JavaRosaServiceProvider.instance().getDisplay();
		this.currDirName = FileUtility.getDefaultRoot();
			
	}
	public void contextChanged(Context globalContext) {
		// TODO Auto-generated method stub

	}

	public void destroy() {
		shell.returnFromActivity(this, "Success!", null);

	}

	public Context getActivityContext() {
		// TODO Auto-generated method stub
		return null;
	}

	public void halt() {
		// TODO Auto-generated method stub

	}

	public void resume(Context globalContext) {
		// TODO Auto-generated method stub

	}

	public void setShell(IShell shell) {
		// TODO Auto-generated method stub

	}

	public void start(Context context) {
		boolean isAPIAvailable = false;
		if (System.getProperty("microedition.io.file.FileConnection.version") != null) {
			isAPIAvailable = true;
			try {
				showCurrDir();
				System.out.println("h5");
			} catch (SecurityException e) {
				System.out.println(e);
			} catch (Exception e) {
				System.out.println(e);
			}
		} else {
			String splashText = new String("Sorry - not available"); 
			Alert splashScreen = new Alert(null, splashText, null,
					AlertType.INFO);
			splashScreen.setTimeout(3000);
			display.setView(DisplayViewFactory.createView(splashScreen));
		}
	}

	public void commandAction(Command c, Displayable d) {
		System.out.println("updir:" + UP_DIRECTORY);
		if (c == view) {
			List curr = (List) d;
			final String currFile = curr.getString(curr.getSelectedIndex());
			System.out.println("currFile:" + currFile);

			new Thread(new Runnable() {
				public void run() {
					if (currFile.endsWith(SEP_STR)
							|| currFile.equals(UP_DIRECTORY)) {
						System.out.println("h7");

						traverseDirectory(currFile);
					} else {
						showFile(currFile);
					}
				}
			}).start();
		} else if (c == back) {
			showCurrDir();
		} else if (c == exit) {
			destroy();
		}
	}

	void showCurrDir() {
		Enumeration e;
		FileConnection currDir = null;
		List browser;
		try {
			System.out.println("In showCurrDir");
			System.out.println("mega_root:" + MEGA_ROOT + "cur_dir:"
					+ currDirName);
			if (MEGA_ROOT.equals(currDirName)) {
				e = FileSystemRegistry.listRoots();
				browser = new List(currDirName, List.IMPLICIT);
				System.out.println("here");
			} else {
				System.out.println("connector");
				currDir = (FileConnection) Connector.open("file://localhost/"
						+ currDirName);
				System.out.println("curr_dir:" + currDir);
				// currDir =
				// (FileConnection)Connector.open("http://localhost:8080/" +
				// currDirName);
				e = currDir.list();
				browser = new List(currDirName, List.IMPLICIT);
				browser.append(UP_DIRECTORY, null);
			}
			while (e.hasMoreElements()) {
				System.out.println("h2");
				String fileName = (String) e.nextElement();
				System.out.println("fileName:" + fileName + " char_at:"
						+ fileName.charAt(fileName.length() - 1));

				if (fileName.charAt(fileName.length() - 1) == SEP) {
					browser.append(fileName, null);

				}
				// if((fileName.charAt(fileName.length()-1))).equals("g"))){}
				else {
					System.out.println("h4");
					// Image image = Image.createImage(fileName);
					browser.append(fileName, null);
					// Form form = new Form("Image here");

					// form.append(image);
				}
			}
			browser.setSelectCommand(view);
			browser.addCommand(exit);
			browser.setCommandListener(this);
			if (currDir != null) {
				currDir.close();
			}
			display.setView(DisplayViewFactory.createView(browser));
		} catch (IOException ioe) {
			//System.out.println(ioe);
		}
	}

	void traverseDirectory(String fileName) {
		System.out.println("fileName:" + fileName + "cur_dir:" + currDirName
				+ "mega_root:" + MEGA_ROOT);
		if (currDirName.equals(MEGA_ROOT)) {
			if (fileName.equals(UP_DIRECTORY)) {
				// can not go up from MEGA_ROOT
				return;
			}
			currDirName = fileName;
		} else if (fileName.equals(UP_DIRECTORY)) {
			System.out.println("up");
			// Go up one directory
			// TODO use setFileConnection when implemented
			int i = currDirName.lastIndexOf(SEP, currDirName.length() - 2);
			if (i != -1) {
				currDirName = currDirName.substring(0, i + 1);
			} else {
				currDirName = MEGA_ROOT;
			}
		} else {
			currDirName = currDirName + fileName;
		}
		showCurrDir();
	}

	void showFile(String fileName) {
		try {
			FileConnection fc = (FileConnection) Connector
					.open("file://localhost/" + currDirName + fileName);
			if (!fc.exists()) {
				throw new IOException("File does not exists");
			}
			InputStream fis = fc.openInputStream();
			byte[] b = new byte[1024];
			int length = fis.read(b, 0, 1024);
			fis.close();
			fc.close();

			TextBox tb = new TextBox("View File: " + fileName, null, 1024,
					TextField.ANY | TextField.UNEDITABLE);

			tb.addCommand(back);
			tb.addCommand(exit);
			tb.setCommandListener(this);

			if (length > 0) {
				tb.setString(new String(b, 0, length));
			}
			display.setView(DisplayViewFactory.createView(tb));
		} catch (Exception e) {
		}
	}
	
}
