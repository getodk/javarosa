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

import java.util.Vector;

import org.javarosa.core.util.externalizable.Externalizable;

/**
 * IPatientRecord is a storage location for historical
 * patient record entries. 
 * 
 * @author Clayton Sims
 *
 */
public interface IPatientRecord extends Externalizable {
	
	/**
	 * Returns a filtered set of historical record entries
	 * 
	 * @param selector The Selector string to be used to
	 * filter the entries
	 * @return a subset of IRecordEntry objects which correspond
	 * to the set of data specified by the selector
	 */
	public Vector getHistoricalRecords(String selector);
}
