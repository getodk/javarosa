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
