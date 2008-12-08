package org.javarosa.media.image.utilities;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Display;
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
		// CZUE - I'm not sure the most appropriate thing to do here.  For now find the folder
		// with the most recent file and assume that's correct
		try {
			//if (true) throw new RuntimeException("Is this message Showing up?");
			chooser.addTextToUI("Searching directory: " + directory);
			
			String directoryToUse = getDirectoryToSniff();
			
			chooser.addTextToUI("Searching sub directory: " + directoryToUse);
			System.out.println("Most recently modified directory below: " + directory + " is: " + directoryToUse);
		
		
			while (!quit) {
			// sleep a second between polls
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				System.out.println("Thread interrupted!" + e.getMessage());
				e.printStackTrace();
			}
			Enumeration directoryContents = FileUtility.listDirectory(directoryToUse);
			while (directoryContents.hasMoreElements()) {
				String fileName = (String) directoryContents.nextElement();
				if (!foundFiles.contains(fileName)) {
					foundFiles.addElement(fileName);
					// hard code this for now
					if (fileName.endsWith(".jpg")) {
						FileDataPointer fdp = new FileDataPointer(directoryToUse + fileName);
						chooser.addImageToUI(fdp);
					}
					System.out.println("Got a new file: " + fileName);
				}
			}
			
			}
			System.out.println("Bye bye!");
		} catch (Exception e) {
			chooser.addTextToUI(e.getMessage());
			e.printStackTrace();
			
		} finally {
			// TODO cleanup
			System.out.println("Really really leaving from here.");
		}

	}

	private String getDirectoryToSniff() {
		String mostRecentMod =FileUtility.getMostRecentlyModifiedDirectoryBelow(directory);
		/*
		if (!directory.endsWith("/"))
		{
			directory += "/"; 
		}
		String mostRecentMod = directory + "200812A0/";
		*/ 
		return mostRecentMod; 
	}

	public void quit() {
		quit = true;
	}

}
