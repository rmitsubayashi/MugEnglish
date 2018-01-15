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
	//attributeで検索する機能がJavaのDOMに付いてないから
	//簡単に作った
	//ノードの構成図
	//<result>
	//  <binding name='name'>
	//    <literal xml:lang='ja/en'>value</literal>
	//  </binding>
	//</result>
	public static String findValueByNodeName(Node headNode, String name){
		//"name"が固定なのはSPARQLクエリーすべてに対して
		Node resultNode = findNodeByNameOfAttribute(headNode, "name", name);
		//なかった場合
		if (resultNode == null)
			return "";
		//なぜか<literal>タグの周りに二つtextタグが付いてる。
		//だからitem(0) ではなくてitem(1)をとる
		return resultNode.getChildNodes().item(1).getTextContent();
	}

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
