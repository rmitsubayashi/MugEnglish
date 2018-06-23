package pelicann.linnca.com.corefunctionality.connectors;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class SPARQLDocumentParserHelper {
	//no method to search by attribute in the Java DOM :(
	//The node looks like:
	//<result>
	//  <binding name='name'>
	//    <literal xml:lang='ja/en'>value</literal>
	//  </binding>
	//</result>
	public static String findValueByNodeName(Node headNode, String name){
		//we want all the 'name' attributes because that's what is returned
		// by SPARQL queries
		Node resultNode = findNodeByNameOfAttribute(headNode, "name", name);
		//if the SPARQL query returned empty
		if (resultNode == null)
			return "";
		//there are two random text tags around the <literal> tag,
		// so don't get item(0) but item(1)
		return resultNode.getChildNodes().item(1).getTextContent();
	}

	//recursive method to check all nodes and their children
	private static Element findNodeByNameOfAttribute(Node node, String attribute, String name){
		if (node.getNodeType() == Node.ELEMENT_NODE){
			Element e =(Element)node;
			if (e.hasAttribute(attribute)){
				if (e.getAttribute(attribute).equals(name))
					return e;
			}
		}

		NodeList children = node.getChildNodes();
		for (int i=0; i<children.getLength(); i++ ){
			node = findNodeByNameOfAttribute(children.item(i), attribute, name);
			if (node != null)
				return (Element)node;
		}

		return null;
	}

	public static String DOMToString(Document document){
		DOMSource domSource = new DOMSource(document);
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		TransformerFactory tf = TransformerFactory.newInstance();
		try {
			Transformer transformer = tf.newTransformer();
			transformer.transform(domSource, result);
		} catch (Exception e){
			e.printStackTrace();
		}
		return writer.toString();
	}
}
