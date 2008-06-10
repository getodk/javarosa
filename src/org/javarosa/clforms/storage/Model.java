package org.javarosa.clforms.storage;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import org.javarosa.clforms.xml.XMLUtil;
import org.kxml2.io.KXmlSerializer;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;

public class Model implements Externalizable, IDRecordable

{
	private int XformReference;
	private Document xmlModel;
	private int recordId = 0;
	private String name;
	private Date dateSaved;
	private int editID = -1;

	public Model() {
		loadName();

	}

	public void loadName() {

		if(xmlModel != null	){

			Element e = xmlModel.getRootElement();
			this.name = e.getAttributeValue("", "id");
			System.out.println(e.getName()+" MODEL SET NAME: "+this.name);
			for(int i=0; i<e.getAttributeCount();i++){
				System.out.println(e.getAttributeName(i));
			}

			Element ee = xmlModel.getElement(null, "model");
		}else{
			this.name = null;
		}
	}

	public void readExternal(DataInputStream in) throws IOException {
		XMLUtil.parseModel(new InputStreamReader(in),this);
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		try {
			KXmlSerializer serializer = new KXmlSerializer();
			serializer.setOutput(out, null);
			xmlModel.write(serializer);
			serializer.flush();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage()); //$NON-NLS-1$
		}

	}

	public void setRecordId(int recordId) {
		this.recordId = recordId;

	}

	public Document getXmlModel() {
		return xmlModel;
	}

	public void setXmlModel(Document xmlModel) {
		this.xmlModel = xmlModel;
	}

	public int getRecordId() {
		return recordId;
	}

	/**
	 * Updates the xmlModel with the data in a particular prompt.
	 *
	 * @param prompt
	 */
	public String toString() {
		String value = "";

		//value = traverseXML(xmlModel.getRootElement(), value, false, false, 0);

		KXmlSerializer serializer = new KXmlSerializer();

		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		/*//OutputConnection connection = (OutputConnection) Connector.open(file,
				Connector.WRITE);
		// TODO do something appropriate if the file does not exist
		OutputStream os = connection.openOutputStream();*/
		//Document xml = this.xmlModel;
		try {
			serializer.setOutput(bao, null);
			this.xmlModel.getRootElement().write(serializer);
			serializer.flush();
			value = bao.toString();
			System.out.println("MODEL TOSTRING:\n"+value);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return value;
	}

	/**
	 * @param node
	 * @param value
	 * @param cleanText		if true removes all TEXT children from nodes
	 * @param formatting 	if true return string is indented XML
	 * @param indent		recursive parameter for formatting
	 * @return
	 */
	private String traverseXML(Node node, String value, boolean cleanText, boolean formatting, int indent) {

		if (!cleanText && node instanceof Element){

			if(formatting)
				for (int i = 0; i<indent; i++)
					value += "   ";
			value += "<"+((Element )node).getName();//+((Element )node).getChildCount();

			for (int i = 0; i < ((Element )node).getAttributeCount();i++) {
					value += ((Element )node).getAttributeName(i)+ "=" +((Element )node).getAttributeValue(i);
			}
			value+= ">";
			if(formatting)
				value+= "\n";
		}
			// children
		for (int i = 0; i < node.getChildCount(); i++) {
			if ( node.getType(i) == Node.ELEMENT)
				value = traverseXML((Element)node.getChild(i), value, cleanText, formatting, indent+1);
			else if (node.getType(i) == Node.TEXT) {
				if (cleanText)
					node.removeChild(i);
				else{
					if(formatting)
						for (int j = 0; j<indent+1; j++)
							value += "   ";
					value +=  node.getText(i);
					if(formatting)
						value += "\n";
				}
			}
		}

		if(formatting)
			for (int i = 0; i<indent; i++)
				value += "   ";
		value += "</"+((Element )node).getName()+">";
		if(formatting)
			value += "\n";

		return value;
	}

	public void trimXML(){
		trimWSnodes(xmlModel.getRootElement());
	}

	private void trimWSnodes(Node node) {

		for (int i = 0; i < node.getChildCount(); i++) {
			if ( node.getType(i) == Node.ELEMENT){
				trimWSnodes((Element)node.getChild(i));
			}
			else if (node.getType(i) == Node.TEXT) {
				if (node.getText(i).trim().length()==0)
					node.removeChild(i);  //droos: won't this skip over the next node?
										  //also, there are circumstances where we wouldn't want to remove this node
			}
			else if (node.getType(i) == Node.IGNORABLE_WHITESPACE) {
				node.removeChild(i);
			}
		}
	}

	public void clearData() {
		String value = traverseXML(xmlModel.getRootElement(), null, true, false, 0);
	}

	public int getXformReference() {
		return XformReference;
	}

	public void setXformReference(int xformReference) {
		XformReference = xformReference;
	}

	public Date getDateSaved() {
		return dateSaved;
	}

	public void setDateSaved(Date date) {
		this.dateSaved = date;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setEditID(int recordId2) {
		this.editID  = recordId2;
	}

	public int getEditID() {
		return editID;
	}
}
