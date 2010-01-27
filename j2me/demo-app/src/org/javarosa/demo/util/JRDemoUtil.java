package org.javarosa.demo.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import org.javarosa.core.api.State;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.services.storage.IStorageIterator;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.core.services.storage.StorageFullException;
import org.javarosa.core.services.storage.StorageManager;
import org.javarosa.core.util.OrderedHashtable;
import org.javarosa.demo.applogic.JRDemoContext;
import org.javarosa.demo.applogic.JRDemoFormListState;
import org.javarosa.demo.applogic.JRDemoSavedFormListState;
import org.javarosa.demo.applogic.JRDemoSplashScreenState;
import org.javarosa.patient.model.Patient;
import org.javarosa.user.model.Constants;
import org.javarosa.user.model.User;

public class JRDemoUtil {

	static OrderedHashtable formList;
	private static OrderedHashtable savedFormList;

	public static String getAppProperty(String key) {
		return JRDemoContext._().getMidlet().getAppProperty(key);
	}

	public static void start() {
		new JRDemoSplashScreenState().start();
	}

	public static void exit() {
		JRDemoContext._().getMidlet().notifyDestroyed();
	}

	public static void goToList(boolean formList) {
		((State) (formList ? new JRDemoFormListState()
				: new JRDemoSavedFormListState())).start();
	}

	/**
	 * 
	 * generate and store in RMS several sample patients from the file
	 * "testpatients"
	 * 
	 * 
	 * @param prms
	 */
	public static void loadDemoPatients(IStorageUtility patients) {
		final String patientsFile = "/testpatients";

		// #debug debug
		System.out.println("Initializing the test patients ");

		// read test patient data into byte buffer
		byte[] buffer = new byte[4000]; // make sure buffer is big enough for
										// entire file; it will not grow
		// to file size (budget 40 bytes per patient)
		InputStream is = System.class.getResourceAsStream(patientsFile);
		if (is == null) {
			String err = "Test patient data file: " + patientsFile
					+ " not found";
			// #debug error
			System.out.println(err);
			throw new RuntimeException(err);
		}

		int len = 0;
		try {
			len = is.read(buffer);
		} catch (IOException e) {
			// #debug error
			throw new RuntimeException(e.getMessage());
		}

		// copy byte buffer into character string
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < len; i++)
			sb.append((char) buffer[i]);
		buffer = null;
		String data = sb.toString();

		// split lines
		Vector lines = DateUtils.split(data, "\n", false);
		data = null;

		// parse patients
		for (int i = 0; i < lines.size(); i++) {
			String line = (String) lines.elementAt(i);
			if (line.trim().length() == 0)
				continue;
			Vector pat = DateUtils.split(line, "|", false);
			if (pat.size() != 6)
				throw new RuntimeException("Malformed patient data at line: "
						+ (i + 1));

			try {
				patients.write(parseSinglePatient(i, pat));
			} catch (StorageFullException e) {
				throw new RuntimeException("uh-oh, storage full [patients]"); // TODO:
																				// handle
																				// this
			}
		}
	}

	private static Patient parseSinglePatient(int i, Vector pat) {
		Patient p = new Patient();
		p.setFamilyName((String) pat.elementAt(0));
		p.setGivenName((String) pat.elementAt(1));
		p.setMiddleName((String) pat.elementAt(2));
		p.setPatientIdentifier((String) pat.elementAt(3));
		p.setGender("m".equals((String) pat.elementAt(4)) ? Patient.SEX_MALE
				: Patient.SEX_FEMALE);
		p.setBirthDate(DateUtils.dateAdd(DateUtils.today(), -Integer
				.parseInt((String) pat.elementAt(5))));
		return p;
	}

	public static void initAdminUser(String defaultPassword) {
		IStorageUtility users = StorageManager.getStorage(User.STORAGE_KEY);
		boolean adminUserFound = false;

		IStorageIterator ui = users.iterate();
		while (ui.hasMore()) {
			User user = (User) ui.nextRecord();
			if (User.ADMINUSER.equals(user.getUserType())) {
				adminUserFound = true;
				break;
			}
		}

		if (!adminUserFound) {
			User admin = new User();
			admin.setUsername("admin");
			admin.setPassword(defaultPassword);
			admin.setUserType(Constants.ADMINUSER);

			try {
				users.write(admin);
			} catch (StorageFullException e) {
				throw new RuntimeException("uh-oh, storage full [users]"); // TODO:
																			// handle
																			// this
			}
		}
	}

	public static User demoUser() {
		User demo = new User("demo", "", 999);
		demo.setUserType(User.ADMINUSER);
		return demo;
	}

	// cache this because the storage utility doesn't yet support quick
	// meta-data iteration
	public static OrderedHashtable getFormList() {
		if (formList == null) {
			formList = new OrderedHashtable();
			IStorageUtility forms = StorageManager
					.getStorage(FormDef.STORAGE_KEY);
			IStorageIterator fi = forms.iterate();
			while (fi.hasMore()) {
				FormDef f = (FormDef) fi.nextRecord();
				formList.put(new Integer(f.getID()), f.getTitle());
			}
		}
		return formList;
	}

	// cache this because the storage utility doesn't yet support quick
	// meta-data iteration
	public static OrderedHashtable getSavedFormList() {
		if (savedFormList == null) {
			savedFormList = new OrderedHashtable();
			IStorageUtility forms = StorageManager
					.getStorage(FormInstance.STORAGE_KEY);
			IStorageIterator fi = forms.iterate();
			while (fi.hasMore()) {
				FormInstance f = (FormInstance) fi.nextRecord();
				System.out.println("adding saved form: " + f.getID() + " - "
						+ f.getName() + " - " + f.getFormId());
				savedFormList.put(new Integer(f.getID()), f.getName());
			}
		}
		return savedFormList;
	}
}
