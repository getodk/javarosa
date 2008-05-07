package org.javarosa.clforms;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import org.javarosa.chatscreen.ChatScreenForm;
import org.javarosa.clforms.api.Constants;
import org.javarosa.clforms.api.Form;
import org.javarosa.clforms.storage.ModelRMSUtility;
import org.javarosa.clforms.storage.XFormMetaData;
import org.javarosa.clforms.storage.XFormRMSUtility;
import org.javarosa.clforms.util.J2MEUtil;
import org.javarosa.clforms.view.FormViewScreen;
import org.javarosa.clforms.view.PromptScreen;
import org.javarosa.clforms.xml.XMLUtil;
//#if polish.usePolishGui
import org.javarosa.polishforms.ChatScreen;
//#endif
import org.javarosa.properties.JavaRosaPropertyRules;
import org.javarosa.properties.PropertyManager;
//#if polish.usePolishGui
import org.javarosa.properties.view.PropertiesScreen;
//#endif
import org.netbeans.microedition.lcdui.pda.FileBrowser;
import org.openmrs.transport.midp.TransportLayer;

import com.ev.evgetme.getMidlet;

import de.enough.polish.util.TextUtil;

public class TransportShell extends MIDlet implements CommandListener
{
    public final static String WORKING_DIRECTORY = "C:/predefgallery/predefphotos/";

    private final Command EXIT_CMD = new Command("Close", Command.EXIT, 2);
    private final Command OK_CMD = new Command("Ok", Command.OK, 1);
    private final Command BACK_CMD = new Command("Back", Command.BACK, 1);
    private XFormRMSUtility xformRMS;
    private ModelRMSUtility modelRMS;
    public static final int ALERT_TIMEOUT = 2000;
    private Display display;
    private Controller formController;
    private List selectFunction;
    private List availableXForms;
  //#if app.usefileconnections
    private FileBrowser fileBrowser;
    //#endif
    private boolean writeDummy = false;


    private ChatScreenForm customChatScreen = new ChatScreenForm();
    //#if polish.usePolishGui
    private ChatScreen chatScreen = new ChatScreen();
    //#endif
    private PromptScreen promptScreen = new PromptScreen();
    private FormViewScreen formViewScreen = new FormViewScreen();

	private FormList formList;

	private VisualXFormServer xformServer;

	private VisualXFormClient xformClient;

	//#if polish.usePolishGui
	PropertiesScreen propertyScreen;
	//#endif

	private ModelList modelList;

	//public MIDPLogger log;

	private TransportLayer transportLayer;

	private getMidlet getMidlet;


    public TransportShell()
    {
    	configureLogger();
    	//log.write("STARTING APP",MIDPLogger.DEBUG);

        try {

			String[] optionsMenu = {"Select XForms", "Review Completed Forms", "Get New Forms", "Submit Completed Forms"};

			// dead?
			this.selectFunction = new List("What do you want to do?", List.IMPLICIT, optionsMenu, null);
			this.selectFunction.addCommand(EXIT_CMD);
			this.selectFunction.addCommand(OK_CMD);
			selectFunction.setCommandListener(this);
			this.createAvailableXformsList();
			//through here?
			
			//log.write("PRE-configCntllr",MIDPLogger.DEBUG);
			configureController();
			loadProperties();
			//log.write("POST-configCntllr",MIDPLogger.DEBUG);
			PropertyManager.instance().setRules(new JavaRosaPropertyRules());
		} catch (Exception e) {
			//log.write(e.getMessage(),MIDPLogger.DEBUG);
			e.printStackTrace();
		}

        //log.write("END tshell.CONST()",MIDPLogger.DEBUG);
        System.out.println(System.getProperty("microedition.profiles"));
    }


	private void configureLogger() {

	   /* try {
	     log = new MIDPLogger(MIDPLogger.DEBUG,true,false);
	    }
	    catch (Exception e) {
	     System.out.println("Exception creating MIDPLogger");
	     e.printStackTrace();
	    }*/
	}

