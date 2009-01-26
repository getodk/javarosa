package org.javarosa.utilities.file.services;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;

import org.javarosa.utilities.file.FileException;

/**
 * 
 * Service providing File I/O using J2ME libraries.
 * @author Ndubisi Onuora
 *
 */

public class J2MEFileService implements IFileService 
{
	private static final String serviceName = "J2MEFileService";
	
	public String getName()
	{
		return serviceName;
	}
	
	/**
	 * Create a directory from the path
	 * @param path
	 * @return true if the operation succeeded or the directory already existed
	 */
	public boolean createDirectory(String path) throws FileException 
	{		
		FileConnection directory = null;
		try {
			directory = (FileConnection) Connector.open(path);
			if (!directory.exists())
				directory.mkdir();
		}
		catch(IOException ex) 
		{
			//handleException(ex);
			throw new FileException("An error occurred with creating a directory.");
			return false;
		}
		finally {
			close(directory);
		}
		return true;
	}
	
	public boolean deleteDirectory(String path) throws FileException
	{
		FileConnection directory = null;
		try 
		{
			directory = (FileConnection)Connector.open(path);
			if(!directory.exists())
				directory.rmdir();
		}
		catch(IOException ex) 
		{
			//handleException(ex);
			throw new FileException("An error occurred with deleting a directory.");
			return false;
		}
		finally 
		{
			close(directory);
		}
		return true;
	}	
	
	/**
	 * Get a list of root directories on the device
	 * @return
	 */
	public String[] getRootNames() throws FileException
	{
		
		return enumtoStringArr( FileSystemRegistry.listRoots() );
	}
	
	/**
	 * Get the default root directory
	 * @return
	 */
	public String getDefaultRoot() throws FileException 
	{
		Enumeration root = getRootNames();
		String rootName = "";
		while (root.hasMoreElements()) {
			rootName = (String) root.nextElement();
		}
		return rootName;
	}
	
	private static String[] enumtoStringArr(Enumeration enumer)
	{
		ArrayList enumerationList = new ArrayList();
		Enumeration e = enumer;
		while(e.hasMoreElements())
			(String)e.nextElement();
		
		return (String[])enumerationList.toArray();
	}
	
	public String[]/*Enumeration*/ listDirectory(String directoryPath) throws FileException 
	{
		FileConnection dir = null;
		System.out.println("Listing the contents of: " + directoryPath);
		try
		{
			dir = (FileConnection)Connector.open(directoryPath);
			return enumtoStringArr(dir.list());
		} 
		catch(IOException ioe)
		{
			throw new FileException("Error listing directory in " + directoryPath + "path");
		}
		catch(Exception ex) 
		{
			handleException(ex);
			/*
			return new Enumeration() 
			{
				public boolean hasMoreElements() 
				{
					return false;
				}
				public Object nextElement() 
				{
					return null;
				}
			};
			*/
		} 
		finally 
		{
			close(dir);			
		}
	}	
	
	/**
	 * Create a file 
	 * @param fullName
	 * @param data
	 * @return whether the file was created
	 */
	public boolean createFile(String fullName, byte[] data) throws FileException 
	{
		OutputStream fos = null;
		FileConnection file = null;
		boolean isSaved = false;
		try {
			file = (FileConnection) Connector.open(fullName);
			if (!file.exists()) {
				file.create();
			}				
			fos = file.openOutputStream();
			fos.write(data);
		} 
		catch(IOException ex) 
		{				
			//handleException(ex);
			throw new FileException("Error creating file.");
			return false;
		} 
		finally 
		{		
			close(fos);
			close(file);
		}
		return true;
	}
	
	/**
	 * Delete a file
	 * @param fileName
	 */
	public boolean deleteFile(String fileName) throws FileException 
	{		
		FileConnection file = null;
		boolean isSaved = false;
		try {
			file = (FileConnection) Connector.open(fileName);
			if (file.exists()) {
				file.delete();
				return true;
			}				
		} 
		catch(IOException ioe)
		{
			throw new FileException("Error deleing file.");
		}
		catch(Exception ex)
		{				
			handleException(ex);
		} 
		finally {		
			close(file);
		}
		return false;
	}

	/**
	 * 
	 * @param fileName
	 * @return whether or not the file exists
	 * @throws FileException
	 */
	public boolean fileExists(String fileName) throws FileException
	{
		FileConnection file = null;
		try
		{
			file = (FileConnection) Connector.open(fileName);
			return (file.exists());
		}
		catch(IOException ioe)
		{
			throw new FileException("A error occurred while determining the existence of " + fileName);
		}
		finally
		{
			close(file);
		}
	}
	
