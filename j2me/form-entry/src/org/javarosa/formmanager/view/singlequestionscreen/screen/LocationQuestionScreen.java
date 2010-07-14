package org.javarosa.formmanager.view.singlequestionscreen.screen;

import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.j2me.view.J2MEDisplay;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.Style;
import de.enough.polish.util.MathUtil;

public class LocationQuestionScreen extends SingleQuestionScreen {

	protected StringItem label;
	protected TextField tfLat;
	protected TextField tfLon;
	protected TextField tfAlt;
	protected TextField tfAcc;
	
	public static Command captureCommand = new Command(Localization.get("menu.Capture"), Command.SCREEN, 2);

	public LocationQuestionScreen(FormEntryPrompt prompt, String groupName, Style style) {
		super(prompt,groupName,style);
	}

	public void createView() {
		setHint(Localization.get("activity.locationcapture.capturelocationhint"));
		
		label = new StringItem("", "");
		if (prompt.isRequired())
			label.setLabel("*" + prompt.getLongText());
		else
			label.setLabel(prompt.getLongText());
		this.append(label);
		
		//#style leftlabeledTextField
		tfLat = new TextField(Localization.get("activity.locationcapture.Latitude")+":", "", 20, TextField.DECIMAL);
		//#style leftlabeledTextField
		tfLon = new TextField(Localization.get("activity.locationcapture.Longitude")+":", "", 20, TextField.DECIMAL);
		//#style leftlabeledTextField
		tfAlt = new TextField(Localization.get("activity.locationcapture.Altitude")+"(m):", "", 20, TextField.DECIMAL);
		//#style leftlabeledTextField
		tfAcc = new TextField(Localization.get("activity.locationcapture.Accuracy")+"(m):", "", 20, TextField.DECIMAL);
		

		IAnswerData answerData = prompt.getAnswerValue();
		if ((answerData != null) && (answerData instanceof GeoPointData))
		{
			double[] gp = (double[])((GeoPointData)answerData).getValue();
			tfLat.setString(""+MathUtil.round(gp[0]*10000)/10000.00);
			tfLon.setString(""+MathUtil.round(gp[1]*10000)/10000.00);
			tfAlt.setString(""+MathUtil.round(gp[2]));
			tfAcc.setString(""+MathUtil.round(gp[3]));
		}

		this.append(tfLat);
		this.append(tfLon);
		this.append(tfAlt);
		this.append(tfAcc);
		
		this.addNavigationWidgets();
		if (prompt.getHelpText() != null) {
			setHint(prompt.getHelpText());
		}
		
		this.addCommand(captureCommand);
	}

	public IAnswerData getWidgetValue() {
		double[] gp = new double[4];

		if ((tfLat.getString() == null)
				|| (tfLat.getString().trim().equals(""))
				|| (tfLon.getString() == null)
				|| (tfLon.getString().trim().equals(""))) {
			return null;
		}

		try {

			gp[0] = Double.parseDouble(tfLat.getString());
			gp[1] = Double.parseDouble(tfLon.getString());
		} catch (NumberFormatException nfe) {
			System.err.println("Non-numeric data in numeric entry field!");
			return null;
		}

		try {
			gp[2] = Double.parseDouble(tfAlt.getString());
		} catch (NumberFormatException nfe) {
			gp[2] = Double.NaN;
			System.err
					.println("Non-numeric or no data in Altitude field; skipping");
		}

		try {
			gp[3] = Double.parseDouble(tfAcc.getString());
		} catch (NumberFormatException nfe) {
			gp[3] = Double.NaN;
			System.err
					.println("Non-numeric or no data in Accuracy field; skipping");
		}
		return new GeoPointData(gp);

	}

}

