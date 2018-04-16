/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.javarosa.core.util;

import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.PolygonArea;
import net.sf.geographiclib.PolygonResult;

import java.util.List;

import static net.sf.geographiclib.Constants.WGS84_a;
import static net.sf.geographiclib.Constants.WGS84_f;

public final class GeoUtils {
    public static double calculateAreaOfGPSPolygonOnEarthInSquareMeters(final List<LatLong> points) {
        return points.size() < 3 ? 0 : getPolygonResult(points, false).area;
    }

    public static double calculateDistance(List<LatLong> points) {
        return points.size() < 3 ? 0 : getPolygonResult(points, true).perimeter;
    }

    private static PolygonResult getPolygonResult(List<LatLong> points, boolean polyline) {
        boolean backwardsCompatible = true; // We used a less precise radius for the Earth, and treated it as spherical (not an oblate spheroid)
        Geodesic geod = new Geodesic(backwardsCompatible ? 6_378_100 : WGS84_a, backwardsCompatible ? 0 : WGS84_f);
        PolygonArea poly = new PolygonArea(geod, polyline);
        for (LatLong point : points) {
            poly.AddPoint(point.latitude, point.longitude);
        }
        return poly.Compute(false, true);
    }

    public static class LatLong {
        private final double latitude;
        private final double longitude;

        public LatLong(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
