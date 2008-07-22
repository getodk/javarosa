package org.javarosa.core.model.data;

public interface IAnswerData {
  void setValue (Object o); //can't be null
  Object getValue ();       //will never be null
  String getDisplayText ();
}
