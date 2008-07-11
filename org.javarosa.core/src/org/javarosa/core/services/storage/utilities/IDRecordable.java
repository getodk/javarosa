package org.javarosa.core.services.storage.utilities;

/**
 * IDRecordable classes represent a record with an integer
 * ID.  
 *   
 * @author Munier
 */
public interface IDRecordable
{
    
    /**
     * Sets the id for this record.
     * 
     * @param recordId The Id to be used for  current record.
     */
    public abstract void setRecordId(int recordId);
    
}
