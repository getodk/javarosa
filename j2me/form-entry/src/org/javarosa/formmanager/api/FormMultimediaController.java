/**
 * 
 */
package org.javarosa.formmanager.api;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.form.api.FormEntryPrompt;

/**
 * @author ctsims
 *
 */
public interface FormMultimediaController {
	
	
	public void playAudioOnLoad(FormEntryPrompt fep);
	
	public void playAudioOnDemand(FormEntryPrompt fep);
	
    /**
     * Plays audio for the SelectChoice (if AudioURI is present and media is available)
     * @param fep
     * @param select
     * @return
     */
	public int playAudioOnDemand(FormEntryPrompt fep,SelectChoice select);
}
