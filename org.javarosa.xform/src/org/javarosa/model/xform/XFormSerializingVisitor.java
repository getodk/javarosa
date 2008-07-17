package org.javarosa.model.xform;

import java.io.DataOutputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import org.javarosa.core.model.DataModelTree;
import org.javarosa.core.model.IAnswerDataSerializer;
import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.QuestionDataElement;
import org.javarosa.core.model.QuestionDataGroup;
import org.javarosa.core.model.TreeElement;
import org.javarosa.core.model.utils.IDataModelSerializingVisitor;
import org.javarosa.core.model.utils.ITreeVisitor;
import org.javarosa.xform.util.JavaRosaXformsParser;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;

public class XFormSerializingVisitor implements IDataModelSerializingVisitor, ITreeVisitor {

	Document theXmlDoc;
	
	Hashtable parentList;
	
	IAnswerDataSerializer serializer;
	
	
	public void visit(DataModelTree tree) {
		theXmlDoc = new Document();
		tree.accept(this);
		parentList = new Hashtable();
		parentList.put(tree.getRootElement(), theXmlDoc);
	}

	public void visit(QuestionDataElement element) {
		
		//First create the textual element for this question data
		Element text = new Element();
		text.setName(element.getName()); 
		//(I think that the below is right. I could be very wrong, and it could
		//require us to create a new element, instead of throwing the string in)
		Object serializedAnswerData = serializer.serializeAnswerData(element.getValue());
		if(serializedAnswerData.getClass() == String.class) {
			text.addChild(Element.TEXT, serializedAnswerData);
		} else if(serializedAnswerData.getClass() == Element.class) {
			text.addChild(Element.ELEMENT, serializedAnswerData);
			
		}

		//Attach to parent
		Node parentNode = (Node)parentList.get(element);
		
		if(parentNode != null) {
			parentNode.addChild(Element.ELEMENT, text);
		}
	}

	public void visit(QuestionDataGroup element) {
		Element thisNode = new Element();
		thisNode.setName(element.getName());
		
		Node parentNode = (Node)parentList.get(element);
		
		if(parentNode != null) {
			parentNode.addChild(Element.ELEMENT, thisNode);
		}
		
		Enumeration en = element.getChildren().elements();
		while (en.hasMoreElements()){ 
			parentList.put(en.nextElement(), thisNode);
		}
	}

	public void visit(TreeElement element) {
		// TODO Auto-generated method stub
		
	}

	public DataOutputStream serializeDataModel(IFormDataModel model) {
		model.accept(this);
		if(theXmlDoc != null) {
			DataOutputStream dos = new DataOutputStream(JavaRosaXformsParser.fromDoc2Stream(theXmlDoc));
			return dos;
		}
		else {
			return null;
		}
	}

	public void visit(IFormDataModel dataModel) {
		if(dataModel.getClass() == DataModelTree.class) {
			((DataModelTree)dataModel).accept(this); 
		}
	}
	
	public void setAnswerDataSerializer(IAnswerDataSerializer ads) {
		this.serializer = ads;
	}
}
