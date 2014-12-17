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
import java.util.List;

import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.xpath.IExprDataType;


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
public class GeoPointData implements IAnswerData, IExprDataType {

	public static final int REQUIRED_ARRAY_SIZE = 2;
	public static final double MISSING_VALUE = 0.0;
	// value to be reported if we never captured a datapoint
	public static final double NO_ACCURACY_VALUE = 9999999.0;

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


    @Override
    public IAnswerData clone() {
        return new GeoPointData(gp);
    }


    /*
     * (non-Javadoc)
     *
     * @see org.javarosa.core.model.data.IAnswerData#getDisplayText()
     */
    @Override
    public String getDisplayText() {
    	if ( !toBoolean() ) {
    		// it hasn't been set...
    		return "";
    	}
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < len; i++) {
            b.append(gp[i]);
            if ( i != len - 1) {
            	b.append(" ");
            }
        }
        return b.toString();

    }


    /*
     * (non-Javadoc)
     *
     * @see org.javarosa.core.model.data.IAnswerData#getValue()
     */
    @Override
    public Object getValue() {
    	// clone()'ing to prevent some potential bad direct accesses
    	// when these values are returned by GeoLine or GeoShape objects.
        return gp.clone();
    }


    @Override
    public void setValue(Object o) {
        if (o == null) {
            throw new NullPointerException("Attempt to set an IAnswerData class to null.");
        }
        this.fillArray((double[]) o);
    }


    @Override
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


    @Override
    public void writeExternal(DataOutputStream out) throws IOException {
        ExtUtil.writeNumeric(out, len);
        for (int i = 0; i < len; i++) {
            ExtUtil.writeDecimal(out, gp[i]);
        }
    }


    @Override
	public UncastData uncast() {
		return new UncastData(getDisplayText());
	}

    @Override
	public GeoPointData cast(UncastData data) throws IllegalArgumentException {
		double[] ret = new double[4];
        // make sure that missing data is flagged as absent...
        for (int i = REQUIRED_ARRAY_SIZE ; i < ret.length ; ++i ) {
        	ret[i] = MISSING_VALUE;
        }

      List<String> choices = DateUtils.split(data.value, " ", true);
		int i = 0;
		for(String s : choices) {
			double d = Double.parseDouble(s);
			ret[i] = d;
			++i;
		}
		return new GeoPointData(ret);
	}

	@Override
	public Boolean toBoolean() {
		// return whether or not the Geopoint has been set
		return (gp[0] != 0.0 || gp[1] != 0.0 || gp[2] != 0.0 || gp[3] != 0.0);
	}

	@Override
	public Double toNumeric() {
		// return accuracy...
		if ( !toBoolean() ) {
			// we have no captured geopoint...
			// bigger than the radius of the earth (meters)...
			return NO_ACCURACY_VALUE;
		}
		return gp[3];
	}

	@Override
	public String toString() {
		return getDisplayText();
	}


  public double getPart(int i) {
    if (i < len) {
      return gp[i];
    } else {
      throw new ArrayIndexOutOfBoundsException("Cannot find coordinates part with index " + i);
    }
  }
}
