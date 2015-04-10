package org.javarosa.core.model.osm;

import java.util.ArrayList;
import java.util.List;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapList;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 *	Structure defining possible tag values that can be selected.
 *  Created by Nicholas Hallahan nhallahan@spatialdev.com
 */
public class OSMTagItem implements Externalizable {
	public String label;
	public String value;

	public void readExternal(DataInputStream dis, PrototypeFactory pf) throws IOException, DeserializationException {
		label = ExtUtil.nullIfEmpty(ExtUtil.readString(dis));
		value = ExtUtil.nullIfEmpty(ExtUtil.readString(dis));
	}

	public void writeExternal(DataOutputStream dos) throws IOException {
		ExtUtil.writeString(dos, ExtUtil.emptyIfNull(label));
		ExtUtil.writeString(dos, ExtUtil.emptyIfNull(value));
	}
}
