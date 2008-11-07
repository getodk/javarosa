/* License
 * 
 * Modifications by Cory Zue, Oct-Nov 2008
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

package org.javarosa.media.image.view;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.media.MediaException;
import javax.microedition.media.control.VideoControl;
import javax.microedition.midlet.MIDlet;

public class CameraCanvas extends Canvas {
	private MIDlet mMIDlet;
	  
	  public CameraCanvas(MIDlet midlet, VideoControl videoControl) {
	    int width = getWidth();
	    int height = getHeight();
	    
	    mMIDlet = midlet;
	    
	    videoControl.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, this);
	    try {
	      videoControl.setDisplayLocation(2, 2);
	      videoControl.setDisplaySize(width - 4, height - 4);
	    }
	    catch (MediaException me) {
	      try { videoControl.setDisplayFullScreen(true); }
	      catch (MediaException me2) {}
	    }
	    videoControl.setVisible(true);
	  }
	  
	  public void paint(Graphics g) {
	    int width = getWidth();
	    int height = getHeight();

	    // Draw a green border around the VideoControl.
	    g.setColor(0x00ff00);
	    g.drawRect(0, 0, width - 1, height - 1);
	    g.drawRect(1, 1, width - 3, height - 3);
	  }
	  
	  public void keyPressed(int keyCode) {
	    int action = getGameAction(keyCode);
	    //if (action == FIRE) 
		//mSnapperMIDlet.capture();
	  }
	}
 