/*
 * Copyright (C) 2009 JavaRosa
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

package org.javarosa.core.services.properties.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.services.properties.Property;
import org.javarosa.core.services.storage.utilities.MetaDataObject;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * PropertyMetaData is the Meta Data object used for Property records in the RMS
 * @author Clayton Sims
 *
 */
public class PropertyMetaData extends MetaDataObject {
    private String name = "";

    public PropertyMetaData()
    {
    }

    /**
     * Creates a Property Meta Data object for a given property
     * @param property The property whose meta data will be captured
     */
    public PropertyMetaData(Property  property)
    {
        setMetaDataParameters(property);
    }

    /** (non-Javadoc)
     *  @see org.javarosa.clforms.storage.MetaDataTyped#setMetaDataParemeters(Object)
     */
    public void setMetaDataParameters(Object originalObject) {
        Property property =(Property)originalObject;
        this.name = property.name;
    }

    /**
     * Gets the name of the property this object represents
     * @return The name of the property that this object is the meta data for
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of thie property that this object represents
     * @param name The name of the property that this object is the meta data for
     */
    public void setName(String name) {
        this.name = name;
    }

    /** (non-Javadoc)
     *  @see org.javarosa.core.util.externalizable.clforms.storage.Externalizable#readExternal(DataInputStream)
     */
    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
        this.name = in.readUTF();
    }

    /** (non-Javadoc)
     *  @see org.javarosa.core.util.externalizable.clforms.storage.Externalizable#writeExternal(DataOutputStream)
     */
    public void writeExternal(DataOutputStream out) throws IOException
    {
        out.writeUTF(this.name);
    }
}
