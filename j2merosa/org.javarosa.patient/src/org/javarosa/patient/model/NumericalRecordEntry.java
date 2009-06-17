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

package org.javarosa.patient.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class NumericalRecordEntry implements IRecordEntry, Externalizable {
	Date recordDate;
	
	//Sticking with Int for now
	int recordValue;
	
	public NumericalRecordEntry(Date recordDate, int recordValue) {
		this.recordValue = recordValue;
		this.recordDate = recordDate;
	}
	
	public Date getEntryDate() {
		return recordDate;
	}
	
	public int getRecordValue() {
		return recordValue;
	}

	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		recordValue = in.readInt();
		
		recordDate = new Date(in.readLong());
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		out.writeInt(recordValue);
		
		out.writeLong(recordDate.getTime());
	}
	
	
}
