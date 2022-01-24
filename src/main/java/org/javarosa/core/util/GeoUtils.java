/*
 * Copyright (C) 2014 University of Washington
 *
 * Originally developed by Dobility, Inc. (as part of SurveyCTO)
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
 *
 *
 * The formula is based on http://mathworld.wolfram.com/PolygonArea.html
 */

package org.javarosa.core.util;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: Meletis Margaritis
 * Date: 8/4/14
 * Time: 1:38 PM
 */
public final class GeoUtils {
    private static final Logger logger = LoggerFactory.getLogger(GeoUtils.class);
    static final double EARTH_EQUATORIAL_RADIUS_METERS = 6_378_100;
    static final double EARTH_EQUATORIAL_CIRCUMFERENCE_METERS = 2 * EARTH_EQUATORIAL_RADIUS_METERS * PI;

    /**
     * Calculates the enclosed area that is defined by a list of gps coordinates on earth.
     *
     * @param latLongs the list of coordinates.
     * @return the enclosed area in square meters (with a double precision).
     */
    public static double calculateAreaOfGPSPolygonOnEarthInSquareMeters(final List<LatLong> latLongs) {
        if (latLongs.size() < 3) {
            return 0;
        }

        final List<Double> listY = new ArrayList<>();
        final List<Double> listX = new ArrayList<>();

        // calculate segment x and y in degrees for each point
        final double latitudeRef = latLongs.get(0).latitude;
        final double longitudeRef = latLongs.get(0).longitude;
        for (int i = 1; i < latLongs.size(); i++) {
            double latitude = latLongs.get(i).latitude;
            double longitude = latLongs.get(i).longitude;
            listY.add(calculateYSegment(latitudeRef, latitude));
            listX.add(calculateXSegment(longitudeRef, longitude, latitude));
        }

        // sum areas of all triangle segments
        double areasSum = 0;
        for (int i = 1; i < listX.size(); i++) {
            areasSum += calculateAreaInSquareMeters(listX.get(i - 1), listX.get(i), listY.get(i - 1), listY.get(i));
        }

        return abs(areasSum); // Area can‘t be negative
    }

    /**
     * Returns the sum of the distances between points[i] and [i - 1], for all points starting with the second point
     * @param points the points between which the distances are to be summed
     * @return the sum of the distances, in meters
     */
    public static double calculateDistance(List<LatLong> points) {
        double totalDistance = 0;

        if (points.size() >= 2) {
            for (int i = 1; i < points.size(); ++i) {
                LatLong p1 = points.get(i - 1);
                LatLong p2 = points.get(i);
                double distance = distanceBetween(p1, p2);
                totalDistance += distance;
                logDistance(p1, p2, distance, totalDistance);
            }
        }

        return totalDistance;
    }

    private static void logDistance(LatLong p1, LatLong p2, double distance, double totalDistance) {
        logger.trace("\t{}\t{}\t{}\t{}\t{}\t{}",
            p1.latitude, p1.longitude,
            p2.latitude, p2.longitude,
            distance, totalDistance);
    }

    private static Double calculateAreaInSquareMeters(double x1, double x2, double y1, double y2) {
        return (y1 * x2 - x1 * y2) / 2;
    }

    private static double calculateYSegment(double latitudeRef, double latitude) {
        return (latitude - latitudeRef) * EARTH_EQUATORIAL_CIRCUMFERENCE_METERS / 360.0;
    }

    private static double calculateXSegment(double longitudeRef, double longitude, double latitude) {
        return (longitude - longitudeRef) * EARTH_EQUATORIAL_CIRCUMFERENCE_METERS * cos(toRadians(latitude)) / 360.0;
    }

    /**
     * Returns the distance between two points. Uses the Spherical Law of Cosines, as described in various
     * places, including <a href=https://www.movable-type.co.uk/scripts/latlong.html>this Movable Type
     * Scripts</a> page.
     *
     * @param p1 the first point
     * @param p2 the second point
     * @return the distance between the two points, in meters
     */
    private static double distanceBetween(LatLong p1, LatLong p2) {
        double Δλ = toRadians(p1.longitude - p2.longitude);
        double φ1 = toRadians(p1.latitude);
        double φ2 = toRadians(p2.latitude);
        return acos(sin(φ1) * sin(φ2) + cos(φ1) * cos(φ2) * cos(Δλ)) * EARTH_EQUATORIAL_RADIUS_METERS;
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
