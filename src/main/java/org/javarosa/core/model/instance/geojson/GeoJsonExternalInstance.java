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
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.xform.parse.ExternalInstanceParser;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

public class GeoJsonExternalInstance implements ExternalInstanceParser.FileInstanceParser {

    public TreeElement parse(@NotNull String instanceId, @NotNull String path) throws IOException {
        final TreeElement root = new TreeElement("root", 0);
        root.setInstanceName(instanceId);

        ObjectMapper objectMapper = new ObjectMapper();
        try (JsonParser jsonParser = objectMapper.getFactory().createParser(new FileInputStream(path))) {
            if (jsonParser.nextToken() != JsonToken.START_OBJECT) {
                throw new IOException("GeoJSON file must contain a top-level Object");
            }

            boolean typeValidated = false;
            boolean featuresIdentified = false;

            while (jsonParser.hasCurrentToken() && !(typeValidated && featuresIdentified)) {
                String propertyName = jsonParser.nextFieldName();
                JsonToken propertyType = jsonParser.nextValue();

                if (Objects.equals(propertyName, "type") && propertyType == JsonToken.VALUE_STRING && Objects.equals(jsonParser.getValueAsString(), "FeatureCollection")) {
                    typeValidated = true;
                } else if (Objects.equals(propertyName, "features") && propertyType.equals(JsonToken.START_ARRAY)) {
                    int multiplicity = 0;
                    while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                        GeojsonFeature feature = objectMapper.readValue(jsonParser, GeojsonFeature.class);
                        root.addChild(feature.toTreeElement(multiplicity));
                        multiplicity++;
                    }
                    featuresIdentified = true;
                } else {
                    jsonParser.skipChildren();
                }
            }

            if (!typeValidated) {
                throw new IOException("GeoJSON file must contain a top-level FeatureCollection");
            }

            if (!featuresIdentified) {
                throw new IOException("GeoJSON FeatureCollection must contain an array of features");
            }
        }

        return root;
    }

    @Override
    public boolean isSupported(String instanceId, String instanceSrc) {
        return instanceSrc.endsWith("geojson");
    }
}
