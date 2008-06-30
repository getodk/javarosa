/**
 * 
 */
package org.javarosa.view;

import java.util.Hashtable;

/**
 * @author Brian DeRenzi
 *
 */
public class ReturnValue {
	public int command = Commands.CMD_NONE;
	public Hashtable values = null;
	
	public ReturnValue() {
		this(Commands.CMD_NONE, new Hashtable());	
	}
	
	public ReturnValue(int c) {
		this(c, new Hashtable());
	}
	
	public ReturnValue(Hashtable v) {
		this(Commands.CMD_NONE, v);
	}
	
	public ReturnValue( int c, Hashtable v) {
		this.command = c;
		this.values = v;
	}
}
