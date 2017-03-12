package f5networks;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.util.Base64;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class XMLparsing {
	private final static String filename = "outputxml";
	private final static String url = "http://posttestserver.com/post.php";
	public static void main(String[] args) {
		for(int i = 0; i < args.length; i++) {
            System.out.println("rajkiran " + args[i]);
        }

		String input = args[0];
		String action = args[1];

		try {			
			

			System.out.println(" your input file is " + input );
			System.out.println("Your action is " + action);
			
			File f = new File(input);
			if(!f.exists() || f.isDirectory()) { 
				System.out.println( "you have entered an invalid entry");
				return;
			}
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(input);
			doc.normalize();
		
			
			findElementAndAct( doc.getFirstChild(), "test", "1", action );

			//write out the document
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(filename));
			transformer.transform(source, result);		
			
			
			String charset = "UTF-8";
			String param = "value";
			File textFile = new File(filename);

			String boundary = Long.toHexString(System.currentTimeMillis());
			String CRLF = "\r\n";
			URLConnection connection = new URL(url).openConnection();
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);


			OutputStream output = connection.getOutputStream();
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);

			writer.append("--" + boundary).append(CRLF);
			writer.append("Content-Disposition: form-data; name=\"textFile\"; filename=\"" + textFile.getName() + "\"").append(CRLF);
			writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF); // Text file itself must be saved in this charset!
			writer.append(CRLF).flush();
			Files.copy(textFile.toPath(), output);
			output.flush(); // Important before continuing with writer!
			writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
			// Request is lazily fired whenever you need to obtain information about response.
			int responseCode = ((HttpURLConnection) connection).getResponseCode();
			System.out.println(responseCode); // Should be 200


				
		} catch (IOException e) {			
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	
	
	
	
	
	private static String nodeToString(Node node) {
	    StringWriter sw = new StringWriter();
	    try {
	      Transformer t = TransformerFactory.newInstance().newTransformer();
	      t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	      t.setOutputProperty(OutputKeys.INDENT, "yes");
	      t.transform(new DOMSource(node), new StreamResult(sw));
	    } catch (TransformerException te) {
	      System.out.println("nodeToString Transformer Exception");
	    }
	    return sw.toString();
	  }
	
	
	private static void compressNode( Node subNode ){
		
		String output = new String();
		output = nodeToString( subNode ).trim();
    	System.out.println(output);
    	System.out.println("\n\n");
       
    	byte[]   bytesEncoded = Base64.getEncoder().encode(output.getBytes());
    	
    	String compressedout = new String(bytesEncoded );
    	System.out.println("ecncoded value is " + compressedout);   	
    	subNode.setTextContent(compressedout);
	}


	
	public static void findElementAndAct(Node rootElement, String attributeName, String attributeValue, String action ) {


	    if (rootElement != null && rootElement.hasChildNodes()) {
	        NodeList nodeList = rootElement.getChildNodes();
			 
	        for (int i = 0; i < nodeList.getLength(); i++) {
	            Node subNode = nodeList.item(i);
	            
	            if (subNode.hasAttributes()) {
	                NamedNodeMap nnm = subNode.getAttributes();

	                for (int j = 0; j < nnm.getLength(); j++) {
	                    Node attrNode = nnm.item(j);	                  
	                    if (attrNode.getNodeType() == Node.ATTRIBUTE_NODE) {
	                        Attr attribute = (Attr) attrNode;
	                        
	                        if (attribute.getName().equals( attributeName ) && attributeValue.equals(attribute.getValue())) {
	                        	if( action.equals("--gzip")){
	                        		compressNode( subNode);
	                        	} else {
	                        		decompressNode( subNode );
	                        	}
	                        		                			
	                        } else {
	                        	
	                        	findElementAndAct(subNode, attributeName, attributeValue, action);
	                        }
	                    }
	                }               
	            }
	        }
	    }
	}

	private static void decompressNode(Node subNode) {		
		try {
			byte[] valueDecoded = Base64.getDecoder().decode(subNode.getTextContent().getBytes() );
			String decodedString = new String(valueDecoded);
			System.out.println("Decoded value is " + decodedString );
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Node parent = subNode.getParentNode();
			Document doc = parent.getOwnerDocument();

			System.out.println("rajkiran" + decodedString);
			Node fragment = docBuilder.parse(new ByteArrayInputStream(decodedString.getBytes())).getDocumentElement();
			fragment = doc.importNode(fragment, true);			
			parent.replaceChild(fragment, subNode);
			

		} catch (SAXException | IOException e) {
			
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			
			e.printStackTrace();
		}

	}

}
