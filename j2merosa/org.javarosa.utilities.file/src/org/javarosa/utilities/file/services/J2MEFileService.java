package org.javarosa.utilities.file.services;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;

import org.javarosa.utilities.file.FileException;


/**
 * 
 * Service providing File I/O using J2ME libraries.
 * @author Ndubisi Onuora
 *
 */

public class J2MEFileService implements IFileService 
{
	public static final String serviceName = "J2MEFileService";
	
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
		boolean dirCreated = false;
		try 
		{
			directory = (FileConnection) Connector.open(path);
			if (!directory.exists())
			{
				directory.mkdir();
				dirCreated = true;
			}
		}
		catch(IOException ex) 
		{
			//handleException(ex);
			dirCreated = false;
			throw new FileException("An error occurred with creating a directory.");			
		}
		finally 
		{
			close(directory);
		}
		return dirCreated = false;
	}
	
	public boolean deleteDirectory(String path) throws FileException
	{
		FileConnection directory = null;
		boolean dirDeleted = false;
		try 
		{
			directory = (FileConnection)Connector.open(path);
			if(directory.exists() && directory.isDirectory())
			{
				directory.delete();
				dirDeleted = true;
			}
		}
		catch(IOException ex) 
		{
			//handleException(ex);
			dirDeleted = false;
			throw new FileException("An error occurred when deleting a directory.");			
		}
		finally 
		{
			close(directory);
		}
		return dirDeleted;
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
		Vector v = new Vector();		
		addArrtoVec(v, getRootNames());
		Enumeration root = v.elements();
		String rootName = "";
		while (root.hasMoreElements()) {
			rootName = (String) root.nextElement();
		}
		return rootName;
	}
	
	private static String[] enumtoStringArr(Enumeration enumer)
	{
		Vector enumerationList = new Vector();
		Enumeration e = enumer;
		while(e.hasMoreElements())
			enumerationList.addElement( (String)e.nextElement() );
		
		return vectorToStringArr(enumerationList);
	}
	
	private static Object[] vectorToArr(Vector vec)
	{
		int vecSize = vec.size();
		Object[] arr = new Object[vecSize];
		for(int i = 0; i < vecSize; ++i)		
			arr[i] = vec.elementAt(i);
		
		return arr;
	}
	
	private static String[] vectorToStringArr(Vector vec)
	{
		int vecSize = vec.size();
		String[] arr = new String[vecSize];
		for(int i = 0; i < vecSize; ++i)		
			arr[i] = (String)vec.elementAt(i);
		
		return arr;
	}
	
	private static void addArrtoVec(Vector vec, Object[] arr)
	{
		for(int i = 0; i < arr.length; ++i)
			vec.addElement(arr[i]);
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
		return null; //THIS SHOULD NEVER HAPPEN!!!
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
		try 
		{
			file = (FileConnection) Connector.open(fullName);
			if (!file.exists()) 
			{
				file.create();				
			}				
			fos = file.openOutputStream();
			fos.write(data);
			isSaved = true;
		} 
		catch(IOException ex) 
		{				
			//handleException(ex);
			isSaved = false;
			throw new FileException("Error creating file.");			
		} 
		finally 
		{		
			close(fos);
			close(file);
		}
		return isSaved;
	}
	
	/**
	 * Delete a file
	 * @param fileName
	 */
	public boolean deleteFile(String fileName) throws FileException 
	{
		FileConnection file = null;
		boolean fileDeleted = false;
		try 
		{
			file = (FileConnection)Connector.open(fileName);
			if(file.exists()) 
			{
				System.err.println(fileName + " exists");
				file.delete();
				fileDeleted = true;
			}
			else
			{
				System.err.println(fileName + " does not exist");
			}
		}
		catch(IOException ioe)
		{			
			handleException(ioe);
			throw new FileException("Error deleting file.");
		}
		catch(Exception ex)
		{		
			handleException(ex);
		} 
		finally 
		{		
			close(file);
		}
		return fileDeleted;
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
			//return fis;
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
		//return null;
		return fis;
	}
	
	public OutputStream getFileOutputStream(String fileName) throws FileException
	{
		OutputStream fos = null;
		FileConnection file = null;		
		try 
		{
			file = (FileConnection)Connector.open(fileName);
			if (!file.exists())
			{
				file.create();				
			}				
			fos = file.openOutputStream();			
		} 
		catch(IOException ex) 
		{
			handleException(ex);
			throw new FileException("Error creating file.");			
		}
		finally
		{
			close(file);
		}
		return fos;
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
		try 
		{			
			Date latestFoundDate = getModifiedDate(directory);
			Vector v = new Vector();
			addArrtoVec(v, listDirectory(directory) );
			Enumeration filesBelow = v.elements();
			// this is not very efficient and could be significantly tweaked if
			// desired. Does way too many trips up and down the tree.
			while (filesBelow.hasMoreElements()) 
			{
				FileConnection subFile = null;
				String subFileName = (String) filesBelow.nextElement();
				try 
				{
					String fullPathConstructed = directory + subFileName;
					subFile = (FileConnection) Connector.open(fullPathConstructed);
					if (subFile.isDirectory()) 
					{
						// continue otherwise
						Date subFileDate = getModifiedDateRecursive(fullPathConstructed);
						if (subFileDate.getTime() > latestFoundDate.getTime()) 
						{
							latestFoundDate = subFileDate;
							toReturn = fullPathConstructed;
						}
					}
				} 
				finally
				{
					close(subFile);
				}
			}
			if (toReturn != directory) 
			{
				return getMostRecentlyModifiedDirectoryBelow(toReturn);
			}
		} 
		catch(IOException e) 
		{
			handleException(e);
			toReturn = null;
		}
		catch(FileException fe)
		{
			System.err.println("An error occurred while attempting to get most recently modified directory below.");
			fe.printStackTrace();			
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
			if (file.isDirectory()) 
			{
				Vector v = new Vector();
				addArrtoVec(v, listDirectory(fileName) );
				Enumeration filesBelow = v.elements();
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
		System.out.println("Exception caught in " + serviceName + ex.getMessage());
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
