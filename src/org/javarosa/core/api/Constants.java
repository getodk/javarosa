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

package org.javarosa.core.api;

/**
 * This file is a set of constants for the JavaRosa Core platform.
 * 
 * It should contain constants only pertaining to core usage of JavaRosa's core
 * classes, including Module and Shell return codes, and indexes for the core
 * context.
 * 
 * @author Clayton Sims
 * 
 */
public class Constants {
	/**
	 * Activity Return Codes
	 */
	public static final String ACTIVITY_CANCEL = "activity_cancel";
	public static final String ACTIVITY_COMPLETE = "activity_complete";
	public static final String ACTIVITY_ERROR = "activity_error";
	public static final String ACTIVITY_SUSPEND = "activity_suspend";
	public static final String ACTIVITY_NEEDS_RESOLUTION = "activity_needs_resolution";

	public static final String USER_KEY = "username";
	public static final String USER_ID_KEY = "userid";

	/**
	 * Return arg codes
	 */
	public static final String ACTIVITY_LAUNCH_KEY = "activity_to_launch";
	public static final String RETURN_ARG_KEY = "return_arg";
	public static final String RETURN_ARG_TYPE_KEY = "return_arg_type";
	public static final String RETURN_ARG_TYPE_DATA_POINTER = "data_pointer";
	public static final String RETURN_ARG_TYPE_DATA_POINTER_LIST = "data_pointer_list";

	/**
	 * Activity codes
	 */
	public static final String ACTIVITY_TYPE_GET_IMAGES = "get_images";
	public static final String ACTIVITY_TYPE_GET_AUDIO = "get_audio";

	/**
	 * Service codes
	 */
	public static final String TRANSPORT_MANAGER = "Transport Manager";
	public static final String PROPERTY_MANAGER = "Property Manager";

}
