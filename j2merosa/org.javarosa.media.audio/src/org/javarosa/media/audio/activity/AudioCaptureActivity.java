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
import org.javarosa.media.image.utilities.FileUtility;
//import org.javarosa.media.audio.midlet.RecordForm;
//import org.javarosa.j2me.view.DisplayViewFactory;

import org.javarosa.media.audio.model.FileDataPointer;
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
	private Command recordCommand, playCommand, stopCommand, backCommand, 
	                saveCommand, finishCommand;
	private IDisplay display;
	private ByteArrayOutputStream audioDataStream;
	private String fullName;
	private RecorderForm form;
	private FileDataPointer recordFile;
	
	private StringItem messageItem;
    private StringItem errorItem;
    
    Thread captureThread;    
    boolean captureThreadStarted = false;
    
    private static int counter = 0; //Used for saving files
    	
	public AudioCaptureActivity(IShell shell)
	{
		parentShell = shell;
		display = JavaRosaServiceProvider.instance().getDisplay();
		++counter;
	}
	
	//Finish off construction of Activity
	public void start(Context context)
	{		
		currentContext = context;
		form = new RecorderForm();
		
		messageItem = form.getMessageItem();
		errorItem = form.getErrorItem();
		
		initCommands();		
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
		if(recordP != null)
		{
			recordP.close();
			recordP = null;
		}
		else if(playP != null)
		{
			playP.close();
			playP = null;
		}
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
	
	private void initCommands()
	{
		recordCommand = new Command("Record", Command.SCREEN, 0);
    	form.addCommand(recordCommand);
    	playCommand = new Command("Play", Command.SCREEN, 1);
    	form.addCommand(playCommand);
    	stopCommand = new Command("Stop", Command.SCREEN, 0); //Do not add immediately    	
    	backCommand = new Command("Back", Command.BACK, 0);    	
    	form.addCommand(backCommand);
    	finishCommand = new Command("Finish", Command.OK, 0);
    	saveCommand = new Command("Save", Command.SCREEN, 0);
	}
	
	public void commandAction(Command comm, Displayable disp)
	{
		//Record to file
		if(comm == recordCommand)
		{			
	        captureThread.setPriority(Thread.currentThread().getPriority() +1 );
	        if(!captureThreadStarted)
	        {
	        	captureThread.start();
	        	captureThreadStarted = true;	        	
	        }
	        else
	        	captureThread.run();	        	        
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
	        
	    }
	    //Stop recording audio
	    else if(comm == stopCommand)
	    {
	    	try
	    	{
	    		stop();
	    	}	    		    	
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
	    else if(comm == finishCommand)
	    {
	    	finalizeTask();
	    }
	    else if(comm == saveCommand)
	    {
	    	String fileName = "Audio" + counter + ".wav";
	    	saveFile(fileName, audioDataStream.toByteArray());
	    }
	}
	
	public void recordAudio() throws MediaException, IOException, InterruptedException
	{
		  recordP = Manager.createPlayer("capture://audio");
		  recordP.realize();                
		  recordControl = (RecordControl)recordP.getControl("RecordControl");                
	      audioDataStream = new ByteArrayOutputStream();
	      recordControl.setRecordStream(audioDataStream);                
	      recordControl.startRecord();
	      //errorItem.setText("Started recording");
	      recordP.start();
	      messageItem.setText("Recording...");
	      
	      form.removeCommand(recordCommand); //"Hide" recordCommand when recording has stopped
		  form.addCommand(stopCommand);	    
		  
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
	      
		  checkStreamSize(audioDataStream);
		  
		  playP = Manager.createPlayer(recordedInputStream,"audio/x-wav");
          playP.prefetch();
	      //playP.realize();
	      playP.start();
	      System.err.println("Player has started.");
	      
	      form.removeCommand(saveCommand);
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
		  form.addCommand(finishCommand);
		  form.addCommand(saveCommand);
		  
		  messageItem.setText("done!");	                     
	      //recordedSoundArray = audioDataStream.toByteArray();
	      //errorItem.setText("Sound size=" + audioDataStream.toByteArray().length);
	      	      
	      recordP.stop();	      
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
	  
	  public FileDataPointer getRecordedAudio()
	  {
		  recordFile = new FileDataPointer(fullName);
		  return recordFile;
	  }
	  
	  private void readFile(String fileName)
	  {
		  String rootName = FileUtility.getDefaultRoot();
		  String restorepath = "file:///" + rootName + "JRSounds";				
		  String fullName = restorepath + "/" + fileName;
		  
		  recordFile = new FileDataPointer(fullName);
		  System.err.println("Successfully read Music file");
		  
		  System.out.println("Sound Size =" + recordFile.getData().length);		  
		  
		  finalizeTask();
	  }
	  
	  private String saveFile(String filename, byte[] sound) 
	  {
		  String rootName = FileUtility.getDefaultRoot();
		  String restorepath = "file:///" + rootName + "JRSounds";				
		  FileUtility.createDirectory(restorepath);
		  String fullName = restorepath + "/" + filename;
		  if(FileUtility.createFile(fullName, sound)) 
		  {
			System.out.println("Sound saved to:" + fullName);	
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
	  }	  
	  
	  //Finish capturing audio, inform shell, and return data as well
	  public void finalizeTask()
	  {
		  System.err.println("Finalizing audio capture");
		  Hashtable returnArgs = new Hashtable();
		  
		  returnArgs.put(Constants.RETURN_ARG_KEY, recordFile);
		  returnArgs.put(Constants.RETURN_ARG_TYPE_KEY, Constants.RETURN_ARG_TYPE_DATA_POINTER);
		  
		  parentShell.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, returnArgs);
	  }	  
	  
	  public void checkStreamSize(ByteArrayOutputStream ba) throws MediaException
	  {
		  if(ba.toByteArray().length <= 0)
			  throw new MediaException("Cannot create Player with 0 or less bytes in stream");
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