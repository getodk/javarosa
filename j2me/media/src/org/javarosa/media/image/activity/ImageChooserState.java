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

package org.javarosa.media.image.activity;

import java.util.Hashtable;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;

import org.javarosa.core.api.State;
import org.javarosa.core.data.IDataPointer;
import org.javarosa.core.services.UnavailableServiceException;
import org.javarosa.j2me.log.CrashHandler;
import org.javarosa.j2me.log.HandledCommandListener;
import org.javarosa.j2me.services.FileService;
import org.javarosa.j2me.services.exception.FileException;
import org.javarosa.j2me.view.J2MEDisplay;
import org.javarosa.media.image.model.FileDataPointer;
import org.javarosa.media.image.utilities.ImageSniffer;
import org.javarosa.utilities.file.J2MEFileService;

import de.enough.polish.ui.ChoiceGroup;
import de.enough.polish.ui.ChoiceItem;
import de.enough.polish.ui.Form;
import de.enough.polish.util.ArrayList;

/**
 * An Activity that represents the selection of zero or more images. This will
 * launch a UI that supports displaying a list of images, marking some subset of
 * them and returning those images. New images can also be added via an
 * ImageCaptureActivity.
 * 
 * @author Cory Zue
 * 
 */
public abstract class ImageChooserState implements DataCaptureTransitions, State, HandledCommandListener {
	
	/**
	 * String -> IDataPointer map of image names to references
	 **/
	private Hashtable allImages;
	// private FileRMSUtility dataModel;
	//private Form mainFormOld;
	private Form mainForm;
	private ChoiceGroup mainList;
	
	private Command cancelCommand;
	private Command cameraCommand;
	private Command browseCommand;
	private Command returnCommand;
	private Command viewCommand;
	private Command deleteCommand;
	private Command changeSniffDirectoryCommand;
	
	/**
	 * This holds the key to lookup the return value from a capture or browse activity
	 * when it gets control back.
	 */
	private Thread snifferThread;
	private ImageSniffer sniffer;
	private MIDlet midlet;

	private String sniffingPath;
	private FileService fileService;
	private DataCaptureTransitions transitions;
	
	private boolean isSniffingImages = true;

	private boolean isActivelySniffing = false;

	
	public ImageChooserState(MIDlet midlet) 
	{
		this.midlet = midlet;
		transitions = this;
		allImages = new Hashtable();

		cancelCommand = new Command("Cancel", Command.CANCEL, 0);
		returnCommand = new Command("Return", Command.OK, 0);
		cameraCommand = new Command("Camera", Command.SCREEN, 0);
		browseCommand = new Command("Browse", Command.SCREEN, 0);
		viewCommand = new Command("View", Command.SCREEN, 0);
		deleteCommand = new Command("Delete", Command.SCREEN, 0);
		changeSniffDirectoryCommand = new Command("Change Search Directory", Command.SCREEN, 0);
		
		try 
		{
			fileService = getFileService();
		}
		catch(UnavailableServiceException ue)
		{
			serviceUnavailable(ue);
		}
		
	}

	public void destroy() {
		if (sniffer != null) {
			sniffer.quit();
		}
	}

	public void imageFetched (IDataPointer data) {
		addImageToUI(data);
		updateView();
	}
	
	public void dirChanged (IDataPointer data) {
		changeSniffingDirectory(data.getDisplayText());
		updateView();
	}
		
	private void updateView() {
		J2MEDisplay.setView(mainForm);
	}

	public void start() {

		// mainList = new List("Available Images", List.IMPLICIT);
		mainForm = new Form("Image Chooser");
		//mainFormOld = new Form("Old Image Chooser");
		mainForm.addCommand(cancelCommand);
		mainForm.addCommand(cameraCommand);
		mainForm.addCommand(browseCommand);
		mainForm.addCommand(returnCommand);
		mainForm.addCommand(viewCommand);
		mainForm.addCommand(deleteCommand);
		// mainList.setCommandListener(this);
		mainForm.setCommandListener(this);
		// mainForm.append("Use the menu options to take pictures or browse.");

		// also create the sniffer
		if(isSniffingImages) 
		{			
			try
			{
				mainList = new ChoiceGroup("Available Images (searching in " + getImageSniffingPath() + ")", ChoiceGroup.MULTIPLE);
				sniffer = new ImageSniffer(getImageSniffingPath(), this);
			}
			catch(FileException fe)
			{				
				System.err.println("An error occurred while getting image sniffing path.");
				System.err.println("Sniffer could not be created. QUITTING!!!");
				fe.printStackTrace();
				processCancel();
			}
			snifferThread = new Thread(sniffer);
			snifferThread.start();
			isActivelySniffing = true;
			mainForm.addCommand(changeSniffDirectoryCommand);
		} else {
			mainList = new ChoiceGroup("Available Images (not searching file system)", ChoiceGroup.MULTIPLE);
		}
		mainForm.append(mainList);
		updateView();
	}

