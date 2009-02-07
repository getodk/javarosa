package org.javarosa.formtester;

import java.awt.Button;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

public class FormTesterGUI extends Frame implements ActionListener, KeyListener {

	private final String PROPERTIES_FILE = new String("settings.properties");
	private final String WTK_PATH = new String("wtk.path");
	private final String NEW_FORM = new String("new.form");
	private final String ORIGINAL_JAR_DIR = new String("original.jar.dir");
	
	private final String JAR_NAME = new String("JavaRosaFormTest.jar");
	private final String JAD_NAME = new String("JavaRosaFormTest.jad");
	private final String FORM_NAME = new String("a.xhtml");
	
	private final String JAD_START = new String("MIDlet-Name: JavaRosaFormTest\nMIDlet-Version: 0.0.1\n" +
			"MIDlet-Vendor: OpenRosa Consortium\nMIDlet-Jar-URL: JavaRosaFormTest.jar\nMIDlet-Jar-Size: ");
	private final String JAD_FINISH = new String("\nMIDlet-Info-URL: http://code.dimagi.com/JavaRosa/\n" + 
			"MIDlet-1: JavaRosaFormTest,,org.javarosa.shellformtest.midlet.JavaRosaFormTestMidlet");
	
	private final int BUFFER = 2048;
	private final int TF_SIZE = 70;
	
	private String wtkPath = new String("C:\\WTK2.5.2\\");
	private String origJarDir = new String("C:\\TEST\\");
	private String newForm = new String("C:\\TEST\\b.xml");
	
	private Button testBtn = null;
	private Label status = new Label();
	private Label formName = new Label();
	
	private TextField wtkTF = null;
	private TextField jarTF = null;
	private TextField formTF = null;
	
	public static void main(String argv[]) {
		new FormTesterGUI();
	}

