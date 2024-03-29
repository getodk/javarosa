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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.IOException;
import java.util.Map;
import org.javarosa.core.model.data.UncastData;
import org.javarosa.core.model.instance.TreeElement;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeojsonFeature {
    public static final String GEOMETRY_CHILD_NAME = "geometry";
    private String type;
    private GeojsonGeometry geometry;
    private Map<String, String> properties;

    private String id;

    public TreeElement toTreeElement(int multiplicity) throws IOException {
        if (!type.equals("Feature")) {
            throw new IOException("Item of type " + type + " found but expected item of type Feature");
        }

        TreeElement item = new TreeElement("item", multiplicity);

        TreeElement geoField = new TreeElement(GEOMETRY_CHILD_NAME, 0);
        geoField.setValue(new UncastData(geometry.getOdkCoordinates()));
        item.addChild(geoField);

        if (properties != null) {
            for (String property : properties.keySet()) {
                TreeElement field = new TreeElement(property, 0);
                if (properties.get(property) != null) {
                    field.setValue(new UncastData(properties.get(property)));
                } else {
                    field.setValue(new UncastData(""));
                }
                item.addChild(field);
            }
        }

        if (id != null) {
            if (!item.getChildrenWithName("id").isEmpty()) {
                item.getChildrenWithName("id").get(0).setValue(new UncastData(id));
            } else {
                TreeElement field = new TreeElement("id", 0);
                field.setValue(new UncastData(id));
                item.addChild(field);
            }
        }

        return item;
    }
}
