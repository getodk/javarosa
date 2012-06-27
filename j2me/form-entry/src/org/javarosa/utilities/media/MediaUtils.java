package org.javarosa.utilities.media;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;

import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.Reference;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.services.Logger;
import org.javarosa.core.services.PropertyManager;
import org.javarosa.formmanager.properties.FormManagerProperties;
import org.javarosa.j2me.view.J2MEDisplay;

public class MediaUtils {
	
    /////////AUDIO PLAYBACK
	
	//We only really every want to play audio through one centralized player, so we'll keep a static
	//instance
	private static Player audioPlayer;
	
	public static final int AUDIO_SUCCESS = 1;
	public static final int AUDIO_NO_RESOURCE = 2;
	public static final int AUDIO_ERROR = 3;
	public static final int AUDIO_DISABLED = 4;
	public static final int AUDIO_BUSY = 5;
	public static final int AUDIO_NOT_RECOGNIZED = 6;
	

	
	private static boolean IMAGE_DEBUG_MODE = false;
	public static Image getImage(String URI){
		if(URI != null && !URI.equals("")){
			try {
				Reference ref = ReferenceManager._().DeriveReference(URI);
				InputStream is = ref.getStream();
				Image i = Image.createImage(is);
				is.close();
				return i;
			} catch (IOException e) {
				System.out.println("IOException for URI:"+URI);
				e.printStackTrace();
				if(IMAGE_DEBUG_MODE) throw new RuntimeException("ERROR! Cant find image at URI: "+URI);	
				return null;
			} catch (InvalidReferenceException ire){
				System.out.println("Invalid Reference Exception for URI:"+URI);
				ire.printStackTrace();
				if(IMAGE_DEBUG_MODE) throw new RuntimeException("Invalid Reference for image at: " +URI);
				return null;
			}
		} else{
			return null;
		}
	}
	
	
	/**
	  * This methog resizes an image by resampling its pixels
	  * @param src The image to be resized
	  * @return The resized image
	  */

	  public static Image resizeImage(Image src, int newWidth, int newHeight) {
	      int srcWidth = src.getWidth();
	      int srcHeight = src.getHeight();
	      Image tmp = Image.createImage(newWidth, srcHeight);
	      Graphics g = tmp.getGraphics();
	      int ratio = (srcWidth << 16) / newWidth;
	      int pos = ratio/2;

	      //Horizontal Resize        

	      for (int x = 0; x < newWidth; x++) {
	          g.setClip(x, 0, 1, srcHeight);
	          g.drawImage(src, x - (pos >> 16), 0, Graphics.LEFT | Graphics.TOP);
	          pos += ratio;
	      }

	      Image resizedImage = Image.createImage(newWidth, newHeight);
	      g = resizedImage.getGraphics();
	      ratio = (srcHeight << 16) / newHeight;
	      pos = ratio/2;        

	      //Vertical resize

	      for (int y = 0; y < newHeight; y++) {
	          g.setClip(0, y, newWidth, 1);
	          g.drawImage(tmp, 0, y - (pos >> 16), Graphics.LEFT | Graphics.TOP);
	          pos += ratio;
	      }
	      return resizedImage;

	  }//resize image    
	  
	  /**
	   * Used for scaling an image.  Checks to see if an image is bigger than the 
	   * provided dimensions, and provides new dimensions such that the image
	   * scales to fit within the dimensions given. If the image is smaller (in both width and height)
	   * than the given dimensions, returns the original image dimensions.
	   * @param source image
	   * @return int array [height, width]
	   */
	  public static int[] getNewDimensions(Image im, int height, int width){
		double scalef = im.getHeight()*1.0/im.getWidth();
		int w = 1;
		int h = 1;
		if(im.getHeight() > height && im.getWidth() <= width){ //height is overbounds
			h = height;
			w = (int)Math.floor(h/scalef);
		}else if (im.getHeight() <= height && im.getWidth() > width){  //width is overbouds
			w = width;
			h = (int)Math.floor(w*scalef);
		}else if (im.getHeight() > height && im.getWidth() > width){ //both are overbounds
			if(height > width){	//screen width is smaller dimension, so reduce im width and scale height				
				w = width;
				h = (int)Math.floor(w*scalef);
			}else if(height <= width){ //reduce height and scale width
				h = height;
				w = (int)Math.floor(h/scalef);
			}
		}else{
			h = im.getHeight();
			w = im.getWidth();
		}
			int[] dim = {h,w};
			return dim;
	  }
	  
	  
	  
