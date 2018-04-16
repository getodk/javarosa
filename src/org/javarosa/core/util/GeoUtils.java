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

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Meletis Margaritis
 * Date: 8/4/14
 * Time: 1:38 PM
 */
public final class GeoUtils {

  private static final double EARTH_RADIUS = 6378100; // in meters
  private static final double EARTH_CIRCUMFERENCE = 2 * Math.PI * EARTH_RADIUS;

    /**
   * Calculates the enclosed area that is defined by a list of gps coordinates on earth.
   *
   * @param gpsCoordinatesList the list of coordinates.
   * @return the enclosed area in square meters (with a double precision).
   */
  public static double calculateAreaOfGPSPolygonOnEarthInSquareMeters(final List<GPSCoordinates> gpsCoordinatesList) {
    if (gpsCoordinatesList.size() < 3) {
      return 0;
    }

    final List<Double> listY = new ArrayList<>();
    final List<Double> listX = new ArrayList<>();

    // calculate segment x and y in degrees for each point
    final double latitudeRef = gpsCoordinatesList.get(0).getLatitude();
    final double longitudeRef = gpsCoordinatesList.get(0).getLongitude();
    for (int i = 1; i < gpsCoordinatesList.size(); i++) {
      double latitude = gpsCoordinatesList.get(i).getLatitude();
      double longitude = gpsCoordinatesList.get(i).getLongitude();
      listY.add(calculateYSegment(latitudeRef, latitude));
      listX.add(calculateXSegment(longitudeRef, longitude, latitude));
    }

    // sum areas of all triangle segments
    double areasSum = 0;
    for (int i = 1; i < listX.size(); i++) {
      areasSum += calculateAreaInSquareMeters(listX.get(i - 1), listX.get(i), listY.get(i - 1), listY.get(i));
    }

    return Math.abs(areasSum); // Area can‘t be negative
  }

  public static double calculateDistance(List<GPSCoordinates> gpsCoordinatesList) {
    throw new UnsupportedOperationException(); // Not yet implemented
  }

  private static Double calculateAreaInSquareMeters(double x1, double x2, double y1, double y2) {
    return (y1 * x2 - x1 * y2) / 2;
  }

  private static double calculateYSegment(double latitudeRef, double latitude) {
    return (latitude - latitudeRef) * EARTH_CIRCUMFERENCE / 360.0;
  }

  private static double calculateXSegment(double longitudeRef, double longitude, double latitude) {
    return (longitude - longitudeRef) * EARTH_CIRCUMFERENCE * Math.cos(Math.toRadians(latitude)) / 360.0;
  }

  public static class GPSCoordinates {

    private double latitude;
    private double longitude;

    public GPSCoordinates(double latitude, double longitude) {
      this.latitude = latitude;
      this.longitude = longitude;
    }

    double getLatitude() {
      return latitude;
    }

    double getLongitude() {
      return longitude;
    }
  }
}
