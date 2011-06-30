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

import java.io.IOException;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;

import org.javarosa.core.api.State;
import org.javarosa.core.services.UnavailableServiceException;
import org.javarosa.j2me.log.CrashHandler;
import org.javarosa.j2me.log.HandledCommandListener;
import org.javarosa.j2me.services.FileService;
import org.javarosa.j2me.services.exception.FileException;
import org.javarosa.j2me.view.J2MEDisplay;
import org.javarosa.media.image.model.FileDataPointer;
import org.javarosa.media.image.view.CameraCanvas;
import org.javarosa.utilities.file.J2MEFileService;

//TODO: image capture should be factored out into a service

/**
 * An Activity that represents the capture of a single Image.  This will talk to the
 * native device camera and return the selected image.
 * 
 * @author Cory Zue
 *
 */
public abstract class ImageCaptureState implements DataCaptureTransitions, State, HandledCommandListener
{
	// camera needed variables
	
	private Player mPlayer;
	private VideoControl mVideoControl;
	private Command mBackCommand;
	private Command mCaptureCommand;
	private byte[] imageData;
	private int width;
	private int height;
	private String fullName;
	
	private FileService fileService;
	
	DataCaptureTransitions transitions;
	
	public ImageCaptureState()
	{
		transitions = this;
		setResolution(width, height);
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
		mPlayer.close();
		mPlayer = null;
		mVideoControl = null;
	}

	public void start() {
		// initialize GUI
		// take a pointer to the context and shell
		showCamera();
	}
	
	public void setResolution(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	
	/**
	 * takes the selected image return it (and control) to the shell
	 * Other images are deleted?
	 */
	private void doFinish() {
		destroy();
		transitions.captured(new FileDataPointer(fullName));
	}

	private void doError() {
		destroy();
		transitions.cancel();
	}
	
	private void showCamera() {
		try {
			mPlayer = Manager.createPlayer("capture://video");
			mPlayer.realize();

			mVideoControl = (VideoControl) mPlayer.getControl("VideoControl");

			//	Command mExitCommand = new Command("Exit", Command.EXIT, 0);
			//	Command mCameraCommand = new Command("Camera", Command.SCREEN, 0);
			mBackCommand = new Command("Back", Command.BACK, 0);
			mCaptureCommand = new Command("Capture", Command.SCREEN, 0);

			Canvas canvas = new CameraCanvas(null, mVideoControl);
			canvas.addCommand(mBackCommand);
			canvas.addCommand(mCaptureCommand);
			canvas.setCommandListener(this);
			
			J2MEDisplay.setView(canvas);
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
		System.out.println(e.getMessage());
		e.printStackTrace();
		String toLog = e.getMessage();
		toLog += e.toString();
		try
		{
			saveFile("log" + System.currentTimeMillis() + ".txt", toLog.getBytes());
		}
		catch(FileException fe)
		{
			System.err.println("The was an error saving the file.");
			fe.printStackTrace();
		}
		
		doError();
	}

	public void commandAction(Command c, Displayable d) {
		CrashHandler.commandAction(this, c, d);
	}  

	public void _commandAction(Command cmd, Displayable display) {
		if (cmd.equals(this.mBackCommand)) {
			goBack();
		}
		else if (cmd.equals(this.mCaptureCommand)) {
			doCapture();
			//doCaptureLoop();
		}
	}

	private void goBack() {
		destroy();
		transitions.cancel();
	}

	private void doCapture() {
		try {
			// Get the image.
			imageData = mVideoControl.getSnapshot("encoding=jpeg&quality=100&width=" + width + "&height=" + height);
			//image = Image.createImage(jpg, 0, jpg.length);
			// Save to file no longer
			String fileName = "test" + System.currentTimeMillis();
			fullName = saveFile(fileName + ".jpg", imageData);
			doFinish();
			
		}
		catch(FileException fe)
		{
			System.err.println("The was an error saving the file.");
			fe.printStackTrace();
		}
		catch(Exception me) 
		{
			handleException(me);
		}
	}
	
	
	/**
	 * This method was used in memory profiling to loop take images at different resolutions until it fails
	
	private void doCaptureLoop() {
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
			boolean saved = saveFile(fileName + ".jpg", jpg) == "";
			if (saved) {
				text += "Success!";
				
			}
			//jpg = mVideoControl.getSnapshot("encoding=jpeg&quality=100&width=2048&height=1536");
			//jpg = mVideoControl.getSnapshot("encoding=jpeg&quality=100&width=1280&height=960");
		} 
		catch(FileException fe)
		{
			System.err.println("The was an error saving the file.");
			fe.printStackTrace();
		}
		catch (MediaException me) 
		{
			handleException(me);
			failures++;
			jpg = null;
			text += "Fail!";
		}
		text += "\n";
			width += 80;
			height += 60;
		}
		try
		{
			saveFile("photo_log" + System.currentTimeMillis() + ".txt", text.getBytes());
		}
		catch(FileException fe)
		{
			System.err.println("The was an error saving the file.");
			fe.printStackTrace();
		}
	}
	
	 */
	private String saveFile(String filename, byte[] image) throws FileException 
	{
		String rootName = fileService.getDefaultRoot();
		String restorepath = "file:///" + rootName + "JRImages";				
		fileService.createDirectory(restorepath);
		String fullName = restorepath + "/" + filename;
		if (fileService.createFile(fullName, image)) {
			System.out.println("Image saved.");	
			return fullName;	
		} else {
			return "";
		}
		
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
}
