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
import java.io.IOException;
import java.util.ArrayList;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class GeojsonGeometry {
    private String type;
    private ArrayList<String> coordinates;

    public String getType() {
        return type;
    }

    public String getOdkCoordinates() throws IOException {
        if (!(getType().equals("Point") && coordinates.size() == 2)) {
            throw new IOException("Only Points are currently supported");
        }

        return coordinates.get(0) + " " + coordinates.get(1) + " 0 0";
    }
}