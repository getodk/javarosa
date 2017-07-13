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
 *	Structure for OSM Tag elements in XForm.
 *  Created by Nicholas Hallahan nhallahan@spatialdev.com
 */
public class OSMTag implements Externalizable {
	public String key;
	public String label;
	public List<OSMTagItem> items = new ArrayList<OSMTagItem>();

	public void readExternal(DataInputStream dis, PrototypeFactory pf) throws IOException, DeserializationException {
		key = ExtUtil.nullIfEmpty(ExtUtil.readString(dis));
		label = ExtUtil.nullIfEmpty(ExtUtil.readString(dis));
		items = (List<OSMTagItem>) ExtUtil.nullIfEmpty((List<OSMTagItem>)ExtUtil.read(dis, new ExtWrapList(OSMTagItem.class), pf));
	}

	public void writeExternal(DataOutputStream dos) throws IOException {
		ExtUtil.writeString(dos, ExtUtil.emptyIfNull(key));
		ExtUtil.writeString(dos, ExtUtil.emptyIfNull(label));
		ExtUtil.write(dos, new ExtWrapList(ExtUtil.emptyIfNull(items)));
	}
}
