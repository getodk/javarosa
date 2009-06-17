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

package org.javarosa.core.services;

/**
 * A Service provides access to some external source of 
 * data. 
 * 
 * This interface should be more robustly expanded
 * to cover more common ground between services in the
 * future.
 * 
 * @author Clayton Sims
 *
 */
public interface IService {
	
	/**
	 * Gets the unique name for this service
	 * 
	 * @return A unique string identifying this service.
	 */
	public String getName();
}