	/**
	 * Gets file data from the OS
	 * @param fileName
	 * @return
	 */
	public byte[] getFileData(String fileName) throws FileException 
	{
		InputStream fis = null;
		FileConnection file = null;
		try 
		{
			file = (FileConnection) Connector.open(fileName);
			int bytesToRead = (int) file.fileSize();
			byte[] toReturn = new byte[bytesToRead];
			fis = file.openInputStream();
			int bytesRead = 0;
			int blockSize = 1024;
			while (bytesToRead > bytesRead) 
			{
				int thisBlock = blockSize;
				if (bytesToRead - bytesRead < blockSize) 
				{
					thisBlock = bytesToRead-bytesRead;
				}
				fis.read(toReturn, bytesRead, thisBlock);
				bytesRead += blockSize;
			}
			return toReturn;
		}
		catch(IOException ioe)
		{
			throw new FileException("Error obtaining file data.");
		}
		catch(Exception ex) 
		{				
			handleException(ex);			
		}
		finally 
		{		
			close(fis);
			close(file);
		}
		return null;
	}

	public InputStream getFileDataStream(String fileName) throws FileException 
	{
		InputStream fis = null;
		FileConnection file = null;
		try
		{
			file = (FileConnection)Connector.open(fileName);
			fis = file.openInputStream();
			return fis;
		} 
		catch(Exception ex) 
		{				
			handleException(ex);
			throw new FileException("Error obtaining FileDataStream.");
		}
		finally
		{		
			close(file);
		}
		return null;
	}	

	/**
	 * Gets the directory with the most recent changes below the one passed in.
	 * This call is recursive
	 * @param directory
	 * @return
	 */
	public String getMostRecentlyModifiedDirectoryBelow(String directory)
	{
		if (!directory.endsWith("/")) {
			directory = directory + "/";
		}
		String toReturn = directory;
		try {
			
			Date latestFoundDate = getModifiedDate(directory);
			Enumeration filesBelow = listDirectory(directory);
			// this is not very efficient and could be significantly tweaked if
			// desired. Does way too many trips up and down the tree.
			while (filesBelow.hasMoreElements()) {
				FileConnection subFile = null;
				String subFileName = (String) filesBelow.nextElement();
				try {
					String fullPathConstructed = directory + subFileName;
					subFile = (FileConnection) Connector.open(fullPathConstructed);
					if (subFile.isDirectory()) {
						// continue otherwise
						Date subFileDate = getModifiedDateRecursive(fullPathConstructed);
						if (subFileDate.getTime() > latestFoundDate.getTime()) {
							latestFoundDate = subFileDate;
							toReturn = fullPathConstructed;
						}
					}
				} finally {
					close(subFile);
				}
			}
			if (toReturn != directory) {
				return getMostRecentlyModifiedDirectoryBelow(toReturn);
			}
		} catch(IOException e) 
		{
			handleException(e);
			toReturn = null;
		}
		return toReturn;
	}	
	
	private Date getModifiedDate(String fileName) throws FileException 
	{
		FileConnection file = null;
		try 
		{
			file = (FileConnection) Connector.open(fileName);
			return new Date(file.lastModified());
		} 
		catch(IOException i)
		{
			throw new FileException("An error occurred while obtaining the modified date of the file " + fileName);
		}
		finally 
		{
			close(file);
		}
	}

	private Date getModifiedDateRecursive(String fileName) throws FileException 
	{
		FileConnection file = null;
		System.out.println("Recursive modification check - searching: " + fileName);
		if (!fileName.endsWith("/")) {
			fileName += "/";
		}
		try {
			file = (FileConnection) Connector.open(fileName);
			Date toReturn = new Date(file.lastModified());
			if (file.isDirectory()) {
				Enumeration filesBelow = listDirectory(fileName);
				while (filesBelow.hasMoreElements()) {
					String subFileName = (String) filesBelow.nextElement();
					String fullPathConstructed = fileName + subFileName;
					System.out.println("Recursive modification check - searching: " + fullPathConstructed);
					Date thisFileDate = getModifiedDateRecursive(fullPathConstructed);
					if (thisFileDate.getTime() > toReturn.getTime()) {
						toReturn = thisFileDate;
					}
				}
			} else {
				// nothing to do - the toReturn date is correct
			}
			return toReturn;
		} 
		catch(IOException ie)
		{
			throw new FileException("An error occurred while obtaining the modified date recursively.");
		}
		finally 
		{
			close(file);
		}
	}

	private void close(InputStream stream) {
		try {					
			if (stream != null) {
				stream.close();
			}
		} catch (Exception e) {
		}
	}


	private static void close(OutputStream stream) {
		try {					
			if (stream != null) {
				stream.flush();
				stream.close();
			}
		} catch (Exception e) {
		}	
	}

	private static void close(FileConnection connection) {
		try {
			if (connection != null)
				connection.close();
		}
		catch(IOException e) {
			
		}
	}
	
	private static void handleException(Exception ex) 
	{
		// TODO Auto-generated method stub
		System.out.println("Exception caught in J2MEFileService" + ex.getMessage());
		ex.printStackTrace();
	}


	public static long getFileLength(String fileName) {
		InputStream fis = null;
		FileConnection file = null;
		try {
			file = (FileConnection) Connector.open(fileName);
			return file.availableSize();
		} catch (Exception ex) {				
			handleException(ex);
		} 
		finally {		
			close(file);
		}
		return -1;
	}

}
