package com.actionscript.m07;

/**
 * Every object that want to have information about Connection must implement
 * this interface
 *
 * @author Massimo Carli
 */
public interface ConnectionThreadListener {
    
    /**
     * Callback method for Data
     *@param data Information read
     */
    public void notifyData(String data);
    
    /**
     * Callback method for Error 
     *@param errorMessage Error message
     */
    public void notifyError(String errorMessage);
    
}
