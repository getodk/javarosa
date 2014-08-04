/**
 * 
 */
package org.javarosa.utilities.media;

import java.io.IOException;

import javax.microedition.lcdui.Graphics;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.control.VideoControl;

import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.Reference;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.j2me.view.J2MEDisplay;

import de.enough.polish.ui.CustomItem;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.Style;

/**
 * @author ctsims
 *
 */
public class VideoItem extends CustomItem {
	
	int width, height;
	int pw, ph, cw, ch;
	int vw, vh;
	boolean started = false;
	
	Reference videoRef;
	
	VideoControl vc;

	   private Player player;
	   void defplayer() throws MediaException {
		      if (player != null) {
		         if(player.getState() == Player.STARTED) {
		            player.stop();
		         }
		         if(player.getState() == Player.PREFETCHED) {
		            player.deallocate();
		         }
		         if(player.getState() == Player.REALIZED || 
				    player.getState() == Player.UNREALIZED) {
		            player.close();
		         }
		      }
		      player = null;
		   }


	
	public VideoItem(String URI) throws MediaException, IOException, InvalidReferenceException {
		this(URI, null);
	}
	
	public VideoItem(String URI, Style style) throws MediaException, IOException, InvalidReferenceException {
		super(null, style);
		
		this.appearanceMode = Item.PLAIN;
		
        defplayer();
        // create a player instance
        
        videoRef = ReferenceManager._().DeriveReference(URI);
        
        player = MediaUtils.getPlayerLoose(videoRef);
        
        player.addPlayerListener(new PlayerListener() {

			public void playerUpdate(Player arg0, String event, Object arg2) {
			      if(event == PlayerListener.END_OF_MEDIA) {
			    	  //Anything?
			          }
			}
      	  
        });
        // realize the player
        player.realize();
        vc = (VideoControl)player.getControl("VideoControl");
        if(vc == null) {
        	//ERROR!
        	throw new RuntimeException("NO Video!");
        }
  	  	vc.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, J2MEDisplay.getDisplay());
  	  	
  	  	pw = vc.getSourceWidth();
  	  	ph = vc.getSourceHeight();
  	  	
  	  	//The formats involved in video scale somewhat oddly (3gp specifically), and don't actually give
  	  	//the right values here. We really want to scale if possible.
  	  	
  	  	int optimal = J2MEDisplay.getScreenWidth(240) * 3 / 4;
  	  	
  	  	//We'd optimally like to be around 3/4 of the screen's width, if available.

  	  	if(optimal > pw) {
  	  		//we can only scale up to 2x
  	  		double scale = Math.min(2.0, (optimal * 1.0 / pw));
  	  		
  	  		pw = (int)Math.floor(pw * scale);
  	  		ph = (int)Math.floor(ph * scale);
  	  	}
  	  	
  	  	
        player.prefetch();

        
  	  	vh = vc.getSourceHeight();
  	  	vw = vc.getSourceWidth();
  	  	  	  	
	}

	protected void paintContent(int x, int y, int leftBorder, int rightBorder, Graphics g) {
        	if(player != null) {
        		//center stuff
        		width = rightBorder - leftBorder;
        		int offsetX = (width - cw) / 2;
	        	vc.setDisplayLocation(x + offsetX, y);
        	}
	}
	
	public int getPreferredWidth() { 
		return pw;
	}
	
	public int getPreferredHeight() {
		return ph;
	}


	protected int getMinContentWidth() {
		return 0;
	}

	protected void start() {
		try {
    		player.start();
		} catch (MediaException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		}
	}


	protected int getMinContentHeight() {
		return 0;
	}



	private int availHeight;
	protected int getPrefContentWidth(int height) {
		availHeight = ph;
		return pw;
	}
	
	int aw;
	int ah;

	protected int getPrefContentHeight(int availWidth) {
		int h = ph;
		int w = pw;
		
		aw = availWidth;
		ah = availHeight;
		
  	  	if(h > availHeight) {
  	  		double ratio = availHeight  / (h * 1.0);
  	  		h = availHeight;
  	  		w = (int)Math.floor(w * ratio);
  	  	}
  	  	
  	  	if(w > availWidth) {
  	  		double ratio = availWidth  / (w * 1.0);
	  		w = availWidth;
	  		h = (int)Math.floor(h * ratio);
  	  	}

  	  	cw = w;
  	  	ch = h;

  	  	try {
			vc.setDisplaySize(cw, ch);
		} catch (MediaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  	  	
  	  	
  	  	vc.setVisible(true);
		
		return ph;
	}



	protected void paint(Graphics g, int w, int h) {
		width = w;
		height = h;
	}
	
	public Player getPlayer() {
		return player;
	}

	public void releaseResources() {
		super.releaseResources();
		try {
			defplayer();
		} catch (MediaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
