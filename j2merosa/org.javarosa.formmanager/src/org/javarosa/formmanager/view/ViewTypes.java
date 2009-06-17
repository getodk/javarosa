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

package org.javarosa.formmanager.view;

/**
 * This static class just has a list of unique ids for all
 * available views.
 * @author Brian DeRenzi
 *
 */
public class ViewTypes {
	public static final int LOGIN_SCREEN = 1;
	public static final int FORM_LIST = 2;
	public static final int CONTROLLER = 3;

	/**
	 *  Prohibit anyone trying to instantiate this class.
	 */
	protected ViewTypes() {}
}
