/**
 * An Activity that represents the capture of audio. 
 * 
 * @author Ndubisi Onuora
 *
 */

package org.javarosa.media.audio.activity;

import java.io.IOException;
import java.util.Hashtable;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.RecordControl;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IDisplay;
import org.javarosa.core.api.IShell;
import org.javarosa.j2me.view.DisplayViewFactory;
//import org.javarosa.media.image.activity.String;
import org.javarosa.media.image.utilities.FileUtility;
//import org.javarosa.media.audio.midlet.RecordForm;
//import org.javarosa.j2me.view.DisplayViewFactory;

//import org.javarosa.media.audio.model.FileDataPointer;
//import org.javarosa.media.audio.utilities.FileUtility;
//import org.javarosa.media.image.view.CameraCanvas;


/*Specify imports later on*/
import java.io.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.media.control.*;

public class AudioCaptureActivity implements IActivity, CommandListener, Runnable 
{
	private final long FOREVER = 1000000;
	private IShell parentShell;
	private Context currentContext;
	
	private Player recordP;
    private Player playP;
	private RecordControl recordControl;
	//Obviously, we also want to implement a Stop command
	private Command recordCommand, playCommand, stopCommand, backCommand;
	private IDisplay display;
	private ByteArrayOutputStream audioDataStream;
	private String fullName;
	private RecorderForm form;
	
	private StringItem messageItem;
    private StringItem errorItem;
    
    Thread captureThread;    
    boolean captureThreadStarted = false;
    	
	public AudioCaptureActivity(IShell shell)
	{
		parentShell = shell;
		display = JavaRosaServiceProvider.instance().getDisplay();
	}
	
	//Finish off construction of Activity
	public void start(Context context)
	{		
		currentContext = context;
		form = new RecorderForm();
		
		messageItem = form.getMessageItem();
		errorItem = form.getErrorItem();
		
		recordCommand = new Command("Record", Command.SCREEN, 0);
    	form.addCommand(recordCommand);
    	playCommand = new Command("Play", Command.SCREEN, 1);
    	form.addCommand(playCommand);
    	stopCommand = new Command("Stop", Command.SCREEN, 0); //Do not add immediately    	
    	backCommand = new Command("Back", Command.BACK, 0);
    	form.addCommand(backCommand);
		
    	captureThread = new Thread(this, "CaptureThread");
    	    	
		//Display.getDisplay(this).setCurrent(new RecordForm());
		display.setView(DisplayViewFactory.createView(form));
		form.setCommandListener(this);
	}

	//@Override
	public void contextChanged(Context globalContext) 
	{
		// TODO Auto-generated method stub
		
	}

	//@Override
	public void destroy() 
	{
		// TODO Auto-generated method stub
		recordP.close();
		recordP = null;
		playP.close();
		playP = null;
		
		
	}

	//@Override
	public Context getActivityContext() 
	{
		// TODO Auto-generated method stub
		return currentContext;
	}

	//@Override
	/*
	 * Pauses the recorder 
	 * Should have a placeholder to keep track of position of recorder
	 */
	public void halt() 
	{
		// TODO Auto-generated method stub		
	}

	//@Override
	public void resume(Context globalContext) 
	{
		// TODO Auto-generated method stub		
	}

	//@Override
	public void setShell(IShell shell) 
	{
		parentShell = shell;		
	}
	
	public void commandAction(Command comm, Displayable disp)
	{
		//Record to file
		if(comm==recordCommand)
		{	           
	        captureThread.setPriority(Thread.currentThread().getPriority() +1 );
	        if(!captureThreadStarted)
	        {
	        	captureThread.start();
	        	captureThreadStarted = true;	        	
	        }
	        else
	        	captureThread.run();
	        //recordAudio();	        
	    } 
	    //User should be able to replay recording
	    else if(comm == playCommand)
	    {
	        try 
	        {
	        	playAudio();
	        }  catch (IOException ioe) {
	            errorItem.setLabel("Error");
	            errorItem.setText(ioe.toString());
	        } catch (MediaException me) {
	            errorItem.setLabel("Error");
	            errorItem.setText(me.toString());
	        }
	        /*
	        catch(InterruptedException ie)
	        {
	        	errorItem.setLabel("Error");
	            errorItem.setText(ie.toString());
	        }
	        */
	    }
	    //Stop recording audio
	    else if(comm == stopCommand)
	    {
	    	try
	    	{
	    		stop();
	    	}
	    	/*
	    	catch(InterruptedException ie)
	    	{
	    		errorItem.setLabel("Error");
	            errorItem.setText(ie.toString());
	    	}
	    	*/	    	
	    	catch(IOException ioe) 
	    	{
	            errorItem.setLabel("Error");
	            errorItem.setText(ioe.toString());
	        } 
	    	catch(MediaException me) 
	    	{
	            errorItem.setLabel("Error");
	            errorItem.setText(me.toString());
	        }
	    }		
	    else if(comm == backCommand)
	    {
	    	moveBack();
	    }	    
	}
	
