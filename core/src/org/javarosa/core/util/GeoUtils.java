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

  /**
   * Calculates the enclosed area that is defined by a list of gps coordinates on earth.
   *
   * @param gpsCoordinatesList the list of coordinates.
   * @return the enclosed area in square meters (with a double precision).
   */
  public static double calculateAreaOfGPSPolygonOnEarthInSquareMeters(final List<GPSCoordinates> gpsCoordinatesList) {
    return calculateAreaOfGPSPolygonOnSphereInSquareMeters(gpsCoordinatesList, EARTH_RADIUS);
  }

  /**
   * Calculates the enclosed area that is defined by a list of gps coordinates on a sphere.
   *
   * @param gpsCoordinatesList the list of coordinates.
   * @param radius the radius of the sphere in meters.
   * @return the enclosed area in square meters (with a double precision).
   */
  private static double calculateAreaOfGPSPolygonOnSphereInSquareMeters(final List<GPSCoordinates> gpsCoordinatesList, final double radius) {
    if (gpsCoordinatesList.size() < 3) {
      return 0;
    }

    final double diameter = radius * 2;
    final double circumference = diameter * Math.PI;
    final List<Double> listY = new ArrayList<Double>();
    final List<Double> listX = new ArrayList<Double>();
    final List<Double> listArea = new ArrayList<Double>();

    // calculate segment x and y in degrees for each point
    final double latitudeRef = gpsCoordinatesList.get(0).getLatitude();
    final double longitudeRef = gpsCoordinatesList.get(0).getLongitude();
    for (int i = 1; i < gpsCoordinatesList.size(); i++) {
      final double latitude = gpsCoordinatesList.get(i).getLatitude();
      final double longitude = gpsCoordinatesList.get(i).getLongitude();
      listY.add(calculateYSegment(latitudeRef, latitude, circumference));
      listX.add(calculateXSegment(longitudeRef, longitude, latitude, circumference));
    }

    // calculate areas for each triangle segment
    for (int i = 1; i < listX.size(); i++) {
      final double x1 = listX.get(i - 1);
      final double y1 = listY.get(i - 1);
      final double x2 = listX.get(i);
      final double y2 = listY.get(i);
      listArea.add(calculateAreaInSquareMeters(x1, x2, y1, y2));
    }

    // sum areas of all triangle segments
    double areasSum = 0;
    for (final Double area : listArea) {
      areasSum = areasSum + area;
    }

    // get absolute value of area, it can't be negative
    return Math.abs(areasSum);// Math.sqrt(areasSum * areasSum);
  }

  private static Double calculateAreaInSquareMeters(final double x1, final double x2, final double y1, final double y2) {
    return (y1 * x2 - x1 * y2) / 2;
  }

  private static double calculateYSegment(final double latitudeRef, final double latitude, final double circumference) {
    return (latitude - latitudeRef) * circumference / 360.0;
  }

  private static double calculateXSegment(final double longitudeRef, final double longitude, final double latitude, final double circumference) {
    return (longitude - longitudeRef) * circumference * Math.cos(Math.toRadians(latitude)) / 360.0;
  }

  public static class GPSCoordinates {

    private double latitude;
    private double longitude;

    public GPSCoordinates(double latitude, double longitude) {
      this.latitude = latitude;
      this.longitude = longitude;
    }

    public double getLatitude() {
      return latitude;
    }

    public double getLongitude() {
      return longitude;
    }
  }
}
