package org.javarosa.core.model;

import java.util.TimeZone;

/**
 * Constants shared throught classes in the containing package.
 *
 * @version ,
 */
public class Constants {
	
	/** Empty strig representation */
	public static final String EMPTY_STRING = "";
	
	/** Index for no selection */
	public static final byte NO_SELECTION = -1;
	
	/** ID not set numeric value */
	public static final byte NULL_ID = -1;
	
	/** Operator not set numeric value */
	public static final byte OPERATOR_NULL = 0;
	
	/** Operator Equal */
	public static final byte OPERATOR_EQUAL = 1;
	
	/** Operator Not Equal */
	public static final byte OPERATOR_NOT_EQUAL = 2;
	
	/** Operator Greater */
	public static final byte OPERATOR_GREATER = 3;
	
	/** Operator Greater of Equal */
	public static final byte OPERATOR_GREATER_EQUAL = 4;
	
	/** Operator Less */
	public static final byte OPERATOR_LESS = 5;
	
	/** Operator Less than */
	public static final byte OPERATOR_LESS_EQUAL = 6;
	
	/** No rule action specified */
	public static final byte ACTION_NONE = 0;
	
	/** Rule action to hide questions */
	public static final byte ACTION_HIDE= 1;
	
	/** Rule action to show questions */
	public static final byte ACTION_SHOW = 2;
	
	/** Rule action to disable questions */
	public static final byte ACTION_DISABLE = 3;
	
	/** Rule action to enable questions */
	public static final byte ACTION_ENABLE = 4;
	
	/** Rule action to make a question mandatory */
	public static final byte ACTION_MAKE_MANDATORY = 5;
	
	/** Rule action to make a question optional */
	public static final byte ACTION_MAKE_OPTIONAL = 6;
	
	/** Connection type not specified */
	public static final byte CONNECTION_NONE = 0;
	
	/** Infrared connection */
	public static final byte CONNECTION_INFRARED = 1;
	
	/** Bluetooth connection */
	public static final byte CONNECTION_BLUETOOTH = 2;
	
	/** Data cable connection. Can be USB or Serial */
	public static final byte CONNECTION_CABLE = 3;
	
	/** Over The Air or HTTP Connection */
	public static final byte CONNECTION_OTA = 4;
	
	public static final String NULLS_NOT_ALLOWED = "Nulls not allowed. Use empty string";
	
	/** The maximum number of characters for text input. */
	public static final int MAX_NUM_CHARS = 500;
	
	/** The default study id for those that dont deal with studies, they just have forms. */
	public static final byte DEFAULT_STUDY_ID = 1;
	
	/** The default time zone. */
	public static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getTimeZone("GMT");
}
