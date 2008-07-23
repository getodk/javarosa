package org.javarosa.core.model.data;

import org.javarosa.core.services.storage.utilities.Externalizable;

public interface IAnswerData extends Externalizable {
  void setValue (Object o); //can't be null
  Object getValue ();       //will never be null
  String getDisplayText ();
}
