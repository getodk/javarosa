///*
// * Copyright (C) 2009 JavaRosa
// *
// * Licensed under the Apache License, Version 2.0 (the "License"); you may not
// * use this file except in compliance with the License. You may obtain a copy of
// * the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
// * License for the specific language governing permissions and limitations under
// * the License.
// */
//
//package org.javarosa.formmanager.activity;
//
//import java.io.ByteArrayInputStream;
//import java.util.Hashtable;
//
//import javax.microedition.lcdui.Command;
//import javax.microedition.lcdui.CommandListener;
//import javax.microedition.lcdui.Displayable;
//
//import org.javarosa.communication.http.HttpTransportDestination;
//import org.javarosa.core.Context;
//import org.javarosa.core.JavaRosaServiceProvider;
//import org.javarosa.core.api.Constants;
//import org.javarosa.core.api.IActivity;
//import org.javarosa.core.api.ICommand;
//import org.javarosa.core.api.IShell;
//import org.javarosa.core.model.storage.FormDefRMSUtility;
//import org.javarosa.core.services.TransportManager;
//import org.javarosa.core.services.transport.ByteArrayPayload;
//import org.javarosa.core.services.transport.IDataPayload;
//import org.javarosa.core.services.transport.ITransportDestination;
//import org.javarosa.core.services.transport.TransportMessage;
//import org.javarosa.core.services.transport.TransportMethod;
//import org.javarosa.core.util.Observable;
//import org.javarosa.core.util.Observer;
//import org.javarosa.formmanager.view.ProgressScreen;
//import org.javarosa.xform.util.XFormUtils;
//
//public class GetFormHttpActivity implements IActivity,CommandListener,Observer {
//	private Context context;
//	private ProgressScreen progressScreen =  new ProgressScreen("Downloadng","Please Wait. Fetching Form...", this);
//	private TransportMessage message;
//	private TransportManager transportManager;
//	private String getFormUrl;
//	//private String formName = "";
//	private IShell parent;
//
//	private ByteArrayInputStream bin;
//
//
//
//	public GetFormHttpActivity(IShell parent,Hashtable args) {
//		this.parent = parent;
//		init(args);
//
//	}
//
//	public void init(Hashtable args){
//		//getFormUrl = JavaRosaServiceProvider.instance().getPropertyManager().getSingularProperty(HttpTransportProperties.GET_URL_PROPERTY);
//		//getFormUrl = "http://dev.cell-life.org/javarosa/web/limesurvey/admin/getXform.php";
//		getFormUrl = (String)args.get("selected_form_url");
//		//getFormUrl = "http://172.16.23.220/limesurvey/xforms/CHMT%20TREATMENT%20LITERACY%20SESSION%20REPORT.xhtml";
//		//formName = "?name="+(String)args.get(DisplayFormsHttpActivity.SELECTED_FORM);//send GET request
//		System.out.println("URL SHALL BE: "+ getFormUrl);
//	}
//
//	public void fetchForm(){
//		ITransportDestination requestDest= new HttpTransportDestination(getFormUrl);
//		message = new TransportMessage();
//		message.setPayloadData(new ByteArrayPayload("".getBytes(),null,IDataPayload.PAYLOAD_TYPE_TEXT)); // TODO change this to send xml msg with search options /uname, form type
//		message.setDestination(requestDest);
//		message.addObserver(this);
//
//		transportManager = (TransportManager)JavaRosaServiceProvider.instance().getTransportManager();
//		transportManager.send(message, TransportMethod.HTTP_GCF);
//
//	}
//
//	public void contextChanged(Context globalContext) {
//		context.mergeInContext(globalContext);
//
//	}
//
//	public void destroy() {
//		if(progressScreen!=null){
//			progressScreen.closeThread();
//			progressScreen =null;
//		}
//		if(transportManager!=null){
//			//transportManager.closeSend();
//			transportManager = null;
//
//		}
//
//	}
//
//	public Context getActivityContext() {
//		return context;
//	}
//
//	public void halt() {
//		// TODO Auto-generated method stub
//
//	}
//
//	public void resume(Context globalContext) {
//		// TODO Auto-generated method stub
//
//	}
//
//	public void start(Context context) {
//		this.context=context;
//		fetchForm();
//		//parent.setDisplay(this, new IView() {public Object getScreenObject() {return progressScreen;}});
//
//
//	}
//
//	public void setShell(IShell shell) {
//		this.parent = shell;
//
//	}
//
//	public void commandAction(Command command, Displayable display) {
//		if(display == progressScreen){
//			if(command==progressScreen.CMD_CANCEL){
//				parent.returnFromActivity(this, Constants.ACTIVITY_CANCEL, null);
//			}
//
//		}
//
//	}
//
//	public void update(Observable observable, Object arg) {
//		byte[] data = (byte[])arg;
//		process(data);
//
//	}
//
//	public void process(byte[] data) {
//		
//		//System.out.println("MYFORM:"+new String(data).trim());
//		FormDefRMSUtility formDef = (FormDefRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(FormDefRMSUtility.getUtilityName());
//
//		bin = new ByteArrayInputStream(data);
//		formDef.writeToRMS(XFormUtils.getFormFromInputStream(bin));
//		parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, null);
//
//	}
//	/* (non-Javadoc)
//	 * @see org.javarosa.core.api.IActivity#annotateCommand(org.javarosa.core.api.ICommand)
//	 */
//	public void annotateCommand(ICommand command) {
//		throw new RuntimeException("The Activity Class " + this.getClass().getName() + " Does Not Yet Implement the annotateCommand Interface Method. Please Implement It.");
//	}
//}