	/**
	 * The path to sniff images
	 * @return
	 */
	public String getImageSniffingPath() throws FileException 
	{

		if (sniffingPath == null) {
			// default
			// file system testing
			//sniffingPath = "file://localhost/root1/photos/"; 
			// phone testing
			String rootName = fileService.getDefaultRoot();
			sniffingPath = "file://localhost/" + rootName + "Images/";
		}
		return sniffingPath;
	}

	/**
	 * Set or change the path to sniff images.  
	 * @return
	 */
	public void changeSniffingDirectory(String path) {
		sniffingPath = path;
		
		if (isActivelySniffing ) { 
			System.out.println("Setting directory to: " + path);
			sniffer.setSniffDirectory(sniffingPath); 
			mainList.setLabel("Available Images (searching in " + sniffingPath + ")");
		} else {
			throw new RuntimeException("Tried to change the sniffing directory but wasn't sniffing!");
		}
	}


	/**
	 * Set whether to sniff a directory for images.  This will have no effect after
	 * the activity has been started.
	 * @param toSniff
	 */
	public void setSniffingImages(boolean toSniff) {
		isSniffingImages = toSniff;
	}
	
	/**
	 * Whether a directory is/will be sniffed for images
	 * @return
	 */
	public boolean isSniffingImages() {
		return isSniffingImages;
	}
	
