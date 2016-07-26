/*******************************************************************************
* Copyright (c) 2015 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.arm.cmsis.pack.CpStrings;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpRootItem;

/**
 * Base class to parse CMSIS pack-related files
 */
public abstract class CpXmlParser implements ICpXmlParser {


	protected ICpItem rootItem   = null;  // represents top-level item being constructed
	protected String xmlFile = null;  // current XML file
	protected String xmlString = null;  // current XML string

	protected String xsdFile = null;        // schema file with absolute path
	protected Set<String> ignoreTags = null; // tags to ignore (partly parsed file)
	protected Set<String> ignoreWriteTags = null; // tags to ignore when writing to xml file

	// errors for current file
	protected List<String> errorStrings = new LinkedList<String>();
	protected int nErrors = 0;
	protected int nWarnings = 0;

	// DOM
	private DocumentBuilderFactory docBuilderFactory = null;
	private DocumentBuilder docBuilder = null;
	protected XmlErrorHandler errorHandler = null;

	public CpXmlParser() {
	}

	public CpXmlParser(String xsdFile) {
		this.xsdFile = xsdFile;
	}

	@Override
	public void clear() {
		xmlFile = null;
		xmlString = null;
		rootItem 	= null;
		errorStrings.clear();
		nErrors   = 0;
		nWarnings = 0;
		if (docBuilder != null) {
			docBuilder.reset();
		}
	}


	@Override
	public String getXsdFile() {
		return xsdFile;
	}

	@Override
	public void setXsdFile(String xsdFile) {
		this.xsdFile = xsdFile;
	}

	/**
	 * @return the xmlFile
	 */
	public String getXmlFile() {
		return xmlFile;
	}

	@Override
	public List<String> getErrorStrings() {
		return errorStrings;
	}

	@Override
	public int getErrorCount() {
		return nErrors;
	}

	@Override
	public int getWarningCount() {
		return nWarnings;
	}


	@Override
	public void setIgnoreTags(Set<String> ignoreTags) {
		this.ignoreTags = ignoreTags;
	}

	@Override
	public void setWriteIgnoreTags(Set<String> ignoreTags) {
		ignoreWriteTags = ignoreTags;
	}


	@Override
	public ICpItem createItem(ICpItem parent, String tag) {
		if(parent != null) {
			return parent.createItem(parent, tag);
		}
		if(rootItem == null) {
			rootItem = createRootItem(tag) ;
		}
		return rootItem;
	}


	private class XmlErrorHandler implements ErrorHandler {

		public XmlErrorHandler() {
			// TODO Auto-generated constructor stub
		}

		@Override
		public void error(SAXParseException arg0) throws SAXException {

			addErrorString(arg0, CpStrings.CpXmlParser_Error);
			nErrors++;
		}

		@Override
		public void fatalError(SAXParseException arg0) throws SAXException {
			addErrorString(arg0, CpStrings.CpXmlParser_FatalError);
			nErrors++;
			throw arg0;
		}

		@Override
		public void warning(SAXParseException arg0) throws SAXException {
			addErrorString(arg0, CpStrings.CpXmlParser_Warning);
			nWarnings++;
		}

		private void addErrorString(SAXParseException arg0,	final String severity) {
			String err = xmlFile;
			int line = arg0.getLineNumber();
			int col = arg0.getColumnNumber();
			if (line > 0) {
				err += "("; //$NON-NLS-1$
				err += line;
				err += ","; //$NON-NLS-1$
				err += col;
				err += ")"; //$NON-NLS-1$
			}
			err += ": " + severity + ": "; //$NON-NLS-1$ //$NON-NLS-2$
			err += arg0.getLocalizedMessage();
			errorStrings.add(err);
		}
	}

	@Override
	public boolean init() {

		try {
			if(docBuilderFactory == null) {
				docBuilderFactory = DocumentBuilderFactory.newInstance();
				docBuilderFactory.setValidating(false);
				docBuilderFactory.setNamespaceAware(true);
				if (xsdFile != null && !xsdFile.isEmpty()) {
					SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
					Schema schema = schemaFactory.newSchema(new Source[] {new StreamSource(xsdFile)});
					docBuilderFactory.setSchema(schema);
				}
			}
			if(docBuilder == null){
				docBuilder = docBuilderFactory.newDocumentBuilder();
				errorHandler = new XmlErrorHandler();
			}
			docBuilder.setErrorHandler(errorHandler);
		} catch (ParserConfigurationException e) {
			String err = CpStrings.CpXmlParser_ErrorParserInit;
			err += ": "; //$NON-NLS-1$
			err += e.toString();
			errorStrings.add(err);
			nErrors++;
			docBuilder = null;
			e.printStackTrace();
			return false;
		} catch (SAXException e) {
			String err = CpStrings.CpXmlParser_ErrorSchemaInit;
			err += " " +  xsdFile; //$NON-NLS-1$
			err += ": "; //$NON-NLS-1$
			err += e.toString();
			errorStrings.add(err);
			nErrors++;
			docBuilder = null;
			e.printStackTrace();
		}

		return true;
	}


