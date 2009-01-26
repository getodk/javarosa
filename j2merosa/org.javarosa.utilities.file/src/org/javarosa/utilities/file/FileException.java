package org.javarosa.utilities.file;

/**
 * 
 * A FileException corresponds to any type of error received by the IFileService
 * during a File I/O operation.
 * @author Ndubisi Onuora
 *
 */

public class FileException extends Exception 
{
	public FileException(String message)
	{
		super(message);
	}
}
