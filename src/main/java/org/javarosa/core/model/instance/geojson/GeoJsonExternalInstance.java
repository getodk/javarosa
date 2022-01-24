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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.javarosa.core.model.instance.TreeElement;

public class GeoJsonExternalInstance {
    public static TreeElement parse(String instanceId, String path) throws IOException {
        return parse(instanceId, new FileInputStream(path));
    }

    protected static TreeElement parse(String instanceId, InputStream geojsonStream) throws IOException {
        final TreeElement root = new TreeElement("root", 0);
        root.setInstanceName(instanceId);

        ObjectMapper objectMapper = new ObjectMapper();
        try (JsonParser jsonParser = objectMapper.getFactory().createParser(geojsonStream)) {
            validatePreamble(jsonParser);

            int multiplicity = 0;
            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                GeojsonFeature feature = objectMapper.readValue(jsonParser, GeojsonFeature.class);

                root.addChild(feature.toTreeElement(multiplicity));
                multiplicity++;
            }
        }

        return root;
    }

    private static void validatePreamble(JsonParser jsonParser) throws IOException {
        if (jsonParser.nextToken() != JsonToken.START_OBJECT) {
            throw new IOException("GeoJSON file must contain a top-level Object");
        }

        jsonParser.nextToken();
        String propertyName = jsonParser.getCurrentName();
        jsonParser.nextToken();
        if (!(propertyName.equals("type") && jsonParser.getValueAsString().equals("FeatureCollection"))) {
            throw new IOException("GeoJSON file must contain a top-level FeatureCollection");
        }

        jsonParser.nextToken();
        propertyName = jsonParser.getCurrentName();

        if (!(propertyName.equals("features") && jsonParser.nextToken() == JsonToken.START_ARRAY)) {
            throw new IOException("GeoJSON FeatureCollection must contain an array of features");
        }
    }
}
