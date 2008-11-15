package org.javarosa.media.image.activity;

import java.util.Hashtable;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IDisplay;
import org.javarosa.core.api.IShell;
import org.javarosa.core.model.data.IDataPointer;
import org.javarosa.j2me.view.DisplayViewFactory;
import org.javarosa.media.image.model.FileDataPointer;
import org.javarosa.media.image.storage.FileRMSUtility;
import org.javarosa.media.image.utilities.ImageSniffer;
import org.javarosa.media.image.utilities.ImageUtility;

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
	private FileRMSUtility dataModel;
	private Form mainForm;
	private Command cancelCommand;
	private Command cameraCommand;
	private Command browseCommand;
	private Command returnCommand;
	private Command viewCommand;
	private Command markCommand;
	private String currentKey;
	private Thread snifferThread;
	private ImageSniffer sniffer;

	public ImageChooserActivity(IShell shell) {
		this.shell = shell;
		display = JavaRosaServiceProvider.instance().getDisplay();
		dataModel = new FileRMSUtility("image_store");

		cancelCommand = new Command("Cancel", Command.CANCEL, 0);
		returnCommand = new Command("Return", Command.OK, 0);
		cameraCommand = new Command("Camera", Command.SCREEN, 0);
		browseCommand = new Command("Browse", Command.SCREEN, 0);
		viewCommand = new Command("View", Command.SCREEN, 0);
		markCommand = new Command("Mark/Unmark", Command.SCREEN, 0);
	}

	public void contextChanged(Context globalContext) {

	}

	public void destroy() {
		sniffer.quit();
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
		addImageToUI(pointer);
		updateView();
	}

	
	private void updateView() {
		display.setView(DisplayViewFactory.createView(mainForm));
	}

	public void setShell(IShell shell) {
		this.shell = shell;

	}

	public void start(Context context) {
		this.context = context;
		mainForm = new Form("Image Chooser");
		mainForm.addCommand(cancelCommand);
		mainForm.addCommand(cameraCommand);
		mainForm.addCommand(browseCommand);
		mainForm.addCommand(returnCommand);
		mainForm.addCommand(viewCommand);
		mainForm.addCommand(markCommand);
		mainForm.setCommandListener(this);

		mainForm.append("Use the menu options to take pictures or browse.");
	      
		// also create the sniffer
		sniffer = new ImageSniffer("file://localhost/root1/photos/", this);
		snifferThread = new Thread(sniffer);
		snifferThread.start();
		updateView();
	}
	
	public synchronized void addImageToUI(IDataPointer pointer) {
		try {
			if (pointer != null) { 
				byte[] data = pointer.getData();
				Image img = Image.createImage(data, 0, data.length);
				Image thumbNail = ImageUtility.createThumbnail(img);
				mainForm.append(thumbNail);
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			System.out.println(ex);
			
		}
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
		} else if (command.equals(viewCommand)) {
			processView();
		} else if (command.equals(cancelCommand)) {
			processCancel();
		} else if (command.equals(returnCommand)) {
			processReturn();
		} else if (command.equals(markCommand)) {
			processMark();
		}
	}

	private void processReturn() {
		shell.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, null);
	}

	private void processMark() {
		// TODO Auto-generated method stub

	}

	private void processCancel() {
		shell.returnFromActivity(this, Constants.ACTIVITY_CANCEL, null);
	}

	private void processView() {
		// TODO Auto-generated method stub

	}

	private void processBrowser() {
		currentKey = FileBrowseActivity.FILE_POINTER;
		returnFromActivity(new FileBrowseActivity(shell));
	}

	private void processCamera() {
		currentKey = ImageCaptureActivity.IMAGE_KEY;
		returnFromActivity(new ImageCaptureActivity(shell));
	}

	private void returnFromActivity(IActivity activity) {
		Hashtable returnArgs = buildReturnArgsFromActivity(activity);
		shell.returnFromActivity(this, Constants.ACTIVITY_NEEDS_RESOLUTION,
				returnArgs);
	}

	private Hashtable buildReturnArgsFromActivity(IActivity activity) {
		Hashtable table = new Hashtable();
		table.put(ACTIVITY_KEY, activity);
		return table;
	}

}
