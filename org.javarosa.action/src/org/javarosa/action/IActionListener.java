package org.javarosa.action;


/**
 * Interface through which an action communicates back to whoever launches it.
 * 
 * @author daniel
 *
 */
public interface IActionListener {
	public void actionCompleted(IAction action, byte actionCommand,Object data);
}
