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

package org.javarosa.core.services.storage;

import java.util.HashMap;

public abstract class EntityFilter<E> {
	
	public static final int PREFILTER_EXCLUDE = -1;
	public static final int PREFILTER_INCLUDE = 1;
	public static final int PREFILTER_FILTER = 0;
	
	/**
	 * filter based just on ID and metadata (metadata not supported yet!! will always be 'null', currently)
	 * 
	 * @param id
	 * @param metaData
	 * @return if PREFILTER_INCLUDE, record will be included, matches() not called
	 *         if PREFILTER_EXCLUDE, record will be excluded, matches() not called
	 *         if PREFILTER_FILTER, matches() will be called and record will be included or excluded based on return value
	 */
	public int preFilter (int id, HashMap<String, Object> metaData) {
		return PREFILTER_FILTER;
	}
	
	public abstract boolean matches(E e);
}
