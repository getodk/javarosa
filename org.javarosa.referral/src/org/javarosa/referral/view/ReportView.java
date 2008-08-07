package org.javarosa.referral.view;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;

public class ReportView extends Form {

	private StringItem label;
	public ReportView(String title) {
		super(title);
		this.addCommand(new Command("Done", Command.SCREEN, 1));
	}
	public void setReferrals(Vector strings) {
		this.deleteAll();
		if (strings.size() > 0) {
			//#style title
			label = new StringItem("", "Reasons for Referral");
			this.append(label);
			Enumeration en = strings.elements();
			while (en.hasMoreElements()) {
				String s = (String) en.nextElement();
				this.append(new StringItem(s, ""));
			}
		} else {
			label = new StringItem("No Referral Necessary", "");
			this.append(label);
		}
	}
	
	
}
