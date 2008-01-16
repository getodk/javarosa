package org.celllife.clforms;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.celllife.clforms.api.Constants;
import org.celllife.clforms.api.Form;
import org.celllife.clforms.api.IForm;
import org.celllife.clforms.api.Prompt;
import org.celllife.clforms.api.ResponseEvent;
import org.celllife.clforms.storage.Model;
import org.celllife.clforms.storage.ModelMetaData;
import org.celllife.clforms.storage.ModelRMSUtility;
import org.celllife.clforms.storage.RMSManager;
import org.celllife.clforms.storage.XFormRMSUtility;
import org.celllife.clforms.util.J2MEUtil;
import org.celllife.clforms.view.FormView;
import org.celllife.clforms.view.IPrompter;

import org.celllife.clforms.storage.DummyForm;
import org.dimagi.demo.ExampleForm;

public class Controller
{

    public static final String XFORM_RMS = "XFORM_RMS_NEW";
    public static final String MODEL_RMS = "MODEL_RMS_NEW";
    public IPrompter prompter;
    public FormView formview;
    private IForm form;
    private RMSManager rmsManager;
    private XFormRMSUtility xformRMS;
    private ModelRMSUtility modelRMS;
    private int promptIndex;
    private TransportShell shell;

	public Controller(TransportShell shell) {
		super();
		System.out.println("Controller()");
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
	
    public void cancelForm()
    {
    }

    private void registerFormView()
    {
        this.formview.registerController(this);
    }

    public void registerPrompter()
    {
        this.prompter.registerController(this);
    }

    public void completeForm()
    {
    	System.out.println("Completing form..."+form.getName());

        registerPrompter();
        registerFormView();
        form.setShortForms();
        promptIndex = 0;
        if(form.getXmlModel().getEditID() != -1){ // we are editing
        	form.updatePromptsValues();
        }else{      	
        	form.updatePromptsDefaults();
        	form.loadPromptsDefaultValues();
        }
        formview.showPrompt(form.getPrompt(promptIndex));
        System.out.println(((Prompt)(form.getPrompts().elementAt(0))).getLongText());
    }

    private void getNextPrompt()
    {
    	System.out.println("Controller.getNextPrompt()");
        promptIndex++;
        if (promptIndex >= form.getPrompts().size())
        {
            goToFormView();
        }
        
        form.calculateRelavant(form.getPrompt(promptIndex)); 
		if(form.getPrompt(promptIndex).isRelevant())	
			showPromptAtIndex();
		else
			getNextPrompt();
    }

    private void getPreviousPrompt()
    {
        promptIndex--;
        if (promptIndex < 0)
        {
            goToFormView();
        }
        form.calculateRelavant(form.getPrompt(promptIndex)); 
		if(form.getPrompt(promptIndex).isRelevant())
			showPromptAtIndex();
		else
			getPreviousPrompt();
    }

    /**
     * @param formId
     */
    public void loadForm(int recordId)
    {
    	// TODO put this in shell
    	System.out.println("start Controller.loadForm() id:"+recordId);
//    	DummyForm df = new DummyForm();
//    	form = df.getXFormObject();
    	ExampleForm ef = new ExampleForm();
    	form = ef.getXFormObject();
    	form.setRecordId(recordId);
//    	form.setName("DimagiSurvey2");
  
   //        form = new Form(); //storageManager.getForm(recordId);
//        try {
//        	this.xformRMS.retrieveFromRMS(recordId, form);
//        	System.out.println("retrieve from rms");
//        	// TODO Sort this out so that the recordID is added in the deserialisation
//        	form.setRecordId(recordId);
//        	System.out.println("set record id");
//			// TODO fix this so IDs are in form objects properly
//        	form.setName(this.xformRMS.getName(recordId));
//        	System.out.println("get name");

        	
//        	System.out.println("form "+recordId+form.getName()+"loaded");
//        } catch (IOException e) {
//        	e.printStackTrace();
//		}
        if (form == null)
        {
            System.out.println("Form retuned null");
        }
        else
        {
            System.out.println("Form loaded");
        }
    }
    
    public void deleteForm(int recordId) {
		// TODO move this to Shell
    	System.out.println("deleting form id:"+recordId);
        this.xformRMS.deleteRecord(recordId);
	}
    
    public void deleteModel(int recordId) {
		// TODO move this to Shell
    	System.out.println("deleting record id:"+recordId);
        this.modelRMS.deleteRecord(recordId);
	}
    
    
    public void processEvent(ResponseEvent event)
    {
        switch (event.getType())
        {
            case ResponseEvent.NEXT:
                form.updateModel(form.getPrompt(promptIndex));
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
                formview.showPrompt(form.getPrompt(promptIndex));
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

	private void goToFormView() {
		promptIndex = 0;
		formview.showPrompt(form.getPrompt(promptIndex));
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

    public void saveFormModel()
    {
        try
        {
        	//System.out.println("SAVING MODEL- pre populate: "+form.getXmlModel().toString());            
            Model model = form.getXmlModel();
			Calendar cd = Calendar.getInstance();
			Date d = (Date) cd.getTime();
			String date = J2MEUtil.getStringValue(d,Constants.RETURN_DATE);
            model.setName(form.getName()+"_"+date);
            model.setXformReference(form.getRecordId());
            System.out.println("under refID:"+form.getRecordId());
            if(model.getEditID() != -1){
            	System.out.println("updating model- ref:"+model.getXformReference());
            	this.modelRMS.updateToRMS(model.getEditID(),model,new ModelMetaData(model));
            }
            else
            	this.modelRMS.writeToRMS(model, new ModelMetaData(model));
            ///storageManager.saveFormModel(form);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateModel()
    {
    }

    public IForm getForm()
    {
        return form;
    }

    public void setForm(IForm form)
    {
        this.form = form;
    }

    public IPrompter getPrompter()
    {
        return prompter;
    }

    public void setPrompter(IPrompter prompter)
    {
        this.prompter = prompter;
    }

    public FormView getFormview()
    {
        return formview;
    }

    public void setFormview(FormView formview)
    {
        this.formview = formview;
    }

	private void showPromptAtIndex() {
		System.out.println("controller.showPromptAtIndex()");
		Prompt prompt = form.getPrompt(promptIndex);
		System.out.println(prompt.getLongText());
		prompter.showPrompt(prompt, promptIndex+1,form.getPrompts().size());
	}


    public RMSManager getRMSManager()
    {
        return this.rmsManager;
    }
}