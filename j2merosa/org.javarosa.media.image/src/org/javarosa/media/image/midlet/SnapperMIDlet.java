/* License
* Modifications by Andres Monroy-Hernandez, 2008
 * 
 * Copyright 1994-2004 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *  
 *  * Redistribution of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * 
 *  * Redistribution in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *  
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *  
 * You acknowledge that this software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility. 
 */

package org.javarosa.media.image.midlet;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.DateField;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.TextField;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;
import javax.microedition.midlet.MIDlet;

import org.javarosa.media.image.view.CameraCanvas;

public class SnapperMIDlet extends MIDlet implements CommandListener {
	private Display mDisplay;

	private Form mMainForm;

	private Command mExitCommand, mCameraCommand;

	private Command mBackCommand, mCaptureCommand;

	private Player mPlayer;

	private VideoControl mVideoControl;

	private static int imgnum = 0;

	private Grabber grabber = null;

	private TextField clinicLocation, textURL, textINT, textQuality,
			patientAge;

	private java.util.Calendar calendar = java.util.Calendar
			.getInstance(java.util.TimeZone.getDefault());

	private ChoiceGroup hivStatus, arvStatus, cd4Status, prevLesion,
			prevTreatment, nurseImpression, treatmentPlan;

	private Alert alert = new Alert("Snapper Alert");

	// change this to true for emulator use
	private final static boolean emulator = false;

	public SnapperMIDlet() {
		mExitCommand = new Command("Exit", Command.EXIT, 0);
		mCameraCommand = new Command("Camera", Command.SCREEN, 0);
		mBackCommand = new Command("Back", Command.BACK, 0);
		mCaptureCommand = new Command("Capture", Command.SCREEN, 0);

		Date today = new Date(System.currentTimeMillis());
		DateField datefield = new DateField("", DateField.DATE_TIME);
		datefield.setDate(today);

		hivStatus = new ChoiceGroup("HIV Status", Choice.EXCLUSIVE);
		hivStatus.append("Positive", null);
		hivStatus.append("Negative", null);
		hivStatus.append("Not tested", null);

		arvStatus = new ChoiceGroup("ARV Status", Choice.EXCLUSIVE);
		arvStatus.append("On ARV", null);
		arvStatus.append("Not on ARV", null);

		cd4Status = new ChoiceGroup("CD4 count", Choice.EXCLUSIVE);
		cd4Status.append("<100", null);
		cd4Status.append("100-200", null);
		cd4Status.append(">200", null);

		prevLesion = new ChoiceGroup("Previous cervical lesion",
				Choice.EXCLUSIVE);
		prevLesion.append("Yes", null);
		prevLesion.append("No", null);

		prevTreatment = new ChoiceGroup("Previous treatment", Choice.EXCLUSIVE);
		prevTreatment.append("Antibiotics", null);
		prevTreatment.append("Cryotherapy", null);
		prevTreatment.append("Other", null);

		nurseImpression = new ChoiceGroup("Nurse's impression",
				Choice.EXCLUSIVE);
		nurseImpression.append("Normal", null);
		nurseImpression.append("Pre-cancerous", null);
		nurseImpression.append("Cervical Cancer", null);
		nurseImpression.append("Infection", null);
		nurseImpression.append("Others", null);

		treatmentPlan = new ChoiceGroup("Treatment Plan", Choice.EXCLUSIVE);
		treatmentPlan.append("No treatment necessary", null);
		treatmentPlan.append("No treatment now. Needs repeat.", null);
		treatmentPlan.append("Refer to hospital", null);
		treatmentPlan.append("Cryotherapy", null);
		treatmentPlan.append("Antibiotics", null);
		treatmentPlan.append("Other", null);

		clinicLocation = new TextField("Clinic location", "Lusaka", 10,
				TextField.ANY); //, TextField.ANY);
		patientAge = new TextField("Patient Age", "0", 10, TextField.NUMERIC); //, TextField.ANY);

		mMainForm = new Form("Patient Information");
		mMainForm.addCommand(mExitCommand);
		String supports = System.getProperty("video.snapshot.encodings");
		if (supports != null && supports.length() > 0) {
			mMainForm.append(datefield);
			mMainForm.append(clinicLocation);
			mMainForm.append(patientAge);
			mMainForm.append(hivStatus);
			mMainForm.append(arvStatus);
			mMainForm.append(prevLesion);
			mMainForm.append(prevTreatment);
			mMainForm.append(nurseImpression);
			mMainForm.append(treatmentPlan);

			mMainForm.append(textURL = new TextField("URL",
					(emulator ? "http://localhost:9000/up.php"
							: "http://yourserver.com/up.php"), 64,
					TextField.URL));

			mMainForm.append(textINT = new TextField("Interval ", "999", 5,
					TextField.NUMERIC));
			mMainForm.append(textQuality = new TextField("Quality ", "10", 5,
					TextField.NUMERIC));
			mMainForm.addCommand(mCameraCommand);
		} else
			mMainForm
					.append("Snapper cannot use this device to take pictures.");
		mMainForm.setCommandListener(this);
	}

