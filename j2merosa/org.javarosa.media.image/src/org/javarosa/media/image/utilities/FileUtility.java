package org.javarosa.media.image.utilities;

import java.io.IOException;
import java.io.OutputStream;
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
			directory.close();
		}
		catch (IOException ex) {
			handleException(ex);
			return false;
		}
		finally {
			try {
				if (directory != null)
					directory.close();
			}
			catch(Exception e) {}
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
			try {					
				if (fos != null) {
					fos.flush();
					fos.close();
				}
				if (file != null)
					file.close();
			} catch (Exception e) {
				
			}
		}
		return true;
	}

	private static void handleException(Exception ex) {
		// TODO Auto-generated method stub
		
	}

	
}
