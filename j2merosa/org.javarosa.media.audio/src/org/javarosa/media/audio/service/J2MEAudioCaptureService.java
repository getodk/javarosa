package org.javarosa.media.audio.service;

import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.RecordControl;

/**
 * An audio capture service that utilizes J2ME's beautiful Media API
 * 
 * @author Ndubisi Onuora
 */

public class J2MEAudioCaptureService implements IAudioCaptureService 
{
	private final String serviceName = "J2MEAudioCaptureService";
	private STATE serviceState;
	
	private Player recordP;
	private RecordControl recordControl;
	private ByteArrayOutputStream audioDataStream;
	
	private Player playP;
	private ByteArrayInputStream recordedInputStream;
	
	public J2MEAudioCaptureService()
	{
		serviceState = IDLE;
	}

	public String getName()
	{
		return serviceName;		
	}
	
	//@Override
	public ByteArrayOutputStream getAudio() 
	{		
		return audioDataStream;
	}

	public STATE getState()
	{
		return serviceState;
	}	
	
	//@Override
	public void startRecord()
	{
		recordP = Manager.createPlayer("capture://audio");
		recordP.realize();                
		recordControl = (RecordControl)recordP.getControl("RecordControl");                
	    audioDataStream = new ByteArrayOutputStream();
	    recordControl.setRecordStream(audioDataStream);                
	    recordControl.startRecord();
	      
	    recordP.start();	    
	  
	   /*
	    * If the method does not die before here, 
	    * then the capture has officially started.	    
	    */	   
	    serviceState = CAPTURE_STARTED; 
	}

	//@Override
	public void stopRecord() 
	{
		recordControl.commit();
		recordP.stop();
		
		serviceState = CAPTURE_STOPPED;
	}
	
	//@Override
	public void startPlayback() 
	{
		ByteArrayInputStream recordedInputStream = new ByteArrayInputStream(audioDataStream.toByteArray());	      
		checkStreamSize(audioDataStream);
		playP = Manager.createPlayer(recordedInputStream,"audio/x-wav");
		
		playP.prefetch();
	    playP.start();
	    
	    serviceState = PLAYBACK_STARTED;
	}
	
	//@Override
	public void stopPlayback() 
	{
		playP.stop();
		serviceState = PLAYBACK_STOPPED;
	}
	
	//Closes all types of streams that are used
	public void closeStreams()
	{		
		recordP.close();
		playP.close();
		serviceState = CLOSED;
	}

}
