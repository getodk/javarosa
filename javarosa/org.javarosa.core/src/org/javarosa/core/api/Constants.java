package org.javarosa.core.api;

/**
 * This file is a set of constants for the JavaRosa Core platform.
 * 
 * It should contain constants only pertaining to core usage of JavaRosa's
 * core classes, including Module and Shell return codes, and indexes for
 * the core context.
 * 
 * @author Clayton Sims
 *
 */
public class Constants {
	/**
	 * Activity Return Codes
	 */
	final public static String ACTIVITY_CANCEL = "activity_cancel";
	final public static String ACTIVITY_COMPLETE  = "activity_complete";
	final public static String ACTIVITY_ERROR  = "activity_error";
	final public static String ACTIVITY_SUSPEND  = "activity_suspend";
	final public static String ACTIVITY_NEEDS_RESOLUTION  = "activity_needs_resolution";
	
	
	final public static String USER_KEY = "username";
	final public static String USER_ID_KEY = "userid";
	
	/**
	 * Return arg codes
	 */
	final public static String ACTIVITY_LAUNCH_KEY = "activity_to_launch";
	final public static String RETURN_ARG_KEY = "return_arg";
	final public static String RETURN_ARG_TYPE_KEY = "return_arg_type";
	final public static String RETURN_ARG_TYPE_DATA_POINTER = "data_pointer";
	final public static String RETURN_ARG_TYPE_DATA_POINTER_LIST = "data_pointer_list";	
	
	/**
	 * Activity codes
	 */
	final public static String ACTIVITY_TYPE_GET_IMAGES = "get_images";
	final public static String ACTIVITY_TYPE_GET_AUDIO = "get_audio";
	
	/**
	 * Service codes
	 */
	final public static String TRANSPORT_MANAGER = "Transport Manager";
	final public static String PROPERTY_MANAGER = "Property Manager";

}
