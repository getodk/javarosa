package org.javarosa.formmanager.activity;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.javarosa.communication.http.HttpTransportDestination;
import org.javarosa.communication.http.HttpTransportProperties;
import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IShell;
import org.javarosa.core.api.IView;
import org.javarosa.core.services.TransportManager;
import org.javarosa.core.services.transport.ByteArrayPayload;
import org.javarosa.core.services.transport.ITransportDestination;
import org.javarosa.core.services.transport.TransportMessage;
import org.javarosa.core.services.transport.TransportMethod;
import org.javarosa.core.util.Observable;
import org.javarosa.core.util.Observer;
import org.javarosa.formmanager.view.ProgressScreen;

public class GetFormListHttpActivity implements IActivity,CommandListener,Observer{
	public final Command CMD_CANCEL = new Command("Cancel",Command.BACK, 1);
	private ProgressScreen progressScreen = new ProgressScreen("Searching","Please Wait. Contacting Server...",this);
	private Alert alertdialog = new Alert("Web Service Error", "No response from server", null, AlertType.ERROR);
	private TransportMessage message;
	private TransportManager transportManager;
	private IShell parent;
	private String getListUrl;
	private String credentials;
	private Context context;
	
	public static final String RETURN_KEY = "returnval";
	
	private String requestPayload = "#";
	
	

	public GetFormListHttpActivity(IShell parent) {
		this.parent = parent;
		
	}
	
	private void init(){
		//System.out.println("NOW STARTING RETRIEVE");
		getListUrl = JavaRosaServiceProvider.instance().getPropertyManager().getSingularProperty(HttpTransportProperties.GET_URL_PROPERTY);
		credentials = "?user="+context.getCurrentUser();
		requestPayload = credentials;
	}

	public void contextChanged(Context globalContext) {
		context.mergeInContext(globalContext);
		
	}

	public void destroy() {
		if(progressScreen!=null){
			progressScreen.closeThread();
			progressScreen =null;
		}
		if(transportManager!=null){
			//transportManager.closeSend();
			transportManager = null;
		}
	}

	public Context getActivityContext() {
		
		return context;
	}

	public void halt() {
		// TODO Auto-generated method stub
		
	}

	public void resume(Context globalContext) {
		// TODO Auto-generated method stub
		
	}

	public void start(Context context) {
		this.context = context;
		
		progressScreen.addCommand(CMD_CANCEL);
		parent.setDisplay(this, new IView() {public Object getScreenObject() {return progressScreen;}});
		init();
		fetchList();
	}
	
	public void fetchList() {
		ITransportDestination requestDest= new HttpTransportDestination(getListUrl+credentials);//send username and url
		message = new TransportMessage();
		message.setPayloadData(new ByteArrayPayload(requestPayload.getBytes(),null,Constants.PAYLOAD_TYPE_TEXT)); // TODO change this to send xml msg with search options /uname, form type
		// TODO add url stuff here below
		message.setDestination(requestDest);
		message.addObserver(this);
		transportManager = (TransportManager)JavaRosaServiceProvider.instance().getTransportManager();
		transportManager.send(message, TransportMethod.HTTP_GCF);

	}
	
	

	public void commandAction(Command command, Displayable display) {
		
		if(display== progressScreen){
			if(command == CMD_CANCEL){
				parent.returnFromActivity(this, Constants.ACTIVITY_CANCEL, null);
				
			}
			
		}
		
	}

	public void update(Observable observable, Object arg) {

		byte[] data = (byte[])arg;
		process(data);
		
	}

	

	public void process(byte[] data) {
		String response;
		response = new String(data).trim();
		
		//FIXME
//		if(response ==null){
//			parent.setDisplay(this, new IView() {public Object getScreenObject() {return alertdialog;}});
//			parent.returnFromActivity(this, Constants.ACTIVITY_CANCEL, null);
//		}else if(response.equals(WebServerResponses.GET_LIST_ERROR)){
//			parent.setDisplay(this, new IView() {public Object getScreenObject() {return alertdialog;}});
//			parent.returnFromActivity(this, Constants.ACTIVITY_CANCEL, null);
//		}else if(response.equals(WebServerResponses.GET_LIST_NO_SURVEY)){
//			parent.setDisplay(this, new IView() {public Object getScreenObject() {return alertdialog;}});
//			parent.returnFromActivity(this, Constants.ACTIVITY_CANCEL, null);
//		}else{
//			Hashtable returnArgs = new Hashtable();
//			returnArgs.put(RETURN_KEY, data);
//			parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, returnArgs);
//		}
		
	}

	public void setShell(IShell shell) {
		this.parent = shell;
		
	}
	

}
