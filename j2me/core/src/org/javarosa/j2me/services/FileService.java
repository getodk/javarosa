/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.j2me.services;

import java.io.InputStream;
import java.io.OutputStream;

import org.javarosa.j2me.services.exception.FileException;

/**
 * 
 * Service providing an interface for File I/O
 * @author Ndubisi Onuora
 *
 */
public interface FileService {	
	/**
	 * 
	 * @param path to place the directory
	 * @return whether or the directory was created
	 * @throws FileException
	 */
	public boolean createDirectory(String path) throws FileException;
	
	/**
	 * 
	 * @param path to delete directory
	 * @return whether or the directory was deleted
	 * @throws FileException
	 */
	public boolean deleteDirectory(String path) throws FileException;
	
	/**
	 * 
	 * @return the default root directory
	 * @throws FileException
	 */
	public String getDefaultRoot() throws FileException;
	
	/**
	 * 
	 * @return a list of root directories
	 * @throws FileException
	 */
	public String[] getRootNames() throws FileException;
	
	/**
	 * 
	 * @param directoryPath, the path of the directory
	 * @return a list of files in the current directory
	 * @throws FileException
	 */
	public String[] listDirectory(String directoryPath) throws FileException;
	
	
	/**
	 * 
	 * @param fileName for the created file
	 * @param data to write to the created file
	 * @return whether or not the file was created
	 * @throws FileException
	 */
	public boolean createFile(String fileName, byte[] data) throws FileException;
	
	/**
	 * 
	 * @param fileName for the file to be deleted
	 * @return whether or not the file was created
	 * @throws FileException
	 */
	public boolean deleteFile(String fileName) throws FileException;
	
	/**
	 * 
	 * @param fileName
	 * @return whether or not the file exists
	 * @throws FileException
	 */
	public boolean fileExists(String fileName) throws FileException;
	
	/**
	 * 
	 * @param filename to retrieve data
	 * @return data within the file
	 * @throws FileException
	 */
	public byte[] getFileData(String filename) throws FileException;
	
	/**
	 * 
	 * @param fileName to retrieve InputStream
	 * @return access to the file's data as a stream
	 * @throws FileException
	 */
	public InputStream getFileDataStream(String fileName) throws FileException;
	
	/**
	 * 
	 * @param fileName to retrieve opened OutputStream
	 * @return access to the output stream created by the file
	 * @throws FileException
	 */
	public OutputStream getFileOutputStream(String fileName) throws FileException;
}
