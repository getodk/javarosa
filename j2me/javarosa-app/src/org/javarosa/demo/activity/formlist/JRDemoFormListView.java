package org.javarosa.demo.activity.formlist;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.formmanager.view.chatterbox.widget.LabelWidget;

import de.enough.polish.ui.TextField;


public class JRDemoFormListView extends List {

	public final Command CMD_VIEW_SAVED = new Command(Localization.get("jrdemo.formlist.command.viewsaved"), Command.OK, 5);
	public final Command CMD_EXIT = new Command(Localization.get("polish.command.exit"), Command.EXIT, 8);
	public final Command CMD_DOWNLOAD_FORMS = new Command(Localization.get("jrdemo.formlist.command.downloadforms"),Command.EXIT,2);
	public final Command CMD_SETTINGS = new Command(Localization.get("menu.Settings"),Command.SCREEN,5);

	Image img;
	
	public JRDemoFormListView (String title, Vector formNames, boolean admin)  {
		
		super(title, List.IMPLICIT);
	   
		for (int i = 0; i < formNames.size(); i++) {
			//#style formSelected
			append((String)formNames.elementAt(i), null);
		}
//		try { 
		
		//#style downloadNewForms
		this.append(Localization.get("jrdemo.formlist.command.downloadforms"), null);//Image.createImage("/download_form_en.jpg"));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		addCommand(CMD_VIEW_SAVED);
		addCommand(CMD_SETTINGS);
		addCommand(CMD_EXIT);
		
	}
	
}
