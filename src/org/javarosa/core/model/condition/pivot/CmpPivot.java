/**
 * 
 */
package org.javarosa.core.model.condition.pivot;

/**
 * @author ctsims
 *
 */
public class CmpPivot implements Pivot {
	private double val;
	private int op;
	private boolean outcome;
	
	public CmpPivot(double val, int op) {
		this.val = val;
		this.op = op;
	}
	
	public void setOutcome(boolean outcome){ 
		this.outcome = outcome;
	}
	
	public double getVal(){ 
		return val;
	}
	
	public int getOp() {
		return op;
	}
	
	public boolean getOutcome() {
		return outcome;
	}
}
