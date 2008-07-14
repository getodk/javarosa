package org.javarosa.formmanager.model;

import org.javarosa.core.model.*;
import org.javarosa.formmanager.model.temp.*;
import org.javarosa.clforms.api.Form;
import org.javarosa.formmanager.utility.*;

public class FormEntryModel {
    private Form form;

    int activeQuestionIndex;
    boolean unsavedChanges;
    
    public int getQuestionIndex () {
    	return -1;
    }
    
    public Form getForm () {
    	return null;
    }
    
    public Prompt getQuestion (int questionIndex) {
    	return null;
    }
    
    public boolean isRelevant (int questionIndex) {
    	return false;
    }

    public void registerObservable (FormEntryModelListener feml) {
    	
    }

    public void unregisterObservable (FormEntryModelListener feml) {
    	
    }
}