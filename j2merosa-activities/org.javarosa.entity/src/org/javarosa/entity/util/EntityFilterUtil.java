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

/**
 * 
 */
package org.javarosa.entity.util;

import org.javarosa.entity.model.IEntity;

/**
 * Entity Filter static utility methods, because Java won't allow them to be defined
 * in the interface
 * 
 * @author Clayton Sims
 * @date May 29, 2009 
 *
 */
public class EntityFilterUtil {
	public static IEntityFilter stack(final IEntityFilter one, final IEntityFilter two) {
		 return new IEntityFilter() {
			public boolean isPermitted(IEntity entity) {
				return one.isPermitted(entity) && two.isPermitted(entity);
			}
		 };
	}
}
