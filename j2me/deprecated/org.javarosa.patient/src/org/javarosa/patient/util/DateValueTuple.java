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

package org.javarosa.patient.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * DateValueTuple is an externalizable data object that represents
 * an integer measurement that is associated with the date that the
 * measurement was taken.
 * 
 * @author Clayton Sims
 *
 */
public class DateValueTuple implements Externalizable {
	/** The date of the measurement */
	public Date date;
	/** The measurement's value */
	public int value;
	
	public DateValueTuple(){
		
	}
	
	public DateValueTuple(Date date, int value) {
		this.date = date;
		this.value = value;
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		date = (Date)ExtUtil.read(in, new ExtWrapNullable(Date.class));
		value = in.readInt();
		
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	 public void writeExternal(DataOutputStream out) throws IOException {
		 ExtUtil.write(out, new ExtWrapNullable(date));
		 out.writeInt(value);
	 }
	
	 public DateValueTuple clone() {
		 return new DateValueTuple(date,value);
	 }
}
