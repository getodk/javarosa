package org.javarosa.formmanager.view.clforms.acquire;

import org.javarosa.core.services.IService;
import org.javarosa.formmanager.view.FormElementBinding;

/**
 * @author mel
 * 
 * A service that provides data acquisition widgets
 *
 */
public interface IAcquiringService extends IService {

	public String getName();
	
	public AcquiringQuestionScreen getWidget(FormElementBinding prompt, int temp);

	public AcquiringQuestionScreen getWidget(FormElementBinding prompt);

	public AcquiringQuestionScreen getWidget(FormElementBinding prompt,
			String str);

	public AcquiringQuestionScreen getWidget(FormElementBinding prompt, char str);
}
