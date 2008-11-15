package org.javarosa.media.image.utilities;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.List;

import org.javarosa.media.image.activity.ImageChooserActivity;
import org.javarosa.media.image.model.FileDataPointer;

/**
 * Image Sniffer that polls the contents of a directory and notifies someone when they change
 * 
 * @author Cory Zue
 *
 */
public class ImageSniffer implements Runnable {

	private boolean quit = false;
	private String directory;
	private Vector foundFiles;
	private ImageChooserActivity chooser;
	
	public ImageSniffer(String directory, ImageChooserActivity chooser) {
		this.directory = directory;
		this.chooser = chooser;
		foundFiles = new Vector();
		
		System.out.println("Created Sniffer.");
	}
		
	public void run() {
		// first pass - run in the background and find new images, just printing out their names
		System.out.println("Starting to sniff: " + directory);
		try {
			while (!quit) {
			// sleep a second between polls
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				System.out.println("Thread interrupted!" + e.getMessage());
				e.printStackTrace();
			}
			Enumeration directoryContents = FileUtility.listDirectory(directory);
			while (directoryContents.hasMoreElements()) {
				String fileName = (String) directoryContents.nextElement();
				if (!foundFiles.contains(fileName)) {
					foundFiles.addElement(fileName);
					// hard code this for now
					if (fileName.endsWith(".jpg")) {
						FileDataPointer fdp = new FileDataPointer(directory + fileName);
						chooser.addImageToUI(fdp);
					}
					System.out.println("Got a new file: " + fileName);
				}
			}
			
			}
			System.out.println("Bye bye!");
		} finally {
			// TODO cleanup
			System.out.println("Really really leaving from here.");
		}

	}

	public void quit() {
		quit = true;
	}

}
