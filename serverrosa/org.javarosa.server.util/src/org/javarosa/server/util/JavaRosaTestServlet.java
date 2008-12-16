package org.javarosa.server.util;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JavaRosaTestServlet extends HttpServlet {

	private static final String HEADER = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\">";
	private static final String _storageRoot = "/usr/share/tomcat5.5/webapps/jrtest/store/";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String _lastPostParsable = null;
	private Object _lastFileName;
	private String _lastError;
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.getOutputStream().write(HEADER.getBytes());
		//resp.getOutputStream().write("Yee haw!".getBytes());
		resp.getOutputStream().write(new String("<br>Your Last Post was:<br>" + _lastPostParsable + "<br>").getBytes());
		resp.getOutputStream().write("<br>Please see <a href=http://192.168.7.210/jr-posts/>the post listing page</a> to review all posts.".getBytes());
		if (_lastError != null) {
			resp.getOutputStream().write(_lastError.getBytes());
		}
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		// do something smart here
		try {
		Enumeration headers = req.getHeaderNames();
		//String postData = "Headers:<br>";
		String postData = "Headers:\n";
		
		while (headers.hasMoreElements()) {
			String nextName = (String) headers.nextElement();
			String nextValue = req.getHeader(nextName);
			//postData += nextName + ":" + nextValue + "<br>";
			postData += nextName + ":" + nextValue + "\n";
		}
		byte[] temp = new byte[1024];
		int bytesRead =req.getInputStream().read(temp); 
		int totalBytesRead = 0;
		String bodyString = "";
		while (bytesRead > 0) {
			totalBytesRead += bytesRead;
			if (bytesRead == temp.length) {
				bodyString += new String(temp);
				bytesRead =req.getInputStream().read(temp);
			} else {
				byte[] newTemp = new byte[bytesRead];
				for (int i = 0; i < bytesRead; i++) {
					newTemp[i] = temp[i];
				}
				bodyString += new String(newTemp);
				bytesRead = 0;
			}
		}
		//postData += "<br>Body: (" + totalBytesRead + " bytes total)<br>" ;
		postData += "\nBody: (" + totalBytesRead + " bytes total)\n" ;
		postData += bodyString;
		_lastPostParsable = postData;
		_lastFileName = saveFile(postData);
		} catch (Exception e) {
			_lastError = HEADER + "<br>" + e.getMessage();
			_lastError += "<br>";
			e.printStackTrace(resp.getWriter());
		}
		
	}
	
	private String saveFile(String postData) throws IOException {
		String fileName = _storageRoot + "jrpost-" + System.currentTimeMillis() + ".txt";
		File f = new File(fileName);
		f.createNewFile();
		 //use buffering
	    Writer output = new BufferedWriter(new FileWriter(f));
	    try {
	      //FileWriter always assumes default encoding is OK!
	      output.write( postData);
	    }
	    finally {
	      output.close();
	    }
	    return fileName;
	}

	public void init() throws ServletException {
		
	}
}