	public void startApp() {
		mDisplay = Display.getDisplay(this);
		mDisplay.setCurrent(mMainForm);
	}

	public void pauseApp() {
	}

	public void destroyApp(boolean unconditional) {
	}

	public void commandAction(Command c, Displayable s) {
		if (c.getCommandType() == Command.EXIT) {
			destroyApp(true);
			notifyDestroyed();
		} else if (c == mCameraCommand) {
			showCamera();
		} else if (c == mBackCommand) {
			if (null != grabber) {
				grabber.quit();
				// wait grabber to finish its job
				//try {
				//	grabber.join();
				//} catch (Exception e) {}
				mDisplay.setCurrent(mMainForm);
			}
		} else if (c == mCaptureCommand) {
			String url = textURL.getString().trim()
					+ "?clinic_location="
					+ clinicLocation.getString()
					+ "&patient_age="
					+ patientAge.getString()								
					+ "&hiv_status="
					+ hivStatus.getString(hivStatus.getSelectedIndex())
					+ "&arv_status="
					+ arvStatus.getString(arvStatus.getSelectedIndex())
					+ "&prev_lesion="
					+ prevLesion.getString(prevLesion.getSelectedIndex())
					+ "&prev_treatment="
					+ prevTreatment.getString(prevTreatment.getSelectedIndex())
					+ "&nurse_impression="
					+ nurseImpression.getString(nurseImpression.getSelectedIndex()) 
					+ "&treatment_plan="
					+ treatmentPlan.getString(treatmentPlan.getSelectedIndex());
					
			while (url.indexOf(' ') > 0) {
				url = url.replace(' ', '+');
			}
			System.out.println("url = " + url);
			grabber = new Grabber(url, Integer.parseInt(textINT.getString()
					.trim()), Integer.parseInt(textQuality.getString().trim()));			
		}
	}

	private class Grabber extends Canvas implements Runnable{
		private int interval, quality;
		private String url;
		private boolean running;
		private int height,width;

		Grabber(String url, int interval, int quality) {
			this.url = url;
			this.interval = interval < 1 ? 1000 : 1000 * interval;
			this.quality = quality <= 0 && quality <= 100 ? quality : 50;
			Thread t = new Thread(this);
			t.start();
		}
		
		public void paint(Graphics g) {
			height = getHeight();
			width = getWidth();
		}
		
		public void run() {
			System.out.println("Run started");
			running = true;
			try {
				while (running) {
					System.out.println("Running");
					boolean status;

					if (emulator)
						status = capture_emulator(url);
					else
						status = capture(url, quality);

					if (false == status)
						running = false;

					if (running) {
						try {
							showCamera();
							Thread.sleep(interval);
						} catch (Exception e) {
						}
					}
				}
			} finally {
				// Shut down the player when this thread finishes
				mPlayer.close();
				mPlayer = null;
				mVideoControl = null;
			}
			System.out.println("Run ended");
		}