	private void navigateSelectFunctionList() {
		switch (this.selectFunction.getSelectedIndex())
		{
		case 0:
			displayFormList();
			break;
		case 1:
			displayModelList();
			break;
		case 2:
			displayAvailableXFormMethods();
			break;
		case 3:
			displayTransportLayer();
			break;
		}
	}

    public void displayTransportLayer() {
    	transportLayer = new TransportLayer(this);
	}

	public void displayFormList() {
    	this.formList = new FormList(this);
	}

	public FormList getFormList() {

    	this.formList = new FormList(this);
    	return this.formList;
	}

	public void displayModelList() {
    	this.modelList = new ModelList(this);
	}

	public void displayAvailableXFormMethods() {
    	Display.getDisplay(this).setCurrent(availableXForms);
	}

	public void editProperties() {
	    //#if polish.usePolishGui
	    propertyScreen = new PropertiesScreen();
	    propertyScreen.setCommandListener(this);
	    Display.getDisplay(this).setCurrent(propertyScreen);
	    //#endif
	}

	//dead?
	private void createAvailableXformsList() {
    	//"In file system",
    	String[] getNewFormsMenu = {"From File system", "BlueTooth:Receive an XForm", "From URL"};
        this.availableXForms = new List("Where do you want to look for Xforms?", List.IMPLICIT,getNewFormsMenu, null);
        this.availableXForms.setCommandListener(this);
        this.availableXForms.addCommand(BACK_CMD);
	}

    private void navigateAvailbleXformsList() {
    	switch (this.availableXForms.getSelectedIndex())
		{
		case 0:
			initServiceFileBrowser();
			//#if app.usefileconnections
			Display.getDisplay(this).setCurrent(this.fileBrowser);
			//#endif
			break;
		case 1:
			this.xformClient = new VisualXFormClient(this);
			break;
		case 2:
			callEvGetMe();
			break;
		}
	}

    public void getNewFormsByTransportPropertySetting() {
    	String method = PropertyManager.instance().getProperty("GetFormsMethod");


    	if (TextUtil.equalsIgnoreCase(method, Constants.GETFORMS_EVGETME)){
    		callEvGetMe();
    	} if (TextUtil.equalsIgnoreCase(method, Constants.GETFORMS_AUTOHTTP)){
    		callEvGetMeAUTO();
    	} else if (TextUtil.equalsIgnoreCase(method, Constants.GETFORMS_BLUETOOTH)){
    		this.xformClient = new VisualXFormClient(this);
    	} else if (TextUtil.equalsIgnoreCase(method, Constants.GETFORMS_FILE)){
    		initServiceFileBrowser();
    		//#if app.usefileconnections
    		Display.getDisplay(this).setCurrent(this.fileBrowser);
    		//#endif
    	}
	}

	private void callEvGetMeAUTO() {
		this.getMidlet = new getMidlet(this,true);
	}

	public void callEvGetMe() {
		this.getMidlet = new getMidlet(this,false);
	}

    public void startBToothClient() {
		this.xformServer = new VisualXFormServer(this);
	}

	protected void destroyApp(boolean unconditional)
    {
		//log.write("IN DESTROY APP",MIDPLogger.DEBUG);
		//log.close();
        System.out.println("Application succesfully destroyed");
        notifyDestroyed();

    }

    protected void pauseApp()
    {
        // TODO Auto-generated method stub
    }

    protected void startApp() throws MIDletStateChangeException
    {
    	//log.write("TRYING CREATEView",MIDPLogger.DEBUG);
    	initRMS();
    	createView();
    	//log.write("completed CREATEVIEW",MIDPLogger.DEBUG);
    }

    private void initRMS() {
    	//log.write("PRE-initXFormUtil",MIDPLogger.DEBUG);
		this.xformRMS = new XFormRMSUtility(Controller.XFORM_RMS);
		this.modelRMS = new ModelRMSUtility(Controller.MODEL_RMS);
		//log.write("POST-initXformUtil",MIDPLogger.DEBUG);
		//log.write("PRE-writeDummy",MIDPLogger.DEBUG);
		if (writeDummy || xformRMS.getNumberOfRecords() == 0)
		{
		    System.out.println("***NUMBER OF RECORDS : " + xformRMS.getNumberOfRecords());
		    this.xformRMS.writeDummy();
		}
		//log.write("POST-writeDummy",MIDPLogger.DEBUG);
	}

