package org.javarosa.referral.view;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Command;

import de.enough.polish.ui.FramedForm;
import de.enough.polish.ui.IconItem;

public class ReportView extends FramedForm {

	private IconItem label;
	public ReportView(String title) {
		super(title);
		this.addCommand(new Command("Done", Command.SCREEN, 1));
	}
	public void setReferrals(Vector strings) {
		this.deleteAll();
		//#style title
		label = new IconItem("Reasons for Referral", null);
		this.append(label);
		Enumeration en = strings.elements();
		while(en.hasMoreElements()){
			String s = (String)en.nextElement();
			this.append(new IconItem(s,null));
		}
	}
	
	
}