	public FormTesterGUI() {
		super();
		// Destroy the window when the user requests it
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});
		
		// Read in the properties!
		readProperties();

		this.setTitle("JavaRosa Form Launcher GUI");

		createGUI();		
		
		this.pack();
		setVisible(true);
		
		//		
		Font orig = this.formName.getFont();
		Font bold = orig.deriveFont(orig.getStyle() ^ Font.BOLD);
		this.formName.setFont(bold);

	}
	
	private void createGUI() {
		Panel f = new Panel();
		f.setSize(800,600);
		
		// Using gridbag layout
		GridBagConstraints gbc = new GridBagConstraints();
		GridBagLayout gridBag = new GridBagLayout();
		f.setLayout(gridBag);
		
		// WTK DIR
		Label l = new Label("Set the WTK directory:");
		l.setAlignment(Label.LEFT);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		f.add(l, gbc);
		
		this.wtkTF = new TextField(this.wtkPath, this.TF_SIZE);
		this.wtkTF.addKeyListener(this);
		gbc.gridy = 1;
		gbc.gridx = 0;
		gbc.gridwidth = 3;
		f.add(this.wtkTF, gbc);
		
		/*Button b = new Button("Choose Directory...");
		b.addActionListener(this);
		b.setActionCommand("wtkdir");
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		f.add(b, gbc);*/
		
		// JAR
		l = new Label("Set the directory for the JAR:");
		l.setAlignment(Label.LEFT);
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		f.add(l, gbc);
		
		this.jarTF = new TextField(this.origJarDir, this.TF_SIZE);
		this.jarTF.addKeyListener(this);
		gbc.gridy = 3;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		f.add(this.jarTF, gbc);
		
		Button b = new Button("Choose Jar...");
		b.addActionListener(this);
		b.setActionCommand("jarfile");
		gbc.gridx = 2;
		gbc.gridy = 3;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		f.add(b, gbc);
		
		// form
		l = new Label("Form to test:");
		l.setAlignment(Label.LEFT);
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		f.add(l, gbc);	
		
		
		this.formName = new Label(this.newForm.substring( this.newForm.lastIndexOf("\\")+1));
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.gridwidth = 2;
		f.add(this.formName, gbc);
		
		this.formTF = new TextField(this.newForm, this.TF_SIZE);
		this.formTF.addKeyListener(this);
		gbc.gridy = 5;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		f.add(this.formTF, gbc);
		
		b = new Button("Choose form...");
		b.addActionListener(this);
		b.setActionCommand("form");
		gbc.gridx = 2;
		gbc.gridy = 5;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		f.add(b, gbc);//		this.add(p);

		l = new Label("Status:");
		l.setAlignment(Label.LEFT);
		gbc.gridy = 6;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		f.add(l,gbc);
		
		updateStatus("The current status will be displayed here.");
		status.setAlignment(Label.LEFT);
		gbc.gridy = 6;
		gbc.gridx = 1;
		gbc.gridwidth = 2;
		f.add(this.status,gbc);
		
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.NORTHEAST;
		gbc.gridy = 7;
		gbc.gridx = 0;
		this.testBtn = new Button("Test Form");
		this.testBtn.addActionListener(this);
		this.testBtn.setActionCommand("test");
		
		f.add(this.testBtn, gbc);
		
		this.add(f);
	}
	
	private void tryForm() {
		updateStatus("Creating work directory");
		
		// mkdir work, delete if exists
		File workDir = new File(this.origJarDir + "work");
		if(workDir.exists()) {
			deleteDirectory(workDir);
		}
		workDir.mkdirs();
		
		updateStatus("Unpacking original JAR");
		
		// expand jar into work
		unjar(this.origJarDir + this.JAR_NAME, workDir.getAbsolutePath());
		
		updateStatus("Replacing file");
		
		// modify the file
		File form = new File( workDir.getAbsolutePath() + "/" + this.FORM_NAME );
		File newForm = new File(this.newForm);
		if(!form.exists()) {
			System.err.println("Error!  Can't find form to replace in JAR file, please make sure you have the correct JAR");
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
		
		// mkdir test, delete if exists
		File testDir = new File(this.origJarDir + "test");
		if(testDir.exists()) {
			deleteDirectory(testDir);
		}
		testDir.mkdirs();
		
		updateStatus("Repacking JAR");
		
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
	private void readProperties() {
		File f = new File(this.PROPERTIES_FILE);
		if(!f.exists()) {
			// No settings, so make the default
			System.out.println("Making a properties file");
			writeProperties();
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
		
		System.out.println(props);
		
		// Read in properties
		this.wtkPath = props.getProperty(this.WTK_PATH);
		this.newForm = props.getProperty(this.NEW_FORM);
		this.origJarDir = props.getProperty(this.ORIGINAL_JAR_DIR);
	}
	
	private void emulate( String wtkPath, String jad) {
		if (wtkPath.charAt(wtkPath.length() - 1) != '/' && wtkPath.charAt(wtkPath.length() - 1) != '\\')
			wtkPath += "\\";
		wtkPath = "\"" + wtkPath;
		jad = "\"" + jad + "\"";
		try {
			System.out.println("Launching emulator: \n\t" + wtkPath + "bin\\emulator.exe\"" + " -Xdescriptor " + jad );
			Runtime.getRuntime().exec( new String[] { wtkPath + "bin\\emulator.exe\"", "-Xdescriptor", jad } );
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
					System.out.println("Found directory: " + files[i]);
					jarDirectory(srcDir, currDir + files[i] + "/", out);
					continue;
				}
				FileInputStream fi = new 
					FileInputStream(srcDir + currDir + files[i]);
				
				System.out.println("Adding: "+currDir + files[i]);
				
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
	
	private void replaceInJar(String jar, String src, String dest) {
		//JarFile jf = new JarFile(jar);
	//	jf.
		
	}

	private void unjar(String filename, String destDir) {
		final int BUFFER = 2048;
		// Make sure we end in a slash
		if (destDir.charAt(destDir.length() - 1) != '/')
			destDir += "\\";

		try {
			BufferedOutputStream dest = null;
			BufferedInputStream is = null;
			JarEntry entry;
			JarFile jarfile = new JarFile(filename);
			Enumeration e = jarfile.entries();
			while (e.hasMoreElements()) {
				entry = (JarEntry) e.nextElement();
				System.out.println("Extracting: " + entry);

				if (entry.isDirectory()) {
					System.out.println("Directory!");
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
					System.err.println("Overwriting file!!!!");
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
			this.origJarDir = this.jarTF.getText();
		}else if(ke.getComponent().equals(this.formTF)) {
			String s = this.formTF.getText();
			this.formName.setText(s.substring(s.lastIndexOf("\\")+1));
			this.newForm = s;
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
	/**
	 * Handle button clicks
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if(cmd.equals("test")) {
			if(!checkParams())
				return;
			
			// We have good params, so save them
			this.writeProperties();
			
			// Now run!
			this.testBtn.setEnabled(false);
			tryForm();
			this.testBtn.setEnabled(true);
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
			this.jarTF.setText(f.getDirectory());
			
			f.dispose(); // Get rid of the dialog box
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
		} //else if(cmd.)
	}
}