		private boolean capture(String url, int quality) {
			byte[] jpg;
			try {
				// Get the image.
				jpg = mVideoControl.getSnapshot("encoding=jpeg&quality="+ quality);
			} catch (MediaException me) {
				handleException(me);
				jpg = null;
			}

			if (null != jpg) {
				boolean saved = saveImage(getDateTime() + ".jpg", jpg);
				if (saved)
					httpUpload(url, getDateTime() + ".jpg", jpg);
			}
			return true;
		}

		public void quit() {
			running = false;
		}

		public boolean saveImage(String file_name, byte[] jpg) {
			boolean isSaved = false;
			Enumeration root = null;
			String restorepath = "";
			String rootName = "";
			OutputStream fos = null;
			FileConnection fc = null;
			FileConnection fw = null;

			root = FileSystemRegistry.listRoots();
			while (root.hasMoreElements()) {
				rootName = (String) root.nextElement();
			}
			
			restorepath = "file:///" + rootName + "backup";				
			try {
				fc = (FileConnection) Connector.open(restorepath);
				if (!fc.exists())
					fc.mkdir();
				fc.close();
			}
			catch (Exception ex) {
				showAlert("Error - Folder not created - 1 : " + ex.getMessage());
			}
			finally {
				try {
					if (fc != null)
						fc.close();
				}
				catch(Exception e) {}
			}
			try {
				fw = (FileConnection) Connector.open(restorepath + "/"
						+ file_name);
				if (!fw.exists()) {
					fw.create();
				}				
				fos = fw.openOutputStream();
				fos.write(jpg);
				isSaved = true;
			} catch (Exception ex) {				
				showAlert("Error - File is not writable - 1 " + ex.getMessage());
				ex.printStackTrace();
			} 
			finally {
				try {					
					if (fos != null) {
						fos.flush();
						fos.close();
					}
					if (fw != null)
						fw.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			restorepath = "file://localhost/" + rootName + "backup";
			try {
				fc = (FileConnection) Connector.open(restorepath);
				if (!fc.exists())
					fc.mkdir();
				fc.close();
			}
			catch (Exception ex) {				
				showAlert("Error - Folder not created - 2 " + ex.getMessage());
			} 
			finally {
				try {
					if (fc != null)
						fc.close();
				}
				catch(Exception e) {}
			}
			
			try {
				fw = (FileConnection) Connector.open(restorepath + "/"
						+ file_name);
				if (!fw.exists()) {
					fw.create();
				}				
				fos = fw.openOutputStream();
				fos.write(jpg);
				isSaved = true;
			} catch (Exception ex) {				
				showAlert("Error - File is not writable - 2 " + ex.getMessage());
				ex.printStackTrace();
			} 
			finally {
				try {					
					if (fos != null) {
						fos.flush();
						fos.close();
					}
					if (fw != null)
						fw.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return isSaved;
		}

		public void showAlert(String error) {
			alert.setString(error);
			alert.setTimeout(Alert.FOREVER);
			alert.setType(AlertType.ERROR);
			mDisplay.setCurrent(alert);
		}

		private boolean capture_emulator(String url) {
			boolean success = false;
			if (emulator) {
				// JPEG 10x8 10x8+0+0 DirectClass 363
				byte[] dummy = { (byte) 255, (byte) 216, (byte) 255,
						(byte) 224, (byte) 0, (byte) 16, (byte) 74, (byte) 70,
						(byte) 73, (byte) 70, (byte) 0, (byte) 1, (byte) 1,
						(byte) 1, (byte) 0, (byte) 72, (byte) 0, (byte) 72,
						(byte) 0, (byte) 0, (byte) 255, (byte) 219, (byte) 0,
						(byte) 67, (byte) 0, (byte) 8, (byte) 6, (byte) 6,
						(byte) 7, (byte) 6, (byte) 5, (byte) 8, (byte) 7,
						(byte) 7, (byte) 7, (byte) 9, (byte) 9, (byte) 8,
						(byte) 10, (byte) 12, (byte) 20, (byte) 13, (byte) 12,
						(byte) 11, (byte) 11, (byte) 12, (byte) 25, (byte) 18,
						(byte) 19, (byte) 15, (byte) 20, (byte) 29, (byte) 26,
						(byte) 31, (byte) 30, (byte) 29, (byte) 26, (byte) 28,
						(byte) 28, (byte) 32, (byte) 36, (byte) 46, (byte) 39,
						(byte) 32, (byte) 34, (byte) 44, (byte) 35, (byte) 28,
						(byte) 28, (byte) 40, (byte) 55, (byte) 41, (byte) 44,
						(byte) 48, (byte) 49, (byte) 52, (byte) 52, (byte) 52,
						(byte) 31, (byte) 39, (byte) 57, (byte) 61, (byte) 56,
						(byte) 50, (byte) 60, (byte) 46, (byte) 51, (byte) 52,
						(byte) 50, (byte) 255, (byte) 219, (byte) 0, (byte) 67,
						(byte) 1, (byte) 9, (byte) 9, (byte) 9, (byte) 12,
						(byte) 11, (byte) 12, (byte) 24, (byte) 13, (byte) 13,
						(byte) 24, (byte) 50, (byte) 33, (byte) 28, (byte) 33,
						(byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50,
						(byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50,
						(byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50,
						(byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50,
						(byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50,
						(byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50,
						(byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50,
						(byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50,
						(byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50,
						(byte) 50, (byte) 50, (byte) 50, (byte) 50, (byte) 50,
						(byte) 255, (byte) 192, (byte) 0, (byte) 17, (byte) 8,
						(byte) 0, (byte) 8, (byte) 0, (byte) 10, (byte) 3,
						(byte) 1, (byte) 17, (byte) 0, (byte) 2, (byte) 17,
						(byte) 1, (byte) 3, (byte) 17, (byte) 1, (byte) 255,
						(byte) 196, (byte) 0, (byte) 21, (byte) 0, (byte) 1,
						(byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
						(byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
						(byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
						(byte) 2, (byte) 5, (byte) 255, (byte) 196, (byte) 0,
						(byte) 30, (byte) 16, (byte) 0, (byte) 2, (byte) 2,
						(byte) 2, (byte) 2, (byte) 3, (byte) 0, (byte) 0,
						(byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
						(byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 2,
						(byte) 4, (byte) 17, (byte) 3, (byte) 5, (byte) 0,
						(byte) 6, (byte) 65, (byte) 97, (byte) 209, (byte) 255,
						(byte) 196, (byte) 0, (byte) 22, (byte) 1, (byte) 1,
						(byte) 1, (byte) 1, (byte) 0, (byte) 0, (byte) 0,
						(byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
						(byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
						(byte) 3, (byte) 1, (byte) 4, (byte) 255, (byte) 196,
						(byte) 0, (byte) 28, (byte) 17, (byte) 1, (byte) 0,
						(byte) 1, (byte) 4, (byte) 3, (byte) 0, (byte) 0,
						(byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
						(byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1,
						(byte) 0, (byte) 2, (byte) 3, (byte) 4, (byte) 17,
						(byte) 19, (byte) 49, (byte) 65, (byte) 255,
						(byte) 218, (byte) 0, (byte) 12, (byte) 3, (byte) 1,
						(byte) 0, (byte) 2, (byte) 17, (byte) 3, (byte) 17,
						(byte) 0, (byte) 63, (byte) 0, (byte) 93, (byte) 142,
						(byte) 78, (byte) 207, (byte) 85, (byte) 3, (byte) 44,
						(byte) 56, (byte) 115, (byte) 228, (byte) 28,
						(byte) 165, (byte) 202, (byte) 187, (byte) 176,
						(byte) 80, (byte) 89, (byte) 104, (byte) 21,
						(byte) 187, (byte) 187, (byte) 34, (byte) 234,
						(byte) 254, (byte) 243, (byte) 55, (byte) 2,
						(byte) 118, (byte) 236, (byte) 98, (byte) 55,
						(byte) 138, (byte) 141, (byte) 6, (byte) 146,
						(byte) 72, (byte) 94, (byte) 255, (byte) 0, (byte) 49,
						(byte) 84, (byte) 12, (byte) 207, (byte) 170,
						(byte) 124, (byte) 160, (byte) 83, (byte) 179,
						(byte) 225, (byte) 182, (byte) 39, (byte) 201,
						(byte) 62, (byte) 248, (byte) 110, (byte) 45,
						(byte) 75, (byte) 236, (byte) 165, (byte) 226,
						(byte) 127, (byte) 255, (byte) 217 };

				try {
					success = httpUpload(url, getDateTime() + ".jpg", dummy);
				} catch (Exception e) {
					e.printStackTrace();

				}
			}
			return success;
		}
	}

	private void showCamera() {
		try {
			mPlayer = Manager.createPlayer("capture://video");
			mPlayer.realize();

			mVideoControl = (VideoControl) mPlayer.getControl("VideoControl");

			Canvas canvas = new CameraCanvas(this, mVideoControl);
			canvas.addCommand(mBackCommand);
			canvas.addCommand(mCaptureCommand);
			canvas.setCommandListener(this);
			mDisplay.setCurrent(canvas);

			/*
			 Form form = new Form("Camera form");
			 Item item = (Item)mVideoControl.initDisplayMode(
			 GUIControl.USE_GUI_PRIMITIVE, null);
			 form.append(item);
			 form.addCommand(mBackCommand);
			 form.addCommand(mCaptureCommand);
			 form.setCommandListener(this);
			 mDisplay.setCurrent(form);
			 */

			mPlayer.start();
		} catch (IOException ioe) {
			handleException(ioe);
		} catch (MediaException me) {
			handleException(me);
		}
	}

	// Multipart request
	//    - http://www.faqs.org/rfcs/rfc2388.html
	//    - http://www.faqs.org/rfcs/rfc2046.html
	// http://www.devx.com/getHelpOn/10MinuteSolution/16646/0/page/3
	// http://developers.sun.com/mobility/midp/ttips/HTTPPost/
	// http://www.devx.com/Java/Article/17679/1954

	// FIXME create upload queue and upload in separate thread
	private boolean httpUpload(String url, String fname, byte[] buf) {
		boolean success = false;
		int rc = 0;

		if (1 > buf.length)
			return false;

		StringBuffer resp = new StringBuffer();
		HttpConnection conn = null;
		OutputStream os = null;
		InputStream is = null;
		String CRLF = "\r\n";
		String boundary = "c0ffeec0ffeec0ffeekossu";
		mVideoControl.setVisible(false);

		try {
			conn = (HttpConnection) Connector.open(url, Connector.READ_WRITE);
			conn.setRequestMethod(HttpConnection.POST);
			conn.setRequestProperty("Content-Type",
					"multipart/form-data; boundary=" + boundary);

			String part1 = "--"
					+ boundary
					+ CRLF
					+ "Content-Disposition: form-data; name=\"imgfile\"; filename=\""
					+ fname + "\"" + CRLF + "Content-Type: image/jpeg" + CRLF
					+ CRLF;

			String part2 = CRLF + "--" + boundary + "--" + CRLF;

			// FIXME conn.setRequestProperty("Content-Length", ...  (maybe???)

			// open stream and write preceding multipart boundary
			os = conn.openOutputStream();
			os.write(part1.getBytes());

			int i = 0, chunk = 1024;

			do {
				os.write(buf, i, (buf.length < chunk + i ? buf.length - i
						: chunk));
				i += chunk;
			} while (i < buf.length);

			// boundary delimiter line following the last body part
			os.write(part2.getBytes());
			os.flush();

			rc = conn.getResponseCode();

			if (emulator) {
				System.out.println("RESP CODE " + rc);
			}

			success = (HttpConnection.HTTP_OK == rc);

			// FIXME server script should not produce output if success, error mgs otherwise

			is = new DataInputStream(conn.openInputStream());

			int ch;
			while ((ch = is.read()) != -1) {
				resp = resp.append((char) ch);
			}

		} catch (Exception e) {
			if (emulator)
				e.printStackTrace();
			handleException(e);
		} finally {
			try {
				os.close();
			} catch (Exception e) {
			}
			try {
				is.close();
			} catch (Exception e) {
			}
			try {
				conn.close();
			} catch (Exception e) {
			}
		}

		if (emulator)
			System.out.println("RESPONSE: " + resp.toString());

		if (!success) {
			Alert a = new Alert("" + rc, resp.toString(), null, null);
			a.setTimeout(Alert.FOREVER);
			mDisplay.setCurrent(a, mMainForm);
		}

		return success;
	}

	private String getDateTime() {

		calendar.setTime(new Date());

		String s = "" + calendar.get(Calendar.YEAR);

		int x = calendar.get(Calendar.MONTH);
		if (x < 9)
			s += "0";
		s += "" + (1 + x);

		x = calendar.get(Calendar.DAY_OF_MONTH);
		if (x < 10)
			s += "0";
		s += "" + x + "T";

		x = calendar.get(Calendar.HOUR_OF_DAY);
		if (x < 10)
			s += "0";
		s += "" + x;

		x = calendar.get(Calendar.MINUTE);
		if (x < 10)
			s += "0";
		s += "" + x;

		x = calendar.get(Calendar.SECOND);
		if (x < 10)
			s += "0";
		s += "" + x;

		return s;
	}

	// FIXME

	/*
	 private boolean writeFile(String path, byte[] data) {
	 javax.microedition.io.Connection c = null;
	 java.io.OutputStream os = null;
	 try {
	 c = javax.microedition.io.Connector.open("file:///" + path, javax.microedition.io.Connector.READ_WRITE);
	 javax.microedition.io.file.FileConnection fc = 
	 (javax.microedition.io.file.FileConnection) c;
	 if (!fc.exists())
	 fc.create();
	 else
	 fc.truncate(0);
	 os = fc.openOutputStream();
	 os.write(data);
	 os.flush();
	 return true;
	 } catch (Exception e) {
	 handleException(e);
	 return false;
	 } finally {
	 try {
	 if (os != null)
	 os.close();
	 if (c != null)
	 c.close();
	 } catch (Exception ex) {
	 handleException(ex);
	 ex.printStackTrace();
	 }
	 }
	 }
	 */

	private void handleException(Exception e) {
		Alert a = new Alert("Exception", e.toString(), null, null);
		a.setTimeout(Alert.FOREVER);
		mDisplay.setCurrent(a, mMainForm);
	}

	/*
	 private Image createThumbnail(Image image) {
	 int sourceWidth = image.getWidth();
	 int sourceHeight = image.getHeight();
	 
	 int thumbWidth = 64;
	 int thumbHeight = -1;
	 
	 if (thumbHeight == -1)
	 thumbHeight = thumbWidth * sourceHeight / sourceWidth;
	 
	 Image thumb = Image.createImage(thumbWidth, thumbHeight);
	 Graphics g = thumb.getGraphics();
	 
	 for (int y = 0; y < thumbHeight; y++) {
	 for (int x = 0; x < thumbWidth; x++) {
	 g.setClip(x, y, 1, 1);
	 int dx = x * sourceWidth / thumbWidth;
	 int dy = y * sourceHeight / thumbHeight;
	 g.drawImage(image, x - dx, y - dy, Graphics.LEFT | Graphics.TOP);
	 }
	 }
	 
	 Image immutableThumb = Image.createImage(thumb);
	 
	 return immutableThumb;
	 }
	 */
}
