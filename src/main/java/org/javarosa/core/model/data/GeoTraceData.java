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

import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.xpath.IExprDataType;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * A response to a question requesting an GeoTrace Value.
 * Consisting of a comma-separated ordered list of GeoPoint values.
 *
 * GeoTrace data is an open sequence of geo-locations.
 * GeoShape data is a closed sequence of geo-locations.
 *
 * @author mitchellsundt@gmail.com
 *
 */
public class GeoTraceData implements IAnswerData, IExprDataType {

    /**
     * The data value contained in a GeoTraceData object is a GeoTrace
     *
     * @author mitchellsundt@gmail.com
     *
     */
    public static class GeoTrace {
        public final ArrayList<double[]> points;

        public GeoTrace() {
            points = new ArrayList<>();
        }

        public GeoTrace(ArrayList<double[]> points) {
            this.points = points;
        }
    }

    public final ArrayList<GeoPointData> points = new ArrayList<>();


    /**
     * Empty Constructor, necessary for dynamic construction during
     * deserialization. Shouldn't be used otherwise.
     */
    public GeoTraceData() {

    }

    /**
     * Copy constructor (deep)
     *
     * @param data
     */
    public GeoTraceData(GeoTraceData data) {
        for ( GeoPointData p : data.points ) {
            points.add(new GeoPointData(p));
        }
    }

    public GeoTraceData(GeoTrace atrace) {
        for ( double[] da : atrace.points ) {
            points.add(new GeoPointData(da));
        }
   }

    @Override
    public IAnswerData clone() {
        return new GeoTraceData(this);
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


    @Override
    public @NotNull Object getValue() {
        ArrayList<double[]> pts = new ArrayList<>();
        for ( GeoPointData p : points ) {
            pts.add((double[])p.getValue());
        }
        return new GeoTrace(pts);
    }


    @Override
    public void setValue(@NotNull Object o) {
        if (o == null) {
            throw new NullPointerException("Attempt to set an IAnswerData class to null.");
        }
        if ( !(o instanceof GeoTrace) ) {
            GeoTraceData t = new GeoTraceData();
            GeoTraceData v = t.cast(new UncastData(o.toString()));
            o = v.getValue();
        }
        GeoTrace gs = (GeoTrace) o;
        ArrayList<GeoPointData> temp = new ArrayList<>();
        for ( double[] da : gs.points ) {
            temp.add(new GeoPointData(da));
        }
        points.clear();
        points.addAll(temp);
    }


    @Override
    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException {
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
        for (GeoPointData t : points) {
            t.writeExternal(out);
        }
    }


    @Override
    public UncastData uncast() {
        return new UncastData(getDisplayText());
    }

    @Override
    public GeoTraceData cast(UncastData data) throws IllegalArgumentException {
        String[] parts = data.value.split(";");

        // silly...
        GeoPointData t = new GeoPointData();

        GeoTraceData d = new GeoTraceData();
        for ( String part : parts ) {
            // allow for arbitrary surrounding whitespace
            d.points.add(t.cast(new UncastData(part.trim())));
        }
        return d;
    }


    @Override
    public Boolean toBoolean() {
        // return whether or not any Geopoints have been set
        return !points.isEmpty();
    }

    @Override
    public Double toNumeric() {
        if ( points.size() == 0 ) {
            // we have no trace, so no accuracy...
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
