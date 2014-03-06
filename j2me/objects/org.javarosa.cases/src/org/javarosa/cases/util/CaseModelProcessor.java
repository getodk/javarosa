/**
 * 
 */
package org.javarosa.cases.util;

import java.util.Date;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.Vector;

import org.javarosa.cases.model.Case;
import org.javarosa.chsreferral.model.PatientReferral;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.services.Logger;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.core.services.storage.IStorageUtilityIndexed;
import org.javarosa.core.services.storage.StorageFullException;
import org.javarosa.core.services.storage.StorageManager;
import org.javarosa.core.util.InvalidIndexException;
import org.javarosa.core.util.PropertyUtils;
import org.javarosa.xform.util.XFormAnswerDataSerializer;

/**
 * @author ctsims
 *
 */
public class CaseModelProcessor implements ICaseModelProcessor {

	XFormAnswerDataSerializer serializer = new XFormAnswerDataSerializer();
	Case c;
	
	public Case getCase() {
		return c;
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.model.utils.IInstanceProcessor#processModel(org.javarosa.core.model.instance.FormInstance)
	 */
	public void processInstance(FormInstance tree) {
		Vector caseElements = scrapeForCaseElements(tree);
		for(int i=0; i < caseElements.size(); ++i) {
			try {
				processCase((TreeElement)caseElements.elementAt(i));
			} catch (MalformedCaseModelException e) {
				throw new RuntimeException(e.getMessage());
			}
		}
	}
	
	private void processCase(TreeElement caseElement) throws MalformedCaseModelException {
		Vector caseIdKids = caseElement.getChildrenWithName("case_id");
		if(caseIdKids.size() < 1) {
			throw new MalformedCaseModelException("Invalid <case> model. Required element (case_id) is missing.","<case>");
		}
		String caseId = ((TreeElement)caseIdKids.elementAt(0)).getValue().uncast().getString();
		Vector dateModified = caseElement.getChildrenWithName("date_modified");
		if(dateModified.size() < 1) {
			throw new MalformedCaseModelException("Invalid <case> model. Required element (date_modified) is missing.","<case>");
		}
		Date date = (Date)((TreeElement)dateModified.elementAt(0)).getValue().getValue();
		
		for(int i=0; i < caseElement.getNumChildren(); ++i ){
			TreeElement kid = caseElement.getChildAt(i);
			if(!caseElement.isRelevant()) {
				continue;
			}
			if(kid.getName().equals("create")) {
				if(kid.isRelevant()) {
					c = processCaseCreate(kid,caseId, date);
				}
			} else if(kid.getName().equals("update")) {
				if(c == null) {
					c = getCase(caseId);
				}
				if(kid.isRelevant()) {
					processCaseMutate(kid,c,date);
				}
			} else if(kid.getName().equals("close")) {
				if(c == null) {
					c = getCase(caseId);
				}
				if(kid.isRelevant()) {
					processCaseClose(kid,c,date);
				}
			} else if(kid.getName().equals("referral")) {
				if(c == null) {
					c = getCase(caseId);
				}
				if(kid.isRelevant()) {
					processCaseReferral(kid,c,date);
				}
			}
		}
	}
	
	private Case getCase(String id) {
		IStorageUtilityIndexed storage = (IStorageUtilityIndexed)StorageManager.getStorage(Case.STORAGE_KEY);
		
		try {
			Case c =  (Case)storage.getRecordForValue("case-id", id);
			return c;
		} catch(NoSuchElementException e) {
			//We eventually probably want to deal with this. For now, it's a dealbreaker. Throw it up.
			e.printStackTrace();
			throw e;
		} catch(InvalidIndexException iie) {
			//We eventually probably want to deal with this. For now, it's a dealbreaker. Throw it up.
			iie.printStackTrace();
			throw iie;
		}
	}
	
	private PatientReferral getReferral(String refId, String refType) {
		IStorageUtilityIndexed storage = (IStorageUtilityIndexed)StorageManager.getStorage(PatientReferral.STORAGE_KEY);
		Vector IDs = storage.getIDsForValue("referral-id", refId);
		for(Enumeration en = IDs.elements(); en.hasMoreElements(); ) {
			int id = ((Integer)en.nextElement()).intValue();
			PatientReferral r = (PatientReferral)storage.read(id);
			if(r.getType().equals(refType)) {
				return r; 
			}
		}
		throw new RuntimeException("Referral for ID:" + refId + " doesn't exist");
	}
	
	private void commit(Case c) {
		IStorageUtility utility = StorageManager.getStorage(Case.STORAGE_KEY);
		try {
			utility.write(c);
		} catch (StorageFullException e) {
			e.printStackTrace();
			throw new RuntimeException("Uh oh! Case Storage Full!");
		}
	}
	
	private void commit(PatientReferral r) {
		IStorageUtility utility = StorageManager.getStorage(PatientReferral.STORAGE_KEY);
		try {
			utility.write(r);
		} catch (StorageFullException e) {
			e.printStackTrace();
			throw new RuntimeException("Uh oh! Referral Storage Full!");
		}
	}
	
	private Case processCaseCreate(TreeElement create, String caseId, Date date) throws MalformedCaseModelException {
		String caseTypeId = null;
		String extId = null;
		String caseName = null;
		String userId = null;
		
		for(int i=0; i < create.getNumChildren(); ++i ){
			TreeElement kid = create.getChildAt(i);
			if(!kid.isRelevant()) {
				continue;
			}
			if(kid.getName().equals("case_type_id")) {
				caseTypeId = (String)serializer.serializeAnswerData(kid.getValue());
			}
			if(kid.getName().equals("external_id")) {
				extId = (String)serializer.serializeAnswerData(kid.getValue());
			}
			if(kid.getName().equals("user_id")) {
				userId = kid.getValue().uncast().getString();
			}
			if(kid.getName().equals("case_name")) {
				caseName = (String)serializer.serializeAnswerData(kid.getValue());
			}
			
		}
		
		if(caseTypeId == null || caseName == null) {
			throw new MalformedCaseModelException("Invalid <create> model. Required element is missing.","<create>");
		}
		Case c = new Case(caseName, caseTypeId);
		c.setCaseId(caseId);
		c.setExternalId(extId);
		c.setDateOpened(date);
		if(userId != null) {
			c.setUserId(userId);
		}
		commit(c);
		Logger.log("case-create", c.getID() + ";" + PropertyUtils.trim(c.getCaseId(), 12) + ";" + c.getTypeId());
		return c;
	}
	
	private void processCaseMutate(TreeElement mutate,Case c, Date date) throws MalformedCaseModelException {
		for(int i=0; i < mutate.getNumChildren(); ++i ){
			TreeElement kid = mutate.getChildAt(i);
			if(!kid.isRelevant()) {
				continue;
			}
			if(kid.getName().equals("case_type_id")) {
				c.setTypeId((String)serializer.serializeAnswerData(kid.getValue()));
			}
			else if(kid.getName().equals("case_name")) {
				c.setName((String)serializer.serializeAnswerData(kid.getValue()));
			}
			else if(kid.getName().equals("date_opened")) {
				c.setDateOpened((Date)(kid.getValue().getValue()));
			} else{
				String vname = kid.getName();
				
				if(kid.getValue() != null) {
					String value = kid.getValue().uncast().getString();
					c.setProperty(vname, value);
				}
			}
		}
		commit(c);
	}
	
	private void processCaseClose(TreeElement close,Case c, Date date) throws MalformedCaseModelException {
		c.setClosed(true);
		commit(c);
		Logger.log("case-close", PropertyUtils.trim(c.getCaseId(), 12));
	}
	
	private void processCaseReferral(TreeElement referral, Case c, Date date) throws MalformedCaseModelException {
		
		Vector referralIds = referral.getChildrenWithName("referral_id");
		if(referralIds.size() < 1) {
			throw new MalformedCaseModelException("Invalid <referral> model. Required element (referral_id) is missing.","<referral>");
		}
		String referralId = (String)serializer.serializeAnswerData(((TreeElement)referralIds.elementAt(0)).getValue());
		
		//Use some smart default for followup dates here. Generally from case type.
		Date followup = DateUtils.dateAdd(date,3);
		Vector followupDates = referral.getChildrenWithName("followup_date");
		if(followupDates.size() >= 1 && ((TreeElement)followupDates.elementAt(0)).isRelevant()) {
			IAnswerData followupDate = (((TreeElement)followupDates.elementAt(0))).getValue();
			if(followupDate == null) {
				throw new MalformedCaseModelException("Invalid <referral> model. followup_date is present, but empty!","<followup_date>");
			}
			followup = (Date)followupDate.getValue();
		}
		
		for(int i=0; i < referral.getNumChildren(); ++i ){
			TreeElement kid = referral.getChildAt(i);
			if(!kid.isRelevant()) {
				continue;
			}
			if(kid.getName().equals("open")) {
				Vector types = kid.getChildrenWithName("referral_types");
				if(types.size() < 1) {
					throw new MalformedCaseModelException("Invalid <open> model. Required element (referral_types) is missing.","<referral>");
				}
				String typeString = (String)serializer.serializeAnswerData(((TreeElement)types.elementAt(0)).getValue());
				Vector referralTypeList = DateUtils.split(typeString, " ", true);
				for(int ir = 0; ir < referralTypeList.size(); ++ir) {
					String referralType = (String)referralTypeList.elementAt(ir);
					PatientReferral r = new PatientReferral(referralType, date, referralId, c.getCaseId(), followup);
					commit(r);
					Logger.log("referral-open", r.getID() + ";" + PropertyUtils.trim(r.getReferralId(), 12) + ";" + r.getType());
				}
			}
			else if(kid.getName().equals("update")) {
				Vector types = kid.getChildrenWithName("referral_type");
				if(types.size() < 1) {
					throw new MalformedCaseModelException("Invalid <update> model. Required element (referral_type) is missing.","<referral>");
				}
				String refType = (String)serializer.serializeAnswerData((((TreeElement)types.elementAt(0)).getValue()));
				PatientReferral r = getReferral(referralId,refType);
				r.setDateDue(followup);
				Vector dateCloseds = kid.getChildrenWithName("date_closed");
				boolean closing = (dateCloseds.size() > 0 && ((TreeElement)dateCloseds.elementAt(0)).isRelevant());
				if(closing) {
					r.close();
				}
				commit(r);
				if (closing) {
					Logger.log("referral-resolve", PropertyUtils.trim(r.getReferralId(), 12) + ";" + r.getType()); //type currently needed to uniquely identify referral
				}
			}
		}
	}
	
	private Vector scrapeForCaseElements(FormInstance tree) {
		Vector caseElements = new Vector();
		
		Stack children = new Stack();
		children.push(tree.getRoot());
		while(!children.empty()) {
			TreeElement element = (TreeElement)children.pop();
			for(int i =0; i < element.getNumChildren(); ++i) {
				TreeElement caseElement = element.getChildAt(i);
				if(!caseElement.isRelevant()) {
					continue;
				}
				if(caseElement.getName().equals("case")) {
					caseElements.addElement(caseElement);
				} else {				
					children.push(caseElement);
				}
			}
		}
		return caseElements;
	}
}
