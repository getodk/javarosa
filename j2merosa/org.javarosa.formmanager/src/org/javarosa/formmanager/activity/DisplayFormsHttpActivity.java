package org.javarosa.formmanager.activity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.javarosa.core.Context;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.ICommand;
import org.javarosa.core.api.IShell;
import org.javarosa.core.api.IView;
import org.javarosa.core.util.Observable;
import org.javarosa.core.util.Observer;
import org.javarosa.formmanager.view.AvailableFormsScreen;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import javax.microedition.lcdui.List;

public class DisplayFormsHttpActivity implements IActivity,CommandListener,Observer{
	private AvailableFormsScreen formList;
	private ByteArrayInputStream bin;

	private KXmlParser parser = new KXmlParser();
	private Vector items;
	private Hashtable formInfo;

	public static final String SELECTED_FORM = "selected_form";
	public static final String FORM_URL = "selected_form_url";

	private Context context;

	private IShell parent;


	public DisplayFormsHttpActivity(IShell parent,Hashtable args) {
		this.parent = parent;

		init(args);
	}

	public void init(Hashtable args){

		byte[] data = (byte[])args.get("returnval");

		try{
			bin = new ByteArrayInputStream(data);

			//setup parser
			parser.setInput(new InputStreamReader(bin));

			items = new Vector();
			formInfo = new Hashtable();

			processSurveyList(parser, formInfo);
			
			String[] formlist = new String[ formInfo.size() ];
			Enumeration e = formInfo.keys();//read available form names from hasttable
	
			for(int i=0;i<formInfo.size(); i++)
		{
			items.addElement(e.nextElement());
		}
		items.copyInto( formlist );

			formList = new AvailableFormsScreen("Available Forms",formlist,this);

		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
		}

	}

public void processSurveyList(KXmlParser parser, Hashtable formInfo) throws XmlPullParserException{
		
		try {
			boolean inItem = false;
			parser.nextTag();
			parser.require(XmlPullParser.START_TAG, null, "forms");
			while( parser.nextTag() != XmlPullParser.END_TAG ){
				//parser file names
				parser.require(XmlPullParser.START_TAG, null, null);
				
				String name = parser.getName();
				String url = parser.getAttributeValue(null, "url");
				String text = parser.nextText();
System.out.println("<"+name+">"+text+" "+url);				
				if(name.equals("form"))
					{
					inItem = true;
					//items.addElement(text);
					formInfo.put(text, url);
					}
				else
					inItem = false;

				parser.require(XmlPullParser.END_TAG, null, "form");
			}
			parser.require(XmlPullParser.END_TAG, null, "forms");
			
			parser.next();
			parser.require(XmlPullParser.END_DOCUMENT, null, null);

		} catch (IOException e) {
			// TODO: handle exception
			System.out.println("XML parser error");
			e.printStackTrace();

		}
	}
	public void contextChanged(Context globalContext) {
		context.mergeInContext(globalContext);

	}

	public void destroy() {
		//parent.exitShell();

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
		System.out.println("Before display form list");
		parent.setDisplay(this, new IView() {public Object getScreenObject() {return formList;}});
		System.out.println("After display form List");

	}

	public void commandAction(Command command, Displayable display) {
		if(display == formList){
			if (command == formList.CMD_CANCEL) {
				parent.returnFromActivity(this, Constants.ACTIVITY_CANCEL, null);
			}if(command == List.SELECT_COMMAND){

				String formName = formList.getString(formList.getSelectedIndex());
				String formurl = (String) formInfo.get(formName);
				System.out.println("OTTIMO!!" + formName+" "+formurl);
				Hashtable returnargs = new Hashtable();
				returnargs.put(SELECTED_FORM, formName);
				returnargs.put(FORM_URL, formurl);
				parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, returnargs);

			}
			//other commands	
		}

	}

	public void update(Observable observable, Object arg) {
		// TODO Auto-generated method stub

	}
	public void setShell(IShell shell) {
		this.parent = shell;

	}
	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#annotateCommand(org.javarosa.core.api.ICommand)
	 */
	public void annotateCommand(ICommand command) {
		throw new RuntimeException("The Activity Class " + this.getClass().getName() + " Does Not Yet Implement the annotateCommand Interface Method. Please Implement It.");
	}
}