	public void recordAudio() throws MediaException, IOException, InterruptedException
	{
		  //if(recordP == null)
			  recordP = Manager.createPlayer("capture://audio");
		  recordP.realize();                
		  recordControl = (RecordControl)recordP.getControl("RecordControl");                
	      audioDataStream = new ByteArrayOutputStream();
	      recordControl.setRecordStream(audioDataStream);                
	      recordControl.startRecord();
	      //errorItem.setText("Started recording");
	      recordP.start();
	      messageItem.setText("recording...");
	      
	      form.removeCommand(recordCommand); //"Hide" recordCommand when recording has stopped
		  form.addCommand(stopCommand);		  
		  
	      //Thread.currentThread().sleep(FOREVER);
		  /*
		  captureThread.start();
		  Thread.yield();
		  captureThread.join();
		  */
		  
		  //Thread.sleep(FOREVER);		  
		  
		  /*
	      messageItem.setText("done!");
	      recordControl.commit();               
	      //recordedSoundArray = audioDataStream.toByteArray();
	      errorItem.setText("Sound size=" + audioDataStream.toByteArray().length);
	      saveFile("Test_Rec.wav", audioDataStream.toByteArray());
	      */	      
	  }
	  
	  public void playAudio() throws MediaException, IOException
	  {
		  System.err.println("Attempting to play audio...");
		  ByteArrayInputStream recordedInputStream = new ByteArrayInputStream(audioDataStream.toByteArray()/*recordedSoundArray*/);
	      playP = Manager.createPlayer(recordedInputStream,"audio/x-wav");
          playP.prefetch();
	      //playP.realize();
	      playP.start();
	      System.err.println("Player has started.");
	      
	      form.removeCommand(playCommand); //"Hide" playCommand when playing has started
	      form.removeCommand(recordCommand); //"Hide" recordCommand when playing has started
	      form.addCommand(stopCommand); //Show recordCommand when playing has started
	  }  
	  
	  //General method to stop recording or playback
	  public void stop() throws MediaException, IOException
	  {
		  //If currently recording
		  if(recordP.getState() == Player.STARTED)
		  {
			  stopCapturing();
		  }
		  else if(playP.getState() == Player.STARTED)
		  {
			  stopPlaying();
		  }
	  }
	  
	  public void stopCapturing() throws MediaException, IOException
	  {
		  System.err.println("Attempting to stop recording");
		  
		  //Thread.currentThread().interrupt();
		  /*
		  captureThread.interrupt();		  
		  captureThread.setPriority(Thread.currentThread().getPriority() -1 );
		  */		  
		  
		  recordControl.commit();
		  
		  form.removeCommand(stopCommand); //"Hide" stopCommand when recording desires to resume
		  form.addCommand(recordCommand);
		  
		  messageItem.setText("done!");	                     
	      //recordedSoundArray = audioDataStream.toByteArray();
	      errorItem.setText("Sound size=" + audioDataStream.toByteArray().length);
	      //saveFile("Test_Rec.wav", audioDataStream.toByteArray());
	      
	      //recordP.deallocate();
	      recordP.stop();
		  //recordP.close();
	  }
	  
	  //Stops the playback of the Recorder
	  public void stopPlaying() throws MediaException
	  {
		  //Application must wait for Player to finish playing
		  playP.stop();
		  System.err.println("Player has been stopped.");
		  
		  form.removeCommand(stopCommand);
		  form.addCommand(playCommand);
		  form.addCommand(recordCommand);
		  
	  }
	  
	  private String saveFile(String filename, byte[] sound) 
	  {
		  String rootName = FileUtility.getDefaultRoot();
		  String restorepath = "file:///" + rootName + "JRSounds";				
		  FileUtility.createDirectory(restorepath);
		  String fullName = restorepath + "/" + filename;
		  if(FileUtility.createFile(fullName, sound)) 
		  {
			System.out.println("Sound saved.");	
			return fullName;	
		  } 
		  else 
		  {
			return "";
		  }		
	  }
	  
	  //Go back one screen
	  public void moveBack()
	  {
		System.err.println("Moving back");
		parentShell.returnFromActivity(this, Constants.ACTIVITY_CANCEL, null);
		/*TODO
		 * Fix NullPointerException.
		 */
		
	  }
	  
	  //Record audio in a separate thread to keep the command listener alert for stopping
	  public void run()
	  {		  
		    try
	        {   
			  recordAudio();
	        } catch (IOException ioe) {
	            errorItem.setLabel("Error");
	            errorItem.setText(ioe.toString());
	        } catch (MediaException me) {
	            errorItem.setLabel("Error");
	            errorItem.setText(me.toString());
	        } catch (InterruptedException ie) {
	            errorItem.setLabel("Error");
	            errorItem.setText(ie.toString());
	        }
	  }
}

class RecorderForm extends Form
{
    private StringItem messageItem;
    private StringItem errorItem;    

    public RecorderForm()
    {    	
    	super("Record Audio");        
    	messageItem = new StringItem("", "Press Record to start recording.");
    	append(messageItem);
    	errorItem = new StringItem("", "");
    	append(errorItem);    	
    	StringBuffer inhalt = new StringBuffer();    	
    }
    
    public StringItem getMessageItem()
    {
    	return messageItem;
    }
    
    public StringItem getErrorItem()
    {
    	return errorItem;
    }
    
}