		public static int playAudio(String jrRefURI) {
			String curAudioURI = jrRefURI;
			int retcode = AUDIO_SUCCESS;
			try {
				Reference curAudRef = ReferenceManager._().DeriveReference(curAudioURI);
				String format = getFileFormat(curAudioURI);

				if(format == null) return AUDIO_NOT_RECOGNIZED;
				if(audioPlayer != null){
					audioPlayer.deallocate();
					audioPlayer.close();
				}
				audioPlayer = MediaUtils.getPlayerLoose(curAudRef);
				audioPlayer.start();
				
			} catch (InvalidReferenceException ire) {
				retcode = AUDIO_ERROR;
				System.err.println("Invalid Reference Exception when attempting to play audio at URI:"+ curAudioURI + "Exception msg:"+ire.getMessage());
			} catch (IOException ioe) {
				retcode = AUDIO_ERROR;
				System.err.println("IO Exception (input cannot be read) when attempting to play audio stream with URI:"+ curAudioURI + "Exception msg:"+ioe.getMessage());
			} catch (MediaException e) {
				//TODO: We need to figure out how to deal with silent stuff correctly
				//Logger.log("auderme", e.getMessage());
				//J2MEDisplay.showError(null, "Phone is on silent!");

				retcode = AUDIO_ERROR;
				System.err.println("Media format not supported! Uri: "+ curAudioURI + "Exception msg:"+e.getMessage());
			} catch(SecurityException e) {
				//Logger.log("auderse", e.getMessage());
				//J2MEDisplay.showError(null, "Phone is on silent!");
			}
			return retcode;
		}
		
		private static String getFileFormat(String fpath){
//			Wave audio files: audio/x-wav
//			AU audio files: audio/basic
//			MP3 audio files: audio/mpeg
//			MIDI files: audio/midi
//			Tone sequences: audio/x-tone-seq
//			MPEG video files: video/mpeg
//			Audio 3GPP files (.3gp) audio/3gpp
//			Audio AMR files (.amr) audio/amr
//			Audio AMR (wideband) files (.awb) audio/amr-wb
//			Audio MIDI files (.mid or .midi) audio/midi
//			Audio MP3 files (.mp3) audio/mpeg
//			Audio MP4 files (.mp4) audio/mp4
//			Audio WAV files (.wav) audio/wav audio/x-wav
			
			if(fpath.indexOf(".mp3") > -1) return "audio/mp3";
			if(fpath.indexOf(".wav") > -1) return "audio/x-wav";
			if(fpath.indexOf(".amr") > -1) return "audio/amr";
			if(fpath.indexOf(".awb") > -1) return "audio/amr-wb";
			if(fpath.indexOf(".mp4") > -1) return "audio/mp4";
			if(fpath.indexOf(".aac") > -1) return "audio/aac";
			if(fpath.indexOf(".3gp") > -1) return "audio/3gpp";
			if(fpath.indexOf(".au") > -1) return "audio/basic";
			throw new RuntimeException("COULDN'T FIND FILE FORMAT");
		}

		
		public static Player getPlayerLoose(Reference reference) throws MediaException, IOException {
			Player thePlayer;
			
	        try{ 
	        	thePlayer = Manager.createPlayer(reference.getLocalURI());
		        return thePlayer;
	        } catch(MediaException e) {
	        	if(!FormManagerProperties.LOOSE_MEDIA_YES.equals(PropertyManager._().getSingularProperty(FormManagerProperties.LOOSE_MEDIA))) {
	        		throw e;
	        	}
	        	Reference[] refs = reference.probeAlternativeReferences();
	        	for(Reference ref : refs) {
	        		if(ref.doesBinaryExist()) {
	        			try{
	        				//TODO: Make sure you create a player of the right type somehow (video/audio), don't want
	        				//to accidentally send back an audio player of a video file
	        				thePlayer = Manager.createPlayer(ref.getLocalURI());
	    	    	        return thePlayer;
	        			}catch(MediaException oe) {
	        				//also bad file, keep trying
	        			} 
	        		}
	        	}
	        	throw e;
	        }
		}

}
