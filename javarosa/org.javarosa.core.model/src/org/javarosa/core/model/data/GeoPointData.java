package org.javarosa.core.model.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;


/**
 * A response to a question requesting an GeoPoint Value.
 * @author Yaw Anokwa
 *
 */
public class GeoPointData implements IAnswerData {
    
    private double[] gp = new double[2];
    
	/**
	 * Empty Constructor, necessary for dynamic construction during deserialization.
	 * Shouldn't be used otherwise.
	 */
	public GeoPointData() {
	    
	}
	
	public GeoPointData(double[] gp) {
		this.gp = gp;
	}
	public GeoPointData(Double[] gp) {
		setValue(gp);
	}
	
	public IAnswerData clone() {
		return new GeoPointData(gp);
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.model.data.IAnswerData#getDisplayText()
	 */
	public String getDisplayText() {
		return gp[0]+","+gp[1];
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.data.IAnswerData#getValue()
	 */
	public Object getValue() {
		return gp; 
	}
	
    public void setValue(Object o) {
        if(o == null) {
            throw new NullPointerException("Attempt to set an IAnswerData class to null.");
        }
        gp = (double[]) o;
    }

    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
        gp[0] = ExtUtil.readDecimal(in);
        gp[1] = ExtUtil.readDecimal(in);
    }

    public void writeExternal(DataOutputStream out) throws IOException {
        ExtUtil.writeDecimal(out, gp[0]);
        ExtUtil.writeDecimal(out, gp[1]);
    }
}
