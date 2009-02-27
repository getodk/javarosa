package org.javarosa.formmanager.utility;

import org.javarosa.core.Context;

/**
 * An interface for specifying actions to occur at the end of a form
 * entry activity. 
 * 
 * @author Clayton Sims
 * @date Jan 30, 2009 
 *
 */
public interface ILoadHost {
	
	/**
	 * A handler method that will be called upon completion of a form entry activity.
	 * @param context The context of the form entry activity that preceded this call.
	 */
	public void returnFromLoading(Context context);
}
