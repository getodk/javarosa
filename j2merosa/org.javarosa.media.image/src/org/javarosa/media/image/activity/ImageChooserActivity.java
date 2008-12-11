package org.javarosa.media.image.activity;

import java.util.Hashtable;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable; //import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDlet;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IDisplay;
import org.javarosa.core.api.IShell;
import org.javarosa.core.model.data.IDataPointer;
import org.javarosa.j2me.view.DisplayViewFactory;
import org.javarosa.media.image.model.FileDataPointer;
import org.javarosa.media.image.utilities.FileUtility;
import org.javarosa.media.image.utilities.ImageSniffer;

import de.enough.polish.ui.ChoiceGroup;
import de.enough.polish.ui.ChoiceItem;
import de.enough.polish.ui.Container;
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
public class ImageChooserActivity implements IActivity, CommandListener {
	public static final String ACTIVITY_KEY = "ACTIVITY_KEY";
	
	// this should map images (IDataPointers) to bool true/false whether
	// selected or not
	private Hashtable allImages;
	private Context context;
	private IShell shell;
	private IDisplay display;
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
	
	private String currentKey;
	private Thread snifferThread;
	private ImageSniffer sniffer;
	private MIDlet midlet;

	private String sniffingPath;
	
	private int callBackActivity = ACTIVITY_NONE;
	private static final int ACTIVITY_NONE = 0;
	private static final int ACTIVITY_DIRECTORY_CHANGE = 1;
	
	private boolean isSniffingImages = true;

	private boolean isActivelySniffing = false;

	
	public ImageChooserActivity(IShell shell, MIDlet midlet) {
		this.shell = shell;
		this.midlet = midlet;
		allImages = new Hashtable();
		display = JavaRosaServiceProvider.instance().getDisplay();
		// dataModel = new FileRMSUtility("image_store");

		cancelCommand = new Command("Cancel", Command.CANCEL, 0);
		returnCommand = new Command("Return", Command.OK, 0);
		cameraCommand = new Command("Camera", Command.SCREEN, 0);
		browseCommand = new Command("Browse", Command.SCREEN, 0);
		viewCommand = new Command("View", Command.SCREEN, 0);
		deleteCommand = new Command("Delete", Command.SCREEN, 0);
		changeSniffDirectoryCommand = new Command("Change Search Directory", Command.SCREEN, 0);
		
	}

	public void contextChanged(Context globalContext) {

	}

	public void destroy() {
		if (sniffer != null) {
			sniffer.quit();
		}
	}

	public Context getActivityContext() {
		return context;
	}

	public void halt() {
		// TODO Auto-generated method stub

	}

	public void resume(Context globalContext) {
		Object o = globalContext.getElement(currentKey);
		IDataPointer pointer = (IDataPointer) o;
		if (callBackActivity == ACTIVITY_DIRECTORY_CHANGE) {
			changeSniffingDirectory(pointer.getDisplayText());
		} else {
			addImageToUI(pointer);
		}
		updateView();
		callBackActivity = ACTIVITY_NONE;
	}

	
	private void updateView() {
		display.setView(DisplayViewFactory.createView(mainForm));
	}

	public void setShell(IShell shell) {
		this.shell = shell;

	}

	public void start(Context context) {
		this.context = context;

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
		if (isSniffingImages) {
			mainList = new ChoiceGroup("Available Images (searching in " + getImageSniffingPath() + ")", ChoiceGroup.MULTIPLE);
			sniffer = new ImageSniffer(getImageSniffingPath(), this);
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
	public String getImageSniffingPath() {

		if (sniffingPath == null) {
			// default
			// file system testing
			//sniffingPath = "file://localhost/root1/photos/"; 
			// phone testing
			String rootName = FileUtility.getDefaultRoot();
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
			if (sniffer != null) {
				sniffer.quit();
			}
			System.out.println("Setting directory to: " + path);
			sniffer.setSniffDirectory(sniffingPath); 
			//sniffer = new ImageSniffer(sniffingPath, this);
			//snifferThread = new Thread(sniffer);
			//snifferThread.start();
			mainList.setLabel("Available Images (searching in " + sniffingPath + ")");
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
		}
	}

	public synchronized void updateImageSniffingDisplay(String newPath) {
		mainList.setLabel("Available Images (searching in " + newPath + ")");
	}

	// utility method for memory debugging
	private void printMemories(String tag) {
		String memory = System.getProperty("com.nokia.memoryramfree");
		long jvmMemory = Runtime.getRuntime().freeMemory();
		System.out.println("Available Memory " + tag + ": " + memory);
		System.out.println("Available JVM Memory " + tag + ": " + jvmMemory);
		System.out.flush();
//		mainFormOld.append("Available Memory " + tag + ": " + memory);
//		mainFormOld.append("Available JVM Memory " + tag + ": " + jvmMemory);
	}

	
	private FileDataPointer captureNewImage() {
		// TODO: I have a feeling this is going to actually be some sort of back
		// and forth chicannery
		// with the shell
		return null;
	}

	/**
	 * Selects an image
	 * 
	 * @param image
	 */
	private void selectImage(FileDataPointer image) {
		// TODO: impl;
	}

	/**
	 * Deselects an image
	 * 
	 * @param image
	 */
	private void deselectImage(FileDataPointer image) {
		// TODO: impl;
	}

	/**
	 * Picks the selected images and returns them (and control) to the shell
	 * Other images are deleted?
	 */
	private void finish() {
		Hashtable args = null; // buildReturnArgs();
		shell.returnFromActivity(this, "Success!", args);

	}

	public void commandAction(Command command, Displayable display) {
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
		// null indicates we're done done
		returnFromActivity(null);
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
		shell.returnFromActivity(this, Constants.ACTIVITY_CANCEL, null);
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
		try {
			currentKey = FileBrowseActivity.FILE_POINTER;
			returnFromActivity(new FileBrowseActivity(shell));
		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void processChangeDirectory() {
		currentKey = FileBrowseActivity.FILE_POINTER;
		callBackActivity = ACTIVITY_DIRECTORY_CHANGE;
		FileBrowseActivity activity = new FileBrowseActivity(shell);
		activity.setMode(FileBrowseActivity.MODE_DIRECTORY);
		returnFromActivity(activity);
	}


	private void processCamera() {
		currentKey = ImageCaptureActivity.IMAGE_KEY;
		returnFromActivity(new ImageCaptureActivity(shell));
	}

	private void returnFromActivity(IActivity activity) {
		Hashtable returnArgs = buildReturnArgsFromActivity(activity);
		String returnCode =Constants.ACTIVITY_NEEDS_RESOLUTION; 
		if (activity == null) {
			returnCode = Constants.ACTIVITY_COMPLETE;
		}
		shell.returnFromActivity(this, returnCode,returnArgs);
	}

	private Hashtable buildReturnArgsFromActivity(IActivity activity) {
		Hashtable table = new Hashtable();
		if (activity == null) {
			// if we're going back to the original caller then add the images 
			IDataPointer[] imageList = getSelectedImages();
			table.put(Constants.RETURN_ARG_KEY, imageList);
			table.put(Constants.RETURN_ARG_TYPE_KEY, Constants.RETURN_ARG_TYPE_DATA_POINTER_LIST);
		} else {
			// otherwise add the callback
			table.put(ACTIVITY_KEY, activity);
		}
		return table;
	}

}
