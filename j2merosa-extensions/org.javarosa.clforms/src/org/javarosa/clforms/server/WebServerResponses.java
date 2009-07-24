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

package org.javarosa.clforms.server;

public class WebServerResponses {
//	If some problem occurs and service not able to send list
	final public static String GET_LIST_ERROR = "300"; 	
//	If no surveys available then
	final public static String GET_LIST_NO_SURVEY ="301";
//	If some problem occurs and service not able to send the xform then 
	final public static String GET_FORM_ERROR = "400";
//	If survey does not exists for the particular name/ID (should be an ID or some form of unique identifier)
	final public static String GET_FORM_NO_EXISTS ="401";
	
//	When successfully data saved in database
	final public static String DATA_SAVED = "500";
//	If some problem occurs and service not able to save the data 
	final public static String DATA_NO_SAVE = "501";
	
//	successful authentication
	final public static String LOGIN_SUCCESS = "600";
//	Invalid Email-ID or Password
	final public static String INVALID_ID_PWD = "601";
//	Server connection problem
	final public static String SERVER_CON_ERROR = "602";
}
