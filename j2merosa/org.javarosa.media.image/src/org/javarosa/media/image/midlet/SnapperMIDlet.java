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

/* License
 * 
 * Copyright 1994-2004 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *  
 *  * Redistribution of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * 
 *  * Redistribution in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *  
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *  
 * You acknowledge that this software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility. 
 */

package org.javarosa.media.image.midlet;


import java.io.IOException;

import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.media.control.*;
import javax.microedition.midlet.MIDlet;

import org.javarosa.media.image.utilities.ImageUtility;
import org.javarosa.media.image.view.CameraCanvas;

public class SnapperMIDlet
    extends MIDlet
    implements CommandListener {
  private Display mDisplay;
  
  private Form mMainForm;
  
  private Command mExitCommand, mCameraCommand;
  private Command mBackCommand, mCaptureCommand;
  
  private Player mPlayer;
  private VideoControl mVideoControl;
  
  public SnapperMIDlet() {
    mExitCommand = new Command("Exit", Command.EXIT, 0);
    mCameraCommand = new Command("Camera", Command.SCREEN, 0);
    mBackCommand = new Command("Back", Command.BACK, 0);
    mCaptureCommand = new Command("Capture", Command.SCREEN, 0);
    
    mMainForm = new Form("Snapper");
    mMainForm.addCommand(mExitCommand);
    String supports = System.getProperty("video.snapshot.encodings");
    if (supports != null && supports.length() > 0) {
      mMainForm.append("Ready to take pictures.");
      mMainForm.addCommand(mCameraCommand);
    }
    else
      mMainForm.append("Snapper cannot use this " +
          "device to take pictures.");
    mMainForm.setCommandListener(this);
  }
  
  public void startApp() {
    mDisplay = Display.getDisplay(this);
    
    mDisplay.setCurrent(mMainForm);
  }
    
  public void pauseApp() {}
  
  public void destroyApp(boolean unconditional) {
  }
  
  public void commandAction(Command c, Displayable s) {
    if (c.getCommandType() == Command.EXIT) {
      destroyApp(true);
      notifyDestroyed();
    }
    else if (c == mCameraCommand)
      showCamera();
    else if (c == mBackCommand)
      mDisplay.setCurrent(mMainForm);
    else if (c == mCaptureCommand) {
      capture();
    }
  }

  private void showCamera() {
    try {
      mPlayer = Manager.createPlayer("capture://video");
      mPlayer.realize();
      
      mVideoControl = (VideoControl)mPlayer.getControl("VideoControl");
      
      Canvas canvas = new CameraCanvas(this, mVideoControl);
      canvas.addCommand(mBackCommand);
      canvas.addCommand(mCaptureCommand);
      canvas.setCommandListener(this);
      mDisplay.setCurrent(canvas);

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
    }
    catch (IOException ioe) { handleException(ioe); }
    catch (MediaException me) { handleException(me); }
  }
  
  public void capture() {
    try {
      // Get the image.
      byte[] raw = mVideoControl.getSnapshot(null);
      Image image = Image.createImage(raw, 0, raw.length);
      
      Image thumb = ImageUtility.createThumbnail(image);
      
      // Place it in the main form.
      if (mMainForm.size() > 0 && mMainForm.get(0) instanceof StringItem)
        mMainForm.delete(0);
      mMainForm.append(thumb);
      
      // Flip back to the main form.
      mDisplay.setCurrent(mMainForm);
      
      // Shut down the player.
      mPlayer.close();
      mPlayer = null;
      mVideoControl = null;
    }
    catch (MediaException me) { handleException(me); }
  }
  
  private void handleException(Exception e) {
    Alert a = new Alert("Exception", e.toString(), null, null);
    a.setTimeout(Alert.FOREVER);
    mDisplay.setCurrent(a, mMainForm);
  }
  
  
}
