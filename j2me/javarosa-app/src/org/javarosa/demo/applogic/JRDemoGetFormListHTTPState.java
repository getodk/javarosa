package org.javarosa.demo.applogic;

import java.io.UnsupportedEncodingException;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;

import org.javarosa.core.api.State;
import org.javarosa.core.log.FatalException;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.demo.util.ProgressScreenFormDownload;
import org.javarosa.formmanager.api.transitions.HttpFetchTransitions;
import org.javarosa.j2me.log.CrashHandler;
import org.javarosa.j2me.log.HandledCommandListener;
import org.javarosa.j2me.view.J2MEDisplay;
import org.javarosa.services.transport.TransportListener;
import org.javarosa.services.transport.TransportMessage;
import org.javarosa.services.transport.TransportService;
import org.javarosa.services.transport.impl.TransportException;
import org.javarosa.services.transport.impl.TransportMessageStatus;
import org.javarosa.services.transport.impl.simplehttp.SimpleHttpTransportMessage;
import org.javarosa.services.transport.senders.SenderThread;

public class JRDemoGetFormListHTTPState implements State,
		HandledCommandListener, TransportListener, HttpFetchTransitions {
	private ProgressScreenFormDownload progressScreen = new ProgressScreenFormDownload(
			Localization.get("jrdemo.searching"), Localization.get("jrdemo.contacting"), this);

	private String getListUrl;

	private SenderThread thread;

	private String response;

	public JRDemoGetFormListHTTPState(String url) {
		this.getListUrl = url;
	}

	public void SetURL(String url) {
		getListUrl = url;
	}

	public String getUrl() {
		return getListUrl;
	}

	public String getUserName() {
		return "";
	}

	public void start() {
		J2MEDisplay.setView(progressScreen);
		fetchList();
	}

	public void fetchList() {
		SimpleHttpTransportMessage message = new SimpleHttpTransportMessage(getListUrl);
		message.setOpenRosaApiVersion(null);

		try {
			thread = TransportService.send(message, 1, 0);// only one try if
															// we're going to
															// give a retry
															// option
			thread.addListener(this);
		} catch (TransportException e) {
			fail("Error Downloading List! Transport Exception while downloading forms list "
					+ e.getMessage());
		}
	}

	private void fail(String message) {
		progressScreen.setText(message);
		progressScreen.stopProgressBar();
		progressScreen.addCommand(progressScreen.CMD_RETRY);

	}

	public void commandAction(Command c, Displayable d) {
		CrashHandler.commandAction(this, c, d);
	}

	public void _commandAction(Command command, Displayable display) {
		if (display == progressScreen) {
			if (command == progressScreen.CMD_CANCEL) {
				cancel();
			}
			if (command == progressScreen.CMD_RETRY) {
				progressScreen = new ProgressScreenFormDownload(
						Localization.get("jrdemo.searching"), Localization.get("jrdemo.contacting"), this);
				J2MEDisplay.setView(progressScreen);
				fetchList();
			}
		}

	}

	public void process(byte[] response) {
		String sResponse = null;
		if (response != null) {
			try {
				sResponse = new String(response, "UTF-8");
				this.response = sResponse;
				System.out.print(sResponse);
			} catch (UnsupportedEncodingException e) {
				throw new FatalException(
						"can't happen; utf8 must be supported", e);
			}
		}

		// FIXME - resolve the responses to be received from the webserver
		if (sResponse == null) {
			// TODO: I don't think this is even possible.
			fail("Null Response from server");
		} else if (sResponse.equals("WebServerResponses.GET_LIST_ERROR")) {
			fail("Get List Error from Server");
		} else if (sResponse.equals("WebServerResponses.GET_LIST_NO_SURVEY")) {
			fail("No survey error from server");
		} else {
			fetched();
		}

	}

	/*
	 * This is actually the method that gets called on status change (?!)
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.javarosa.services.transport.TransportListener#onChange(org.javarosa
	 * .services.transport.TransportMessage, java.lang.String)
	 */
	public void onChange(TransportMessage message, String remark) {
		int responsecode = ((SimpleHttpTransportMessage) message).getResponseCode(); // 200 success, 0 no response yet
		if ((responsecode != 200) && (responsecode != 0)) {
			fail("Error getting list from server");
		} else if (message.getStatus() == TransportMessageStatus.SENT) {

			process(((SimpleHttpTransportMessage) message).getResponseBody());

		} else if (message.getStatus() == TransportMessageStatus.FAILED) {
			String failMessage = message.getFailureReason() != null ? "Transport Failure: "
					+ message.getFailureReason()
					: "Transport Failure";
			fail(failMessage);
		}
	}

	/*
	 * And this seems to get called on completion
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.javarosa.services.transport.TransportListener#onStatusChange(org.
	 * javarosa.services.transport.TransportMessage)
	 */
	public void onStatusChange(TransportMessage message) {
		onChange(message, "");
	}

	public void cancel() {
		new JRDemoFormListState().start();
	}

	public byte[] fetched() {
		JRDemoRemoteFormListState jr = new JRDemoRemoteFormListState(
				this.response);
		jr.start();
		return null;
	}

}
