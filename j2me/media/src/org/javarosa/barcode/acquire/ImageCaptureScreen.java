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

package org.javarosa.barcode.acquire;

import java.io.IOException;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.GUIControl;
import javax.microedition.media.control.VideoControl;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.formmanager.view.singlequestionscreen.acquire.AcquireScreen;
import org.javarosa.formmanager.view.singlequestionscreen.acquire.AcquiringQuestionScreen;

public abstract class ImageCaptureScreen extends AcquireScreen implements CommandListener {

	protected Player player;
	protected VideoControl videoControl;
	protected javax.microedition.lcdui.Item videoItem;

	public static Command cancelCommand;
	public static Command takePictureCommand;
	public static Command dismissMessageCommand;

	/**
	 * @param title
	 *            The screen title to use
	 * @param questionScreen
	 *            The calling question screen (to which the acquired dat will be
	 *            returned)
	 * @param callingCommandListener
	 *            The calling command listener, to which control will be
	 *            returned when the acquiring process is terminated. This can
	 *            happen when acquiring is cancelled or when data is
	 *            successfully acquired and passed back to the question screen
	 */
	public ImageCaptureScreen(String title,
			AcquiringQuestionScreen questionScreen,
			CommandListener callingCommandListener) {
		super(title, questionScreen, callingCommandListener);

	}

	protected void createView() {
		try {
			addCameraViewer();
		}

		catch (MediaException me) {
			System.out.println("MediaException! " + me.getMessage());

		} catch (IOException ioe) {
			System.out.println("IOException! " + ioe.getMessage());

		}
	}

	private void addCameraViewer() throws MediaException, IOException {
		String refForCamera = "";
		//#if polish.identifier.motorola/v3xx
		refForCamera = "capture://camera";
		//#elif polish.group.series60e3
		refForCamera = "capture://devcam0";
		//#else
		refForCamera = "capture://video";
		//#endif

		String[] contentTypes = Manager.getSupportedContentTypes("capture");
		if (contentTypes == null || contentTypes.length == 0) {
			throw new MediaException("capture not supported");
		}

		for (int i = 0; i < contentTypes.length; i++) {
			String contentType = contentTypes[i];

			if ("image".equals(contentType)) { // this is the case on Series 40,
				// for example
				refForCamera = "capture://image";
			}
		}

		System.out.println("Starting player");
		player = Manager.createPlayer(refForCamera);
		player.realize();
		videoControl = (VideoControl) player.getControl("VideoControl");

		videoItem = (javax.microedition.lcdui.Item) videoControl
				.initDisplayMode(GUIControl.USE_GUI_PRIMITIVE, null);
		showVideoScreen();

	}

	/* (non-Javadoc)
	 * @see org.javarosa.formmanager.view.singlequestionscreen.acquire.AcquireScreen#getSetCallingScreenDataCommand()
	 */
	protected Command getSetCallingScreenDataCommand() {
		if (takePictureCommand == null)
			takePictureCommand = new Command("Take Picture", Command.OK, 3);
		return takePictureCommand;
	}

	public Image getSnapshotImage() throws MediaException {
		return getSnapshotImage(null);
	}

	public Image getSnapshotImage(String encoding) throws MediaException {
		byte[] raw = videoControl.getSnapshot(encoding);
		return Image.createImage(raw, 0, raw.length);
	}

	protected abstract IAnswerData getAcquiredData();

	protected void handleCustomCommand(Command c, Displayable d) {
		if (c == dismissMessageCommand) {
			showVideoScreen();
			this.addCommand(getSetCallingScreenDataCommand());

		}
	}

	protected void cleanUp() {
		this.deleteAll();
		if (this.player != null) {
			this.player.close();

			this.player = null;
		}
	}

	protected void showMessage(String message, boolean recoverable) {
		removeVideoScreen();
		String theMessage = "";
		if (message != null)
			theMessage = message;
		try {
			if (player != null)
				player.stop();

		} catch (MediaException me) {
			theMessage += "; also there was a player error.";
			recoverable = false;
		}
		this.append(theMessage);

		if (recoverable) {
			if (dismissMessageCommand == null)
				dismissMessageCommand = new Command("OK", Command.OK, 3);
			this.addCommand(dismissMessageCommand);

		}

	}

	protected void removeMessage() {
		this.deleteAll();
		this.removeCommand(dismissMessageCommand);
	}

	protected void showVideoScreen() {
		removeMessage();

		try {
			videoControl.setDisplayLocation(2, 2);
			videoControl.setDisplaySize(this.getWidth() - 4,
					this.getHeight() - 4);
		} catch (MediaException me) {
			try {
				videoControl.setDisplayFullScreen(true);
			} catch (MediaException me2) {
			}
		}
		
		//Disable this unless we want to use the barcode stuff. This breaks for some reason
		//and someone who knows about the barcode system (and how it works with polish) needs
		//to come fix it
		//#if javarosa.barcode
		this.append(videoItem);
		//#endif

		try {
			if (player != null)
				player.start();
			else
				showMessage("Player error", false);
		} catch (MediaException me) {
			showMessage("Player error", false);
		}

	}

	protected void removeVideoScreen() {
		try {
			if (player != null)
				player.stop();

		} catch (MediaException me) {
		}
		this.deleteAll();
		this.removeCommand(takePictureCommand);

	}

}
