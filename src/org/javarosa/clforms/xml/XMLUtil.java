/**
 * 
 */
package org.javarosa.clforms.xml;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.OutputConnection;
import javax.microedition.io.file.FileConnection;

import minixpath.XPathExpression;

import org.javarosa.clforms.api.Binding;
import org.javarosa.clforms.api.Constants;
import org.javarosa.clforms.api.Form;
import org.javarosa.clforms.api.Prompt;
import org.javarosa.clforms.storage.Model;
import org.javarosa.clforms.util.SimpleOrderedHashtable;
import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;
import org.xmlpull.v1.XmlPullParser;

import de.enough.polish.util.Locale;
import de.enough.polish.util.StringTokenizer;
import de.enough.polish.util.TextUtil;

/**
 * 
 */
public class XMLUtil {

	/**
	 * Method to parse an XForm from an input stream.
	 * 
	 * @param inputStreamReader
	 * @return XForm
	 */
	public static Form parseForm(InputStreamReader isr) {
		Form form = new Form();
		// xform.setName(getRandomName());
		// LOG
		//System.out.println("in parse Form1");

		try {
			KXmlParser parser = new KXmlParser();
			parser.setInput(isr);
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
			Document doc = new Document();
			doc.parse(parser);

			Element html = doc.getRootElement();
			parseElement(form, html, null);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return form;
	}
	
	/**
	 * Method to parse an input stream into an existing XForm.
	 * 
	 * @param inputStreamReader, XForm
	 * @return XForm
	 */
	public static void parseForm(InputStreamReader isr, Form form) throws Exception{

		// LOG
		//System.out.println("in parse Form2");
		// try {
			KXmlParser parser = new KXmlParser();
			parser.setInput(isr);
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
			Document doc = new Document();
			doc.parse(parser);
			// LOG
			//System.out.println("succesfully Kxml parsing");
			
			Element html = doc.getRootElement();
			parseElement(form, html, null);
			
			//TODO FIGURE out why have to trim twice!
			form.getXmlModel().trimXML();
	        form.getXmlModel().trimXML();
			
		/*} catch (Exception ex) {
			
			// TODO handle exception
			
			form = null;
			ex.printStackTrace();
		}*/
	}


	/**
	 * Recursive method to process XML.
	 * 
	 * @param xform
	 * @param element
	 * @param existingPrompt
	 * @return
	 */
	private static Prompt parseElement(Form form, Element element,
			Prompt existingPrompt) throws Exception{
		String label = ""; //$NON-NLS-1$
		String value = ""; //$NON-NLS-1$

		// LOG
		//System.out.println("parsing element: " + element.getName());
		//System.out.println("no children:"+element.getChildCount());
		int numOfEntries = element.getChildCount();
		for (int i = 0; i < numOfEntries; i++) {
			if (element.isText(i)) {
				// Text here are all insignificant white spaces.
				// We are only interested in children elements
				// LOG
				//System.out.println(element.getName()+" skipping a whitespace");
			} else {
				Element child = element.getElement(i);
				// LOG
				//System.out.println("analysing " +element.getName()+" child: "+child.getName());
				
				String tagname = child.getName();
				if (TextUtil.equalsIgnoreCase(tagname,"head")) { //$NON-NLS-1$
					parseElement(form, child, null);
				} else if (TextUtil.equalsIgnoreCase(tagname,"body")) { //$NON-NLS-1$
					parseElement(form, child, null);
				} else if (TextUtil.equalsIgnoreCase(tagname,"title")) { //$NON-NLS-1$
					form.setName(child.getText(0).trim());
				} else if (TextUtil.equalsIgnoreCase(tagname,"model")) { //$NON-NLS-1$
					// LOG
					//System.out.println("found and creating Model"+model.toString());
					Model model = new Model();
					Document xmlDoc = new Document();
					model.setXmlModel(xmlDoc);
					form.setXmlModel(model);
					for(int j=0;j<child.getAttributeCount();j++){
						if (TextUtil.equalsIgnoreCase(child.getAttributeName(j),"id")){
							form.setName(child.getAttributeValue(j));
							if (form.getXmlModel() != null)
								form.getXmlModel().setName(form.getName());
							//LOG 
							//System.out.println("name found!!"+form.getName()+child.getAttributeValue(j));
						}
					}
					parseElement(form, child, null);
				} else if (TextUtil.equalsIgnoreCase(tagname,"instance")) { //$NON-NLS-1$
					Document xmlDoc = form.getXmlModel().getXmlModel();
					xmlDoc.addChild(Node.ELEMENT, child.getElement(1));

				} else if (TextUtil.equalsIgnoreCase(tagname,"bind")) { //$NON-NLS-1$

					Binding b = new Binding();
					b.setId(child.getAttributeValue("", "id")); //$NON-NLS-1$ //$NON-NLS-2$
					b.setNodeset(child.getAttributeValue("", "nodeset")); //$NON-NLS-1$ //$NON-NLS-2$
					b.setRelevancy(child.getAttributeValue("", "relevant")); //$NON-NLS-1$ //$NON-NLS-2$
					if (child.getAttributeValue("", "required") != null)
						b.setRequired(child.getAttributeValue("", "required")); //$NON-NLS-1$ //$NON-NLS-2$
					String type = child.getAttributeValue("", "type"); //$NON-NLS-1$ //$NON-NLS-2$
					if (type.indexOf(':') > 0)
						type = type.substring(type.indexOf(':') + 1);
					b.setType(type);
					form.addBinding(b);
					//LOG
					//System.out.println("Bind added to form = \n"+b.toString());

				} else if (TextUtil.equalsIgnoreCase(tagname,"input")) { //$NON-NLS-1$
					
					//LOG
					//System.out.println("found input");
					Prompt prompt = new Prompt();
					prompt.setFormControlType(Constants.INPUT);
					String ref = child.getAttributeValue(null, "ref"); //$NON-NLS-1$
					String bind = child.getAttributeValue(null, "bind"); //$NON-NLS-1$
					attachBind(form, prompt, ref, bind);
					String relevant = child.getAttributeValue(null, "relevant"); //$NON-NLS-1$
					if (relevant != null){
						prompt.setRelevantString(relevant);
					}
					prompt = parseElement(form, child, prompt);
					form.addPrompt(prompt);

				} else if (TextUtil.equalsIgnoreCase(tagname,"select1")) { //$NON-NLS-1$

					//LOG
					//System.out.println("found select1");
					Prompt prompt = new Prompt();
					String ref = child.getAttributeValue(null, "ref"); //$NON-NLS-1$
					String bind = child.getAttributeValue(null, "bind"); //$NON-NLS-1$
					attachBind(form, prompt, ref, bind);
					prompt.setFormControlType(Constants.SELECT1);
					prompt.setReturnType(org.javarosa.clforms.api.Constants.RETURN_SELECT1);
					prompt.setSelectMap(new SimpleOrderedHashtable());
					prompt = parseElement(form, child, prompt);
					form.addPrompt(prompt);

				} else if (TextUtil.equalsIgnoreCase(tagname,"select")) { //$NON-NLS-1$

					//LOG
					//System.out.println("found select");
					Prompt prompt = new Prompt();
					String ref = child.getAttributeValue(null, "ref"); //$NON-NLS-1$
					String bind = child.getAttributeValue(null, "bind"); //$NON-NLS-1$
					attachBind(form, prompt, ref, bind);
					prompt.setFormControlType(Constants.SELECT);
					prompt.setReturnType(org.javarosa.clforms.api.Constants.RETURN_SELECT_MULTI);
					prompt.setSelectMap(new SimpleOrderedHashtable());
					prompt = parseElement(form, child, prompt);
					form.addPrompt(prompt);

				} else if (TextUtil.equalsIgnoreCase(tagname,"label")) { //$NON-NLS-1$
					label = child.getText(0).trim();
				} else if (TextUtil.equalsIgnoreCase(tagname,"hint")) { //$NON-NLS-1$
					existingPrompt.setHint(child.getText(0).trim());
				} else if (TextUtil.equalsIgnoreCase(tagname,"item")) { //$NON-NLS-1$
					parseElement(form, child, existingPrompt);
					// TODO possible need to handle this return
				} else if (TextUtil.equalsIgnoreCase(tagname,"value")) { //$NON-NLS-1$
					value = child.getText(0).trim();
				} else if (TextUtil.equalsIgnoreCase(tagname,"textbox")) {
					System.out.println("found textbox");
					Prompt prompt = new Prompt();
					prompt.setFormControlType(Constants.TEXTBOX);
					String ref = child.getAttributeValue(null, "ref"); //$NON-NLS-1$
					String bind = child.getAttributeValue(null, "bind"); //$NON-NLS-1$
					if (bind != null) {
						Binding b = (Binding) form.getBindings().get(bind);
						if (b != null) {
							prompt.setBindID(bind);
							prompt.setXpathBinding(b.getNodeset());
							prompt.setReturnType(getReturnTypeFromString(b.getType()));
							prompt.setId(b.getId());
						}
					} else if (ref != null) {
						prompt.setXpathBinding(ref);
						prompt.setId(getLastToken(ref, '/'));
						
					}
					String relevant = child.getAttributeValue(null, "relevant"); //$NON-NLS-1$
					if (relevant != null){
						prompt.setRelevantString(relevant);
					}
					prompt = parseElement(form, child, prompt);
					form.addPrompt(prompt);
				} else{ // tagname not recognised
					parseElement(form, child, null);
					// TODO possible need to handle this return
				}
				// TODO - how are other elements like html:p or br handled?
			}
		}

		if (!label.equals("") && !value.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
			if (existingPrompt.getSelectMap() != null) {
				existingPrompt.getSelectMap().put(label, value);
			}
		} else if (!label.equals("")) { //$NON-NLS-1$
			existingPrompt.setLongText(label);
		}
		
		return existingPrompt;
	}

	private static void attachBind(Form form, Prompt prompt, String ref,
			String bind) {
		if (bind != null) {
			Binding b = (Binding) form.getBindings().get(bind);
			if (b != null) {
				prompt.setBindID(bind);
				prompt.setXpathBinding(b.getNodeset());
				prompt.setReturnType(getReturnTypeFromString(b.getType()));
				prompt.setId(b.getId());
				prompt.setBind(b);
				// LOG
				//System.out.println(prompt.getLongText()+" attached to Bind = "+prompt.getBind().toString());
			}
			else
				//LOG
				System.out.println("MATCHING BIND not found");
		} else if (ref != null) {
			prompt.setXpathBinding(ref);
			prompt.setId(getLastToken(ref, '/'));
		}
	}
	
	/**
	 * Method to parse an input stream into an existing XForm.
	 * 
	 * @param inputStreamReader, XForm
	 * @return XForm
	 */
	public static void parseModel(InputStreamReader isr, Model model) {

		try {
			KXmlParser parser = new KXmlParser();
			parser.setInput(isr);
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
			Document doc = new Document();
			doc.parse(parser);

			Element root = doc.getRootElement();
			
			model.setXmlModel(doc);
			//TODO FIGURE out why have to trim twice!
			model.trimXML();
	        model.trimXML();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	

	private static String getLastToken(String ref, char c) {
		StringTokenizer tok = new StringTokenizer(ref, c);
		String last = ""; //$NON-NLS-1$
		while (tok.hasMoreTokens())
			last = tok.nextToken();
		return last;
	}

	/**
	 * @param type
	 * @return
	 */
	private static int getReturnTypeFromString(String type) {
		int index = type.indexOf(':');
		if (index > 0)
			type = type.substring(index + 1);

		if (TextUtil.equalsIgnoreCase(type,"int")) //$NON-NLS-1$
			return Constants.RETURN_INTEGER;
		else if (TextUtil.equalsIgnoreCase(type,"numeric")) //$NON-NLS-1$
			return Constants.RETURN_INTEGER;
		else if (TextUtil.equalsIgnoreCase(type,"date")) //$NON-NLS-1$
			return Constants.RETURN_DATE;
		else if (TextUtil.equalsIgnoreCase(type,"boolean")) //$NON-NLS-1$
			return Constants.RETURN_BOOLEAN;
		else
			return Constants.RETURN_STRING;
	}

	/**
	 * Opens a file and returns an InputStreamReader to it
	 * 
	 * @param file
	 * @return
	 */
	public static InputStreamReader getReader(String file) {

		InputStreamReader isr = null;
		try {
		  //#if app.usefileconnections
			FileConnection fc = (FileConnection) Connector.open(file);

			if (!fc.exists()) {
				throw new IOException(Locale.get("error.file"));
			} else {
				InputStream fis = fc.openInputStream();

				isr = new InputStreamReader(fis);
				/*
				 * fis.close(); fc.close();
				 */

			}
		  //#endif
		} catch (Exception e) {
			e.printStackTrace();
		}

		return isr;
	}

	/**
	 * Writes string parameter to a file
	 * 
	 * @param file
	 * @param writableString
	 */
	public static void writeStringToFile(String file, String writableString) {

		try {
			// "file://c:/myfile.txt;append=true"
			OutputConnection connection = (OutputConnection) Connector.open(
					file, Connector.WRITE);
			// TODO do something appropriate if the file does not exist
			OutputStream os = connection.openOutputStream();
			os.write(writableString.getBytes());
			os.flush();
			os.close();
		} catch (IOException e) {
			// TODO handle exception appropriately
			System.out.println(Locale.get("error.file.write")); //$NON-NLS-1$
			e.printStackTrace();
		}
	}

	/**
	 * Writes XML Document parameter to a file
	 * 
	 * @param file
	 * @param xml
	 */
	public static void writeXMLToFile(String file, Document xml)
			throws IOException {
		KXmlSerializer serializer = new KXmlSerializer();

		OutputConnection connection = (OutputConnection) Connector.open(file,
				Connector.WRITE);
		// TODO do something appropriate if the file does not exist
		OutputStream os = connection.openOutputStream();

		serializer.setOutput(os, null);
		xml.write(serializer);
		serializer.flush();

		os.close();
		connection.close();
	}

	public static String readTextFile(String url) throws IOException {
	    StringBuffer content = new StringBuffer();
	  //#if app.usefileconnections
		FileConnection fc = (FileConnection) Connector.open(url);

		if (!fc.exists()) {
			throw new IOException("File does not exists");
		}

		InputStream fis = fc.openInputStream();
		int ch;
		while ((ch = fis.read()) != -1) {
			
			content.append((char) ch);
		}


		fis.close();
		fc.close();

		//#else
		try {
		    throw new IOException("No file connection API available");
		}
		catch (IOException e) {
		    throw e;
		}
		finally
		//#endif
		{
	    return content.toString();
		}
	}

	public static void printModel(Document doc) throws IOException {
		KXmlSerializer serializer = new KXmlSerializer();
		serializer.setOutput(System.out, null);
		doc.write(serializer);
		serializer.flush();
	}
	
	
	/**
	 * Evaluates an Xpath expression on the xmlModel and returns a Vector result
	 * set.
	 * 
	 * @param string
	 * @return Vector result set
	 */
	public Vector evaluateXpath(Model xmlModel, String xpath) {
		XPathExpression xpls = new XPathExpression(xmlModel.getXmlModel(), xpath);
		return xpls.getResult();
	}
	
	public static String sendHttpGet(String url) {
		HttpConnection hcon = null;
		DataInputStream dis = null;
		StringBuffer responseMessage = new StringBuffer();

		try {
			// a standard HttpConnection with READ access
			hcon = (HttpConnection) Connector.open(url);

			// obtain a DataInputStream from the HttpConnection
			dis = new DataInputStream(hcon.openInputStream());

			// retrieve the response from the server
			int ch;
			while ((ch = dis.read()) != -1) {
				responseMessage.append((char) ch);
			}
		} catch (Exception e) {
			e.printStackTrace();
			responseMessage.append("ERROR");
		} finally {
			try {
				if (hcon != null)
					hcon.close();
				if (dis != null)
					dis.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		return responseMessage.toString();
	}

	public static String sendHttpPost(String url) {
		HttpConnection hcon = null;
		DataInputStream dis = null;
		DataOutputStream dos = null;
		StringBuffer responseMessage = new StringBuffer();
		// the request body
		String requeststring = "uname=simon" +
				"&pw=123abc" +
				"&redirect=%2FOPENMRSDTHF" +
				"&refererURL=http%3A%2F%2Ftambo.cell-life.org%3A8180%2FOPENMRSDTHF%2Flogin.htm";

		try {
			// an HttpConnection with both read and write access
			hcon = (HttpConnection) Connector.open(url, Connector.READ_WRITE);

			// set the request method to POST
			hcon.setRequestMethod(HttpConnection.POST);
			
			// obtain DataOutputStream for sending the request string
			dos = hcon.openDataOutputStream();
			byte[] request_body = requeststring.getBytes();

			// send request string to server
			for (int i = 0; i < request_body.length; i++) {
				dos.writeByte(request_body[i]);
			}// end for( int i = 0; i < request_body.length; i++ )

			// obtain DataInputStream for receiving server response
			dis = new DataInputStream(hcon.openInputStream());

			// retrieve the response from server
			int ch;
			while ((ch = dis.read()) != -1) {
				responseMessage.append((char) ch);
			}// end while( ( ch = dis.read() ) != -1 ) {
		} catch (Exception e) {
			e.printStackTrace();
			responseMessage.append("ERROR");
		} finally {
			// free up i/o streams and http connection
			try {
				if (hcon != null)
					hcon.close();
				if (dis != null)
					dis.close();
				if (dos != null)
					dos.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}// end try/catch
		}// end try/catch/finally
		return responseMessage.toString();
	}// end sendHttpPost( String )
}
