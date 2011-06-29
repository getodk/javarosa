/**
 * 
 */
package org.javarosa.formmanager.api;

import java.io.IOException;

import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.Reference;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.services.UnavailableServiceException;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.formmanager.api.transitions.FormEntryTransitions;
import org.javarosa.formmanager.properties.FormManagerProperties;
import org.javarosa.formmanager.view.IFormEntryView;

/**
 * Extension of {@link FormEntryController} for J2ME.
 * 
 * @author ctsims
 *
 */
public class JrFormEntryController extends FormEntryController implements FormMultimediaController {
	
	FormEntryTransitions transitions;
	IFormEntryView view;
	boolean quickEntry = true;
	
	
	/** Causes audio player to throw runtime exceptions if there are problems instead of failing silently **/
	private boolean audioFailFast = true;
	
    /////////AUDIO PLAYBACK
	static Player audioPlayer;
	protected static boolean playAudioIfAvailable = true;
	protected static final int AUDIO_SUCCESS = 1;
	protected static final int AUDIO_NO_RESOURCE = 2;
	protected static final int AUDIO_ERROR = 3;
	protected static final int AUDIO_DISABLED = 4;
	protected static final int AUDIO_BUSY = 5;
	protected static final int AUDIO_NOT_RECOGNIZED = 6;
	private static Reference curAudRef = null;
	private static String curAudioURI;
	
	String extraKeyMode;
	
	public JrFormEntryController(JrFormEntryModel model) {
		this(model, FormManagerProperties.EXTRA_KEY_LANGUAGE_CYCLE, false, true);
	}
	
	public JrFormEntryController(JrFormEntryModel model, String extraKeyMode, boolean audioFailFast, boolean quickEntry) {
		super(model);
		tryToInitDefaultLanguage(model);
		this.extraKeyMode = extraKeyMode;
		this.audioFailFast = audioFailFast;
		this.quickEntry = quickEntry;
	}

	private void tryToInitDefaultLanguage(JrFormEntryModel model) {
		//Try to set the current form locale based on the current app locale
		String[] languages = model.getLanguages();
		if(languages != null) {
			String locale = Localization.getGlobalLocalizerAdvanced().getLocale();
			if(locale != null) {
				for(String language : languages) {
					if(locale.equals(language)) {
						model.getForm().getLocalizer().setLocale(locale);
						break;
					}
				}
			}
		}
	}

	public JrFormEntryModel getModel () {
		return (JrFormEntryModel)super.getModel();
	}
	
	public void setView(IFormEntryView view) {
		this.view = view;
	}
	public IFormEntryView getView(){
		return this.view;
	}
	public void setTransitions(FormEntryTransitions transitions) {
		this.transitions = transitions;
	}
	
	public void start() {
		view.show();
	}
	
	/**
	 * Start from a specific index
	 * @param index
	 */
	public void start(FormIndex index){
		view.show(index);
	}
	
	public void abort() {
		transitions.abort();
	}
	
	public void saveAndExit(boolean formComplete) {
		if (formComplete){
			this.getModel().getForm().postProcessInstance();
		}
		transitions.formEntrySaved(this.getModel().getForm(),this.getModel().getForm().getInstance(),formComplete);
	}
	
	public void suspendActivity(int mediaType) throws UnavailableServiceException {
		transitions.suspendForMediaCapture(mediaType);
	}
	
	public void cycleLanguage () {
		setLanguage(getModel().getForm().getLocalizer().getNextLocale());
	}
	
	public String getExtraKeyMode() {
		return extraKeyMode;
	}
	
	
	
	//// New Audio Stuff follows below. I've tried to set it up so that we can split this out into a seperate "view" 
	//// if you will at a later point.
	/**
	 * Checks the boolean playAudioIfAvailable first.
	 * Plays the question audio text
	 */
	public void playAudioOnLoad(FormEntryPrompt fep){
		//If the current session is expecting audio playback w/the extrakey, don't 
		//play it passively, wait for the button to be pressed.
		if(!FormManagerProperties.EXTRA_KEY_AUDIO_PLAYBACK.equals(extraKeyMode)) {
			playAudio(fep,null);
		}
	}
	
