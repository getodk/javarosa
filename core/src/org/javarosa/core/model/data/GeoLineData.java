/*
 * Copyright (C) 2014 JavaRosa
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

import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.xpath.IExprDataType;


/**
 * A response to a question requesting an GeoLine Value.
 * Consisting of a comma-separated pair of GeoPoint values.
 *
 * @author mitchellsundt@gmail.com
 *
 */
public class GeoLineData implements IAnswerData, IExprDataType {

	/**
	 * The data value contained in a GeoLineData object is a GeoLine
	 *
	 * @author mitchellsundt@gmail.com
	 *
	 */
	public static class GeoLine {
		public double[] start;
		public double[] end;

		public GeoLine() {
			this.start = new double[4];
			this.end = new double[4];
		}

		public GeoLine(double[] start, double[] end) {
			this.start = start;
			this.end = end;
		}
	};

	private GeoPointData start = new GeoPointData();
	private GeoPointData end = new GeoPointData();


    /**
     * Empty Constructor, necessary for dynamic construction during
     * deserialization. Shouldn't be used otherwise.
     */
    public GeoLineData() {

    }

    /**
     * Copy constructor (deep)
     *
     * @param data
     */
    public GeoLineData(GeoLineData data) {
        this.start = new GeoPointData(data.start);
        this.end = new GeoPointData(data.end);
    }

    public GeoLineData(GeoLine aline) {
    	this.start = new GeoPointData(aline.start);
    	this.end = new GeoPointData(aline.end);
    }

    public IAnswerData clone() {
        return new GeoLineData(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.javarosa.core.model.data.IAnswerData#getDisplayText()
     */
    public String getDisplayText() {
    	StringBuilder b = new StringBuilder();
    	b.append(start.getDisplayText());
    	b.append("; ");
    	b.append(end.getDisplayText());
    	return b.toString();
    }


    /*
     * (non-Javadoc)
     *
     * @see org.javarosa.core.model.data.IAnswerData#getValue()
     */
    public Object getValue() {
    	GeoLine gl = new GeoLine( (double[]) start.getValue(),
    							  (double[]) end.getValue());
        return gl;
    }


    public void setValue(Object o) {
        if (o == null) {
            throw new NullPointerException("Attempt to set an IAnswerData class to null.");
        }
        GeoLine gl = (GeoLine) o;
        this.start.setValue(gl.start);
        this.end.setValue(gl.end);
    }


    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException,
            DeserializationException {
    	start.readExternal(in, pf);
    	end.readExternal(in, pf);
    }


    public void writeExternal(DataOutputStream out) throws IOException {
    	start.writeExternal(out);
    	end.writeExternal(out);
    }


	public UncastData uncast() {
		return new UncastData(getDisplayText());
	}

	public GeoLineData cast(UncastData data) throws IllegalArgumentException {
		String[] parts = data.value.split(";");

		if ( parts.length != 2 ) throw new IllegalArgumentException("Expected comma-separated GeoPoints");

		// silly...
		GeoPointData t = new GeoPointData();

		GeoLineData d = new GeoLineData();
		// allow for arbitrary surrounding whitespace
		d.start = t.cast(new UncastData(parts[0].trim()));
		d.end = t.cast(new UncastData(parts[1].trim()));

		return d;
	}


	@Override
	public Boolean toBoolean() {
		// return whether both Geopoints have been set
		return start.toBoolean() && end.toBoolean();
	}

	@Override
	public Double toNumeric() {
		// return the worst accuracy...
		return Math.max(start.toNumeric(), end.toNumeric());
	}

	@Override
	public String toString() {
		return getDisplayText();
	}

}
