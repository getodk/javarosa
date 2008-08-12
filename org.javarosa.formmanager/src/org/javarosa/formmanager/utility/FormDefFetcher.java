package org.javarosa.formmanager.utility;

import org.javarosa.core.Context;
import org.javarosa.core.model.FormDef;

public class FormDefFetcher {
	IFormDefRetrievalMethod fetcher;

	public FormDef getFormDef(Context context) {
		if(fetcher != null) {
			return fetcher.retreiveFormDef(context);
		}
		else {
			return null;
		}
	}
	
	/**
	 * @return the fetcher
	 */
	public IFormDefRetrievalMethod getFetcher() {
		return fetcher;
	}

	/**
	 * @param fetcher the fetcher to set
	 */
	public void setFetcher(IFormDefRetrievalMethod fetcher) {
		this.fetcher = fetcher;
	}
	
	
}
