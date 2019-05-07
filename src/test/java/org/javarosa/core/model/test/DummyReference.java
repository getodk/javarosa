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

package org.javarosa.core.model.test;

import org.javarosa.core.model.IDataReference;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Dummy reference for testing purposes.
 * 
 * @author Clayton Sims
 *
 */
public class DummyReference implements IDataReference {
    String ref = "";

    public DummyReference() {
        super();
    }

    public Object getReference() {
        return ref;
    }

    public void setReference(Object reference) {
        ref = (String)reference;

    }

    public void readExternal(DataInputStream in, PrototypeFactory pf)
            throws IOException, DeserializationException {
        ref = in.readUTF();

    }

    public void writeExternal(DataOutputStream out)
            throws IOException {
        out.writeUTF(ref);
    }

    public boolean equals(Object o) {
        if(!(o instanceof DummyReference)) {
            return false;
        } else {
            return ((DummyReference)o).ref.equals(this.ref);
        }
    }
}
