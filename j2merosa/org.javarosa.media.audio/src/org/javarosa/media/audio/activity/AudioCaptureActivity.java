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

public class AudioCaptureActivity implements IActivity, CommandListener 
{
	private IShell parentShell;
	private Context currentContext;
	
	private Player recorderPlayer;
	private RecordControl recordControl;
	private Command backCommand;
	private Command captureCommand;
	private Command playCommand;
	private IDisplay display;
	private ByteArrayOutputStream audioDataStream;
	private String fullName;
	private RecordForm form;
	
	public AudioCaptureActivity(IShell shell)
	{
		parentShell = shell;
		display = JavaRosaServiceProvider.instance().getDisplay();
	}
	
	//Finish off construction of Activity
	public void start(Context context)
	{		
		currentContext = context;
		form = new RecordForm();
		recorderPlayer = form.getRecordPlayer();
		captureCommand = form.getRecordCommand();
		playCommand = form.getPlayCommand();
		//Display.getDisplay(this).setCurrent(new RecordForm());
		display.setView(DisplayViewFactory.createView(form));
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
		recorderPlayer.close();
		recorderPlayer = null;
		
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
	
	//Start the recording
	public void capture()
	{
		//form.commandAction(captureCommand, this);
	}
	
	public void commandAction(Command c, Displayable disp)
	{
		form.commandAction(c, form);		
	}
	
}

class RecordForm extends Form implements CommandListener
{
    private StringItem messageItem;
    private StringItem errorItem;
    private final Command recordCommand, playCommand;
    private Player recordP;
    private Player playP;
    //private byte[] recordedSoundArray = null;
    private ByteArrayOutputStream output;

public RecordForm()
{
    super("Record Audio");        
    messageItem = new StringItem("Record", "Click record to start recording.");
    this.append(messageItem);
    errorItem = new StringItem("", "");
    this.append(errorItem);        
    recordCommand = new Command("Record", Command.SCREEN, 1);
    this.addCommand(recordCommand);
    playCommand = new Command("Play", Command.SCREEN, 2);
    this.addCommand(playCommand);        
    StringBuffer inhalt = new StringBuffer();        
    this.setCommandListener(this);
}

public void commandAction(Command comm, Displayable disp){
    //Record to file
	if(comm==recordCommand){
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
    //User should be able to replay recording
    else if(comm == playCommand) {
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
  }
  public Command getRecordCommand()
  {	  
	  return recordCommand;
  }
  
  public Command getPlayCommand()
  {
	  return playCommand;
  }
  
  public Player getRecordPlayer()
  {	  
	  return recordP;
  }
  
  public Player getPlayer()
  {
	  return playP;
  }
  
  public ByteArrayOutputStream getRecordedAudio()
  {
	  return output;
  }
  
  public void recordAudio() throws MediaException, IOException, InterruptedException
  {	  
	  recordP = Manager.createPlayer("capture://audio");
	  recordP.realize();                
      RecordControl rc = (RecordControl)recordP.getControl("RecordControl");                
      output = new ByteArrayOutputStream();
      rc.setRecordStream(output);                
      rc.startRecord();
      recordP.start();
      messageItem.setText("recording...");
      Thread.currentThread().sleep(5000);
      messageItem.setText("done!");
      rc.commit();               
      //recordedSoundArray = output.toByteArray();                
      recordP.close();
  }
  
  public void playAudio() throws MediaException, IOException
  {
	  ByteArrayInputStream recordedInputStream = new ByteArrayInputStream(output.toByteArray()/*recordedSoundArray*/);
      playP = Manager.createPlayer(recordedInputStream,"audio/basic");
      playP.prefetch();
      playP.start();
  }  
}