package org.javarosa.core.api;

/**
 * IView provides an interface for UI screens that can
 * be displayed by a shell.  
 * 
 * @author Clayton Sims
 *
 */
public interface IView {
	/**
	 * @return A platform specific view object. NOTE: this must be the same type 
	 * as expected by the Display it is passed to or a runtime exception will be
	 * thrown. This would be handled by Generics if that could be handled by j2me. 
	 */
	public Object getScreenObject();
}
