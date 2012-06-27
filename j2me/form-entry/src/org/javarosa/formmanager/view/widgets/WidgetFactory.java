/**
 * 
 */
package org.javarosa.formmanager.view.widgets;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.LongData;
import org.javarosa.core.util.externalizable.PrototypeFactoryDeprecated;

import de.enough.polish.ui.ChoiceGroup;
import de.enough.polish.ui.TextField;

/**
 * @author ctsims
 *
 */
public class WidgetFactory {
	
	PrototypeFactoryDeprecated widgetFactory;
	boolean optimizeEntry;
	
	public WidgetFactory(boolean optimizeEntry) {
		widgetFactory = new PrototypeFactoryDeprecated();
		this.optimizeEntry = optimizeEntry;
	}
	
	public void registerExtendedWidget(int controlType, IWidgetStyle prototype) {
		widgetFactory.addNewPrototype(String.valueOf(controlType), prototype.getClass());
	}
	
	public IWidgetStyleEditable getWidget(int controlType, int dataType, String appearanceAttr) {
		IWidgetStyleEditable expandedStyle = null;
		switch (controlType) {
		case Constants.CONTROL_INPUT:
		case Constants.CONTROL_SECRET:
			switch (dataType) {
			case Constants.DATATYPE_INTEGER:
				expandedStyle = new NumericEntryWidget();
				pw(controlType, (NumericEntryWidget)expandedStyle);
				break;
			case Constants.DATATYPE_LONG:
				expandedStyle = new NumericEntryWidget(false, new LongData());
				pw(controlType, (NumericEntryWidget)expandedStyle);
				break;
			case Constants.DATATYPE_DECIMAL:
				expandedStyle = new NumericEntryWidget(true, new DecimalData());
				pw(controlType, (NumericEntryWidget)expandedStyle);
				break;
			case Constants.DATATYPE_DATE_TIME:
				expandedStyle = new DateEntryWidget(true);
				break;
			case Constants.DATATYPE_DATE:
				//#if javarosa.useNewDatePicker 
				expandedStyle = new SimpleDateEntryWidget();
				//expandedStyle = new InlineDateEntryWidget();
				//#else
				expandedStyle = new DateEntryWidget();
				//#endif
				break;
			case Constants.DATATYPE_TIME:
				expandedStyle = new TimeEntryWidget();
				break;
			case Constants.DATATYPE_GEOPOINT:
				expandedStyle = new GeoPointWidget();
				break;
			}
			break;
		case Constants.CONTROL_SELECT_ONE:
			int style;

			if ("minimal".equals(appearanceAttr))
				style = ChoiceGroup.POPUP;
			else
				style = ChoiceGroup.EXCLUSIVE;

			expandedStyle = new SelectOneEntryWidget(style,optimizeEntry, !optimizeEntry);
			break;
		case Constants.CONTROL_SELECT_MULTI:
			expandedStyle = new SelectMultiEntryWidget(optimizeEntry, !optimizeEntry);
			break;
		case Constants.CONTROL_TEXTAREA:
			expandedStyle = new TextEntryWidget();
			break;
		case Constants.CONTROL_TRIGGER:
			expandedStyle = new MessageWidget();
			break;
		case Constants.CONTROL_IMAGE_CHOOSE:
			expandedStyle = new ImageChooserWidget();
			break;
		case Constants.CONTROL_AUDIO_CAPTURE:
			expandedStyle = new AudioCaptureWidget();
			break;	
		}

		if (expandedStyle == null) { //catch types text, null, unsupported
			expandedStyle = new TextEntryWidget();
			if(controlType == Constants.CONTROL_SECRET) {
				((TextEntryWidget)expandedStyle).setConstraint(TextField.PASSWORD);
			}
			
			String name = String.valueOf(controlType); //huh? controlType is an int
			Object widget = widgetFactory.getNewInstance(name);
			if (widget != null) {
				expandedStyle = (IWidgetStyleEditable) widget;
			}
		}
		return expandedStyle;
	}
	
	private void pw(int controlType, NumericEntryWidget w) {
		if(controlType == Constants.CONTROL_SECRET) {
			w.setConstraint(TextField.PASSWORD);
		}
	}

	public void setOptimizeEntry(boolean entryOptimized) {
		this.optimizeEntry = entryOptimized;
	}
}
