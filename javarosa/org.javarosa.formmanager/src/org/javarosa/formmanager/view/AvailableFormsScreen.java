//package org.javarosa.formmanager.view;
//
//import javax.microedition.lcdui.Command;
//import javax.microedition.lcdui.List;
//
//import org.javarosa.formmanager.activity.DisplayFormsHttpActivity;
//
//public class AvailableFormsScreen extends List{
//	private DisplayFormsHttpActivity parentActivity;
//	
//	public final Command CMD_CANCEL = new Command("Cancel",Command.BACK, 1);
//
//	public AvailableFormsScreen(String label,String[] elements,DisplayFormsHttpActivity parentActivity) {
//		super(label, List.IMPLICIT,elements,null);
//		addCommand(CMD_CANCEL);
//		this.parentActivity = parentActivity;
//		setCommandListener(parentActivity);
//	}
//
//}
