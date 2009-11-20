package org.javarosa.chsreferral.util;

import java.util.Vector;

import org.javarosa.chsreferral.model.PatientReferral;
import org.javarosa.core.services.storage.IStorageIterator;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.core.services.storage.StorageManager;

public class PatientReferralUtil {
	public static Vector getPendingReferrals (IPatientReferralFilter filter) {
		IStorageUtility referrals = StorageManager.getStorage(PatientReferral.STORAGE_KEY);
		
		Vector pending = new Vector();
		IStorageIterator ri = referrals.iterate();
		while (ri.hasMore()) {
			PatientReferral pr = (PatientReferral)ri.nextRecord();
			if (pr.isPending() && (filter == null || filter.inFilter(pr))) {
				pending.addElement(pr);
			}
		}
		return pending;
	}
	
	public static int getNumberOfOpenReferralsByType(String caseTypeId) {
		final String internalid = caseTypeId;
		return getNumberOfOpenReferrals(new IPatientReferralFilter() {
			public boolean inFilter(PatientReferral ref) {
				return ref.getType().equals(internalid);
			}
		});
	}
	
	public static int getNumberOfOpenReferrals(IPatientReferralFilter filter) {
		return getPendingReferrals(filter).size();
	}
}
