/*
 * Copyright (C) 2009 JavaRosa-Core Project
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

package org.javarosa.xform.validator.gui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.CardLayout;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import org.javarosa.core.model.FormDef;
import org.javarosa.xform.util.XFormUtils;

/*
 * Written by Brian DeRenzi 2009.
 * 
 * Some parts taken from: http://www.javafaq.nu/java-example-code-788.html
 * 
 * Parts Copyright (c) 2004 David Flanagan.  All rights reserved.
 * This code is from the book Java Examples in a Nutshell, 3nd Edition.
 * It is provided AS-IS, WITHOUT ANY WARRANTY either expressed or implied.
 * You may study, use, and modify it for any non-commercial purpose,
 * including teaching and use in open-source projects.
 * You may distribute it non-commercially as long as you retain this notice.
 * For a commercial use license, or to purchase the book,
 * please visit http://www.davidflanagan.com/javaexamples3.
 */

public class XFormValidatorGUI extends Frame implements ActionListener, KeyListener, ItemListener {

	private final String PROPERTIES_FILE = new String("settings.properties");
	private final String WTK_PATH = new String("wtk.path");
	private final String NEW_FORM = new String("new.form");
	private final String ORIGINAL_JAR_DIR = new String("original.jar.dir");
	private final String OPEN_XML_WITH = new String("open.xml.with");
	private final String OPEN_AT_END = new String("open.at.end");
	private final String DEPLOY_JAR_PATH = new String("deploy.jar.path");
	
	private final String JAR_NAME = new String("JavaRosaFormTest.jar");
	private final String JAD_NAME = new String("JavaRosaFormTest.jad");
	private final String FORM_NAME = new String("a.xhtml");
	
	private final String JAD_START = new String("MIDlet-Name: JavaRosaFormTest\nMIDlet-Version: 0.0.1\n" +
			"MIDlet-Vendor: OpenRosa Consortium\nMIDlet-Jar-URL: JavaRosaFormTest.jar\nMIDlet-Jar-Size: ");
	private final String JAD_FINISH = new String("\nMIDlet-Info-URL: http://code.dimagi.com/JavaRosa/\n" + 
			"MIDlet-1: JavaRosaFormTest,,org.javarosa.shellformtest.midlet.JavaRosaFormTestMidlet");
	
	private final int BUFFER = 2048;
	private final int TF_SIZE = 70;
	
	// Properties that we save
	private String wtkPath = new String("C:\\WTK2.5.2\\");
	private String origJarDir = new String(""); // set in constructor
	private String newForm = new String("C:\\TEST\\b.xml");
	private String deployJar = "";
	private static String openXMLWith = new String("C:\\Program Files\\Internet Explorer\\iexplore.exe");
	private static Boolean openAtEnd = new Boolean(true);
	
	// For the gui
	private Panel mainScreen = null;
	private CardLayout cl = null;
	private final static String SETTINGS_SCREEN = "SETTINGS_SCREEN";
	private final static String MAIN_SCREEN = "MAIN_SCREEN";
	private Button testBtn = null;
	private Label status = new Label();
	private Label formName = new Label();
	
	private Checkbox noOpenAtEnd = null;
	private Checkbox yesOpenAtEnd = null;
	
	private TextField wtkTF = null;
	private TextField jarTF = null;
	private TextField deployJarTF = null;
	private TextField formTF = null;
	private TextField viewerEXE = null;
	
	private BufferLogger bufferedlogger = new BufferLogger();
	public static TextArea textarea = new TextArea("", 24, 80);

	public static XFormValidatorGUI instance = null;
	public static BufferedReader brErr = null;
	public static BufferedReader brOut = null;
	public static Process process = null;
	
	public static void main(String argv[]) {
		XFormValidatorGUI.instance = new XFormValidatorGUI();
	}

