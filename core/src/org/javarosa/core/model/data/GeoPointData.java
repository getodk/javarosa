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
import java.util.Vector;

import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;


/**
 * A response to a question requesting an GeoPoint Value.
 *
 * Ensure that any missing values are reset to MISSING_VALUE
 * This is currently 0.0, but perhaps should be NaN?
 *
 * An uninitialized GeoPoint is:
 *  [0.0, 0.0, MISSING_VALUE, MISSING_VALUE]
 *
 * @author mitchellsundt@gmail.com
 * @author Yaw Anokwa
 *
 */
public class GeoPointData implements IAnswerData {

	public static final int REQUIRED_ARRAY_SIZE = 2;
	public static final double MISSING_VALUE = 0.0;

    private double[] gp = new double[4];
    private int len = REQUIRED_ARRAY_SIZE;


    /**
     * Empty Constructor, necessary for dynamic construction during
     * deserialization. Shouldn't be used otherwise.
     */
    public GeoPointData() {
        // reset missing data...
        for (int i = REQUIRED_ARRAY_SIZE ; i < gp.length ; ++i ) {
        	this.gp[i] = MISSING_VALUE;
        }
    }

    public GeoPointData(GeoPointData gpd) {
    	this.fillArray(gpd.gp);
    }

    public GeoPointData(double[] gp) {
        this.fillArray(gp);
    }


    private void fillArray(double[] gp) {
        len = gp.length;
        for (int i = 0; i < len; i++) {
            this.gp[i] = gp[i];
        }
        // make sure that any old data is removed...
        for (int i = len ; i < gp.length ; ++i ) {
        	this.gp[i] = MISSING_VALUE;
        }
    }


    public IAnswerData clone() {
        return new GeoPointData(gp);
    }


    /*
     * (non-Javadoc)
     *
     * @see org.javarosa.core.model.data.IAnswerData#getDisplayText()
     */
    public String getDisplayText() {
        String s = "";
        for (int i = 0; i < len; i++) {
            s += gp[i] + " ";
        }
        return s.trim();

    }


    /*
     * (non-Javadoc)
     *
     * @see org.javarosa.core.model.data.IAnswerData#getValue()
     */
    public Object getValue() {
    	// clone()'ing to prevent some potential bad direct accesses
    	// when these values are returned by GeoLine or GeoShape objects.
        return gp.clone();
    }


    public void setValue(Object o) {
        if (o == null) {
            throw new NullPointerException("Attempt to set an IAnswerData class to null.");
        }
        this.fillArray((double[]) o);
    }


    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException,
            DeserializationException {
        len = (int) ExtUtil.readNumeric(in);
        for (int i = 0; i < len; i++) {
            gp[i] = ExtUtil.readDecimal(in);
        }
        // make sure that any old data is removed...
        for (int i = len ; i < gp.length ; ++i ) {
        	this.gp[i] = MISSING_VALUE;
        }
    }


    public void writeExternal(DataOutputStream out) throws IOException {
        ExtUtil.writeNumeric(out, len);
        for (int i = 0; i < len; i++) {
            ExtUtil.writeDecimal(out, gp[i]);
        }
    }


	public UncastData uncast() {
		return new UncastData(getDisplayText());
	}

	public GeoPointData cast(UncastData data) throws IllegalArgumentException {
		double[] ret = new double[4];
        // make sure that missing data is flagged as absent...
        for (int i = REQUIRED_ARRAY_SIZE ; i < ret.length ; ++i ) {
        	ret[i] = MISSING_VALUE;
        }

		Vector<String> choices = DateUtils.split(data.value, " ", true);
		int i = 0;
		for(String s : choices) {
			double d = Double.parseDouble(s);
			ret[i] = d;
			++i;
		}
		return new GeoPointData(ret);
	}
}
