/**
 *   An interface to describe the supported functions of all types of AudioCapture services
 * 
 *   @author Ndubisi Onuora
 */

package org.javarosa.media.audio.service;

import org.javarosa.core.services.IService;

public interface IAudioCaptureService extends IService 
{
	public static enum STATE{
		IDLE,
		CAPTURE_STARTED, 
		CAPTURE_STOPPED, 
		PLAYBACK_STARTED,
		PLAYBACK_STOPPED,
		CLOSED};
	
	//Get the name of the service
	public String getName();	
	
	//Returns the state of this service
	public STATE getState();
	
	//Start recording audio
	public void startRecord();	
	
	//Stop recording audio
	public void stopRecord();	
	
	//Start playing the recorded audio
	public void startPlayback();
	
	//Stop playback of the recorded audio
	public void stopPlayback();
	
	//Return the captured audio
	public ByteArrayOutputStream getAudio();
	
	//Closes all types of streams that are used
	public void closeStreams();
}