	public void createView() {
		displayFormList();
    	//Display.getDisplay(this).setCurrent(selectFunction);
	}

    public void commandAction(Command c, Displayable d)
    {
        if (c == EXIT_CMD)
        {
            destroyApp(true);
            notifyDestroyed();
            return;
        }
        else if (c == BACK_CMD) {
			if (d == availableXForms) {
				createView();
			}
		}
        else if (c == List.SELECT_COMMAND) {
        	if (d == selectFunction){
        		navigateSelectFunctionList();
        	}
        	else if (d == availableXForms) {
        		navigateAvailbleXformsList();
			}
		}
      //#if app.usefileconnections
        else if (c == FileBrowser.SELECT_FILE_COMMAND)
        {
            List directory = (List) d;
            String selectedFile = directory.getString(directory.getSelectedIndex());
            this.openFileIntoNewForm(selectedFile);
        }
        else if (c == FileBrowser.EXIT_COMMAND)
        {
            this.createView();
        }
      //#endif
        //#if polish.usePolishGui
        else if (c == PropertiesScreen.CMD_DONE) {
            propertyScreen.commitChanges();
            loadAndSetViewType();
            this.createView();
        }
        else if (c == PropertiesScreen.CMD_CANCEL) {
            this.createView();
        }
        //#endif
    }

	private void initServiceFileBrowser() {
	  //#if app.usefileconnections
    	fileBrowser = new FileBrowser(Display.getDisplay(this));
        fileBrowser.setTitle("Browse XForms");
        fileBrowser.setCommandListener(this);
        fileBrowser.addCommand(FileBrowser.SELECT_FILE_COMMAND);
        fileBrowser.addCommand(FileBrowser.EXIT_COMMAND);
        //#endif
	}

	public void loadSpecificForm(int i)
    {
        System.out.println("Application succesfully started");
        display = Display.getDisplay(this);
        MVCComponent.display = display;
        if (i > this.xformRMS.getNumberOfRecords())
        {
            this.createView();
        }
        else
        {
            controllerLoadForm(i);
        }
    }

	private void loadProperties() {
		loadAndSetViewType();
		initProperty("PostURL", Constants.POST_URL);
		initProperty("GetURL", Constants.GET_URL);
		initProperty("GetFormsMethod", Constants.GETFORMS_BLUETOOTH);
	}

	private String initProperty (String propName, String defaultValue) {
		String propVal = PropertyManager.instance().getProperty(propName);
		if (propVal == null) {
			propVal = defaultValue;
			PropertyManager.instance().setProperty(propName, propVal);
			System.out.println("No default value for [" + propName + "]; setting to [" + propVal + "]");  //debug
		}
		return propVal;
	}

	private void loadAndSetViewType () {
		String defaultViewType;
		//#if polish.usePolishGui
        defaultViewType = Constants.VIEW_CHATTERBOX;
        //#else
        defaultViewType = Constants.VIEW_CLFORMS;
        //#endif
		setViewType(initProperty("ViewStyle", defaultViewType));		
	}
	
    public void setViewType(String viewType) {
        if(Constants.VIEW_CUSTOMCHAT.equals(viewType)) {
            formController.setPrompter(customChatScreen);
            formController.setFormview(customChatScreen);
        }
        //#if polish.usePolishGui
        else if(Constants.VIEW_CHATTERBOX.equals(viewType)) {
            formController.setPrompter(chatScreen);
            formController.setFormview(chatScreen);
        }
        //#endif
        else if(Constants.VIEW_CLFORMS.equals(viewType)) {
            formController.setPrompter(promptScreen);
            formController.setFormview(formViewScreen);
        }
        else {
            System.out.println("No valid view found!");
        }
        System.out.println("View Type is " + viewType);
    }
	
