package org.javarosa.media.image.activity;

import java.util.Hashtable;

import javax.microedition.lcdui.Display;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IDisplay;
import org.javarosa.core.api.IShell;
import org.javarosa.media.image.model.FileDataPointer;
import org.javarosa.media.image.storage.ImageRMSUtility;

/**
 * An Activity that represents the selection of zero or more images.
 * This will launch a UI that supports displaying a list of images, marking
 * some subset of them and returning those images.  New images can also 
 * be added via an ImageCaptureActivity.
 * @author Cory Zue
 *
 */
public class ImageChooserActivity implements IActivity
{
	// this should map images (IDataPointers) to bool true/false whether selected or not
	private Hashtable allImages;
	private Context context;
	private IShell shell;
	private IDisplay display;
	private ImageRMSUtility dataModel;

	public ImageChooserActivity(IShell shell) {
		this.shell = shell;
		display = JavaRosaServiceProvider.instance().getDisplay();
		dataModel = new ImageRMSUtility("image_store");
	}

	public void contextChanged(Context globalContext) {

		
	}

	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	public Context getActivityContext() {
		// TODO Auto-generated method stub
		return null;
	}

	public void halt() {
		// TODO Auto-generated method stub
		
	}

	public void resume(Context globalContext) {
		// TODO Auto-generated method stub
		
	}

	public void setShell(IShell shell) {
		// TODO Auto-generated method stub
		
	}

	public void start(Context context) {
		// TODO Auto-generated method stub
		
	}
	
	private FileDataPointer captureNewImage() {
		// TODO: I have a feeling this is going to actually be some sort of back and forth chicannery
		// with the shell
		return null;
	}
	
	/**
	 * Selects an image 
	 * @param image
	 */
	private void selectImage(FileDataPointer image) {
		// TODO: impl;
	}
	
	/**
	 * Deselects an image 
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
		Hashtable args = null; //buildReturnArgs();
		shell.returnFromActivity(this, "Success!", args);

	}

	

}
