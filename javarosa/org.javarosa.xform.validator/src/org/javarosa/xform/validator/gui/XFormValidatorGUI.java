package org.javarosa.xform.validator.gui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;

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

public class XFormValidatorGUI extends Frame implements ActionListener {

	private Label filename = new Label();
	private boolean hasFile = false;
	private BufferLogger bufferedlogger = new BufferLogger();
	private TextArea textarea = new TextArea("", 24, 80);

	public static void main(String argv[]) {
		new XFormValidatorGUI();
	}

	public XFormValidatorGUI() {
		super();
		// Destroy the window when the user requests it
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});

		setLayout(new FlowLayout());

		this.setLayout(new BorderLayout());
		this.setTitle("JavaRosa XFormValidator GUI");

		// put the filename label at the top
		filename.setText("Please load a file...");
		this.add(this.filename, "North");

		// Create a TextArea to display the contents of the file in
		textarea.setFont(new Font("MonoSpaced", Font.PLAIN, 12));
		textarea.setEditable(false);
		this.add("Center", textarea);

		// Create a bottom panel to hold a couple of buttons in
		Panel p = new Panel();
		p.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 5));
		this.add(p, "South");

		// Create the buttons and arrange to handle button clicks
		// Font font = new Font("SansSerif", Font.BOLD, 14);
		Button openfile = new Button("Open File");
		Button eval = new Button("Validate");
		openfile.addActionListener(this);
		openfile.setActionCommand("open");
		// openfile.setFont(font);
		eval.addActionListener(this);
		eval.setActionCommand("eval");
		// eval.setFont(font);
		p.add(openfile);
		p.add(eval);

		PrintStream ps = new PrintStream(this.bufferedlogger);
		System.setErr(ps);
		System.setOut(ps);

		textarea.setText("");

		setSize(400, 1700);

		this.pack();
		setVisible(true);

	}

	/**
	 * Handle button clicks
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd.equals("open")) { // If user clicked "Open" button
			// Create a file dialog box to prompt for a new file to display
			FileDialog f = new FileDialog(this, "Open File", FileDialog.LOAD);

			// Display the dialog and wait for the user's response
			f.setVisible(true);

			String directory = f.getDirectory(); // Remember new default
													// directory
			filename.setText(directory + f.getFile());
			this.hasFile = true;
			f.dispose(); // Get rid of the dialog box
		} else if (cmd.equals("eval")) // If user clicked "validate" button
			this.validateFile(); // then close the window
	}

	private void validateFile() {
		if (!this.hasFile) {
			textarea.setText("Error!  You must open an XForm first!");
			return;
		}

		// Run the test here!
		String xf_name = this.filename.getText();
		System.out.println("XForm validation on file: '" + xf_name + "'...\n");

		FileInputStream is;
		try {
			is = new FileInputStream(xf_name);
		} catch (FileNotFoundException e) {
			System.err.println("Error: the file '" + xf_name
					+ "' could not be found!");
			return;
		}

		XFormUtils.getFormFromInputStream(is);

		// and show the output
		this.textarea.setText(this.bufferedlogger.flush2());
	}
}
