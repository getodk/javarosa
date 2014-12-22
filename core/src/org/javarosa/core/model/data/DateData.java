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

package org.javarosa.core.model.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * A response to a question requesting a Date Value
 * @author Drew Roos
 *
 */
public class DateData implements IAnswerData {
	Date d;
	boolean init = false;

	/**
	 * Empty Constructor, necessary for dynamic construction during deserialization.
	 * Shouldn't be used otherwise.
	 */
	public DateData() {

	}

	public DateData (Date d) {
		setValue(d);
	}

	private void init() {
		if(!init) {
			d = DateUtils.roundDate(d);
			init = true;
		}
	}

    @Override
	public IAnswerData clone () {
		init();
		return new DateData(new Date(d.getTime()));
	}

    @Override
	public void setValue (Object o) {
		//Should not ever be possible to set this to a null value
		if(o == null) {
			throw new NullPointerException("Attempt to set an IAnswerData class to null.");
		}
		d = (Date)o;
		init = false;
	}

    @Override
	public Object getValue () {
		init();
		return new Date(d.getTime());
	}

    @Override
	public String getDisplayText () {
		init();
		return DateUtils.formatDate(d, DateUtils.FORMAT_HUMAN_READABLE_SHORT);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#readExternal(java.io.DataInputStream)
	 */
    @Override
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		init();
		setValue(ExtUtil.readDate(in));
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
    @Override
	public void writeExternal(DataOutputStream out) throws IOException {
		init();
		ExtUtil.writeDate(out, d);
	}

    @Override
	public UncastData uncast() {
		init();
		return new UncastData(DateUtils.formatDate(d, DateUtils.FORMAT_ISO8601));
	}

    @Override
	public DateData cast(UncastData data) throws IllegalArgumentException {
		Date ret = DateUtils.parseDate(data.value);
		if(ret != null) {
			return new DateData(ret);
		}

		throw new IllegalArgumentException("Invalid cast of data [" + data.value + "] to type Date");
	}
}
