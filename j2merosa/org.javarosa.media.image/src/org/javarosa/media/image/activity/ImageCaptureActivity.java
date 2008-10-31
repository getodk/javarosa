package org.javarosa.media.image.activity;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IShell;
import org.javarosa.media.image.model.FileDataPointer;
import org.javarosa.media.image.storage.ImageRMSUtility;
import org.javarosa.media.image.view.CameraCanvas;

/**
 * An Activity that represents the capture of a single Image.  This will talk to the
 * native device camera and return the selected image.
 * 
 * @author Cory Zue
 *
 */
public class ImageCaptureActivity implements IActivity, CommandListener
{

	private Context context;
	private IShell shell;
	

	// camera needed variables
	
	private Player mPlayer;
	private VideoControl mVideoControl;
	private Command mBackCommand;
	private Command mCaptureCommand;
	private Display display;
	private ImageRMSUtility dataModel;

	public ImageCaptureActivity(IShell shell) {
		this.shell = shell;
		display = JavaRosaServiceProvider.instance().getDisplay();
		dataModel = new ImageRMSUtility("image_store");
	}

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
		showCamera();
		
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
	private void showCamera() {
		try {
			mPlayer = Manager.createPlayer("capture://video");
			mPlayer.realize();

			mVideoControl = (VideoControl) mPlayer.getControl("VideoControl");

//			Command mExitCommand = new Command("Exit", Command.EXIT, 0);
//			Command mCameraCommand = new Command("Camera", Command.SCREEN, 0);
			mBackCommand = new Command("Back", Command.BACK, 0);
			mCaptureCommand = new Command("Capture", Command.SCREEN, 0);

			Canvas canvas = new CameraCanvas(null, mVideoControl);
			canvas.addCommand(mBackCommand);
			canvas.addCommand(mCaptureCommand);
			canvas.setCommandListener(this);
			
			display.setCurrent(canvas);
			

			/*
			 Form form = new Form("Camera form");
			 Item item = (Item)mVideoControl.initDisplayMode(
			 GUIControl.USE_GUI_PRIMITIVE, null);
			 form.append(item);
			 form.addCommand(mBackCommand);
			 form.addCommand(mCaptureCommand);
			 form.setCommandListener(this);
			 mDisplay.setCurrent(form);
			 */

			mPlayer.start();
		} catch (IOException ioe) {
			handleException(ioe);
		} catch (MediaException me) {
			handleException(me);
		}
	}

	private void handleException(Exception e) {
//		Alert a = new Alert(e.toString(), e.toString(), null, null);
//		a.setTimeout(Alert.FOREVER);
//		JavaRosaServiceProvider.instance().getDisplay().setCurrent(a);
//		throw new RuntimeException(e.getMessage());
		String toLog = e.getMessage();
		toLog += e.toString();
		saveFile("log" + System.currentTimeMillis() + ".txt", toLog.getBytes());
		
	}

	public void commandAction(Command cmd, Displayable display) {
		// TODO Auto-generated method stub
		if (cmd.equals(this.mBackCommand)) {
			goBack();
		}
		else if (cmd.equals(this.mCaptureCommand)) {
			doCapture();
			System.out.println("Click!");
		}
	}

	private void goBack() {
		// TODO
		System.out.println("Back Again!");
		this.shell.returnFromActivity(this, Constants.ACTIVITY_CANCEL, null);
		
	}

	private void doCapture() {
		byte[] jpg;
		// add a loop to do this a lot and write them to individual files so we know when we fail
		int width = 640;
		int height = 480;
		int failures = 0;
		String text = "";
		while (failures < 3 && width < 3000) {
		try {
			text += width + "x" + height + ": ";
			// Get the image.
			//jpg = mVideoControl.getSnapshot("encoding=jpeg&quality="+ quality);
			jpg = mVideoControl.getSnapshot("encoding=jpeg&quality=100&width=" + width + "&height=" + height);
			String fileName = "test" + System.currentTimeMillis();
			boolean saved = saveFile(fileName + ".jpg", jpg);
			if (saved) {
				text += "Success!";
				
			}
			//jpg = mVideoControl.getSnapshot("encoding=jpeg&quality=100&width=2048&height=1536");
			//jpg = mVideoControl.getSnapshot("encoding=jpeg&quality=100&width=1280&height=960");
		} catch (MediaException me) {
			handleException(me);
			failures++;
			jpg = null;
			text += "Fail!";
		}
		text += "\n";
			width += 80;
			height += 60;
		}
		saveFile("photo_log" + System.currentTimeMillis() + ".txt", text.getBytes());
//		if (null != jpg) {
//			String fileName = "test" + System.currentTimeMillis();
//			boolean saved = saveFile(fileName + ".jpg", jpg);
//			//boolean saved = saveImageToRMS(fileName + ".jpg", jpg);
//		}
		
	}
	private boolean saveImageToRMS(String filename, byte[] image) {
		Image imageObj = Image.createImage(image, 0, image.length);
		dataModel.saveImage(filename, image);
		return true;
	}
	
	private boolean saveFile(String filename, byte[] image) {
		// TODO 
		String rootName = getRootName();
		String restorepath = "file:///" + rootName + "JRImages";				
		createDirectory(restorepath);
		String fullName = restorepath + "/" + filename;
		System.out.println("Image saved.");
		return createFile(fullName, image);
		// not sure why this was being done twice
	}

	private boolean createFile(String fullName, byte[] image) {
		OutputStream fos = null;
		FileConnection file = null;
		boolean isSaved = false;
		try {
			file = (FileConnection) Connector.open(fullName);
			if (!file.exists()) {
				file.create();
			}				
			fos = file.openOutputStream();
			fos.write(image);
			isSaved = true;
		} catch (Exception ex) {				
			showAlert("Error - File is not writable - 1 " + ex.getMessage());
			ex.printStackTrace();
		} 
		finally {
			try {					
				if (fos != null) {
					fos.flush();
					fos.close();
				}
				if (file != null)
					file.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return isSaved;
		
	}
	private void createDirectory(String restorepath) {
		
		FileConnection directory = null;
		try {
			directory = (FileConnection) Connector.open(restorepath);
			if (!directory.exists())
				directory.mkdir();
			directory.close();
		}
		catch (Exception ex) {
			showAlert("Error - Folder not created - 1 : " + ex.getMessage());
		}
		finally {
			try {
				if (directory != null)
					directory.close();
			}
			catch(Exception e) {}
		}
	}

	private String getRootName() {
		Enumeration root = FileSystemRegistry.listRoots();
		String rootName = null;
		while (root.hasMoreElements()) {
			rootName = (String) root.nextElement();
		}
		return rootName;
	}

	public void showAlert(String error) {
		// TODO: should these be polished?
		Alert alert = new Alert(error);
		alert.setTimeout(Alert.FOREVER);
		alert.setType(AlertType.ERROR);
		display.setCurrent(alert);
	}
	
}
