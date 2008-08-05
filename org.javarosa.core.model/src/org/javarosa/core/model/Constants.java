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
	public static final int NO_SELECTION = -1;
	
	/** ID not set to a value */
	public static final int NULL_ID = -1;
	
	/** ID not set numeric value */
	public static final String NULL_STRING_ID = "-1";
	
	/** Operator not set numeric value */
	public static final int OPERATOR_NULL = 0;
	
	/** Operator Equal */
	public static final int OPERATOR_EQUAL = 1;
	
	/** Operator Not Equal */
	public static final int OPERATOR_NOT_EQUAL = 2;
	
	/** Operator Greater */
	public static final int OPERATOR_GREATER = 3;
	
	/** Operator Greater of Equal */
	public static final int OPERATOR_GREATER_EQUAL = 4;
	
	/** Operator Less */
	public static final int OPERATOR_LESS = 5;
	
	/** Operator Less than */
	public static final int OPERATOR_LESS_EQUAL = 6;
	
	/** No rule action specified */
	public static final int ACTION_NONE = 0;
	
	/** Rule action to hide questions */
	public static final int ACTION_HIDE= 1;
	
	/** Rule action to show questions */
	public static final int ACTION_SHOW = 2;
	
	/** Rule action to disable questions */
	public static final int ACTION_DISABLE = 3;
	
	/** Rule action to enable questions */
	public static final int ACTION_ENABLE = 4;
	
	/** Rule action to make a question mandatory */
	public static final int ACTION_MAKE_MANDATORY = 5;
	
	/** Rule action to make a question optional */
	public static final int ACTION_MAKE_OPTIONAL = 6;
	
	/** Connection type not specified */
	public static final int CONNECTION_NONE = 0;
	
	/** Infrared connection */
	public static final int CONNECTION_INFRARED = 1;
	
	/** Bluetooth connection */
	public static final int CONNECTION_BLUETOOTH = 2;
	
	/** Data cable connection. Can be USB or Serial */
	public static final int CONNECTION_CABLE = 3;
	
	/** Over The Air or HTTP Connection */
	public static final int CONNECTION_OTA = 4;
	
	public static final String NULLS_NOT_ALLOWED = "Nulls not allowed. Use empty string";
	
	/** The maximum number of characters for text input. */
	public static final int MAX_NUM_CHARS = 500;
	
	/** The default study id for those that dont deal with studies, they just have forms. */
	public static final int DEFAULT_STUDY_ID = 1;
	
	/** The default time zone. */
	public static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getTimeZone("GMT");

	public static final int DATATYPE_UNSUPPORTED = -1;
	public static final int DATATYPE_NULL = 0;  /* for controls that return no value */
	public static final int DATATYPE_TEXT = 1;	/** Text question type. */
	public static final int DATATYPE_INTEGER = 2;	/** Numeric question type. These are numbers without decimal points*/
	public static final int DATATYPE_DECIMAL = 3;	/** Decimal question type. These are numbers with decimals */
	public static final int DATATYPE_DATE = 4;	/** Date question type. This has only date component without time. */
	public static final int DATATYPE_TIME = 5;	/** Time question type. This has only time element without date*/
	public static final int DATATYPE_DATE_TIME = 6;	/** Date and Time question type. This has both the date and time components*/
	public static final int DATATYPE_LIST_EXCLUSIVE = 7;	/** This is a question with alist of options where not more than one option can be selected at a time. */
	public static final int DATATYPE_LIST_MULTIPLE = 8;	/** This is a question with alist of options where more than one option can be selected at a time. */
	public static final int DATATYPE_BOOLEAN = 9;	/** Question with true and false answers. */
	/** Question with repeat sets of questions. */
	public static final int DATATYPE_REPEAT = 10; //droos: not sure this is a 'question data type'
	
	public static final int CONTROL_UNTYPED = -1;
	public static final int CONTROL_INPUT = 1;
	public static final int CONTROL_SELECT_ONE = 2;
	public static final int CONTROL_SELECT_MULTI = 3;
	public static final int CONTROL_TEXTAREA = 4;
	public static final int CONTROL_SECRET = 5;
	public static final int CONTROL_RANGE = 6;
	public static final int CONTROL_UPLOAD = 7;
	public static final int CONTROL_SUBMIT = 8;
	public static final int CONTROL_TRIGGER = 9;
}
