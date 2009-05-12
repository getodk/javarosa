package org.javarosa.media.audio.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.RecordControl;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.services.UnavailableServiceException;
import org.javarosa.media.audio.AudioException;
import org.javarosa.utilities.file.FileException;
import org.javarosa.utilities.file.services.IFileService;
import org.javarosa.utilities.file.services.J2MEFileService;

/**
 * An audio capture service that utilizes J2ME's robust Media API
 * 
 * @author Ndubisi Onuora
 */

public class J2MEAudioCaptureService implements IAudioCaptureService 
{
	public static final String serviceName = "J2MEAudioCaptureService";
	private int serviceState;
	
	private Player recordP;
	private RecordControl recordControl;
	private OutputStream audioDataStream;
	
	private Player playP;
	private InputStream recordedInputStream;
	
	private IFileService fileService;
	private String recordFileName;
	private String defaultFileName;
	//private String recordDirectory;
	private boolean recordingDeleted;
	private boolean recordingCreated;
	private boolean recordingDirectoryCreated;
	private String audioFormat;
	
	private int counter;
	
	public J2MEAudioCaptureService() throws UnavailableServiceException
	{
		try
		{
			fileService = getFileService();
		}
		catch(UnavailableServiceException ue)
		{			
			ue.printStackTrace();
			throw new UnavailableServiceException("File service is unavailable. Unable to start " + serviceName);
		}
		serviceState = IAudioCaptureService.IDLE;
		recordingDeleted = false;
		recordingCreated = false;
		recordingDirectoryCreated = false;
		counter = 0;
		audioFormat = ".wav"; //Default audio format is WAV
	}

	public String getName()
	{
		return serviceName;		
	}
	
	//@Override
	public OutputStream getAudio() 
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
			recordP = Manager.createPlayer("capture://audio");			
			recordP.realize();
			recordControl = (RecordControl)recordP.getControl("RecordControl");
			try
			{
				recordFileName = null; //Reset file name to prevent concatenation of previous recordFileName twice
				createFileName(recordFileName);
				System.err.println("Recorded Filename=" + recordFileName);
				audioDataStream = fileService.getFileOutputStream(recordFileName);				
			}
			catch(FileException fe)
			{
				audioDataStream = null;
				System.err.println("Error obtaining audio output stream.");
				fe.printStackTrace();
			}
			if(audioDataStream == null)
			{
				throw new AudioException("Could not record audio due to null audio output stream!");
			}
			
			recordControl.setRecordStream(audioDataStream);
			recordControl.startRecord();
	      
			recordingCreated = true;
			recordingDeleted = false;
			recordP.start();	    
	  
	   /*
	    * If the method does not die before here, 
	    * then the capture has officially started.	    
	    */	   
			serviceState = IAudioCaptureService.CAPTURE_STARTED;
		}
		catch(MediaException me)
		{
			throw new AudioException(me.getMessage());
		}
		catch(IOException ioe)
		{
			System.err.println(ioe.getMessage());
		}
		++counter;
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
			throw new AudioException(me.getMessage());
		}
		catch(IOException ioe)
		{
			System.err.println(ioe.getMessage());
		}
		
	}
	
	//@Override
	public void startPlayback() throws AudioException 
	{
		try
		{
			try
			{
				audioDataStream = fileService.getFileOutputStream(recordFileName);
				recordedInputStream = fileService.getFileDataStream(recordFileName);
			}
			catch(FileException fe)
			{
				audioDataStream = null;
				recordedInputStream = null;
				System.err.println("An error occurred while obtaining the file data stream.");
				fe.printStackTrace();				
			}
			if(audioDataStream == null || recordingDeleted)
			{
				throw new AudioException("No audio data recorded!");
			}			     
			playP = Manager.createPlayer(recordedInputStream, "audio/x-wav");
			
			playP.prefetch();
			playP.start();		
	    
			serviceState = IAudioCaptureService.PLAYBACK_STARTED;
		}
		catch(MediaException me)
		{
			throw new AudioException(me.getMessage());
		}
		catch(IOException ioe)
		{
			System.err.println(ioe.getMessage());
		}		
	}
	
	//@Override
	public void stopPlayback() throws AudioException 
	{
		if( recordingCreated && (serviceState == IAudioCaptureService.PLAYBACK_STARTED) )			
		{		
			try
			{
				playP.stop();
				serviceState = IAudioCaptureService.PLAYBACK_STOPPED;
			}
			catch(MediaException me)
			{
				throw new AudioException(me.getMessage());
			}
		}
	}
	
	public String getAudioPath()
	{
		return recordFileName;
	}
	
	public void saveRecording(String fileName) throws FileException
	{
		/*
		 * If saveRecording() is not called before a subsequent recording, previous recording will be erased.
		 */
		if(!recordingCreated)
		{			
			createFileName(fileName);			
			recordingCreated = true;
		}
	}
	
	public void removeRecording() throws FileException
	{
		if(recordingCreated)
		{
			try
			{
				audioDataStream.flush();
				closeRecordingStream();
				//closePlaybackStream();
				System.err.println("Recorded Filename=" + recordFileName);
				fileService.deleteFile(recordFileName);
				recordFileName = null;
				//fileService.deleteDirectory(recordDirectory);
				recordControl.reset();
				recordingCreated = false;
				recordingDeleted = true;
				--counter;
			}		
			catch(IOException ie)
			{
				System.err.println("Error resetting record control!");
				System.err.println(ie.getMessage());
				ie.printStackTrace();			
			}
			catch(FileException fe)
			{
				System.err.println(fe.getMessage());
				fe.printStackTrace();				
				throw new FileException("Error removing recorded audio!");
			}
		}		
	}
	
	  //Retrieve a reference to the first available service
	private IFileService getFileService() throws UnavailableServiceException
	{
		//#if app.usefileconnections
		JavaRosaServiceProvider.instance().registerService(new J2MEFileService());
		IFileService fService = (J2MEFileService)JavaRosaServiceProvider.instance().getService(J2MEFileService.serviceName);
		//#endif
		
		return fService;
	}
	
	private void createFileName(String fileName) throws FileException
	{		
		String rootName = fileService.getDefaultRoot();
	    String restorepath = "file:///" + rootName + "JRSounds";
	    String fullName;
	    defaultFileName = "Audio" + counter + audioFormat;
	    
	    if(!recordingDirectoryCreated)
		{
	    	//recordDirectory = restorepath;
			fileService.createDirectory(restorepath);
			recordingDirectoryCreated = true;
		}
	    
	    if(fileName == null)
		{
	    	fullName = restorepath + "/" + defaultFileName;
		}
	    else
	    {
	    	if(!fileName.endsWith(audioFormat))
	    		fileName += audioFormat;
	    	fullName = restorepath + "/" + fileName;
	    	
	    }
		recordFileName = fullName;
	}	
	
	private void closeRecordingStream() throws IOException
	{
		if(audioDataStream != null && serviceState == CAPTURE_STOPPED)
		{			
			audioDataStream.close();
		}
	}
	
	private void closePlaybackStream() throws IOException
	{		
		if(recordedInputStream != null && serviceState == PLAYBACK_STOPPED)
		{			
			recordedInputStream.close();
		}
	}
	
	//Closes all types of streams that are used
	public void closeStreams() throws IOException
	{
		if(recordP != null)
			recordP.close();
		if(playP != null)
			playP.close();
		closeRecordingStream();
		closePlaybackStream();
		
		serviceState = IAudioCaptureService.CLOSED;		
	}
}
