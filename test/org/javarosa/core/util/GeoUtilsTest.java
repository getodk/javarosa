/*
 * Copyright (C) 2012-14 Dobility, Inc.
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

package org.javarosa.core.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.List;


public class GeoUtilsTest extends TestCase {

  public GeoUtilsTest(String name) {
    super(name);
	System.out.println("Running " + this.getClass().getName() + " test: " + name + "...");
  }

  public static Test suite() {
    TestSuite suite = new TestSuite();

    suite.addTest(new GeoUtilsTest("testCalculateAreaOfGPSPolygonOnEarthInSquareMeters_1st_Route"));
    suite.addTest(new GeoUtilsTest("testCalculateAreaOfGPSPolygonOnEarthInSquareMeters_2nd_Route"));
    suite.addTest(new GeoUtilsTest("testCalculateAreaOfGPSPolygonOnEarthInSquareMeters_3rd_Route"));

    return suite;
  }

  /**
   * |\      /-----------------------------------------------
   * | \    /                                               |
   * |  \  /                                                |
   * |   \/                                                 |
   * |                                                      |
   * |                                                      |
   * |                                                      |
   * |-------------------------------------------------------
   *
   *
   * Tests that the enclosed area is the same as in:
   *
   * http://www.mapdevelopers.com/area_finder.php?&points=%5B%5B38.253094215699576%2C21.756382658677467%5D%2C%5B38.25021274773806%2C21.756382658677467%5D%2C%5B38.25007793942195%2C21.763892843919166%5D%2C%5B38.25290886154963%2C21.763935759263404%5D%2C%5B38.25146813817506%2C21.758421137528785%5D%5D
   *
   *
   *  @throws Exception
   */
  public void testCalculateAreaOfGPSPolygonOnEarthInSquareMeters_1st_Route() throws Exception {
    double[][] points = {
            {38.253094215699576,21.756382658677467},
            {38.25021274773806,21.756382658677467},
            {38.25007793942195,21.763892843919166},
            {38.25290886154963,21.763935759263404},
            {38.25146813817506,21.758421137528785}
    };

    runTestWith(points, 151452);
  }

  /**
   * ------------------------------------------------------
   *  \                                                   |
   *   \                                                  |
   *    \                                                 |
   *    /                                                 |
   *   /                                                  |
   *  /                                                   |
   * ------------------------------------------------------
   *
   *
   * Tests that the enclosed area is the same as in:
   *
   * http://www.mapdevelopers.com/area_finder.php?&points=%5B%5B38.25304740874071%2C21.75644703234866%5D%2C%5B38.25308110946615%2C21.763377860443143%5D%2C%5B38.25078942453431%2C21.763399318115262%5D%2C%5B38.25090738066984%2C21.756640151397733%5D%2C%5B38.25197740258244%2C21.75892539347842%5D%5D
   *
   *
   *  @throws Exception
   */
  public void testCalculateAreaOfGPSPolygonOnEarthInSquareMeters_2nd_Route() throws Exception {
    double[][] points = {
            {38.25304740874071,21.75644703234866},
            {38.25308110946615,21.763377860443143},
            {38.25078942453431,21.763399318115262},
            {38.25090738066984,21.756640151397733},
            {38.25197740258244,21.75892539347842}
    };

    runTestWith(points, 122755);
  }

/**
   * ------------------------------------------------------
   * |                         ........................./
   * |     .................../
   * |    /
   * |    \...................
   * |                        \........................
   * |                                                 \...
   * ------------------------------------------------------
   *
   *
   * Tests that the enclosed area is the same as in:
   *
   * http://www.mapdevelopers.com/area_finder.php?&points=%5B%5B38.252845204059824%2C21.763313487426785%5D%2C%5B38.25303055837213%2C21.755867675201443%5D%2C%5B38.25072202094234%2C21.755803302185086%5D%2C%5B38.25062091543717%2C21.76294870700076%5D%2C%5B38.25183417221606%2C21.75692982997134%5D%5D
   *
   *
   *  @throws Exception
   */
  public void testCalculateAreaOfGPSPolygonOnEarthInSquareMeters_3rd_Route() throws Exception {
    double[][] points = {
            {38.252845204059824,21.763313487426785},
            {38.25303055837213,21.755867675201443},
            {38.25072202094234,21.755803302185086},
            {38.25062091543717,21.76294870700076},
            {38.25183417221606,21.75692982997134}
    };

    runTestWith(points, 93912);
  }

  private void runTestWith(double[][] points, int expectedArea) {
    List<GeoUtils.GPSCoordinates> gpsCoordinatesList = new ArrayList<GeoUtils.GPSCoordinates>();
    for (double[] point : points) {
      gpsCoordinatesList.add(new GeoUtils.GPSCoordinates(point[0], point[1]));
    }

    double area = GeoUtils.calculateAreaOfGPSPolygonOnEarthInSquareMeters(gpsCoordinatesList);

    System.out.println("Area in m2: " + area);

    assertTrue((int)Math.rint(area) == expectedArea);
  }
}