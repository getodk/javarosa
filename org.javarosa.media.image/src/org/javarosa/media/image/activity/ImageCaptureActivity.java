package org.javarosa.media.image.activity;

import java.util.Hashtable;

import org.javarosa.core.Context;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IShell;
import org.javarosa.media.image.model.FileDataPointer;

/**
 * An Activity that represents the capture of a single Image.  This will talk to the
 * native device camera and return the selected image.
 * 
 * @author Cory Zue
 *
 */
public class ImageCaptureActivity implements IActivity
{

	private Context context;
	private IShell shell;

	public void contextChanged(Context globalContext) {
		// TODO Auto-generated method stub
		
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
		this.shell = shell;
	}

	public void start(Context context) {
		// initialize GUI
		// take a pointer to the context and shell
		this.context = context;
		
//		The start method should complete all initialization that was not performed in the 
//		constructor, including all GUI initialization. The context object parameter is 
//		likely a subclass of the global context object. The context should contain required 
//		and optional parameters for running the Activity. It is preferred to pass all this 
//		information through the context rather than call separate initialize(...) methods, 
//		because this way the activity's current 'configuration' can be accessed and passed 
//		around generically.

//		When start() is called, the Activity should initialize itself and then take control 
//		of the application, for instance by setting the Display. Activities shouldn't touch 
//		the device's Display object directly, but rather call the shell's setDisplay() method. 
//		This allows the Shell to mediate requests for the Display, thus preventing confused 
//		Activities from breaking the Application's workflow.

//		In this method you should generally save off a reference to the activity's parent Shell. 
	}
	
	/**
	 * Actually capture an image
	 * 
	 */
	private FileDataPointer captureImage() {
		return null;
	}

	
	/**
	 * takes the selected image return it (and control) to the shell
	 * Other images are deleted?
	 */
	private void finish() {
		Hashtable args = buildReturnArgs();
		shell.returnFromActivity(this, "Success!", args);

	}

	private Hashtable buildReturnArgs() {
		// stick the picture in here. 
		return null;
	}

}
