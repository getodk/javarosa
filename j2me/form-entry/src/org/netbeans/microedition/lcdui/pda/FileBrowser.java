/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.microedition.lcdui.pda;

import java.io.IOException;
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
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;

import org.javarosa.j2me.log.CrashHandler;
import org.javarosa.j2me.log.HandledCommandListener;
import org.javarosa.j2me.log.HandledThread;

/**
 * The <code>FileBrowser</code> custom component lets the user list files and
 * directories. It's uses FileConnection Optional Package (JSR 75). The FileConnection
 * Optional Package APIs give J2ME devices access to file systems residing on mobile devices,
 * primarily access to removable storage media such as external memory cards.
 * @author breh
 */

public class FileBrowser extends List implements HandledCommandListener {

    /**
     * Command fired on file selection.
     */
    public static final Command SELECT_FILE_COMMAND = new Command("Select", Command.OK, 1);
    public static final Command EXIT_COMMAND = new Command("Exit", Command.BACK, 1);

    private String currDirName;
    private String currFile;
    private Image dirIcon;
    private Image fileIcon;
   // private Image[] iconList;
    CommandListener commandListener;

    /* special string denotes upper directory */
    private static final String UP_DIRECTORY = "..";

    /* special string that denotes upper directory accessible by this browser.
     * this virtual directory contains all roots.
     */
    private static final String MEGA_ROOT = "/";

    /* separator string as defined by FC specification */
    private static final String SEP_STR = "/";

    /* separator character as defined by FC specification */
    private static final char SEP = '/';

    private Display display;

    private String selectedURL;

    private String filter = null;

    private String title;

    /**
     * Creates a new instance of FileBrowser for given <code>Display</code> object.
     * @param display non null display object.
     */
    public FileBrowser(Display display) {
        super("", IMPLICIT);
        currDirName = MEGA_ROOT;
        this.display = display;
        super.setCommandListener(this);
        setSelectCommand(SELECT_FILE_COMMAND);
        try {
            dirIcon = Image.createImage("/org/netbeans/microedition/resources/dir.png");
        } catch (IOException e) {
            dirIcon = null;
        }
        try {
            fileIcon = Image.createImage("/org/netbeans/microedition/resources/file.png");
        } catch (IOException e) {
            fileIcon = null;
        }
      //  iconList = new Image[]{fileIcon, dirIcon};

        showDir();
    }

    private void showDir() {
        new HandledThread(new Runnable() {

            public void run() {
                try {
                    showCurrDir();
                } catch (SecurityException e) {
                    Alert alert = new Alert("Error", "You are not authorized to access the restricted API", null, AlertType.ERROR);
                    alert.setTimeout(2000);
                    //This should be set with a JavaRosa alert, not via the normal display
                    //display.setCurrent(alert, FileBrowser.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Indicates that a command event has occurred on Displayable d.
     * @param c a <code>Command</code> object identifying the command. This is either
     * one of the applications have been added to <code>Displayable</code> with <code>addCommand(Command)</code>
     * or is the implicit <code>SELECT_COMMAND</code> of List.
     * @param d the <code>Displayable</code> on which this event has occurred
     */
	public void commandAction(Command c, Displayable d) {
		CrashHandler.commandAction(this, c, d);
	}  

	public void _commandAction(Command c, Displayable d) {
        if (c.equals(SELECT_FILE_COMMAND)) {
            List curr = (List) d;
            currFile = curr.getString(curr.getSelectedIndex());
            new HandledThread(new Runnable() {
                public void run() {
                    if (currFile.endsWith(SEP_STR) || currFile.equals(UP_DIRECTORY)) {
                        openDir(currFile);
                    } else {
                        //switch To Next
                        doDismiss();
                    }
                }
            }).start();
        } else {
            forwardCommand(c, d);
        }
    }

    /**
     * Sets component's title.
     *  @param title component's title.
     */
    public void setTitle(String title) {
        this.title = title;
        super.setTitle(title);
    }

    /**
     * Show file list in the current directory .
     */
    private void showCurrDir() {
        if (title == null) {
            super.setTitle(currDirName);
        }
        Enumeration e = null;
        FileConnection currDir = null;

        deleteAll();
        if (MEGA_ROOT.equals(currDirName)) {
            append(UP_DIRECTORY, dirIcon);
            e = FileSystemRegistry.listRoots();
        } else {
            try {
                currDir = (FileConnection) Connector.open("file:///" + currDirName);
                e = currDir.list();
            } catch (IOException ioe) {
            }
            append(UP_DIRECTORY, dirIcon);
        }

        if (e == null) {
            try {
                currDir.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            return;
        }

        while (e.hasMoreElements()) {
            String fileName = (String) e.nextElement();
            if (fileName.charAt(fileName.length() - 1) == SEP) {
                // This is directory
                append(fileName, dirIcon);
            } else {
                // this is regular file
                if (filter == null || fileName.indexOf(filter) > -1) {
                    append(fileName, fileIcon);
                }
            }
        }

        if (currDir != null) {
            try {
                currDir.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private void openDir(String fileName) {
        /* In case of directory just change the current directory
         * and show it
         */
        if (currDirName.equals(MEGA_ROOT)) {
            if (fileName.equals(UP_DIRECTORY)) {
                // can not go up from MEGA_ROOT
                return;
            }
            currDirName = fileName;
        } else if (fileName.equals(UP_DIRECTORY)) {
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
        showDir();
    }

    /**
     * Returns selected file as a <code>FileConnection</code> object.
     * @return non null <code>FileConection</code> object
     */
    public FileConnection getSelectedFile() throws IOException {
        FileConnection fileConnection = (FileConnection) Connector.open(selectedURL);
        return fileConnection;
    }

    /**
     * Returns selected <code>FileURL</code> object.
     * @return non null <code>FileURL</code> object
     */
    public String getSelectedFileURL() {
        return selectedURL;
    }

    /**
     * Sets the file filter.
     * @param filter file filter String object
     */
    public void setFilter(String filter) {
        this.filter = filter;
    }

    /**
     * Returns command listener.
     * @return non null <code>CommandListener</code> object
     */
    public CommandListener getCommandListener() {
        return commandListener;
    }

    /**
     * Sets command listener to this component.
     * @param commandListener <code>CommandListener</code> to be used
     */
    public void setCommandListener(CommandListener commandListener) {
        this.commandListener = commandListener;
    }

    private void doDismiss() {
        //selectedURL = "file:///" + currDirName + SEP_STR + currFile;
    	selectedURL = "file:///" + currDirName + currFile;
        System.out.println("selURL: "+ currDirName + "    "+ currFile);
        CommandListener commandListener = getCommandListener();
        if (commandListener != null) {
        	forwardCommand(SELECT_FILE_COMMAND, this);
        }
    }

    private void forwardCommand (Command c, Displayable d) {
    	CommandListener cl = getCommandListener();
    	
    	if (cl instanceof HandledCommandListener) {
    		((HandledCommandListener)cl)._commandAction(c, d);
    	} else {
    		cl.commandAction(c, d);
    	}
    }
}