	@Override
	public ICpItem parseXmlString(String xml) {
		clear();
		this.xmlString = xml;
		if(!init()) {
			return null;
		}

		Document domDoc = null;
		StringReader sr = new StringReader(xml);
		InputSource is = new InputSource(sr);
		try {
			domDoc = docBuilder.parse(is);
		} catch (SAXException | IOException e ) {
			String err = CpStrings.CpXmlParser_ErrorParsingFile;
			err += " " + xmlFile; //$NON-NLS-1$
			err += "': "; //$NON-NLS-1$
			err += e.toString();
			errorStrings.add(err);
			nErrors++;
			e.printStackTrace();
		} finally {
			sr.close();
		}
		if(domDoc == null)
			return null;

		Element domElement = domDoc.getDocumentElement();
		if (domElement == null) {
			return null;
		}

		if(parseElement(domElement, null)) {
			return rootItem;
		}


		return null;
	}

	@Override
	public ICpItem parseFile(String file) {
		clear();
		this.xmlFile = file;
		if(!init()) {
			return null;
		}

		Document domDoc = null;
		InputStream sr = null;
		try {
			sr = new FileInputStream(xmlFile);
			domDoc = docBuilder.parse(sr);
		} catch (SAXException | IOException e ) {
			String err = CpStrings.CpXmlParser_ErrorParsingFile;
			err += " " + xmlFile; //$NON-NLS-1$
			err += "': "; //$NON-NLS-1$
			err += e.toString();
			errorStrings.add(err);
			nErrors++;
			e.printStackTrace();
		} 
		if(sr != null) {
			try {
				sr.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		sr = null;
		if(domDoc == null)
			return null;

		Element domElement = domDoc.getDocumentElement();
		if (domElement == null) {
			return null;
		}

		if(parseElement(domElement, null)) {
			return rootItem;
		}
		return null;
	}


	/**
	 * Parses single element node, creates child ICpItem and adds it to parent
	 * @param elementNode node to parse
	 * @param parent parent ICpItem
	 * @return true if successful
	 */
	protected boolean parseElement(Node elementNode, ICpItem parent) {
		// set element tag name
		String tag = elementNode.getNodeName();
		if (isTagIgnored(tag)) {
			return true; // no further processing
		}
		ICpItem item = createItem(parent, tag);
		if(item == null) {
			return false;
		}
		// process node attributes
		NamedNodeMap attributes = elementNode.getAttributes();
		if (attributes != null && attributes.getLength() > 0) {
			for (int i = 0; i < attributes.getLength(); i++) {
				Node node = attributes.item(i);
				if (node != null) {
					String key = node.getNodeName();
					if (key == null) {
						continue;
					}
					key = key.trim();
					if (key.isEmpty()) {
						continue;
					}
					String value = node.getNodeValue();
					if (value == null) {
						continue;
					}
					value = adjustAttributeValue(key, value.trim());
					item.attributes().setAttribute(key, value);
				}
			}
		}

		// add child item here since parent implementation can query item attributes
		if(parent != null) {
			parent.addChild(item);
		}


		// insert children and text
		for (Node node = elementNode.getFirstChild(); node != null; node = node.getNextSibling()) {
			switch (node.getNodeType()) {
			case Node.ELEMENT_NODE: {
				if (!parseElement(node, item)) {
					return false;
				}
				break;
			}
			case Node.TEXT_NODE: {
				String text = node.getNodeValue();
				if (text != null) {
					item.setText(text.trim());
				}
				break;
			}
			default:
				break;
			}
		}
		return true;
	}

	private boolean isTagIgnored(String tag) {
		if (ignoreTags == null || ignoreTags.isEmpty()) {
			return false;
		}
		return ignoreTags.contains(tag);
	}

	private boolean writeTag(String tag) {
		if (ignoreWriteTags == null || ignoreWriteTags.isEmpty()) {
			return true;
		} 
		return !ignoreWriteTags.contains(tag);
	}


	@Override
	public String adjustAttributeValue(String key, String value) {
		if(key.equals(CmsisConstants.DFPU)) {
			switch(value){
			case "1":				 //$NON-NLS-1$
			case "FPU": //$NON-NLS-1$
				return CmsisConstants.SP_FPU;
			case "0": //$NON-NLS-1$
				return CmsisConstants.NO_FPU;
			default:
				return value;
			}
		} else if(key.equals(CmsisConstants.DMPU)){
			switch(value){
			case "1":				 //$NON-NLS-1$
				return CmsisConstants.MPU;
			case "0": //$NON-NLS-1$
				return CmsisConstants.NO_MPU;
			default:
				return value;
			}
		}
		// convert boolean values to 1 for consistency
		if(value.equals("true")) { //$NON-NLS-1$
			return "1"; //$NON-NLS-1$
		} else if(value.equals("false")) //$NON-NLS-1$
		 {
			return "0"; //$NON-NLS-1$
		}
		if(value.startsWith("\\\\")) { //$NON-NLS-1$
			return value;
		}
		if(value.indexOf(':') == 1) {
			return value;
		}
		return value.replace('\\', '/'); // convert all backslashes to slashes for consistency
	}

	@Override
	public boolean writeToXmlFile(ICpItem root, String file) {

		String xml = writeToXmlString(root);
		if(xml == null) {
			return false;
		}
		this.xmlFile = file;

		try {
			File outputFile = new File(file);
			FileWriter fw = new FileWriter(outputFile);
			fw.write(xml);
			fw.close();
			if(root instanceof ICpRootItem)
				((ICpRootItem)root).setFileName(file);
		} catch (IOException e) {
			errorStrings.add("Error writting to '" + file + "' file:"); //$NON-NLS-1$ //$NON-NLS-2$
			errorStrings.add(e.toString());
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
	public String writeToXmlString(ICpItem root) {
		String xml = null;
		clear();
		this.xmlFile = null;
		if(!init()) {
			return xml;
		}

		Document domDoc = createDomDocument(root);

		if(domDoc == null) {
			return xml;
		}

		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
	        DOMSource source = new DOMSource(domDoc);
	        StringWriter buffer = new StringWriter();
	        StreamResult dest = new StreamResult(buffer);
	        transformer.transform(source, dest);
	        buffer.close();
	        xml = buffer.toString();

		} catch ( TransformerFactoryConfigurationError | TransformerException | IOException e) {
			String error = CpStrings.CpXmlParser_ErrorCreatingXML;
			error += ": "; //$NON-NLS-1$
			error += e.toString();
			errorStrings.add(error);
			e.printStackTrace();
			return null;
		}
		return xml;
	}

	/**
	 * Creates DOM document
	 * @param root root IcpItem to create document for
	 * @return creates DOM
	 */
	protected Document createDomDocument(ICpItem root){
		Document domDoc = docBuilder.newDocument();
        createElement(domDoc, null, root);
		return domDoc;
	}


	/**
	 * Recursively creates DOM element node for supplied ICpItem
	 * @param doc DOM document owner of the element
	 * @param parentNode parent DOM node for the element
	 * @param item ICpItem to process
	 * @return created element node
	 */
	protected Node createElement(Document doc, Node parentNode, ICpItem item){
		if(item == null) {
			return null;
		}
		String tag = item.getTag();
		if(tag.isEmpty() || !writeTag(tag)) {
			return null;
		}
		Element node = null;
		node = doc.createElement(item.getTag());

		if(parentNode != null) {
//			node = doc.createElement(item.getTag());
			parentNode.appendChild(node);
		} else {
			doc.appendChild(node);
			node.setAttribute("xmlns:xs", "http://www.w3.org/2001/XMLSchema-instance"); //$NON-NLS-1$ //$NON-NLS-2$
			if (xsdFile != null && !xsdFile.isEmpty()) {
				File f = new File(xsdFile);
				String xsdName = f.getName();
				node.setAttribute("xs:noNamespaceSchemaLocation", xsdName); //$NON-NLS-1$
			}
		}

		if(item.attributes().hasAttributes()) {
			Map<String, String> attributesMap = item.attributes().getAttributesAsMap();
			for(Entry<String, String> e: attributesMap.entrySet()) {
				node.setAttribute(e.getKey(), e.getValue());
			}
		}

		String text = item.getText();
		if(!text.isEmpty()) {
			node.appendChild(doc.createTextNode(text));
		} else if(item.hasChildren()){
			Collection<? extends ICpItem> children = item.getChildren();
			for(ICpItem child : children) {
				createElement(doc, node, child);
			}
		}
		return node;
	}
}