	/**
	 * Checks the boolean playAudioIfAvailable first.
	 * Plays the question audio text
	 */
	public void playAudioOnDemand(FormEntryPrompt fep){
		playAudio(fep,null);
	}
	
	public int playAudioOnDemand(FormEntryPrompt fep,SelectChoice select) {
		return playAudio(fep, select);
	}
	
    /**
     * Plays audio for the SelectChoice (if AudioURI is present and media is available)
     * @param fep
     * @param select
     * @return
     */
	public int playAudio(FormEntryPrompt fep,SelectChoice select){
		if (!playAudioIfAvailable) return AUDIO_DISABLED;
		
		String textID;
		curAudioURI = null;
		if (select == null) {
			if (fep.getAudioText() != null) {
				curAudioURI = fep.getAudioText();
			} else {
				return AUDIO_NO_RESOURCE;
			}	
		}else{
			textID = select.getTextID();
			if(textID == null || textID == "") return AUDIO_NO_RESOURCE;
			
			if (fep.getSpecialFormSelectChoiceText(select, FormEntryCaption.TEXT_FORM_AUDIO) != null) {
				curAudioURI = fep.getSpecialFormSelectChoiceText(select, FormEntryCaption.TEXT_FORM_AUDIO);
			} else {
				return AUDIO_NO_RESOURCE;
			}
		}
		int retcode = AUDIO_SUCCESS;
		try {
			curAudRef = ReferenceManager._().DeriveReference(curAudioURI);
			String format = getFileFormat(curAudioURI);

			if(format == null) return AUDIO_NOT_RECOGNIZED;
			if(audioPlayer == null){
				audioPlayer = Manager.createPlayer(curAudRef.getStream(), format);
				audioPlayer.start();
			}else{
				audioPlayer.deallocate();
				audioPlayer.close();
				audioPlayer = Manager.createPlayer(curAudRef.getStream(), format);
				audioPlayer.start();
			}
			
		} catch (InvalidReferenceException ire) {
			retcode = AUDIO_ERROR;
			if(audioFailFast)throw new RuntimeException("Invalid Reference Exception when attempting to play audio at URI:"+ curAudioURI + "Exception msg:"+ire.getMessage());
			System.err.println("Invalid Reference Exception when attempting to play audio at URI:"+ curAudioURI + "Exception msg:"+ire.getMessage());
		} catch (IOException ioe) {
			retcode = AUDIO_ERROR;
			if(audioFailFast) throw new RuntimeException("IO Exception (input cannot be read) when attempting to play audio stream with URI:"+ curAudioURI + "Exception msg:"+ioe.getMessage());
			System.err.println("IO Exception (input cannot be read) when attempting to play audio stream with URI:"+ curAudioURI + "Exception msg:"+ioe.getMessage());
		} catch (MediaException e) {
			retcode = AUDIO_ERROR;
			if(audioFailFast) throw new RuntimeException("Media format not supported! Uri: "+ curAudioURI + "Exception msg:"+e.getMessage());
			System.err.println("Media format not supported! Uri: "+ curAudioURI + "Exception msg:"+e.getMessage());
		}
		return retcode;
	}
	
	private static String getFileFormat(String fpath){
//		Wave audio files: audio/x-wav
//		AU audio files: audio/basic
//		MP3 audio files: audio/mpeg
//		MIDI files: audio/midi
//		Tone sequences: audio/x-tone-seq
//		MPEG video files: video/mpeg
//		Audio 3GPP files (.3gp) audio/3gpp
//		Audio AMR files (.amr) audio/amr
//		Audio AMR (wideband) files (.awb) audio/amr-wb
//		Audio MIDI files (.mid or .midi) audio/midi
//		Audio MP3 files (.mp3) audio/mpeg
//		Audio MP4 files (.mp4) audio/mp4
//		Audio WAV files (.wav) audio/wav audio/x-wav
		
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

	public boolean isEntryOptimized() {
		return quickEntry;
	}

}