	public void configureController(){
		formController = new Controller(this);
	}

    public void controllerLoadForm(int formId)
    {
        display = Display.getDisplay(this);
        MVCComponent.display = display;
        formController.loadForm(formId);
        formController.completeForm();
    }

    public void controllerLoadForm(Form form)
    {
        display = Display.getDisplay(this);
        MVCComponent.display = display;
        formController.setForm(form);
        formController.completeForm();
    }

    public void deleteModel(int i) {
		// TODO Refactor this method from controller to Shell
    	formController = new Controller(this);
    	formController.deleteModel(i);
	}


    public void deleteForm(int i) {
		// TODO Refactor this method from controller to Shell
    	formController = new Controller(this);
    	formController.deleteForm(i);
	}


    private void openFileIntoNewForm(String fileName)
    {
        try
        {
            //#if app.usefileconnections
        	System.out.println("DIRECTORY: " + this.fileBrowser.getSelectedFileURL());
            FileConnection fc = (FileConnection) Connector.open(this.fileBrowser.getSelectedFileURL());
            System.out.println("FILE SIZE: "+fc.fileSize());
            InputStream fis = fc.openInputStream();
            //So this used to use Double.toInt(), which is a problem because most of these devices
            //don't have floating point processors, and the libraries don't have the datatype.
            //The Double.toInt() method just cast the long to an integer anyway, so this should do
            //the same thing
            // - Clayton Sims Jan 15, 2008
            int length = (int)(fc.fileSize()+1);
            byte[] b = new byte[length];
            int readLength = fis.read(b, 0, length);
            DataInputStream din = new DataInputStream(
				new ByteArrayInputStream(b));
            fis.close();
            fc.close();
            try {
            	// first parse form to check correct format
            	Form form = new Form();
            	XMLUtil.parseForm(new InputStreamReader(din), form);
            	System.out.println("writing form to: "+this.xformRMS.getNextRecordID()+form.getXmlModel().toString());
            	System.out.println("Form metaD: "+new XFormMetaData(form).toString());
            	// TODO once write external fixed for Form change this to right form object
            	this.xformRMS.writeToRMS(form);
            	Display.getDisplay(this).setCurrent(new javax.microedition.lcdui.Alert("save succes","Form Loaded successfully",null,AlertType.CONFIRMATION), this.getFormList());
            	int formNumber = this.xformRMS.getNextRecordID()-1;
            	System.out.println("written to:"+formNumber+"Number of records : " + this.xformRMS.getNumberOfRecords());
            	// if immediately load form
            	this.controllerLoadForm(formNumber);

            } catch (Exception e) {
            	Display.getDisplay(this).setCurrent(new javax.microedition.lcdui.Alert("save error","Form failed to Load",null,AlertType.ERROR), this.getFormList());
            }

            //TextBox viewer = new TextBox("XForm Contents", null, length, TextField.ANY | TextField.UNEDITABLE);
            //viewer.setString(new String(b, 0, length));
            //Display.getDisplay(this).urrent(viewer);
            //#endif
        }
        catch (Exception ex)
        {
            System.out.println("Exception on Parsing : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    Displayable getDisplayable()
    {
        return this.selectFunction;
    }

    public XFormRMSUtility getXFormRMSUtility()
    {
        return this.xformRMS;
    }

    public  ModelRMSUtility getModelRMSUtility()
    {
        return this.modelRMS;
    }

	public Display getDisplay() {
		return display;
	}

	public void setDisplay(Display display) {
		this.display = display;
	}

	public void writeFormToRMS(Form frm) {
		XFormRMSUtility rms = new XFormRMSUtility(Controller.XFORM_RMS);
        System.out.println("RMS SIZE BEFORE : " + rms.getNumberOfRecords());
        rms.writeToRMS(frm);
        System.out.println("RMS SIZE AFTER : " + rms.getNumberOfRecords());

	}

	public TransportLayer getTransportLayer() {
		if(this.transportLayer == null)
			this.transportLayer = new TransportLayer(this);
		return this.transportLayer;
	}


}