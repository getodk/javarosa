/**
 * 
 */
package org.javarosa.core.api;

import java.util.EmptyStackException;
import java.util.Stack;

/**
 * @author ctsims
 *
 */
public class StateMachine {
	private static Stack statesToReturnTo = new Stack();
	
	public static void setStateToReturnTo (State st) {
		statesToReturnTo.push(st);
	}
	
	public static State getStateToReturnTo () {
		try {
			return (State)statesToReturnTo.pop();
		} catch (EmptyStackException e) {
			throw new RuntimeException("Tried to return to a saved state, but no state to return to had been set earlier in the workflow");
		}
	}
}
