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
import java.util.ArrayList;

import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.xpath.IExprDataType;


/**
 * A response to a question requesting an GeoShape Value.
 * Consisting of a comma-separated ordered list of GeoPoint values.
 *
 * GeoTrace data is an open sequence of geo-locations.
 * GeoShape data is a closed sequence of geo-locations.
 *
 * @author mitchellsundt@gmail.com
 *
 */
public class GeoShapeData implements IAnswerData, IExprDataType {

	/**
	 * The data value contained in a GeoShapeData object is a GeoShape
	 *
	 * @author mitchellsundt@gmail.com
	 *
	 */
	public static class GeoShape {
		public ArrayList<double[]> points;

		public GeoShape() {
			points = new ArrayList<double[]>();
		}

		public GeoShape(ArrayList<double[]> points) {
			this.points = points;
		}
	};

	public final ArrayList<GeoPointData> points = new ArrayList<GeoPointData>();


    /**
     * Empty Constructor, necessary for dynamic construction during
     * deserialization. Shouldn't be used otherwise.
     */
    public GeoShapeData() {
    }

    /**
     * Copy constructor (deep)
     *
     * @param data
     */
    public GeoShapeData(GeoShapeData data) {
    	for ( GeoPointData p : data.points ) {
    		points.add(new GeoPointData(p));
    	}
    }

    public GeoShapeData(GeoShape ashape) {
    	for ( double[] da : ashape.points ) {
    		points.add(new GeoPointData(da));
    	}
    }

    @Override
    public IAnswerData clone() {
        return new GeoShapeData(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.javarosa.core.model.data.IAnswerData#getDisplayText()
     */
    @Override
    public String getDisplayText() {
    	StringBuilder b = new StringBuilder();
    	boolean first = true;
    	for ( GeoPointData p : points ) {
    		if ( !first ) {
    			b.append("; ");
    		}
    		first = false;
    		b.append(p.getDisplayText());
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
    	ArrayList<double[]> pts = new ArrayList<double[]>();
    	for ( GeoPointData p : points ) {
    		pts.add((double[])p.getValue());
    	}
    	GeoShape gs = new GeoShape(pts);
        return gs;
    }

    @Override
    public void setValue(Object o) {
        if (o == null) {
            throw new NullPointerException("Attempt to set an IAnswerData class to null.");
        }
        if ( !(o instanceof GeoShape) ) {
        	GeoShapeData t = new GeoShapeData();
        	GeoShapeData v = t.cast(new UncastData(o.toString()));
        	o = v.getValue();
        }
        GeoShape gs = (GeoShape) o;
        ArrayList<GeoPointData> temp = new ArrayList<GeoPointData>();
        for ( double[] da : gs.points ) {
        	temp.add(new GeoPointData(da));
        }
        points.clear();
        points.addAll(temp);
    }


    @Override
    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException,
            DeserializationException {
    	points.clear();
        int len = (int) ExtUtil.readNumeric(in);
        for ( int i = 0 ; i < len ; ++i ) {
        	GeoPointData t = new GeoPointData();
        	t.readExternal(in, pf);
        	points.add(t);
        }
    }


    @Override
    public void writeExternal(DataOutputStream out) throws IOException {
        ExtUtil.writeNumeric(out, points.size());
        for ( int i = 0 ; i < points.size() ; ++i ) {
        	GeoPointData t = points.get(i);
        	t.writeExternal(out);
        }
    }


    @Override
	public UncastData uncast() {
		return new UncastData(getDisplayText());
	}

    @Override
	public GeoShapeData cast(UncastData data) throws IllegalArgumentException {
		String[] parts = data.value.split(";");

		// silly...
		GeoPointData t = new GeoPointData();

		GeoShapeData d = new GeoShapeData();
		for ( String part : parts ) {
			// allow for arbitrary surrounding whitespace
			d.points.add(t.cast(new UncastData(part.trim())));
		}
		return d;
	}


	@Override
	public Boolean toBoolean() {
		// return whether or not any Geopoints have been set
		if ( points.size() == 0 ) {
			return false;
		}
		return true;
	}

	@Override
	public Double toNumeric() {
		if ( points.size() == 0 ) {
			// we have no shape, so no accuracy...
			return GeoPointData.NO_ACCURACY_VALUE;
		}
		// return the worst accuracy...
		double maxValue = 0.0;
		for ( GeoPointData p : points ) {
			maxValue = Math.max(maxValue, p.toNumeric());
		}
		return maxValue;
	}

	@Override
	public String toString() {
		return getDisplayText();
	}

}
