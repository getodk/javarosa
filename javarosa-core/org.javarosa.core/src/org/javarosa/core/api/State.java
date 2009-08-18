/**
 * 
 */
package org.javarosa.core.api;

/**
 * @author ctsims
 *
 */
public interface State<T extends Transitions> {
	public State<T> enter(T transitions);
	public void start();
}
