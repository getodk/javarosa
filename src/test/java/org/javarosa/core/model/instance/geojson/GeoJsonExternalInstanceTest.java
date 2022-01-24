/*
 * Copyright 2022 ODK
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

package org.javarosa.core.model.instance.geojson;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.javarosa.core.model.instance.TreeElement;
import org.junit.Test;

public class GeoJsonExternalInstanceTest {
    @Test
    public void parse_throwsException_ifTopLevelElementNotFeatureCollection() {
        try {
            GeoJsonExternalInstance.parse("id", new ByteArrayInputStream(GEOMETRY_COLLECTION_GEOJSON.getBytes(StandardCharsets.UTF_8)));
            fail("Exception expected");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void parse_parsesMultipleFeatures() throws IOException {
        TreeElement featureCollection = GeoJsonExternalInstance.parse("id", new ByteArrayInputStream(FEATURE_COLLECTION_GEOJSON.getBytes(StandardCharsets.UTF_8)));
        assertThat(featureCollection.getNumChildren(), is(2));
    }

    @Test
    public void parse_addsGeometryAsChild() throws IOException {
        TreeElement featureCollection = GeoJsonExternalInstance.parse("id", new ByteArrayInputStream(FEATURE_COLLECTION_GEOJSON.getBytes(StandardCharsets.UTF_8)));
        assertThat(featureCollection.getChildAt(0).getChild("geometry", 0).getValue().getValue(), is("102 0.5 0 0"));
        assertThat(featureCollection.getChildAt(1).getChild("geometry", 0).getValue().getValue(), is("104 0.5 0 0"));
    }

    @Test
    public void parse_addsAllOtherPropertiesAsChildren() throws IOException {
        TreeElement featureCollection = GeoJsonExternalInstance.parse("id", new ByteArrayInputStream(FEATURE_COLLECTION_GEOJSON.getBytes(StandardCharsets.UTF_8)));
        assertThat(featureCollection.getChildAt(0).getNumChildren(), is(4));
        assertThat(featureCollection.getChildAt(0).getChild("name", 0).getValue().getValue(), is("My cool point"));

        assertThat(featureCollection.getChildAt(1).getNumChildren(), is(5));
        assertThat(featureCollection.getChildAt(1).getChild("special-property", 0).getValue().getValue(), is("special value"));
    }

    @Test
    public void getOdkCoordinates_convertsPointGeoJsonGeometry() throws IOException {
        String point = "{ \"type\": \"Point\", \"coordinates\": [ 102, 0.5 ] }";
        ObjectMapper objectMapper = new ObjectMapper();
        GeojsonGeometry geometry = objectMapper.readValue(point, GeojsonGeometry.class);
        assertThat(geometry.getOdkCoordinates(), is("102 0.5 0 0"));
    }

    @Test
    public void getOdkCoordinates_throwsException_ifGeometryNotPoint() throws IOException {
        String notPoint = "{\n" +
            "               \"type\": \"LineString\",\n" +
            "               \"coordinates\": [102.0, 0.0]\n" +
            "           }";
        ObjectMapper objectMapper = new ObjectMapper();
        GeojsonGeometry geometry = objectMapper.readValue(notPoint, GeojsonGeometry.class);

        try {
            geometry.getOdkCoordinates();
            fail("Exception expected");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    private static final String GEOMETRY_COLLECTION_GEOJSON = "{\n" +
        "    \"geometries\": [\n" +
        "        {\n" +
        "            \"coordinates\": [\n" +
        "                [\n" +
        "                    10.0,\n" +
        "                    11.2\n" +
        "                ],\n" +
        "                [\n" +
        "                    10.5,\n" +
        "                    11.9\n" +
        "                ]\n" +
        "            ],\n" +
        "            \"type\": \"Linestring\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"coordinates\": [\n" +
        "                10.0,\n" +
        "                20.0\n" +
        "            ],\n" +
        "            \"type\": \"Point\"\n" +
        "        }\n" +
        "    ],\n" +
        "    \"type\": \"GeometryCollection\"\n" +
        "}";

    private static final String FEATURE_COLLECTION_GEOJSON = "{\n" +
        "    \"type\": \"FeatureCollection\",\n" +
        "    \"features\": [\n" +
        "        {\n" +
        "            \"type\": \"Feature\",\n" +
        "            \"geometry\": {\n" +
        "                \"type\": \"Point\",\n" +
        "                \"coordinates\": [\n" +
        "                    102,\n" +
        "                    0.5\n" +
        "                ]\n" +
        "            },\n" +
        "            \"properties\": {\n" +
        "                \"id\": \"fs87b\",\n" +
        "                \"name\": \"My cool point\",\n" +
        "                \"foo\": \"bar\"\n" +
        "            }\n" +
        "        },\n" +
        "        {\n" +
        "            \"type\": \"Feature\",\n" +
        "            \"geometry\": {\n" +
        "                \"type\": \"Point\",\n" +
        "                \"coordinates\": [\n" +
        "                    104,\n" +
        "                    0.5\n" +
        "                ]\n" +
        "            },\n" +
        "            \"properties\": {\n" +
        "                \"id\": \"67abie\",\n" +
        "                \"name\": \"Your cool point\",\n" +
        "                \"foo\": \"quux\",\n" +
        "                \"special-property\": \"special value\"\n" +
        "            }\n" +
        "        }\n" +
        "    ]\n" +
        "}";
}
