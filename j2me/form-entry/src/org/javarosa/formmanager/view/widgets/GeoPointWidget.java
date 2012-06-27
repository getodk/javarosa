/**
 * 
 */
package org.javarosa.formmanager.view.widgets;

import javax.microedition.lcdui.TextField;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.form.api.FormEntryPrompt;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.Item;
import de.enough.polish.util.MathUtil;

/**
 * TODO: Add support for a "capture" button
 * 
 * @author ctsims
 *
 */
public class GeoPointWidget extends ExpandedWidget {

	Container parent;
	private WidgetEscapeComponent wec = new WidgetEscapeComponent();

	
	protected TextField tfLat;
	protected TextField tfLon;
	protected TextField tfAlt;
	protected TextField tfAcc;
	
	public static Command captureCommand = new Command(Localization.get("menu.Capture"), Command.SCREEN, 2);

	public GeoPointWidget() {
		//#style leftlabeledTextField
		tfLat = new TextField(Localization.get("activity.locationcapture.Latitude")+":", "", 20, TextField.DECIMAL);
		//#style leftlabeledTextField
		tfLon = new TextField(Localization.get("activity.locationcapture.Longitude")+":", "", 20, TextField.DECIMAL);
		//#style leftlabeledTextField
		tfAlt = new TextField(Localization.get("activity.locationcapture.Altitude")+"(m):", "", 20, TextField.DECIMAL);
		//#style leftlabeledTextField
		tfAcc = new TextField(Localization.get("activity.locationcapture.Accuracy")+"(m):", "", 20, TextField.DECIMAL);

	}

	/* (non-Javadoc)
	 * @see org.javarosa.formmanager.view.chatterbox.widget.IWidgetStyle#widgetType()
	 */
	public int widgetType() {
		return Constants.CONTROL_INPUT;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.formmanager.view.chatterbox.widget.ExpandedWidget#getAnswerTemplate()
	 */
	protected IAnswerData getAnswerTemplate() {
		return new GeoPointData();
	}

	/* (non-Javadoc)
	 * @see org.javarosa.formmanager.view.chatterbox.widget.ExpandedWidget#getEntryWidget(org.javarosa.form.api.FormEntryPrompt)
	 */
	protected Item getEntryWidget(FormEntryPrompt prompt) {
		parent = new Container(true);
		parent.add(tfLat);
		tfLat.addCommand(captureCommand);
		parent.add(tfLon);
		tfLon.addCommand(captureCommand);
		parent.add(tfAlt);
		tfAlt.addCommand(captureCommand);
		parent.add(tfAcc);
		tfAcc.addCommand(captureCommand);
		
		parent.addCommand(captureCommand);
		parent.focusChild(0);
		return wec.wrapEntryWidget(parent);
	}
	
	public int getNextMode() {
		return wec.wrapNextMode(super.getNextMode());
	}
	
	public Item getInteractiveWidget() {
		return wec.wrapInteractiveWidget(super.getInteractiveWidget());
	}

	/* (non-Javadoc)
	 * @see org.javarosa.formmanager.view.chatterbox.widget.ExpandedWidget#updateWidget(org.javarosa.form.api.FormEntryPrompt)
	 */
	protected void updateWidget(FormEntryPrompt prompt) {
		if(prompt.getAnswerValue() == null) {
			//nothing
		} else {
			this.setWidgetValue(prompt.getAnswerValue().getValue());
		}

	}

	/* (non-Javadoc)
	 * @see org.javarosa.formmanager.view.chatterbox.widget.ExpandedWidget#setWidgetValue(java.lang.Object)
	 */
	protected void setWidgetValue(Object o) {
		double[] value = (double[])o; 
		tfLat.setString(""+MathUtil.round(value[0]*10000)/10000.00);
		tfLon.setString(""+MathUtil.round(value[1]*10000)/10000.00);
		tfAlt.setString(""+MathUtil.round(value[2]));
		tfAcc.setString(""+MathUtil.round(value[3]));
	}

	/* (non-Javadoc)
	 * @see org.javarosa.formmanager.view.chatterbox.widget.ExpandedWidget#getWidgetValue()
	 */
	protected IAnswerData getWidgetValue() {
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
