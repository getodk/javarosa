package org.javarosa.media.image.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;

/**
 * Utility class for dealing with files via the FileConnection API.  
 * Since this breaks a lot of phones I'm keeping it separate from the core projects
 * by leaving it in the image namespace.  Really this might be its own project.
 * 
 * @author Cory Zue
 *
 */
public class FileUtility {

	
	/**
	 * Create a directory from the path
	 * @param path
	 * @return true if the operation succeeded or the directory already existed
	 */
	public static boolean createDirectory(String path) {
		
		FileConnection directory = null;
		try {
			directory = (FileConnection) Connector.open(path);
			if (!directory.exists())
				directory.mkdir();
		}
		catch (IOException ex) {
			handleException(ex);
			return false;
		}
		finally {
			close(directory);
		}
		return true;
	}
	

	/**
	 * Get a list of root directories on the device
	 * @return
	 */
	public static Enumeration getRootNames() {
		return FileSystemRegistry.listRoots();
	}
	
	/**
	 * Get the defaut root directory
	 * @return
	 */
	public static String getDefaultRoot() {
		Enumeration root = getRootNames();
		String rootName = "";
		while (root.hasMoreElements()) {
			rootName = (String) root.nextElement();
		}
		return rootName;
	}
	
	
	public static Enumeration listDirectory(String directoryPath) {
		FileConnection dir = null;
		System.out.println("Listing the contents of: " + directoryPath);
		try {
			dir = (FileConnection) Connector.open(directoryPath);
			return dir.list();
		} catch (Exception ex) {
			handleException(ex);
			return new Enumeration() {
				public boolean hasMoreElements() {
					return false;
				}
				public Object nextElement() {
					return null;
				}};
		} finally {
			close(dir);
			
		}
	}

	
	
	/**
	 * Create a file 
	 * @param fullName
	 * @param data
	 * @return whether the file was created
	 */
	public static boolean createFile(String fullName, byte[] data) {
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
		} catch (Exception ex) {				
			handleException(ex);
			return false;
		} 
		finally {		
			close(fos);
			close(file);
		}
		return true;
	}

	
	/**
	 * Delete a file
	 * @param fileName
	 */
	public static boolean deleteFile(String fileName) {
		
		FileConnection file = null;
		boolean isSaved = false;
		try {
			file = (FileConnection) Connector.open(fileName);
			if (file.exists()) {
				file.delete();
				return true;
			}				
		} catch (Exception ex) {				
			handleException(ex);
		} 
		finally {		
			close(file);
		}
		return false;
	}

	/**
	 * Gets file data from the OS
	 * @param fileName
	 * @return
	 */
	public static byte[] getFileData(String fileName) {
		InputStream fis = null;
		FileConnection file = null;
		try {
			file = (FileConnection) Connector.open(fileName);
			int bytesToRead = (int) file.fileSize();
			byte[] toReturn = new byte[bytesToRead];
			fis = file.openInputStream();
			int bytesRead = 0;
			int blockSize = 1024;
			while (bytesToRead > bytesRead) {
				int thisBlock = blockSize;
				if (bytesToRead - bytesRead < blockSize) {
					thisBlock = bytesToRead-bytesRead;
				}
				fis.read(toReturn, bytesRead, thisBlock);
				bytesRead += blockSize;
			}
			return toReturn;
		} catch (Exception ex) {				
			handleException(ex);
		} 
		finally {		
			close(fis);
			close(file);
		}
		return null;
	}
	
	public static InputStream getFileDataStream(String fileName) {
		InputStream fis = null;
		FileConnection file = null;
		try {
			file = (FileConnection) Connector.open(fileName);
			fis = file.openInputStream();
			return fis;
		} catch (Exception ex) {				
			handleException(ex);
		} 
		finally {		
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
	public static String getMostRecentlyModifiedDirectoryBelow(String directory) {
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
		} catch (IOException e) {
			handleException(e);
			toReturn = null;
		}
		return toReturn;
	}

	
	
	private static Date getModifiedDate(String fileName) throws IOException {
		FileConnection file = null;
		try {
			file = (FileConnection) Connector.open(fileName);
			return new Date(file.lastModified());
		} finally {
			close(file);
		}
	}


	private static Date getModifiedDateRecursive(String fileName)
			throws IOException {
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
		} finally {
			close(file);
		}
	}


	private static void close(InputStream stream) {
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
		catch(Exception e) {}
	}
	
	private static void handleException(Exception ex) {
		// TODO Auto-generated method stub
		System.out.println("Exception caught in FileUtility" + ex.getMessage());
		ex.printStackTrace();
	}




	

	
}
