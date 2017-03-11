package f5networks;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Base64;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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
	
	private static byte[] compress(byte[] source)  {
	    if (source == null || source.length == 0) {
	        return source;
	    }

	    ByteArrayInputStream sourceStream = new ByteArrayInputStream(source);
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream(source.length / 2);
	    OutputStream compressor = null;

	    try {
			compressor = new GZIPOutputStream(outputStream);
		    compressor.write(source);
		    compressor.close();	 
		    return outputStream.toByteArray();
		} catch (IOException e) {

			e.printStackTrace();
		}
		return null;
	}
	private static String decompress( byte[] source ){
		
		byte[] valueDecoded = Base64.getDecoder().decode(source );
		System.out.println("Decoded value is " + new String(valueDecoded));
		ByteArrayInputStream sourceStream = new ByteArrayInputStream(valueDecoded);
	    
	    GZIPInputStream decompressor = null;

	    try {
			decompressor = new GZIPInputStream(sourceStream);
			String readed = new String();
			byte[] buffer = new byte[1024];
	        while ( decompressor.read( buffer ) != -1) {
	        	readed += new String( buffer, "UTF-8");	            
	        }
	        System.out.println(readed);
	        decompressor.close();		   		 	 
		    return readed;
		} catch (IOException e) {

			e.printStackTrace();
		}
		return null;

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
		/*
		NodeList childList = subNode.getChildNodes();
    	String output = new String();
    	if( childList.getLength() == 0 ) {
    		output = nodeToString( subNode );
    	}
    	else {
    		for(int iter = 0; iter < childList.getLength(); iter++) {
    			Node child = childList.item(iter);
    			output += nodeToString( child );   			
    		}
    	}
    	*/
		String output = new String();
		output = nodeToString( subNode );
    	System.out.println(output);
    	System.out.println("\n\n");
    	
    	byte[] compressedBytes = compress(output.trim().getBytes());
    	
    	byte[]   bytesEncoded = Base64.getEncoder().encode(compressedBytes);
    	
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
			String decompressed = decompress(subNode.getTextContent().trim().getBytes("utf-8"));	
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Node parent = subNode.getParentNode();
			Document doc = parent.getOwnerDocument();

			System.out.println("rajkran" + decompressed);
			Node fragment = docBuilder.parse(new ByteArrayInputStream(decompressed.trim().getBytes("utf-8"))).getDocumentElement();
			fragment = doc.importNode(fragment, true);



			parent.removeChild(subNode);
			parent.appendChild(fragment);

		} catch (SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
