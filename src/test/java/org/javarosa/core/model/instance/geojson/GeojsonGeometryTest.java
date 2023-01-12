package org.javarosa.core.model.instance.geojson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class GeojsonGeometryTest {

    @Test
    public void whenPolygonHasMultipleCoordinates_onlyUsesFirst() throws Exception {
        String json = "{" +
            "\"type\": \"Polygon\"," +
            "\"coordinates\": [" +
                "[[1, 2], [1, 2]]," +
                "[[3, 4], [3, 4]]" +
            "]" +
        "}";

        ObjectMapper objectMapper = new ObjectMapper();
        GeojsonGeometry geojsonGeometry = objectMapper.readValue(json, GeojsonGeometry.class);
        assertThat(geojsonGeometry.getOdkCoordinates(), equalTo("2 1 0 0; 2 1 0 0"));
    }

    @Test
    public void whenPolygonCoordinatesAreEmpty_returnsEmptyOdkCoordinates() throws Exception {
        String json = "{" +
            "\"type\": \"Polygon\"," +
            "\"coordinates\": []" +
        "}";

        ObjectMapper objectMapper = new ObjectMapper();
        GeojsonGeometry geojsonGeometry = objectMapper.readValue(json, GeojsonGeometry.class);
        assertThat(geojsonGeometry.getOdkCoordinates(), equalTo(""));
    }
}