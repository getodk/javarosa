/*
 * Copyright (C) 2009 JavaRosa-Core Project
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

package org.javarosa.core.api;

/**
 * IView provides an interface for UI screens that can
 * be displayed by a shell.  
 * 
 * @author Clayton Sims
 *
 */
public interface IView {
	/**
	 * @return A platform specific view object. NOTE: this must be the same type 
	 * as expected by the Display it is passed to or a runtime exception will be
	 * thrown. This would be handled by Generics if that could be handled by j2me. 
	 */
	public Object getScreenObject();
}
