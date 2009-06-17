/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

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
	    Double[] d = new Double[] {new Double(gp[0]), new Double(gp[1])};
		return d;
	}
	
    public void setValue(Object o) {
        if(o == null) {
            throw new NullPointerException("Attempt to set an IAnswerData class to null.");
        }
        Double[] d = ((Double[]) o);
        gp[0] = d[0].doubleValue();
        gp[1] = d[1].doubleValue();
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