	/**
	 * Add an image to this object in the form of a data pointer
	 * @param pointer
	 */
	public synchronized void addImageToUI(IDataPointer pointer) {
		try {
			if (pointer != null) {
				// printMemories("BEFORE load");
				// byte[] data = pointer.getData();
				// printMemories("AFTER load");

				String name = pointer.getDisplayText();
				System.out.println("Reading file: " + name);

				// mainForm.append(pointer.getDisplayText());
				mainList.append(name, null);
				allImages.put(name, pointer);

				/*
				 * testing streams // check hack - stream through and make sure
				 * at least that works
				 * 
				 * InputStream s = pointer.getDataStream();
				 * 
				 * byte[] bytes = new byte[1024]; int i = 0; int totalBytesRead =
				 * 0; int bytesRead = s.read(bytes);
				 * 
				 * System.out.println("Reading file: " +
				 * pointer.getDisplayText()); System.out.println("Initial bytes
				 * read: " + bytesRead + " first byte is: " + bytes[0]); while
				 * (bytesRead >= 0) { totalBytesRead += bytesRead; if (i % 10 ==
				 * 0) { System.out.println("Iteration " + i + " total bytes
				 * read: " + totalBytesRead + " first byte is: " + bytes[0]); }
				 * i++; if (bytesRead != 1024) { break; } bytesRead =
				 * s.read(bytes); }
				 * 
				 */

				// Image img = ImageUtility.resizeImage(pointer.getDataStream(),
				// 64, 48);
				// Image img = Image.createImage(data, 0, data.length);
				// printMemories("AFTER image creation");
				// mainForm.append("Image is " + img.getWidth() + " by " +
				// img.getHeight());
				// Image thumbNail = ImageUtility.createThumbnail(img);
				// printMemories("AFTER thumbnail creation");
				// mainForm.append(img);
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			System.out.println(ex);
			ex.printStackTrace();
			throw new RuntimeException(ex.getMessage());
		}
	}

	public synchronized void updateImageSniffingDisplay(String newPath) {
		mainList.setLabel("Available Images (searching in " + newPath + ")");
	}

	// utility method for memory debugging
	/*
	private void printMemories(String tag) {
		String memory = System.getProperty("com.nokia.memoryramfree");
		long jvmMemory = Runtime.getRuntime().freeMemory();
		System.out.println("Available Memory " + tag + ": " + memory);
		System.out.println("Available JVM Memory " + tag + ": " + jvmMemory);
		System.out.flush();
//		mainFormOld.append("Available Memory " + tag + ": " + memory);
//		mainFormOld.append("Available JVM Memory " + tag + ": " + jvmMemory);
	}
	*/

	public void commandAction(Command c, Displayable d) {
		CrashHandler.commandAction(this, c, d);
	}  

	public void _commandAction(Command command, Displayable d) {
		if (command.equals(cameraCommand)) {
			processCamera();
		} else if (command.equals(browseCommand)) {
			processBrowser();
		} else if (command.equals(cancelCommand)) {
			processCancel();
		} else if (command.equals(changeSniffDirectoryCommand)) {
			processChangeDirectory();
		} else if (command.equals(returnCommand)) {
			processReturn();
		} else {
			ChoiceGroup curr = mainList;
			//int index = curr.getSelectedIndex();
			int index = curr.getFocusedIndex();
			if (index != -1) {
				final String currFile = curr.getString(index);
				if (command.equals(viewCommand)) {
					processView(currFile);

				} else if (command.equals(deleteCommand)) {
					processDelete(currFile);
				}
			}
		}
	}

	private void processReturn() {
		// if we're going back to the original caller then add the images 
		destroy();
		transitions.captured(getSelectedImages());
	}

	private IDataPointer[] getSelectedImages() {
		ArrayList tempList = new ArrayList();
		for (int i = 0; i < mainList.size(); i++) {
			ChoiceItem item = mainList.getItem(i);
			if (item.isSelected) {
				tempList.add(item);
			}
		}
		IDataPointer[] toReturn = new IDataPointer[tempList.size()];
		for (int i = 0; i <tempList.size(); i++) {
			ChoiceItem item = (ChoiceItem) tempList.get(i);
			IDataPointer thisOne = new FileDataPointer(item.getText());
			toReturn[i] = thisOne;
		}
		return toReturn;
	}

	private void processDelete(String file) {
		IDataPointer pointer = (IDataPointer) allImages.get(file);
		if (pointer.deleteData()) {
			mainList.delete(mainList.getFocusedIndex());
		}

	}

	private void processCancel() {
		destroy();
		transitions.cancel();
	}

	private void processView(String file) {
		try {
			System.out.println("Opening: " + file);
			midlet.platformRequest(file);
		} catch (ConnectionNotFoundException e) {
			System.out.println("Error displaying image: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void processBrowser() {
		new FileBrowseState () {
			public void cancel() {
				updateView();
			}

			public void captured(IDataPointer data) {
				imageFetched(data);
			}

			public void captured(IDataPointer[] data) {
				throw new RuntimeException("not applicable");		
			}

			public void noCapture() {
				cancel();
			}
		}.start();
	}

	private void processChangeDirectory() {
		new FileBrowseState (FileBrowseState.MODE_DIRECTORY) {
			public void cancel() {
				updateView();
			}

			public void captured(IDataPointer data) {
				dirChanged(data);
			}

			public void captured(IDataPointer[] data) {
				throw new RuntimeException("not applicable");		
			}

			public void noCapture() {
				cancel();
			}
		}.start();
	}

	private void processCamera() {
		new ImageCaptureState () {
			public void cancel() {
				updateView();
			}

			public void captured(IDataPointer data) {
				imageFetched(data);
			}

			public void captured(IDataPointer[] data) {
				throw new RuntimeException("not applicable");
			}

			public void noCapture() {
				cancel();
			}
		}.start();
	}
	
	private FileService getFileService() throws UnavailableServiceException
	{
		//#if app.usefileconnections
		//#  return new J2MEFileService();
		//#else
		throw new UnavailableServiceException("Unavailable service: " +  J2MEFileService.serviceName);
		//#endif
	}
	
	private void serviceUnavailable(Exception e)
	{
		System.err.println("The File Service is unavailable.\n QUITTING!");			
		System.err.println(e.getMessage());
	}
	
	public void captured (IDataPointer data) {
		throw new RuntimeException("not applicable");
	}
	
	public void noCapture () {
		throw new RuntimeException("not applicable");
	}
}
