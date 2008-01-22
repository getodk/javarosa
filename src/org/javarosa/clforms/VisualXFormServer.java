package org.javarosa.clforms;

import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Ticker;

import org.javarosa.clforms.storage.XFormMetaData;
import org.javarosa.clforms.storage.XFormRMSUtility;

public class VisualXFormServer implements CommandListener {

	private final Command CMD_BACK = new Command("Back", Command.BACK, 2);
	private final List xformsList = new List("Select Forms to share.", List.IMPLICIT);
	private TransportShell mainShell;
	private Vector xformMetas;
	private Image selectedImage;
	private Image unselectedImage;
	private boolean[] publicised;
//	private XFormServerBT xformBTServer;
	private XFormRMSUtility xformRMSUtility;

	VisualXFormServer(TransportShell mainShell) {
		this.xformRMSUtility = mainShell.getXFormRMSUtility();
		this.mainShell = mainShell;
//		xformBTServer = new XFormServerBT(this);
		selectedImage = initialiseStateImage(12, 12, 0, 0, 255);
		unselectedImage = initialiseStateImage(12, 12, 255, 0, 0);
		this.createAvailableXFormList();
		publicised = new boolean[xformsList.size()];

		xformsList.addCommand(this.CMD_BACK);
		xformsList.setCommandListener(this);
	}

	public void commandAction(Command c, Displayable d) {
		if ((c == CMD_BACK) && (d == xformsList)) {
			destroy();
			mainShell.createView();
			return;
		}

		int selectedIndex = xformsList.getSelectedIndex();

		if (c == List.SELECT_COMMAND) {
			publicised[selectedIndex] = !publicised[selectedIndex];
			changePublishStatus(selectedIndex);
			return;
		}

	}

	private void changePublishStatus(int selectedIndex) {

		boolean result = false;
//		xformBTServer.changeXFormInfo(xformsList
//				.getString(selectedIndex), publicised[selectedIndex]);

		if (result) {
			Image stateImg = (publicised[selectedIndex]) ? selectedImage
					: unselectedImage;
			xformsList.set(selectedIndex, xformsList.getString(selectedIndex),
					stateImg);
		} else {
			// either a bad record or SDDB is busy
			javax.microedition.lcdui.Alert al = new javax.microedition.lcdui.Alert("Error", "Error publicising", null,
					AlertType.ERROR);
			al.setTimeout(TransportShell.ALERT_TIMEOUT);
			Display.getDisplay(mainShell).setCurrent(al, xformsList);

			// restore internal information
			publicised[selectedIndex] = !publicised[selectedIndex];
		}

	}

	public void initialise(boolean isReady) {

		if (isReady) {
			Ticker t = new Ticker("Choose XForms you want to publicise...");
			xformsList.setTicker(t);
			Display.getDisplay(mainShell).setCurrent(xformsList);
			return;
		}

		javax.microedition.lcdui.Alert alert = new javax.microedition.lcdui.Alert("Error", "Error initialising bluetooth.", null,
				AlertType.ERROR);
		alert.setTimeout(TransportShell.ALERT_TIMEOUT);
		Display.getDisplay(mainShell).setCurrent(alert,
				mainShell.getDisplayable());
	}

	public void destroy() {
		// finalize the image server work
	//	xformBTServer.destroy();
	}

	public int getXFormReference(String xformRef) {
		if (xformRef == null) {
			return -1;
		}

		// no interface in List to get the index - should find
		int listIndex = -1;
		for (int i = 0; i < xformsList.size(); i++) {
			if (xformsList.getString(i).equals(xformRef)) {
				listIndex = i;
				break;
			}
		}

		int recordIndex = -1;
		for (int i = 0; i < xformMetas.size(); i++) {
			XFormMetaData meta = (XFormMetaData) xformMetas.elementAt(i);
			//
			if (meta.getName().equals(xformRef)) {
				recordIndex = meta.getRecordId();
				break;
			}
		}

		// not found or not publicised
		if ((listIndex == -1) || !publicised[listIndex]) {
			if (listIndex == -1) {
				System.out.println("Form not found");
			} else if (!publicised[listIndex]) {
				System.out.println("Form not publicised");
			}
			return -1;
		}
		System.out.println("INDEX : " + recordIndex);
		return (recordIndex);
	}

	private void createAvailableXFormList() {
		xformMetas = this.xformRMSUtility.getXformMetaDataList();
		for (int i = 0; i < this.xformMetas.size(); i++) {
			String label = ((XFormMetaData) xformMetas.elementAt(i)).getName();
			xformsList.append(label, unselectedImage);
		}
		xformsList.setCommandListener(this);
	}

	private Image initialiseStateImage(int w, int h, int r, int g, int b) {
		Image res = Image.createImage(w, h);
		Graphics gc = res.getGraphics();
		gc.setColor(r, g, b);
		gc.fillRect(0, 0, w, h);
		return res;
	}

	public XFormRMSUtility getXFormRMSUtility() {
		return this.xformRMSUtility;
	}

	public TransportShell getMainShell() {
		return this.mainShell;
	}
}