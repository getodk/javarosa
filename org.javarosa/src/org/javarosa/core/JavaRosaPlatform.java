package org.javarosa.core;

import java.util.Hashtable;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

import org.javarosa.clforms.Controller;
import org.javarosa.clforms.storage.ModelRMSUtility;
import org.javarosa.clforms.storage.RMSManager;
import org.javarosa.clforms.storage.XFormRMSUtility;
import org.javarosa.shell.IShell;

/**
 * The main JavaRosaPlatform.  This is a singleton class, ensuring that there is only one.
 * The design pattern came from:
 * http://en.wikipedia.org/wiki/Singleton_pattern
 * @author Brian DeRenzi
 *
 */
public class JavaRosaPlatform {
	protected JavaRosaPlatform() {}

	public static final String XFORM_RMS = "XFORM_RMS_NEW";
    public static final String MODEL_RMS = "MODEL_RMS_NEW";


	private Display display;
	private IShell	currentShell;

	private RMSManager		rmsManager;

	// Core JavaRosa classes for dealing with XForms
	private XFormRMSUtility xformRMS;
    private ModelRMSUtility modelRMS;

    private static Hashtable globals;

    // Should there be one global form controller??? -- BWD
    private Controller		formController;

    /**
     * Setup the core JavaRosa XForms classes
     */
    protected void initXForms() {
    	this.rmsManager = new RMSManager();
    	this.xformRMS = new XFormRMSUtility(JavaRosaPlatform.XFORM_RMS);
		this.modelRMS = new ModelRMSUtility(JavaRosaPlatform.MODEL_RMS);

		// For now let's add the dummy form.
		if (xformRMS.getNumberOfRecords() == 0)
		{
		    System.out.println("***NUMBER OF RECORDS : " + xformRMS.getNumberOfRecords());
		    this.xformRMS.writeDummy();
		}

		// The RMS manager should keep track of all open RMS?
		this.rmsManager.registerRMSUtility(xformRMS);
		this.rmsManager.registerRMSUtility(modelRMS);
    }

	/**
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.instance , not before.
     */
	private static class SingletonHolder {
		private final static JavaRosaPlatform instance = new JavaRosaPlatform();
	}

	/**
	 * Initialize the platform.  Setup things like the RMS for the forms, the transport manager...
	 */
	public static void initialize() {
		SingletonHolder.instance.initXForms();
	}

	/**
	 * Should be called by the midlet to set the display
	 * @param d - the j2me disply
	 */
	public static void setDisplay(Display d) {
		SingletonHolder.instance.display = d;
	}

	/**
	 * @return the display
	 */
	public static Display getDisplay() {
		return SingletonHolder.instance.display;
	}

	/**
	 * Set the shell that should be displayed
	 * @param shell
	 */
	public static void setShell(IShell shell) {
		SingletonHolder.instance.currentShell = shell;
	}

	/**
	 * Display the view that is passed in.
	 * @param view
	 */
	public static void showView(Displayable view) {
		SingletonHolder.instance.display.setCurrent(view);
	}

	public static XFormRMSUtility getXFormRMS() {
		return SingletonHolder.instance.xformRMS;
	}

	public static ModelRMSUtility getModelRMS() {
		return SingletonHolder.instance.modelRMS;
	}


	public static Object getFromGlobals(String key) {
		return globals.get(key);
	}

	public static  void addToGlobals(String key, Object obj) {
		globals.put(key, obj);
	}
}
