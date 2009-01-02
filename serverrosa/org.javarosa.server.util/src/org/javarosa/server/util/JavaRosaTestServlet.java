package org.javarosa.server.util;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Vector;

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

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.getOutputStream().write(HEADER.getBytes());
		// resp.getOutputStream().write("Yee haw!".getBytes());
		resp.getOutputStream().write(
				new String("<br>Your Last Post was:<br>" + _lastPostParsable
						+ "<br>").getBytes());
		resp
				.getOutputStream()
				.write(
						"<br>Please see <a href=http://192.168.7.210/jr-posts/>the post listing page</a> to review all posts."
								.getBytes());
		if (_lastError != null) {
			resp.getOutputStream().write(_lastError.getBytes());
		}
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		// do something smart here
		try {
			Enumeration headers = req.getHeaderNames();
			
			String postData = "Headers:\n";

			while (headers.hasMoreElements()) {
				String nextName = (String) headers.nextElement();
				String nextValue = req.getHeader(nextName);
				postData += nextName + ":" + nextValue + "\n";
			}
			int bufSize = 100;
			byte[] temp = new byte[bufSize];
			InputStream stream = req.getInputStream();
			int bytesRead = stream.read(temp);
			int totalBytesRead = 0;
			ByteArrayOutputStream body = new ByteArrayOutputStream();
			String fileName = this.getNewFileName("jrpost-in-progress");
			File f = new File(fileName);
			f.createNewFile();
			Writer output = new BufferedWriter(new FileWriter(f));
			output.write(postData);
			try {
				// FileWriter always assumes default encoding is OK!
				while (bytesRead != -1) {
					totalBytesRead += bytesRead;
					String thisChunk;
					//If we didn't read in a full buffer.
					if(bytesRead < bufSize) {
						byte[] newTemp = new byte[bytesRead];
						for (int i = 0; i < bytesRead; i++) {
							newTemp[i] = temp[i];
						}
						thisChunk = new String(newTemp);
						output.write(thisChunk);
						body.write(newTemp);
						bytesRead = req.getInputStream().read(temp);
					}
					//We did read in a full buffer.
					else {
						thisChunk = new String(temp);
						output.write(thisChunk);
						body.write(temp);
						bytesRead = req.getInputStream().read(temp);
					}
				}
			} finally {
				output.close();
			}

			postData += "\nBody: (" + totalBytesRead + " bytes total)\n";
			postData += new String(body.toByteArray());
			
			parseAndSaveHtml(req, body.toByteArray());
			
			postData += 
			_lastPostParsable = trim(postData);
			_lastFileName = saveFile(postData);
		} catch (Exception e) {
			logException(e);
			_lastError = HEADER + "<br>" + e.getMessage();
			_lastError += "<br>";
			e.printStackTrace(resp.getWriter());
		}

	}

	private String trim(String postData) {
		// TODO Auto-generated method stub
		if (postData != null && postData.length() > 2000) {
			return postData.substring(0, 2000);
		}
		return postData;
	}

	private void logException(Exception e) throws IOException {
		String fileName = getNewFileName("exception");
		File f = new File(fileName);
		f.createNewFile();
		// use buffering
		Writer output = new BufferedWriter(new FileWriter(f));
		try {
			output.write(e.getMessage());
			output.write(getStackTraceAsString(e));
		} 
		finally {
			output.close();
		}
		
	}

	/**
	* Gets the exception stack trace as a string.
	* @param exception
	* @return
	*/
	public String getStackTraceAsString(Exception exception)
	{
	StringWriter sw = new StringWriter();
	PrintWriter pw = new PrintWriter(sw);
	pw.print(" [ ");
	pw.print(exception.getClass().getName());
	pw.print(" ] ");
	pw.print(exception.getMessage());
	exception.printStackTrace(pw);
	return sw.toString();
	}
	
	private void parseAndSaveHtml(HttpServletRequest req, byte[] body) throws IOException {
		Enumeration headers = req.getHeaderNames();
		
		String htmlPostData = "Headers:<br>";
		
		String type = "text/plain";
		
		String boundary ="";

		while (headers.hasMoreElements()) {
			String nextName = (String) headers.nextElement();
			String nextValue = req.getHeader(nextName);
			htmlPostData += nextName + ":" + nextValue + "<br>";
			if(nextName.equalsIgnoreCase("content-type")) {
				String[] typeData = nextValue.split(";");
				type = typeData[0];
				if(type.equals("multipart/mixed")) {
					String boundData = typeData[1].trim();
					boundary = (boundData.split("=")[1]).replaceAll("'", "");
				}
			}
		}
		htmlPostData += "\n BODY :\n";
		
		if(type.equals("multipart/mixed")) {
			Vector<String> partNames = parseAndHandle(body, boundary);
			for(int i = 0; i < partNames.size() ; ++i) {
				String fullname = partNames.elementAt(i);
				String[] parts =  fullname.split(File.separator);
				htmlPostData += "<a href=" + parts[parts.length - 1] + ">Part " + i + "</a><br/>";
			}
		} else {
			htmlPostData += "<a href=" + handlePart(body, type) + ">Single Part</a><br/>";
		}
		
		String fileName = getNewFileName("jrpost", "html");

		File f = new File(fileName);
		f.createNewFile();
		// use buffering
		Writer output = new BufferedWriter(new FileWriter(f));
		try {
			// FileWriter always assumes default encoding is OK!
			output.write(htmlPostData);
		} finally {
			output.close();
		}
	}
	
	private Vector<String> parseAndHandle(byte[] body, String boundary) throws IOException{
		Vector<String> partNames = new Vector<String>();
		byte[] boundaryData = ("\n--" + boundary + "\n").getBytes();
		byte[] finalBoundary = ("\n--" + boundary + "--\n").getBytes();
		Vector<Integer> boundaries = locate(body, boundaryData);
		boundaries.addAll(locate(body, finalBoundary));
		
		//There should always be two of these surrounding a body
		for(int i = 0 ; i < boundaries.size() -1; ++i) {
			int start = boundaries.elementAt(i).intValue() + boundaryData.length;
			int end = boundaries.elementAt(i + 1);
			byte[] part = getPart(body, start, end);
			byte[] headerDivBytes = "\n\n".getBytes();
			Vector<Integer>  headerEnd = locate(part, headerDivBytes); 
			byte[] headers = getPart(part, 0, headerEnd.elementAt(0).intValue());
			byte[] partBody = getPart(part, headerEnd.elementAt(0).intValue() + headerDivBytes.length, part.length);
			
			String type = "text/plain";
			String[] headerLines = new String(headers).split("\n");
			for(String header : headerLines) {
				String[] pieces = header.split(":");
				if(pieces[0].trim().equalsIgnoreCase("content-type")) {
					type = pieces[1].trim();
				}
			}
			partNames.add(handlePart(partBody, type));
		}
		return partNames;
	}
	
	/**
	 * 
	 * 
	 * @param body
	 * @param start
	 * @param end
	 * @return body[start, end)
	 */
	private byte[] getPart(byte[] body, int start, int end) {
		int length = (end-start);
		byte[] part = new byte[length];
		for(int i = 0; i < length ; ++ i) {
			part[i] = body[start + i];
		}
		return part;
	}
	
	
	
	private String handlePart(byte[] body, String encoding) throws IOException {
		if(encoding.equals("text/plain")) {
			String fileBody = new String(body);
			String fileName = getNewFileName("jr-submit-xml", "xml");
			File f = new File(fileName);
			Writer output = new BufferedWriter(new FileWriter(f));
			try {
				// FileWriter always assumes default encoding is OK!
				output.write(fileBody);
			} finally {
				output.close();
				return fileName;
			}
		} else if(encoding.contains("image/")) {
			String extension = encoding.split("/")[1];
			String fileName = getNewFileName("jr-submit-" + extension, extension);
			File f = new File(fileName);
			OutputStream output = new FileOutputStream(f);
			try {
				// FileWriter always assumes default encoding is OK!
				output.write(body);
				
			} finally {
				output.close();
				return fileName;
			}
		} else {
			return null;
		}
	}
	
	private String saveFile(String postData) throws IOException {
		String fileName = getNewFileName("jrpost");

		File f = new File(fileName);
		f.createNewFile();
		// use buffering
		Writer output = new BufferedWriter(new FileWriter(f));
		try {
			// FileWriter always assumes default encoding is OK!
			output.write(postData);
		} finally {
			output.close();
		}
		return fileName;
	}

	private String getNewFileName(String base, String ext) {
		return _storageRoot + base + "-" + System.currentTimeMillis() + "." + ext;
	}
	
	private String getNewFileName(String base) {
		return getNewFileName(base, "txt");
	}

	public void init() throws ServletException {

	}
	
	//stackoverflow
	static Vector<Integer> Empty = new Vector<Integer>();

    public static Vector<Integer> locate (byte [] self, byte [] candidate)
    {
        if (isEmptyLocate (self, candidate))
                return Empty;

        Vector<Integer> list = new Vector<Integer> ();

        for (int i = 0; i < self.length; i++) {
                if (!isMatch (self, i, candidate))
                        continue;

                list.add (i);
        }
        return list.size() == 0 ? Empty : list;
    }

    static boolean isMatch (byte [] array, int position, byte [] candidate)
    {
        if (candidate.length > (array.length - position))
                return false;

        for (int i = 0; i < candidate.length; i++)
                if (array [position + i] != candidate [i])
                        return false;

        return true;
    }

    static boolean isEmptyLocate (byte [] array, byte [] candidate)
    {
        return array == null
                || candidate == null
                || array.length == 0
                || candidate.length == 0
                || candidate.length > array.length;
    }

    //</stackoverflow>
}