	public XFormValidatorGUI() {
		super();
		// Destroy the window when the user requests it
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
				// To hell with everything else
				System.exit(0);
			}
		});
		
		// Set the original JAR directory
		File f = new File(".");
		try {
			this.origJarDir = f.getCanonicalPath() + "/";
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// Read in the properties!
		readProperties();
		
		this.setTitle("JavaRosa XFormValidator GUI");
		
		createGUI();

		this.pack();
		setVisible(true);

		// Make the form we're using bold
		Font orig = this.formName.getFont();
		Font bold = orig.deriveFont(orig.getStyle() ^ Font.BOLD);
		this.formName.setFont(bold);
		this.status.setFont(bold);
		
		// Start a timer!
		Timer t = new Timer();
		t.schedule(new CheckOutput(), 100, 100);
		
		// Finally, check our settings
		if(!checkParams()) {
			addToTextArea("ERROR! Check settings and make sure you select a valid file!");
		}else {
			addToTextArea("All settings ok.");
		}
	}
	
	private static void showXMLOutput () {
		try {
			addLineToTextArea("xml output detected");
			
			String xml = "";
			String line = brOut.readLine();
			while (!"ENDXMLOUTPUT".equals(line)) {
				xml = xml + line;
				line = brOut.readLine();
			}
			
			addLineToTextArea("xml output: " + xml);
			
			File tempfile = File.createTempFile("jrxform", ".xml");
			tempfile.deleteOnExit();

			addLineToTextArea("temp file created: " + tempfile.getAbsolutePath());
			
			BufferedWriter out = new BufferedWriter(new FileWriter(tempfile));
			out.write(xml);
			out.close();
			
			addLineToTextArea("temp file written");
			
			if( openAtEnd.booleanValue() ) {
				addLineToTextArea("launching xml viewer:");
				addLineToTextArea("\t" + openXMLWith + " " + tempfile.getAbsolutePath() );
				Runtime.getRuntime().exec(new String[] {openXMLWith, tempfile.getAbsolutePath()});
			} else 
				addLineToTextArea("not launching XML viewer");
		} catch (IOException ioe) { }
	}
	
	// This is such hacky terrible code.  I hope no one ever looks at this file.
	class CheckOutput extends TimerTask {
		public void run() {
			if(brErr != null && brOut != null) {
				// Check if there's output.
				try {
					if(brOut.ready()) {
						String line = brOut.readLine();
						if ("BEGINXMLOUTPUT".equals(line)) {
							showXMLOutput();
						} else {
							addToTextArea(line+"\n");
						}
					}
					
					if(brErr.ready())
						addToTextArea(brErr.readLine()+"\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			int i = 0;
			if( process != null ) {
				try {
					i = process.exitValue();
					process = null;
					addToTextArea("\n==================================\nEmulator exited with code (" + i + ")\n==================================\n");
				}catch(Exception e) {
					// process still going
				}
			}
		}
	}

	public void itemStateChanged(ItemEvent e) {
		// triggered by the checkboxes
		openAtEnd = new Boolean(yesOpenAtEnd.getState());
	}

	/**
	 * Handle button clicks
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd.equals("eval")) {
			// Clear the text area...
			this.textarea.setText("");
			
			// If user clicked "validate" button
			if(checkNewForm())
				this.validateFile(); 
		}else if(cmd.equals("test")) {
			// Clear the text area...
			this.textarea.setText("");

			if(!checkParams()) {
				this.addToTextArea("ERROR: Please fix parameters on the settings screen!");
				return;
			}
			
			// Run validation
			if(!validateFile()) {
				this.addToTextArea("\n\nERROR: cannot launch emulator until file validates!");
				return;
			}
			
			// We have good params, so save them
			this.writeProperties();
			
			// Now run!
			this.testBtn.setEnabled(false);
			tryForm();
			this.testBtn.setEnabled(true);
		} else if(cmd.equals("deploy")) {
			// Clear the text area...
			this.textarea.setText("");

			if(!checkDeployJar() || !checkNewForm()) {
				this.addToTextArea("ERROR: Please fix parameters on the settings screen!");
				return;
			}
			
			// Run validation
			if(!validateFile()) {
				this.addToTextArea("\n\nERROR: cannot deploy form to new jar unless it validates!");
				return;
			}
			
			// We have good params, so save them
			this.writeProperties();
			
			// Now run!
			deploy();
		} else if(cmd.equals("jarfile")) {
			// Create a file dialog box to prompt for a new file to display
			FileDialog f = new FileDialog(this, "Choose Jar", FileDialog.LOAD);

			String dir = this.jarTF.getText();
			dir = dir.substring(0,dir.lastIndexOf("\\"));
			System.out.println("default dir: " + dir);
			f.setDirectory(dir);
			
			// Display the dialog and wait for the user's response
			f.setVisible(true);

			if( f.getFile() == null)
				return;
			
			if( !f.getFile().equals(this.JAR_NAME) ) {
				reportError("Error! Jar must be named: " + this.JAR_NAME);
				return;
			}
			
			this.origJarDir = f.getDirectory();
			this.jarTF.setText(f.getDirectory() + f.getFile());
			
			f.dispose(); // Get rid of the dialog box
			if(checkParams() )
				updateStatus("All settings ok.");
			
		} else if (cmd.equals("selectdeployjar")) {
			// Create a file dialog box to prompt for a new file to display
			FileDialog f = new FileDialog(this, "Choose Jar", FileDialog.LOAD);

			String dir = this.deployJarTF.getText();
			if(dir.lastIndexOf("\\") != -1) {
				dir = dir.substring(0,dir.lastIndexOf("\\"));
			}
			System.out.println("default dir: " + dir);
			f.setDirectory(dir);
			
			// Display the dialog and wait for the user's response
			f.setVisible(true);

			if( f.getFile() == null)
				return;
			
			this.deployJarTF.setText(f.getDirectory() + f.getFile());
			
			f.dispose(); // Get rid of the dialog box
			if(checkParams() )
				updateStatus("All settings ok.");

		} else if(cmd.equals("form")){
			// Create a file dialog box to prompt for a new file to display
			FileDialog f = new FileDialog(this, "Open XForm", FileDialog.LOAD);

			String dir = this.formTF.getText();
			dir = dir.substring(0,dir.lastIndexOf("\\"));
			System.out.println("default dir: " + dir);
			f.setDirectory(dir);
			
			// Display the dialog and wait for the user's response
			f.setVisible(true);

			if( f.getFile() == null)
				return;
			
			this.formName.setText(f.getFile());

			this.newForm = f.getDirectory() + f.getFile();
			this.formTF.setText(this.newForm);
			
			f.dispose(); // Get rid of the dialog box
			checkParams();
		} else if(cmd.equals("viewerexe")) {
			// Create a file dialog box to prompt for a new file to display
			FileDialog f = new FileDialog(this, "Choose viewer EXE", FileDialog.LOAD);

			f.setFilenameFilter(new FilenameFilter() {
				public boolean accept(File directory, String filename) {
					return (filename.endsWith(".exe"));
				}
			});
			
			String dir = this.viewerEXE.getText();
			dir = dir.substring(0,dir.lastIndexOf("\\"));
			f.setDirectory(dir);
			
			// Display the dialog and wait for the user's response
			f.setVisible(true);

			if( f.getFile() == null)
				return;
			
			openXMLWith = f.getDirectory() + f.getFile();
			this.viewerEXE.setText(openXMLWith);
			
			f.dispose(); // Get rid of the dialog box
			
			checkParams();
		} else if(cmd.equals(SETTINGS_SCREEN)) {
			if(checkParams())
				updateStatus("All settings ok.");
			this.cl.show(this.mainScreen, SETTINGS_SCREEN);
		} else if(cmd.equals(MAIN_SCREEN)) {
			this.cl.show(this.mainScreen, MAIN_SCREEN);
		} else if(cmd.equals("save_settings")) {
			if( checkParams() ) {
				this.writeProperties();
				updateStatus("All settings ok. Settings saved.");
			} else
				reportError(status.getText() + " - settings not saved!");
		}
	}
	
	public static void addLineToTextArea( String s ) {
		addToTextArea(s + "\n");
	}
	
	// I'm a terrible person
	public static void addToTextArea( String s ) {
		String p = XFormValidatorGUI.textarea.getText();
		if( p == null || p.equals("") )
			XFormValidatorGUI.textarea.setText(s);
		else 
			XFormValidatorGUI.textarea.setText(p + s);
		
		textarea.setCaretPosition(Integer.MAX_VALUE);
	}

	private boolean validateFile() {
		// Clear the buffer
		this.bufferedlogger.flush2();
		
		// Capture output
		PrintStream ps = new PrintStream(this.bufferedlogger);
		PrintStream origE = System.err;
		PrintStream origO = System.out;
		System.setErr(ps);
		System.setOut(ps);
		
		updateStatus("Starting validation");
		boolean success = false;
		
		// Run the test here!
		String xf_name = this.newForm; 
			//this.filename.getText();
		
		System.out.println(Calendar.getInstance().getTime() + "\n");
		System.out.println("XForm validation on file: '" + xf_name + "'...\n");

		FileInputStream is;
		try {
			is = new FileInputStream(xf_name);
		} catch (FileNotFoundException e) {
			System.err.println("Error: the file '" + xf_name
					+ "' could not be found!");
			return false;
		}
		
		// File found, so save it
		writeProperties();

		// Test!
		FormDef xform = XFormUtils.getFormFromInputStream(is);
		
		if( xform == null ) {
			updateStatus("XForm failed validation, see text area for details.");
			System.out.println("\n\n==================================\nERROR: XForm has failed validation!!");
			success = false;
		} else {
			updateStatus("XForm has probably passed validation.  Please check any warnings");
			System.out.println("\n\n==================================\nXForm has passed parsing validation.  There may still be errors, \nso please check for any warnings and test your form!");
			success = true;
		}
		
		// Restore error and out 
		System.setErr(origE);
		System.setOut(origO);
		
		// and show the output
		String prev = this.textarea.getText();
		this.textarea.setText(prev + this.bufferedlogger.flush2());
		
		return success;
	}
	

	
	private void createGUI() {
		this.setPreferredSize(new Dimension(900,570));
		
		// Create the screen.
		Panel screen = new Panel();
		screen.setLayout(new BorderLayout());
		
		Panel p = createTabs();
		Panel p2 = createMainScreen();
		
		screen.add("North", p);
		screen.add("Center", p2);
		
		this.add(screen);
	}
	
	private Panel createTabs() {
		Panel p = new Panel();
		p.setLayout(new FlowLayout());
		Button b = new Button("Main Screen");
		b.addActionListener(this);
		b.setActionCommand(MAIN_SCREEN);
		p.add(b);
		b = new Button("Settings");
		b.addActionListener(this);
		b.setActionCommand(SETTINGS_SCREEN);
		p.add(b);
		return p;
	}
	
	private Panel createMainScreen() {
		mainScreen = new Panel();
		cl = new CardLayout();
		mainScreen.setLayout(cl);
		mainScreen.add(createMainCard(), MAIN_SCREEN);
		mainScreen.add(createSettingsCard(), SETTINGS_SCREEN);
		return mainScreen;
	}
	
	private Panel createSettingsCard() {
		Panel settings = new Panel();
		
		// Using gridbag layout
		GridBagConstraints gbc = new GridBagConstraints();
		GridBagLayout gridBag = new GridBagLayout();
		settings.setLayout(gridBag);
		
		// Status
		Label l = new Label("Status:");
		l.setAlignment(Label.LEFT);
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		settings.add(l,gbc);

		updateStatus("The current status will be displayed here.");
		status.setAlignment(Label.LEFT);
		gbc.gridx = 1;
		gbc.gridwidth = 2;
		settings.add(this.status,gbc);

		
		// WTK DIR
		l = new Label("Set the WTK directory:");
		l.setAlignment(Label.LEFT);
		gbc.gridx = 0;
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		settings.add(l, gbc);
		
		this.wtkTF = new TextField(this.wtkPath, this.TF_SIZE);
		this.wtkTF.addKeyListener(this);
		gbc.gridx = 0;
		gbc.gridwidth = 3;
		settings.add(this.wtkTF, gbc);
		
		// JAR
		l = new Label("Chose the JavaRosaFormTest JAR:");
		l.setAlignment(Label.LEFT);
		gbc.gridx = 0;
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		settings.add(l, gbc);
		
		this.jarTF = new TextField(this.origJarDir + "\\" + this.JAR_NAME, this.TF_SIZE);
		this.jarTF.addKeyListener(this);
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		settings.add(this.jarTF, gbc);
		
		Button b = new Button("Choose Jar...");
		b.addActionListener(this);
		b.setActionCommand("jarfile");
		gbc.gridx = 2;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		settings.add(b, gbc);
		
		//Deployment JAR
		Label deploylabel = new Label("Chose the JAR to Deploy to:");
		deploylabel.setAlignment(Label.LEFT);
		gbc.gridx = 0;
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		settings.add(deploylabel, gbc);
		
		this.deployJarTF = new TextField(this.deployJar);
		this.deployJarTF.addKeyListener(this);
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		settings.add(this.deployJarTF, gbc);
		
		Button deploySelect = new Button("Choose Jar...");
		deploySelect.addActionListener(this);
		deploySelect.setActionCommand("selectdeployjar");
		gbc.gridx = 2;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		settings.add(deploySelect, gbc);
		
		// open at end?
		l = new Label("Open viewer when emulator finishes?");
		l.setAlignment(Label.LEFT);
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.NONE;
		settings.add(l, gbc);
		CheckboxGroup cbg = new CheckboxGroup();
		yesOpenAtEnd = new Checkbox("Yes", cbg, openAtEnd.booleanValue());
		yesOpenAtEnd.addItemListener(this);
		noOpenAtEnd = new Checkbox("No", cbg, !openAtEnd.booleanValue());
		noOpenAtEnd.addItemListener(this);
		settings.add(yesOpenAtEnd,gbc);
		settings.add(noOpenAtEnd,gbc);
		
		// viewer to use
		l = new Label("Choose the executable for the viewer:");
		l.setAlignment(Label.LEFT);
		gbc.gridx = 0;
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		settings.add(l, gbc);
		
		this.viewerEXE = new TextField(openXMLWith, this.TF_SIZE);
		this.viewerEXE.addKeyListener(this);
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		settings.add(this.viewerEXE, gbc);
		
		b = new Button("Choose EXE...");
		b.addActionListener(this);
		b.setActionCommand("viewerexe");
		gbc.gridx = 2;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		settings.add(b, gbc);
		
		// save button
		b = new Button("Save settings");
		b.addActionListener(this);
		b.setActionCommand("save_settings");
		Panel p = new Panel();
		p.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 5));
		p.add(b);
		settings.add(p,gbc);
		
		return settings;
	}
	
	private Panel createMainCard() {		
		Panel main = new Panel();

		// Using gridbag layout
		GridBagConstraints gbc = new GridBagConstraints();
		GridBagLayout gridBag = new GridBagLayout();
		main.setLayout(gridBag);

		// Create a TextArea to display the contents of the file in
		textarea.setFont(new Font("MonoSpaced", Font.PLAIN, 12));
		textarea.setEditable(false);
		textarea.setColumns(120);
		gbc.gridx = 0;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		main.add(textarea,gbc); 
		
		// form
		Label l = new Label("Form to test:");
		l.setAlignment(Label.LEFT);
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		main.add(l, gbc);	
		
		// The form to test
		this.formName = new Label(this.newForm.substring( this.newForm.lastIndexOf("\\")+1));
		gbc.gridx = 1;
		gbc.gridwidth = 2;
		main.add(this.formName, gbc);
		
		this.formTF = new TextField(this.newForm, this.TF_SIZE);
		this.formTF.addKeyListener(this);
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		main.add(this.formTF, gbc);
		
		Button b = new Button("Choose form...");
		b.addActionListener(this);
		b.setActionCommand("form");
		gbc.gridx = 2;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		main.add(b, gbc);//		this.add(p);


		// Test form button
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.NORTHEAST;
//		gbc.gridy = 7;
		gbc.gridx = 0;
		this.testBtn = new Button("Test Form");
		this.testBtn.addActionListener(this);
		this.testBtn.setActionCommand("test");

		// Create a bottom panel to hold a couple of buttons in
		Panel p = new Panel();
		p.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 5));
		
		//this.add(p, "South");

		// Create the buttons and arrange to handle button clicks
		// Font font = new Font("SansSerif", Font.BOLD, 14);
//		Button openfile = new Button("Open File");
		Button eval = new Button("Validate");
//		openfile.addActionListener(this);
//		openfile.setActionCommand("open");
		// openfile.setFont(font);
		eval.addActionListener(this);
		eval.setActionCommand("eval");
		// eval.setFont(font);
//		p.add(openfile);
		p.add(eval);
		
		p.add(this.testBtn);
		
		Button deploy = new Button("Deploy");
		deploy.addActionListener(this);
		deploy.setActionCommand("deploy");
		p.add(deploy);

		main.add(p, gbc);
		textarea.setText("");

		return main;
	}
	
	private static String extractPath(String file) {
		return file.substring(0,file.lastIndexOf("\\"));
	}
	
	private void deploy(){
		addToTextArea("\n\n==================================\nStarting deployment\n==================================\n");
		updateStatus("Creating work directory");
		addToTextArea("Creating work directory\n");
		
		// mkdir work, delete if exists
		File workDir = new File(extractPath(deployJarTF.getText()) + "work");
		if(workDir.exists()) {
			deleteDirectory(workDir);
		}
		workDir.mkdirs();
		
		updateStatus("Unpacking original JAR");
		addToTextArea("Unpacking original JAR...\n");
		
		// expand jar into work
		if( !unjar(deployJarTF.getText(), workDir.getAbsolutePath()) )
			return;
		
		updateStatus("Replacing file");
		addToTextArea("Replacing file\n");
		
		File newForm = new File(this.newForm);
		String filename = newForm.getName();
		
		// modify the file
		File form = new File( workDir.getAbsolutePath() + File.separator + filename );

		if(!form.exists()) {
			System.err.println("Error!  Can't find form to replace in JAR file, please make sure you have the correct JAR");
			addToTextArea("\nError!  Can't find form to replace in JAR file, please make sure you have the correct JAR\n");
			
			deleteDirectory(workDir);
			return;
		}
		if(!newForm.exists()) {
			System.err.println("No new form to copy");
			deleteDirectory(workDir);
			return;
		}
		form.delete();
		
		try {
			byte []data = new byte[BUFFER];
			form.createNewFile();
			
			FileInputStream in = new FileInputStream(newForm);
			FileOutputStream out = new FileOutputStream(form);
			
			int count;
			while((count = in.read(data, 0, 
					BUFFER)) != -1) {
				out.write(data, 0, count);
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		updateStatus("Repacking JAR");
		addToTextArea("Repacking JAR...\n");

		File jar = new File(deployJarTF.getText());
		
		// recompress into test/JavaRosaFormTest.jad
		long size = jar(workDir.getAbsolutePath(), extractPath(deployJarTF.getText()), jar.getName());
		
		// delete work
		deleteDirectory(workDir);
		
		// get the JAR size
		String jarSize = new String("" + size);
		
		updateStatus("Writing Jad File");
		addToTextArea("Writing Jad File...\n");
		// make the jad file
		String outJad = deployJarTF.getText().replaceAll(".jar", ".jad");
		
		try {
			updateJad(outJad, jarSize);
		} catch(FileNotFoundException e) {
			String message = "Jad file could not be found! Make sure that the file " + outJad + " exists along with your Jar file!\n";
			addToTextArea(message);
			System.err.print(message);
			return;
		}
		
		// launch emulator
		updateStatus("Form Deployed!");
		addToTextArea("\n\n==================================\nForm Succesfully Deployed!\n==================================\n");
	}
	
	private void updateJad(String path, String newSize) throws FileNotFoundException {
		File jad = new File(path);
		if(!jad.exists()) {
			throw new FileNotFoundException();
		}
		
		File newJad = new File(path + ".tmp");
		BufferedWriter writer = null;
		if (newJad.exists()) {
			newJad.delete();
		}
		try {
			newJad.createNewFile();
			writer = new BufferedWriter(new FileWriter(newJad));
		} catch (IOException e) {
			throw new FileNotFoundException("Problem Creating new Jad file");
		}

		BufferedReader reader = new BufferedReader(new FileReader(path));
		try {
			
		String line = null;
		while((line = reader.readLine()) != null) {
			if(line.indexOf("MIDlet-Jar-Size:") != -1) {
				writer.write("MIDlet-Jar-Size: " + newSize + "\n");
			} else {
				writer.write(line + "\n");
			}
		}
		
		writer.flush();
		writer.close();
		reader.close();
		
		if(!newJad.renameTo(jad)) {
			jad.delete();
			if(!newJad.renameTo(jad)) {
				throw new RuntimeException("Could not write new jad file");
			}
		}
		
		}
		catch(IOException e) {
			//I don't even know.
			throw new RuntimeException("Problem writing jad", e);
		} finally {
			try {
				writer.close();
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("Problem closing the streams for the files. Seriously.", e);
			}
		}
	}
	
	private void tryForm() {
		addToTextArea("\n\n==================================\nStarting repackage and emulation\n==================================\n");
		updateStatus("Creating work directory");
		addToTextArea("Creating work directory\n");
		
		// mkdir work, delete if exists
		File workDir = new File(this.origJarDir + "work");
		if(workDir.exists()) {
			deleteDirectory(workDir);
		}
		workDir.mkdirs();
		
		updateStatus("Unpacking original JAR");
		addToTextArea("Unpacking original JAR...\n");
		
		// expand jar into work
		if( !unjar(this.origJarDir + this.JAR_NAME, workDir.getAbsolutePath()) )
			return;
		
		updateStatus("Replacing file");
		addToTextArea("Replacing file\n");
		
		// modify the file
		File form = new File( workDir.getAbsolutePath() + "/" + this.FORM_NAME );
		File newForm = new File(this.newForm);
		if(!form.exists()) {
			System.err.println("Error!  Can't find form to replace in JAR file, please make sure you have the correct JAR");
			addToTextArea("\nError!  Can't find form to replace in JAR file, please make sure you have the correct JAR\n");
			
			deleteDirectory(workDir);
			return;
		}
		if(!newForm.exists()) {
			System.err.println("No new form to copy");
			deleteDirectory(workDir);
			return;
		}
		form.delete();
		
		try {
			byte []data = new byte[BUFFER];
			form.createNewFile();
			
			FileInputStream in = new FileInputStream(newForm);
			FileOutputStream out = new FileOutputStream(form);
			
			int count;
			while((count = in.read(data, 0, 
					BUFFER)) != -1) {
				out.write(data, 0, count);
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		updateStatus("Creating test directory");
		addToTextArea("Creating test directory\n");

		// mkdir test, delete if exists
		File testDir = new File(this.origJarDir + "test");
		if(testDir.exists()) {
			deleteDirectory(testDir);
		}
		testDir.mkdirs();
		
		updateStatus("Repacking JAR");
		addToTextArea("Repacking JAR...\n");

		
		// recompress into test/JavaRosaFormTest.jad
		long size = jar(workDir.getAbsolutePath(), testDir.getAbsolutePath(), this.JAR_NAME);
		
		// delete work
		deleteDirectory(workDir);
		
		// get the JAR size
		String jarSize = new String("" + size);
		
		// make the jad file
		String outJad = new String(testDir.getAbsolutePath() + "\\" + this.JAD_NAME);
		try {
			PrintWriter pw = new PrintWriter(outJad);
			pw.print(this.JAD_START);
			pw.print(jarSize);
			pw.print(this.JAD_FINISH);
			pw.flush();
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		// launch emulator
		updateStatus("Launching emulator!");
		
		emulate(this.wtkPath, outJad);
	}
	
	private boolean deleteDirectory(File path) {
		if( path.exists() ) {
			File[] files = path.listFiles();
			for(int i=0; i<files.length; i++) {
				if(files[i].isDirectory()) {
					deleteDirectory(files[i]);
				}
				else {
					files[i].delete();
				}
			}
		}
		return( path.delete() );
	}
	
	private void writeProperties() {
		Properties p = new Properties();
		
		// DEFAULT PROPERTIES
		p.setProperty(this.WTK_PATH, this.wtkPath);
		p.setProperty(this.NEW_FORM, this.newForm);
		p.setProperty(this.ORIGINAL_JAR_DIR, this.origJarDir);
		p.setProperty(this.OPEN_XML_WITH, openXMLWith);
		p.setProperty(this.OPEN_AT_END, openAtEnd.toString());
		p.setProperty(this.DEPLOY_JAR_PATH, deployJarTF.getText());
		
		File f = new File(this.PROPERTIES_FILE);
		try {
			if( !f.exists() )
				f.createNewFile();
			FileOutputStream fos = new FileOutputStream(f);
			p.store(fos, this.PROPERTIES_FILE);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	static boolean noRecurse = false;
	
	private void readProperties() {
		File f = new File(this.PROPERTIES_FILE);
		if(!f.exists() && !noRecurse) {
			// No settings, so make the default
			System.out.println("Making a properties file");
			writeProperties();
			noRecurse = true;
			readProperties();
			return;
		}
		
		Properties props = new java.util.Properties();
		try {
			FileInputStream fis = new FileInputStream(f);
			props.load(fis);
			fis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Read in properties
		this.wtkPath = props.getProperty(this.WTK_PATH);
		this.newForm = props.getProperty(this.NEW_FORM);
		this.origJarDir = props.getProperty(this.ORIGINAL_JAR_DIR);
		openXMLWith = props.getProperty(this.OPEN_XML_WITH);
		openAtEnd = new Boolean(props.getProperty(this.OPEN_AT_END));
		deployJar = props.getProperty(this.DEPLOY_JAR_PATH);
	}
	
	private void emulate( String wtkPath, String jad) {
		if (wtkPath.charAt(wtkPath.length() - 1) != '/' && wtkPath.charAt(wtkPath.length() - 1) != '\\')
			wtkPath += "\\";
		wtkPath = "\"" + wtkPath;
		jad = "\"" + jad + "\"";
		try {
			System.out.println("Launching emulator: \n\t" + wtkPath + "bin\\emulator.exe\"" + " -Xdescriptor " + jad );
			addToTextArea("\nLaunching emulator: \n\t" + wtkPath + "bin\\emulator.exe\"" + " -Xdescriptor " + jad + "\n\n");

			process = Runtime.getRuntime().exec( new String[] { wtkPath + "bin\\emulator.exe\"", "-Xdescriptor", jad } );
			brOut = new BufferedReader(
			        new InputStreamReader(process.getInputStream()) );
			brErr = new BufferedReader(
			        new InputStreamReader(process.getErrorStream()) );
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	private long jar(String srcDir, String destDir, String jarname) {
		if (destDir.charAt(destDir.length() - 1) != '/')
			destDir += "\\";
		if (srcDir.charAt(srcDir.length() - 1) != '/')
			srcDir += "\\";
		try {
			FileOutputStream fos = new FileOutputStream(destDir + jarname);
			JarOutputStream out = new JarOutputStream(new BufferedOutputStream(fos));

			// Recursively add all files and directories in the source directory
			jarDirectory(srcDir, "", out);
			
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("done creating JAR!");
		
		File f = new File(destDir + jarname);
		long size = f.length();
		System.out.println("size: " + size );
		return size;
	}
	
	private void jarDirectory(String srcDir, String currDir, JarOutputStream out) {
		try {
			BufferedInputStream origin = null;
			byte data[] = new byte[BUFFER];
			// get a list of files from current directory
			File f = new File(srcDir + currDir);
			String files[] = f.list();

			for (int i=0; i<files.length; i++) {
				File currFile = new File(srcDir + currDir +  files[i]);
				if(currFile.isDirectory()) {
//					System.out.println("Found directory: " + files[i]);
					jarDirectory(srcDir, currDir + files[i] + "/", out);
					continue;
				}
				FileInputStream fi = new 
					FileInputStream(srcDir + currDir + files[i]);
				
//				System.out.println("Adding: "+currDir + files[i]);
				
				origin = new BufferedInputStream(fi, BUFFER);
				ZipEntry entry = new ZipEntry(currDir + files[i]);
				out.putNextEntry(entry);
				int count;
				while((count = origin.read(data, 0, 
						BUFFER)) != -1) {
					out.write(data, 0, count);
				}
				origin.close();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	private boolean unjar(String filename, String destDir) {
		final int BUFFER = 2048;
		// Make sure we end in a slash
		if (destDir.charAt(destDir.length() - 1) != '/')
			destDir += "\\";

		try {
			BufferedOutputStream dest = null;
			BufferedInputStream is = null;
			JarEntry entry;
			JarFile jarfile;
			try{
				jarfile = new JarFile(filename);
			}catch(Exception e) {
				addToTextArea("\nError cannot find JAR t unpack: "+filename);
				return false;
			}
			Enumeration e = jarfile.entries();
			while (e.hasMoreElements()) {
				entry = (JarEntry) e.nextElement();
//				System.out.println("Extracting: " + entry);

				if (entry.isDirectory()) {
//					System.out.println("Directory!");
					File d = new File(destDir + entry.getName());
					d.mkdirs();
					continue;
				}

				File f = new File(destDir + entry.getName());
				if (!f.exists()) {
					File f2 = new File(f.getParent());
					f2.mkdirs();
					f.createNewFile();
				} else {
//					System.err.println("Overwriting file!!!!");
					// Should probably not do that?
				}

				is = new BufferedInputStream(jarfile.getInputStream(entry));
				int count;
				byte data[] = new byte[BUFFER];
				FileOutputStream fos = new FileOutputStream(f);
				dest = new BufferedOutputStream(fos, BUFFER);
				while ((count = is.read(data, 0, BUFFER)) != -1) {
					dest.write(data, 0, count);
				}
				dest.flush();
				dest.close();
				is.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	private boolean checkParams() {
		// Make sure everything exists.
		// Check WTK directory
		return checkWTK() && checkOriginalJAR() && checkNewForm();
	}
	
	private boolean checkNewForm() {
		File form = new File(this.newForm);
		if(!form.exists() || form.isDirectory()) {
			reportError("Error: New form cannot be found!");
			addToTextArea("Error! File \"" + this.newForm + "\" does not exist.  Please choose a new file.\n" );
			return false;
		}
		return true;
	}
	
	private boolean checkOriginalJAR() {
		File jar = new File(this.origJarDir + "/" + this.JAR_NAME);
		if(!jar.exists() || jar.isDirectory()) {
			reportError("Error: " + this.JAR_NAME + " not found in jar directory");
			return false;
		}
		return true;
	}
	
	private boolean checkDeployJar() {
		File jar = new File(this.deployJarTF.getText());
		if(!jar.exists() || jar.isDirectory()) {
			reportError("Error: no jar file to deploy to found at " + this.deployJarTF);
			return false;
		}
		return true;
	}
	
	private boolean checkWTK() {
		File wtk = new File(this.wtkPath);
		if(!wtk.exists() || !wtk.isDirectory() ) {
			reportError("Error: WTK directory does not exist!");
			return false;
		}
		
		// Check that it's actually a WTK install
		File emu = new File(this.wtkPath + "/bin/emulator.exe");
		if(!emu.exists() || emu.isDirectory() ) {
			reportError("Error: WTK directory is not a valid WTK install!");
			return false;
		}
		
		return true;
	}
	
	public void keyPressed(KeyEvent arg0) {}

	public void keyReleased(KeyEvent ke) {
		if(ke.getComponent().equals(this.wtkTF)) {
			this.wtkPath = this.wtkTF.getText();
		}else if(ke.getComponent().equals(this.jarTF)) {
			String s = this.jarTF.getText();
			this.origJarDir = s.substring(0,s.lastIndexOf("\\"));
		}else if(ke.getComponent().equals(this.formTF)) {
			String s = this.formTF.getText();
			this.formName.setText(s.substring(s.lastIndexOf("\\")+1));
			this.newForm = s;
		}else if(ke.getComponent().equals(this.viewerEXE)) {
			openXMLWith = this.viewerEXE.getText();
		}
	}

	public void keyTyped(KeyEvent ke) {}

	private void reportError(String e) {
		this.status.setForeground(Color.RED);
		this.status.setText(e);
	}
	
	private void updateStatus(String s) {
		this.status.setForeground(Color.BLACK);
		this.status.setText(s);
	}
	
}
