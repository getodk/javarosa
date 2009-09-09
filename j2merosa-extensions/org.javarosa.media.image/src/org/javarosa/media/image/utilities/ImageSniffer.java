/*
 * Copyright (C) 2009 JavaRosa
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

package org.javarosa.media.image.utilities;

import java.util.Vector;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.services.UnavailableServiceException;
import org.javarosa.media.image.activity.ImageChooserActivity;
import org.javarosa.media.image.model.FileDataPointer;
import org.javarosa.utilities.file.FileException;
import org.javarosa.utilities.file.services.IFileService;
import org.javarosa.utilities.file.services.J2MEFileService;

/**
 * Image Sniffer that polls the contents of a directory and notifies someone when they change
 * 
 * @author Cory Zue
 *
 */
public class ImageSniffer implements Runnable
{
	private boolean quit = false;
	private String directory;
	private Vector foundFiles;
	private ImageChooserActivity chooser;
	private String directoryToUse;
	
	private IFileService fileService;
	
	public ImageSniffer(String directory, ImageChooserActivity chooser) 
	{
		this.directory = directory;
		this.chooser = chooser;
		foundFiles = new Vector();
		
		try
		{
			fileService = getFileService();			
		}
		catch(UnavailableServiceException ue)
		{
			System.err.println(ue.getMessage());
			ue.printStackTrace();
		}		
		System.out.println("Created Sniffer.");
	}
		
	public void run()
	{
		// first pass - run in the background and find new images, just printing out their names
		System.out.println("Starting to sniff: " + directory);
		// CZUE - I'm not sure the most appropriate thing to do here.  For now find the folder
		// with the most recent file and assume that's correct
		try {
			//if (true) throw new RuntimeException("Is this message Showing up?");
			System.out.println("Searching directory: " + directory);
			
			directoryToUse = getDirectoryToSniff();
			
			System.out.println("Searching sub directory: " + directoryToUse);
			chooser.updateImageSniffingDisplay(directoryToUse);
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
			String[] directoryContents = fileService.listDirectory(directoryToUse);
			for(int i = 0; i < directoryContents.length; ++i) 
			{
				String fileName = directoryContents[i];
				if (!foundFiles.contains(fileName)) 
				{
					foundFiles.addElement(fileName);
					// hard code this for now
					if (fileName.endsWith(".jpg")) 
					{
						FileDataPointer fdp = new FileDataPointer(directoryToUse + fileName);
						chooser.addImageToUI(fdp);
					}
					System.out.println("Got a new file: " + fileName);
				}
			}
			
			}
		} 
		catch(FileException fe)
		{
			System.err.println(fe.getMessage());			
			fe.printStackTrace();
		}
		catch(Exception e) 
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
			
		}		
		finally 
		{
			System.out.println("Exiting Sniffer Thread");
		}

	}
	
	public synchronized void setSniffDirectory(String path)
	{
		directoryToUse = path;
	}


	private String getDirectoryToSniff() throws FileException 
	{
		String mostRecentMod = ( (J2MEFileService)fileService ).getMostRecentlyModifiedDirectoryBelow(directory);
		/*
		if (!directory.endsWith("/"))
		{
			directory += "/"; 
		}
		String mostRecentMod = directory + "200812A0/";
		*/ 
		return mostRecentMod; 
	}

	public void quit()
	{
		quit = true;
	}
	
	private IFileService getFileService() throws UnavailableServiceException
	{
		//#if app.usefileconnections
		JavaRosaServiceProvider.instance().registerService(new J2MEFileService());
		IFileService service = (J2MEFileService)JavaRosaServiceProvider.instance().getService(J2MEFileService.serviceName);
		//#endif
		return service;
	}
	/*
	private void serviceUnavailable(Exception e)
	{
		System.err.println("The File Service is unavailable.\n QUITTING!");			
		System.err.println(e.getMessage());
	}
	*/
	
}
