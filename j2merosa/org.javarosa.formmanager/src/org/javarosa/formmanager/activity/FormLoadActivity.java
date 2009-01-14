/**
 * 
 */
package org.javarosa.formmanager.activity;

import java.io.IOException;
import java.util.Enumeration;
import java.util.TimerTask;
import java.util.Vector;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.ICommand;
import org.javarosa.core.api.IShell;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.IFunctionHandler;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.storage.DataModelTreeRMSUtility;
import org.javarosa.core.model.utils.ContextPreloadHandler;
import org.javarosa.core.model.utils.IPreloadHandler;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.formmanager.utility.FormDefFetcher;
import org.javarosa.formmanager.utility.IFormDefRetrievalMethod;
import org.javarosa.formmanager.utility.RMSRetreivalMethod;

/**
 * @author Brian DeRenzi
 *
 */
public class FormLoadActivity extends TimerTask implements IActivity {

	/** Current running context **/
	private FormEntryContext context;
	
	private FormDefFetcher fetcher;
	
	private ContextPreloadHandler contextHandler;
	
	private int instanceID = -1;
	
	// This is bad.  Not generic.  No interface
	private FormEntryActivity parent;
	
	private Thread loadThread;
	
	public FormLoadActivity(FormEntryActivity parent) {
		this.parent = parent;
		
		this.fetcher = new FormDefFetcher();
		this.fetcher.setFetcher(new RMSRetreivalMethod());
		
		this.loadThread = new Thread(this);
	}
	
	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	public void run() {
		FormDef theForm = null;
		

		theForm = fetcher.getFormDef(context);
		if (theForm != null) {
			try {
				if (instanceID != -1) {
					DataModelTreeRMSUtility modelUtil = (DataModelTreeRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(DataModelTreeRMSUtility.getUtilityName());
					IFormDataModel theModel = new DataModelTree();
					modelUtil.retrieveFromRMS(this.context.getInstanceID(), theModel);
					theForm.setDataModel(theModel);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (DeserializationException uee) {
				uee.printStackTrace();
			}
			
			theForm.setEvaluationContext(initEvaluationContext());
			initPreloadHandlers(theForm); // must always load; even if we won't preload, we may still post-process!
			theForm.initialize(instanceID == -1);
			
			//#if debug.output==verbose
			System.out.println("Loaded the form successfuly");
			//#endif
			finish(theForm);
		} else {
			// error!
			System.out.println("there's an error loading...");
			finish(null);
		}
	}
	
	private void finish(FormDef theForm) {
		//#if debug.output==verbose
		System.out.println("finishing formloadactivity!");
		//#endif
		this.context.setElement("theForm", theForm);
		
		this.parent.returnFromLoading(this.context);
	}
	
	
	public void contextChanged(Context globalContext) {
		
	}

	public void destroy() {
		// TODO Auto-generated method stub

	}

	public Context getActivityContext() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#halt()
	 */
	public void halt() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#resume(org.javarosa.core.Context)
	 */
	public void resume(Context globalContext) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#setShell(org.javarosa.core.api.IShell)
	 */
	public void setShell(IShell shell) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#start(org.javarosa.core.Context)
	 */
	public void start(Context context) {
		System.out.println("starting thread for formLoadActivity");
		this.instanceID = -1;

		if (context instanceof FormEntryContext) {
			this.context = (FormEntryContext) context;
			this.instanceID = this.context.getInstanceID();
		}
		
		loadThread.start();
	}

	private void initPreloadHandlers (FormDef f) {
		Vector preloadHandlers = this.context.getPreloadHandlers();
		if(preloadHandlers != null) {
			Enumeration en = preloadHandlers.elements();
			while(en.hasMoreElements()) {
				f.getPreloader().addPreloadHandler((IPreloadHandler)en.nextElement());
			}
		}

		//set handler for preload context
		contextHandler = new ContextPreloadHandler(context);
		f.getPreloader().addPreloadHandler(contextHandler);
	}
	
	private EvaluationContext initEvaluationContext () {
		EvaluationContext ec = new EvaluationContext();
		
		Vector functionHandlers = this.context.getFunctionHandlers();
		if(functionHandlers != null) {
			Enumeration en = functionHandlers.elements();
			while(en.hasMoreElements()) {
				ec.addFunctionHandler((IFunctionHandler)en.nextElement());
			}
		}
		
		return ec;
	}
	
	public void setRetrievalMethod(IFormDefRetrievalMethod method) {
		fetcher.setFetcher(method);
	}
	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#annotateCommand(org.javarosa.core.api.ICommand)
	 */
	public void annotateCommand(ICommand command) {
		throw new RuntimeException("The Activity Class " + this.getClass().getName() + " Does Not Yet Implement the annotateCommand Interface Method. Please Implement It.");
	}
}
