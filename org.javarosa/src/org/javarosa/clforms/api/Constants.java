package org.javarosa.clforms.api;

public class Constants {

	//return types
	public static final int RETURN_INTEGER = 1;
	public static final int RETURN_STRING = 2;
	public static final int RETURN_DATE = 3;
	public static final int RETURN_SELECT1 = 4;
	public static final int RETURN_SELECT_MULTI = 5;
	public static final int RETURN_BOOLEAN = 6;
	public static final int RETURN_OUTPUT = 8;
	public static final int RETURN_TRIGGER = 9;
	public static final int RETURN_OUTPUT_GRAPH = 10;

	//form control types
	public static final int INPUT = 1;
	public static final int SELECT1 = 2;
	public static final int SELECT = 3;
	public static final int TEXTAREA = 4;
	public static final int SECRET = 5;
	public static final int OUTPUT = 6;
	public static final int UPLOAD = 7;
	public static final int RANGE = 8;
	public static final int SUBMIT = 9;

	// Dimagi form control types
	public static final int TEXTBOX = 10;
	public static final int DROPDOWN = 11;

	public static final int TRIGGER = 12;
	public static final int SELECT1_MINIMAL_START = 13;
	public static final int SELECT1_MINIMAL_END = 14;
	public static final int OUTPUT_GRAPH = 15;
	
	// View Types
    public static final String VIEW_CHATTERBOX = "v_chatterbox";
    public static final String VIEW_CUSTOMCHAT = "v_customchatter";
    public static final String VIEW_CLFORMS = "v_clforms";

    // Get Forms Methods
    public static final String GETFORMS_AUTOHTTP = "Autoupdate From GetURL";
    public static final String GETFORMS_EVGETME = "Using evGetME";
    public static final String GETFORMS_BLUETOOTH= "From BlueTooth peer";
    public static final String GETFORMS_FILE = "From file system";

    // The property responsible for holding potential values for URL
    public static final String POST_URL_LIST = "post_url_list";
    
    // server URLs
   public static final String POST_URL = "http://update.cell-life.org/save_dump.php";
 //   public static final String POST_URL = "http://demo.gatherdata.org:8180/gather/xforms.html";
    //public static final String GET_URL ="http://update.cell-life.org/forms/AllDataTypes.xhtml";
    public static final String GET_URL ="http://127.0.0.1/form.xhtml";

    // does the file connection API (JSR 75) exist?
	public static boolean FILE_CONNECTION = false;
	public static int MODE;

	//USERTYPEs
	public static final String ADMINUSER = "admin";
	public static final String USERTYPE1 = "TLP";
	public static final String STANDARD = "standard";

}
