/**
 * 
 */
package org.javarosa.core.services.locale;

import org.javarosa.core.util.OrderedHashtable;
import org.javarosa.core.util.externalizable.Externalizable;

/**
 * @author Clayton Sims
 * @date May 26, 2009 
 *
 */
public interface LocaleDataSource extends Externalizable {
	public OrderedHashtable getLocalizedText();
}
