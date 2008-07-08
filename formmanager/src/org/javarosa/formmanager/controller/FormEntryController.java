package org.javarosa.formmanager.controller;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

import org.javarosa.clforms.api.Binding;
import org.javarosa.clforms.api.Constants;
import org.javarosa.clforms.api.Form;
import org.javarosa.clforms.api.Prompt;
import org.javarosa.clforms.api.ResponseEvent;
import org.javarosa.clforms.storage.Model;
import org.javarosa.clforms.storage.ModelMetaData;
import org.javarosa.clforms.storage.ModelRMSUtility;
import org.javarosa.clforms.storage.RMSManager;
import org.javarosa.clforms.storage.XFormRMSUtility;
import org.javarosa.clforms.util.J2MEUtil;
import org.javarosa.clforms.view.FormView;
import org.javarosa.clforms.view.IPrompter;
import org.javarosa.clforms.xml.XMLUtil;
import org.javarosa.polishforms.SubmitScreen;
import org.javarosa.polishforms.SubmitStatusScreen;
import org.javarosa.properties.PropertyManager;
import org.openmrs.transport.TransportMessage;
import org.openmrs.transport.midp.TransportLayer;

public class FormEntryController {
/*
    public static final String XFORM_RMS = "XFORM_RMS_NEW";
    public static final String MODEL_RMS = "MODEL_RMS_NEW";
    public IPrompter prompter;
    public FormView formview;
    private Form form;
    private RMSManager rmsManager;
    private XFormRMSUtility xformRMS;
    private ModelRMSUtility modelRMS;
    private int promptIndex;
    public TransportShell shell;

	public Controller(TransportShell shell) {
		super();
		try {
			//shell.log.write("IN CONTROLLER",MIDPLogger.DEBUG);
			this.shell = shell;
			this.rmsManager = new RMSManager();
			this.xformRMS = new XFormRMSUtility(Controller.XFORM_RMS);
			//shell.log.write("IN CONTROLLER-post XFOrm util",MIDPLogger.DEBUG);
			System.out.println("#REC "+xformRMS.getNumberOfRecords());
			if (xformRMS.getNumberOfRecords() == 0)
			{
			    this.xformRMS.writeDummy();
			}
			this.modelRMS = new ModelRMSUtility(Controller.MODEL_RMS);
			//shell.log.write("IN CONTROLLER-post XFOrm util",MIDPLogger.DEBUG);
			this.rmsManager.registerRMSUtility(this.xformRMS);
			this.rmsManager.registerRMSUtility(this.modelRMS);
			//shell.log.write("OUT CONTROLLER-post rms register",MIDPLogger.DEBUG);
		} catch (Exception e) {
			//shell.log.write(e.getMessage(),MIDPLogger.DEBUG);
			e.printStackTrace();
		}
    }

     (non-Javadoc)
     * @see org.javarosa.clforms.IController#cancelForm()
     
    public void cancelForm()
    {
    }

    private void registerFormView()
    {
        this.formview.registerController(this);
    }

     (non-Javadoc)
     * @see org.javarosa.clforms.IController#registerPrompter()
     
    public void registerPrompter()
    {
        this.prompter.registerController(this);
    }

     (non-Javadoc)
     * @see org.javarosa.clforms.IController#completeForm()
     
    public void completeForm()
    {
    	System.out.println("Completing form..."+form.getName()+form.getPromptCount());

        registerPrompter();
        registerFormView();
        form.setShortForms();
        promptIndex = 0;
        form.updatePromptsDefaultValues();
        if(form.getXmlModel().getEditID() != -1){ // we are editing
        	form.updatePromptsValues();
        }else{
        	form.loadPromptsDefaultValues();
        }
        formview.displayPrompt(form.getPrompt(promptIndex));
    }

    private void getNextPrompt()
    {
        promptIndex++;
        if (promptIndex >= form.getPrompts().size())
        {
            //goToFormView();
        	askSubmitData(saveData());
        }
        else{
        	form.calculateRelavant(form.getPrompt(promptIndex));
        	if(form.getPrompt(promptIndex).isRelevant())
        		showPromptAtIndex();
        	else
        		getNextPrompt();
        }
    }

    private void getPreviousPrompt()
    {
        promptIndex--;
        if (promptIndex < 0)
        {
            goToFormView();
        }
        else{
        	form.calculateRelavant(form.getPrompt(promptIndex));
        	if(form.getPrompt(promptIndex).isRelevant())
        		showPromptAtIndex();
        	else
        		getPreviousPrompt();
        }
    }

     (non-Javadoc)
     * @see org.javarosa.clforms.IController#loadForm(int)
     
    public void loadForm(int recordId)
    {
    	// TODO put this in shell
    	System.out.println("in load form id:"+recordId);
//    	ExampleForm ef = new ExampleForm();
//        form = ef.getXFormObject();
    	//DummyForm df = new DummyForm();
    	//form = df.getXFormObject();
    	form = new Form(); //storageManager.getForm(recordId);
    	try {
    		this.xformRMS.retrieveFromRMS(recordId, form);
    		// TODO Sort this out so that the recordID is added in the deserialisation
    		form.setRecordId(recordId);
    		// TODO fix this so IDs are in form objects properly
    		form.setName(this.xformRMS.getName(recordId));

    		System.out.println("form "+recordId+form.getName()+" loaded");
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
        if (form == null)
        {
            System.out.println("Form retuned null");
        }
        else
        {
            System.out.println("Form loaded");
        }
    }

     (non-Javadoc)
     * @see org.javarosa.clforms.IController#deleteForm(int)
     
    public void deleteForm(int recordId) {
		// TODO move this to Shell
    	System.out.println("deleting form id:"+recordId);
        this.xformRMS.deleteRecord(recordId);
	}

     (non-Javadoc)
     * @see org.javarosa.clforms.IController#deleteModel(int)
     
    public void deleteModel(int recordId) {
		// TODO move this to Shell
    	System.out.println("deleting record id:"+recordId);
        this.modelRMS.deleteRecord(recordId);
	}


     (non-Javadoc)
     * @see org.javarosa.clforms.IController#processEvent(org.javarosa.clforms.api.ResponseEvent)
     
    public void processEvent(ResponseEvent event)
    {
        switch (event.getType())
        {
            case ResponseEvent.NEXT:
            	//check to make sure that we can actually move forward
            	Prompt prompt = form.getPrompt(promptIndex);
            	if(prompt.isRequired() || promptIndex == 2)
            	{
            		if(prompt.isEmpty())
            		{
            			prompter.showError(null, "Required question, you must enter a value", Display.getDisplay(shell));
            			break;
            		}
            	}
            	
                form.updateModel(prompt);
                getNextPrompt();
                break;
            case ResponseEvent.PREVIOUS:
                form.updateModel(form.getPrompt(promptIndex));
				getPreviousPrompt();
                break;
            case ResponseEvent.SAVE_AND_RELOAD:
                checkRMSstatus();
                form.populateModel();
                saveFormModel();
                clearModelData();
                form.loadPromptsDefaultValues();
                promptIndex = 0;
                formview.displayPrompt(form.getPrompt(promptIndex));
                break;
            case ResponseEvent.SAVE_AND_EXIT:
                checkRMSstatus();
                form.populateModel();
                saveFormModel();
                clearModelData();
                break;
            case ResponseEvent.GOTO:
                form.updateModel(form.getPrompt(promptIndex));
                promptIndex = event.getParameter();
                promptIndex--;
                getNextPrompt();
                break;
            case ResponseEvent.LIST:
            	form.updateModel(form.getPrompt(promptIndex));
        		form.populateModel();
                goToFormView();
                break;
            case ResponseEvent.EXIT:
            	 if(form.getXmlModel().getEditID() != -1){ // we are editing
                 	shell.displayModelList();
                 }else{
                	 shell.createView();
                 }
				break;
        }
    }

    private void askSubmitData (ModelMetaData mmd) {
    	//#style submitPopup
    	SubmitScreen ss = new SubmitScreen(this, mmd);
		Display.getDisplay(shell).setCurrent(ss);    	
    }
    
    private Model getModel (ModelMetaData mmd) {
    	Model model = new Model();
    	try {
    		shell.getModelRMSUtility().retrieveFromRMS(mmd.getRecordId(), model);
    	} catch (IOException ioe) { }
    	model.setRecordId(mmd.getRecordId());
    	return model;
    }
    
    public void submitData(final ModelMetaData mmd, final boolean showStatus, boolean promptURL) {
    	final TransportLayer tl = shell.getTransportLayer();
    	final Controller conref = this;
    	tl.setDestURL(PropertyManager.instance().getSingularProperty("PostURL"));
    	tl.setData(getModel(mmd));
		if (promptURL) {
    		tl.showURLform(new CommandListener(){
    			public void commandAction(Command c, Displayable d) {
    				if(c.getLabel().equals("OK")) {
    					tl.processURLform();
    				}
    				try {
    				tl.sendData(org.openmrs.transport.TransportMethod.HTTP_GCF);
    				}
    				catch(IOException ioe) {
    					
    				}
    		    	if (showStatus) {
    		        	// #style submitPopup
    		    		SubmitStatusScreen sss = new SubmitStatusScreen(conref, mmd);
    		    		Display.getDisplay(shell).setCurrent(sss);
    		    	} else {
    		    		closeForm();
    		    	}
    			}
    		});
		} else {
			try {
				tl.sendData(org.openmrs.transport.TransportMethod.HTTP_GCF);
			} catch (IOException ioe) {

			}
    	if (showStatus) {
        	// #style submitPopup
    		SubmitStatusScreen sss = new SubmitStatusScreen(this, mmd);
    		Display.getDisplay(shell).setCurrent(sss);
    	} else {
    		closeForm();
    	}
		}
    }
    
    public void submitData (ModelMetaData mmd, boolean showStatus) {
    	submitData(mmd,showStatus,false);
    }
    
    public int getSubmitStatus (ModelMetaData mmd) {
    	TransportLayer tl = shell.getTransportLayer();
    	Enumeration qMessages = tl.getTransportMessages();
    	TransportMessage message;
    	while(qMessages.hasMoreElements()) {
    		message = (TransportMessage) qMessages.nextElement();
    		if(message.getModelId() == mmd.getRecordId())
    			return message.getStatus();
    	}
    	return -1;
    }

    
    private ModelMetaData saveData () {
    	ModelMetaData mmd;
        checkRMSstatus();
        form.populateModel();
        mmd = saveFormModel2();
        clearModelData();
        return mmd;
    }
    
    public void closeForm () {
    	formview.destroy();
    }
    
	public void goToFormView() {
		promptIndex = 0;
		formview.displayPrompt(form.getPrompt(promptIndex));
	}

    private void checkRMSstatus()
    {
        int iXFormRMSSize = 0;
        int iModelRMSSize = 0;
        iXFormRMSSize = this.xformRMS.getNumberOfRecords();
        iModelRMSSize = this.modelRMS.getNumberOfRecords();
        System.out.println("XForm : " + iXFormRMSSize + "\n" + "Model : " + iModelRMSSize);
    }

    private void clearModelData()
    {
        form.getXmlModel().clearData();
    }

     (non-Javadoc)
     * @see org.javarosa.clforms.IController#saveFormModel()
     
    public void saveFormModel() {
    	saveFormModel2();
    }
    	
    public ModelMetaData saveFormModel2 () {
        ModelMetaData mmd = null;
    	
        postProcessForm();
        
        try
        {
        	//System.out.println("SAVING MODEL- pre populate: "+form.getXmlModel().toString());
            Model model = form.getXmlModel();
			Calendar cd = Calendar.getInstance();
			Date d = (Date) cd.getTime();
			String date = J2MEUtil.getXMLStringValue(d,Constants.RETURN_DATE);
            model.setName(form.getName());
            model.setXformReference(form.getRecordId());
            model.setDateSaved(d);
            System.out.println("under refID:"+form.getRecordId());
            mmd = new ModelMetaData(model);
            if(model.getEditID() != -1){
            	System.out.println("updating model- ref:"+model.getXformReference());
            	this.modelRMS.updateToRMS(model.getEditID(),model, mmd);
            }
            else
            	this.modelRMS.writeToRMS(model, mmd);
            ///storageManager.saveFormModel(form);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        
        return mmd;
    }

    public void postProcessForm () {
    	//binds bound to prompts
    	for (Enumeration e = form.getPrompts().elements(); e.hasMoreElements(); ) {
    		Prompt p = (Prompt)e.nextElement();
    		
    		if (p.getBind() != null && "property".equals(p.getBind().preload)) {
    			String propname = p.getBind().preloadParams;
    			String value = J2MEUtil.getXMLStringValue(p.getValue(), p.getReturnType());
    			
    			if (propname != null && propname.length() > 0 && value != null && value.length() > 0)
    				PropertyManager.instance().setProperty(propname, value);
    		}
    	}
    	
    	//binds not bound (hidden fields)
    	Vector unboundBinds = XMLUtil.getUnattachedBinds(form);
		for (Enumeration e = unboundBinds.elements(); e.hasMoreElements(); ) {
			String value = null;
			Binding b = (Binding)e.nextElement();
			if (b.preload == null)
				continue;			
			
			if (b.preload.equals("timestamp") && "end".equals(b.preloadParams)) {
				value = J2MEUtil.formatDateToTimeStamp(new Date());
			}
			
			if (b.getNodeset() != null && value != null && value.length() > 0)
				form.updateModel(b.getNodeset(), value);				
		}
    }
    
     (non-Javadoc)
     * @see org.javarosa.clforms.IController#updateModel()
     
    public void updateModel()
    {
    }

     (non-Javadoc)
     * @see org.javarosa.clforms.IController#getForm()
     
    public Form getForm()
    {
        return form;
    }

     (non-Javadoc)
     * @see org.javarosa.clforms.IController#setForm(org.javarosa.clforms.api.Form)
     
    public void setForm(Form form)
    {
        this.form = form;
    }

     (non-Javadoc)
     * @see org.javarosa.clforms.IController#getPrompter()
     
    public IPrompter getPrompter()
    {
        return prompter;
    }

     (non-Javadoc)
     * @see org.javarosa.clforms.IController#setPrompter(org.javarosa.clforms.view.IPrompter)
     
    public void setPrompter(IPrompter prompter)
    {
        this.prompter = prompter;
    }

     (non-Javadoc)
     * @see org.javarosa.clforms.IController#getFormview()
     
    public FormView getFormview()
    {
        return formview;
    }

    public int getPromptIndex () {
    	return promptIndex;
    }
    
     (non-Javadoc)
     * @see org.javarosa.clforms.IController#setFormview(org.javarosa.clforms.view.FormView)
     
    public void setFormview(FormView formview)
    {
        this.formview = formview;
    }

	private void showPromptAtIndex() {
		Prompt prompt = form.getPrompt(promptIndex);
		System.out.print("in contoller preload->");
		if (prompt.getBind()!= null)
			System.out.println(prompt.getBindID()+" bind not null -"+prompt.getBind().getId());
		else{
			System.out.println(prompt.getBindID()+" bind null");
		}

		prompter.showPrompt(prompt, promptIndex+1,form.getPrompts().size());
	}


     (non-Javadoc)
     * @see org.javarosa.clforms.IController#getRMSManager()
     
    public RMSManager getRMSManager()
    {
        return this.rmsManager;
    }
*/
}