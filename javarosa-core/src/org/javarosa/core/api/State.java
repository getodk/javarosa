/**
 * 
 */
package org.javarosa.core.api;

/**
 * @author ctsims
 *
 */
public interface State<T extends Transitions> {
	public void enter(T transitions);
	public void start();
}
