package org.javarosa.media.audio.service;

import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.RecordControl;

import org.javarosa.media.audio.AudioException;

/**
 * An audio capture service that utilizes J2ME's beautiful Media API
 * 
 * @author Ndubisi Onuora
 */

public class J2MEAudioCaptureService implements IAudioCaptureService 
{
	private final String serviceName = "J2MEAudioCaptureService";
	private int serviceState;
	
	private Player recordP;
	private RecordControl recordControl;
	private ByteArrayOutputStream audioDataStream;
	
	private Player playP;
	private ByteArrayInputStream recordedInputStream;
	
	public J2MEAudioCaptureService()
	{
		serviceState = IAudioCaptureService.IDLE;
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

	public int getState()
	{
		return serviceState;
	}	
	
	//@Override
	public void startRecord() throws AudioException
	{
		try
		{
			//recordP = Manager.createPlayer("capture://audio");
			recordP = Manager.createPlayer("capture://audio?encoding=audio/mpeg");
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
			serviceState = IAudioCaptureService.CAPTURE_STARTED;
		}
		catch(MediaException me)
		{
			throw new AudioException();
		}
	}

	//@Override
	public void stopRecord() throws AudioException
	{
		try
		{
			recordControl.commit();
			recordP.stop();
		
			serviceState = IAudioCaptureService.CAPTURE_STOPPED;
		}
		catch(MediaException me)
		{
			throw new AudioException();
		}
	}
	
	//@Override
	public void startPlayback() throws AudioException 
	{
		try
		{
			ByteArrayInputStream recordedInputStream = new ByteArrayInputStream(audioDataStream.toByteArray());	      
			//checkStreamSize(audioDataStream);
			//playP = Manager.createPlayer(recordedInputStream,"audio/x-wav");
			playP = Manager.createPlayer(recordedInputStream,"audio/mpeg");
		
			playP.prefetch();
			playP.start();		
	    
			serviceState = IAudioCaptureService.PLAYBACK_STARTED;
		}
		catch(MediaException me)
		{
			throw new AudioException();
		}
	}
	
	//@Override
	public void stopPlayback() throws AudioException 
	{
		try
		{
			playP.stop();
			serviceState = IAudioCaptureService.PLAYBACK_STOPPED;
		}
		catch(MediaException me)
		{
			throw new AudioException();
		}
	}
	
	//Closes all types of streams that are used
	public void closeStreams()
	{		
		recordP.close();
		playP.close();
		serviceState = IAudioCaptureService.CLOSED;
	}

}
