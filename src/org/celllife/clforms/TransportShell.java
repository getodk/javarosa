package org.celllife.clforms;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import org.celllife.clforms.api.Form;
import org.celllife.clforms.storage.ModelRMSUtility;
import org.celllife.clforms.storage.XFormMetaData;
import org.celllife.clforms.storage.XFormRMSUtility;
import org.celllife.clforms.view.FormViewScreen;
import org.celllife.clforms.view.PromptScreen;
import org.celllife.clforms.xml.XMLUtil;
import org.netbeans.microedition.lcdui.pda.FileBrowser;
import org.openmrs.transport.midp.TransportLayer;

import com.ev.evgetme.getMidlet;

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
    private FileBrowser fileBrowser;
    private boolean writeDummy = false;

	private FormList formList;

	private VisualXFormServer xformServer;

	private VisualXFormClient xformClient;

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

			this.selectFunction = new List("What do you want to do?", List.IMPLICIT, optionsMenu, null);
			this.selectFunction.addCommand(EXIT_CMD);
			this.selectFunction.addCommand(OK_CMD);
			selectFunction.setCommandListener(this);
			this.createAvailableXformsList();
			//log.write("PRE-configCntllr",MIDPLogger.DEBUG);
			configureController();
			//log.write("POST-configCntllr",MIDPLogger.DEBUG);
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
			Display.getDisplay(this).setCurrent(this.fileBrowser);
			break;
		case 1:
			this.xformClient = new VisualXFormClient(this);
			break;
		case 2:
			this.getMidlet = new getMidlet(this);
			break;
		}
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
		if (writeDummy)//xformRMS.getNumberOfRecords() == 0)
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
    }

	private void initServiceFileBrowser() {
    	fileBrowser = new FileBrowser(Display.getDisplay(this));
        fileBrowser.setTitle("Browse XForms");
        fileBrowser.setCommandListener(this);
        fileBrowser.addCommand(FileBrowser.SELECT_FILE_COMMAND);
        fileBrowser.addCommand(FileBrowser.EXIT_COMMAND);
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
	
	public void configureController(){
		formController = new Controller(this);
        formController.setPrompter(new PromptScreen());
        formController.setFormview(new FormViewScreen());
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
        	System.out.println("DIRECTORY: " + this.fileBrowser.getSelectedFileURL());
            FileConnection fc = (FileConnection) Connector.open(this.fileBrowser.getSelectedFileURL());
            System.out.println("FILE SIZE: "+fc.fileSize());
            InputStream fis = fc.openInputStream();
            int length = new Double(fc.fileSize()+1).intValue();
            byte[] b = new byte[length];
            int readLength = fis.read(b, 0, length);
            DataInputStream din = new DataInputStream(
				new ByteArrayInputStream(b));
            fis.close();
            fc.close();
           // first parse form to check correct format 
            Form form = new Form();
            XMLUtil.parseForm(new InputStreamReader(din), form);
            System.out.println("writing form to: "+this.xformRMS.getNextRecordID()+form.getXmlModel().toString());
            System.out.println("Form metaD: "+new XFormMetaData(form).toString());
            // TODO once write external fixed for Form change this to right form object
            try {
            	this.xformRMS.writeToRMS(form);
            	Display.getDisplay(this).setCurrent(new javax.microedition.lcdui.Alert("save succes","Form Loaded successfully",null,AlertType.CONFIRMATION), this.getFormList());
            	int iFormNumber = this.xformRMS.getNextRecordID()-1;
            	System.out.println("written to:"+iFormNumber+"Number of records : " + this.xformRMS.getNumberOfRecords());
            	//this.controllerLoadForm(iFormNumber);

            } catch (Exception e) {
            	Display.getDisplay(this).setCurrent(new javax.microedition.lcdui.Alert("save error","Form failed to Load",null,AlertType.ERROR), this.getFormList());
            }
            
            //TextBox viewer = new TextBox("XForm Contents", null, length, TextField.ANY | TextField.UNEDITABLE);
            //viewer.setString(new String(b, 0, length));
            //Display.getDisplay(this).urrent(viewer);
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