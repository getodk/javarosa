/**
 * 
 */
package org.javarosa.formmanager.api;

import javax.microedition.media.Player;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.form.api.FormEntryPrompt;

/**
 * The FormMultimediaController is used to give form elements a single point
 * of access to trigger audio playback. It is used to allow for expanding
 * audio capacities as they occur, and also to give the form elements a way
 * to signal that audio is available, but let the controller decide when it
 * should actually be played.
 * 
 * @author ctsims
 *
 */
public interface FormMultimediaController {
	
	/**
	 * Signal that a form element has loaded, and has audio available
	 * for playback.
	 * 
	 * @param fep
	 */
	public void playAudioOnLoad(FormEntryPrompt fep);
	
	/**
	 * Signal that a form element has specifically requested that audio be
	 * played due to a user action. 
	 * 
	 * @param fep
	 */
	public void playAudioOnDemand(FormEntryPrompt fep);
	
    /**
     * Signal that a form element's select choice has specifically requested 
     * that audio be played due to a user action.
	 * 
     * @param fep
     * @param select
     * @return
     */
	public int playAudioOnDemand(FormEntryPrompt fep,SelectChoice select);
	
	/**
	 * Attaches a video player that this controller should be triggering and providing
	 * the interface for. The controller is not responsible for managing the player's
	 * lifecycle, only its interaction.
	 * 
	 * Any attached player will be detached before the incoming player is attached
	 * 
	 * @param player The player 
	 */
	public void attachVideoPlayer(Player player);
	
	/**
	 * Detaches a video player, allowing its resources to be freed and removing any
	 * interface items that were created to manage the player.
	 * 
	 * If the player is not currently attached, this is a no-op (since the player
	 * would have already been attached when the present one is)
	 * 
	 * @param player The player
	 */
	public void detachVideoPlayer(Player player);
}
