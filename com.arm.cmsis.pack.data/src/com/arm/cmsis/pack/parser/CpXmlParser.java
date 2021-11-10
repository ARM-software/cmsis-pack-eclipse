/*******************************************************************************
 * Copyright (c) 2021 ARM Ltd. and others
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
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
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

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpRootItem;
import com.arm.cmsis.pack.enums.ESeverity;
import com.arm.cmsis.pack.error.CmsisError;
import com.arm.cmsis.pack.error.CmsisErrorCollection;

/**
 * Base class to parse CMSIS-Pack-related files
 */
public class CpXmlParser extends CmsisErrorCollection implements ICpXmlParser, ErrorHandler {

    public static final String SCHEMAVERSION = "schemaVersion"; //$NON-NLS-1$
    public static final String SCHEMAINSTANCE = "http://www.w3.org/2001/XMLSchema-instance"; //$NON-NLS-1$
    public static final String SCHEMALOCATION = "xs:noNamespaceSchemaLocation"; //$NON-NLS-1$
    public static final String DATETIMEFORMAT = "yyyy-MM-dd'T'HH:mm:ss"; //$NON-NLS-1$

    protected ICpItem rootItem = null; // represents top-level item being constructed
    protected String xmlFile = null; // current XML file
    protected String xmlString = null; // current XML string

    protected String xsdFile = null; // schema file with absolute path
    protected Set<String> ignoreTags = null; // tags to ignore during read and write

    // DOM
    private DocumentBuilderFactory docBuilderFactory = null;
    private DocumentBuilder docBuilder = null;
    protected boolean bExplicitRoot = false;

    public CpXmlParser() {
    }

    public CpXmlParser(String xsdFile) {
        this.xsdFile = xsdFile;
    }

    @Override
    public void clear() {
        if (!bExplicitRoot)
            rootItem = null;
        xmlFile = null;
        xmlString = null;
        clearErrors();
        if (docBuilder != null) {
            docBuilder.reset();
        }
    }

    protected void setExplicitRoot(ICpItem root) {
        rootItem = root;
        bExplicitRoot = root != null;
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
    public void setIgnoreTags(Set<String> ignoreTags) {
        this.ignoreTags = ignoreTags;
    }

    @Override
    public void setWriteIgnoreTags(Set<String> ignoreTags) {
        setIgnoreTags(ignoreTags);
    }

    /**
     * Check if an item with given tag should be ignored by reading or writing
     *
     * @param tag tag to check
     * @return true if tag is ignored
     */
    protected boolean isTagIgnored(String tag) {
        if (tag == null || tag.isEmpty()) {
            return true;
        }
        if (ignoreTags == null || ignoreTags.isEmpty()) {
            return false;
        }
        return ignoreTags.contains(tag);
    }

    /**
     * Check if item should be ignored by writing
     *
     * @param item {@link ICpItem} to check
     * @return true if ignored
     */
    protected boolean isItemIgnored(ICpItem item) {
        if (item == null) {
            return true;
        }
        return isTagIgnored(item.getTag());
    }

    @Override
    public ICpItem createItem(ICpItem parent, String tag) {
        if (parent != null) {
            return parent.createItem(parent, tag);
        }
        if (rootItem == null) {
            rootItem = createRootItem(tag);
        }
        return rootItem;
    }

    // from ErrorHandler
    @Override
    public void error(SAXParseException arg0) throws SAXException {

        addError(arg0, CpXmlParserError.X401, ESeverity.Error);
    }

    @Override
    public void fatalError(SAXParseException arg0) throws SAXException {
        addError(arg0, CpXmlParserError.X801, ESeverity.Error);
        throw arg0;
    }

    @Override
    public void warning(SAXParseException arg0) throws SAXException {
        addError(arg0, CpXmlParserError.X201, ESeverity.Warning);
    }

    protected void addError(SAXParseException e, String id, ESeverity severity) {
        CmsisError err = new CpXmlParserError(xmlFile, id, severity, null, e);
        addError(err);
    }

    @Override
    public boolean init() {

        try {
            if (docBuilderFactory == null) {
                docBuilderFactory = DocumentBuilderFactory.newInstance();
                docBuilderFactory.setValidating(false);
                docBuilderFactory.setNamespaceAware(true);
                docBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                if (xsdFile != null && !xsdFile.isEmpty()) {
                    File f = new File(xsdFile);
                    if (f.isAbsolute() && f.exists()) {
                        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                        Schema schema = schemaFactory.newSchema(new Source[] { new StreamSource(xsdFile) });
                        docBuilderFactory.setSchema(schema);
                    }
                }
            }
            if (docBuilder == null) {
                docBuilder = docBuilderFactory.newDocumentBuilder();
            }
            docBuilder.setErrorHandler(this);
        } catch (ParserConfigurationException e) {
            CmsisError err = new CpXmlParserError(e);
            addError(err);
            docBuilder = null;
            e.printStackTrace();
            return false;
        } catch (SAXException e) {
            CmsisError err = new CpXmlParserError(xsdFile, CpXmlParserError.X803, ESeverity.FatalError,
                    "Error initializing XML schema", e); //$NON-NLS-1$
            addError(err);
            docBuilder = null;
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public ICpItem parseXmlString(String xml) {
        clear();
        this.xmlString = xml;
        if (!init()) {
            return null;
        }

        Document domDoc = null;
        try (StringReader sr = new StringReader(xml);) {
            InputSource is = new InputSource(sr);
            domDoc = docBuilder.parse(is);
        } catch (SAXException | IOException e) {
            CmsisError err = new CpXmlParserError(xmlFile, CpXmlParserError.X402, ESeverity.Error, "Error parsing file", //$NON-NLS-1$
                    e);
            addError(err);
        }

        if (domDoc == null) {
            return null;
        }

        Element domElement = domDoc.getDocumentElement();
        if (domElement == null) {
            return null;
        }

        if (parseElement(domElement, null)) {
            return rootItem;
        }

        return null;
    }

    @Override
    public ICpItem parseFile(String file) {
        clear();
        this.xmlFile = file;
        if (!init()) {
            return null;
        }

        Document domDoc = null;

        try (InputStream sr = new FileInputStream(xmlFile);) {
            domDoc = docBuilder.parse(sr);
        } catch (SAXException | IOException e) {
            CmsisError err = new CpXmlParserError(xmlFile, CpXmlParserError.X402, ESeverity.Error, "Error parsing file", //$NON-NLS-1$
                    e);
            addError(err);
        }
        if (domDoc == null) {
            return null;
        }

        Element domElement = domDoc.getDocumentElement();
        if (domElement == null) {
            return null;
        }

        if (parseElement(domElement, null)) {
            return rootItem;
        }
        return null;
    }

    /**
     * Parses single element node, creates child ICpItem and adds it to parent
     *
     * @param elementNode node to parse
     * @param parent      parent ICpItem
     * @return true if successful
     */
    protected boolean parseElement(Node elementNode, ICpItem parent) {
        // set element tag name
        String tag = elementNode.getNodeName();
        if (isTagIgnored(tag)) {
            return true; // no further processing
        }
        ICpItem item = createItem(parent, tag);
        if (item == null) {
            return false;
        }
        // process node attributes
        addAttributes(item, elementNode.getAttributes());

        // add child item here since parent implementation can query item attributes
        if (parent != null) {
            parent.addChild(item);
        }

        // insert children and text
        for (Node node = elementNode.getFirstChild(); node != null; node = node.getNextSibling()) {
            switch (node.getNodeType()) {
            case Node.ELEMENT_NODE:
                if (!parseElement(node, item)) {
                    return false;
                }
                break;
            case Node.TEXT_NODE:
                String text = node.getNodeValue();
                if (text != null) {
                    item.setText(text.trim());
                }
                break;
            default:
                break;
            }
        }

        // do some post processing of the item
        processItem(item);

        return true;
    }

    protected void addAttributes(ICpItem item, NamedNodeMap attributes) {
        if (attributes == null || attributes.getLength() < 1)
            return;
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

    /**
     * Process the item just created
     *
     * @param item item just created
     */
    protected void processItem(ICpItem item) {
        // default does nothing
    }

    @Override
    public String adjustAttributeValue(String key, String value) {
        if (key.equals(CmsisConstants.DFPU)) {
            switch (value) {
            case CmsisConstants.ONE:
            case "FPU": //$NON-NLS-1$
                return CmsisConstants.SP_FPU;
            case CmsisConstants.ZERO:
                return CmsisConstants.NO_FPU;
            default:
                return value;
            }
        }
        if (key.equals(CmsisConstants.DMPU)) {
            switch (value) {
            case CmsisConstants.ONE:
                return CmsisConstants.MPU;
            case CmsisConstants.ZERO:
                return CmsisConstants.NO_MPU;
            default:
                return value;
            }
        }
        if (key.equals(CmsisConstants.DDSP)) {
            switch (value) {
            case CmsisConstants.ONE:
                return CmsisConstants.DSP;
            case CmsisConstants.ZERO:
                return CmsisConstants.NO_DSP;
            default:
                return value;
            }
        }
        if (key.equals(CmsisConstants.DTZ)) {
            switch (value) {
            case CmsisConstants.ONE:
                return CmsisConstants.TZ;
            case CmsisConstants.ZERO:
                return CmsisConstants.NO_TZ;
            default:
                return value;
            }
        }
        if (key.equals(CmsisConstants.DSECURE)) {
            switch (value) {
            case CmsisConstants.ONE:
                return CmsisConstants.SECURE;
            case CmsisConstants.ZERO:
                return CmsisConstants.NON_SECURE;
            default:
                return value;
            }
        }

        // convert boolean values to 1 for consistency
        if (value.equals("true")) { //$NON-NLS-1$
            return "1"; //$NON-NLS-1$
        }
        if (value.equals("false")) //$NON-NLS-1$
        {
            return "0"; //$NON-NLS-1$
        }
        if (value.startsWith("\\\\") || value.indexOf(':') == 1) { //$NON-NLS-1$
            return value;
        }
        return value.replace('\\', '/'); // convert all backslashes to slashes for consistency
    }

    @Override
    public boolean writeToXmlFile(ICpItem root, String file) {

        String xml = writeToXmlString(root);
        if (xml == null) {
            return false;
        }
        this.xmlFile = file;
        File outputFile = new File(file);

        try (FileWriter fw = new FileWriter(outputFile);) {
            fw.write(xml);
            if (root instanceof ICpRootItem) {
                ((ICpRootItem) root).setFileName(file);
            }
        } catch (IOException e) {
            CmsisError err = new CpXmlParserError(file, CpXmlParserError.X403, ESeverity.Error,
                    "Error writting to file", e); //$NON-NLS-1$
            addError(err);
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
        if (!init()) {
            return xml;
        }

        Document domDoc = createDomDocument(root);

        if (domDoc == null) {
            return xml;
        }

        try (StringWriter buffer = new StringWriter()) {
            TransformerFactory tf = TransformerFactory.newInstance();
            tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            tf.setAttribute("indent-number", Integer.valueOf(2)); //$NON-NLS-1$
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
            DOMSource source = new DOMSource(domDoc);

            StreamResult dest = new StreamResult(buffer);
            transformer.transform(source, dest);
            xml = buffer.toString();

        } catch (TransformerFactoryConfigurationError | TransformerException | IOException e) {
            CmsisError err = new CpXmlParserError(null, CpXmlParserError.X404, ESeverity.Error, "Error creating XML", //$NON-NLS-1$
                    e);
            addError(err);
            e.printStackTrace();
            return null;
        }
        return xml;
    }

    /**
     * Creates DOM document
     *
     * @param root root ICpItem to create document for
     * @return creates DOM
     */
    protected Document createDomDocument(ICpItem root) {
        if (root == null) {
            return null;
        }
        root = root.getEffectiveItem(); // skips artificial root node if needed
        Document domDoc = docBuilder.newDocument();
        createElement(domDoc, null, root);
        return domDoc;
    }

    /**
     * Recursively creates DOM element node for supplied ICpItem
     *
     * @param doc        DOM document owner of the element
     * @param parentNode parent DOM node for the element
     * @param item       ICpItem to process
     * @return created element node
     */
    protected Node createElement(Document doc, Node parentNode, ICpItem item) {

        if (isItemIgnored(item)) {
            return null;
        }

        String tag = item.getTag();
        if (tag == null || tag.isEmpty()) {
            return null;
        }
        Element node = doc.createElement(tag);
        if (parentNode != null) {
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

        if (item.attributes().hasAttributes()) {
            Map<String, String> attributesMap = item.attributes().getAttributesAsMap();
            for (Entry<String, String> e : attributesMap.entrySet()) {
                node.setAttribute(e.getKey(), e.getValue());
            }
        }

        String text = item.getText();
        if (!text.isEmpty()) {
            node.appendChild(doc.createTextNode(text));
        } else if (item.hasChildren()) {
            Collection<? extends ICpItem> children = item.getChildren();
            for (ICpItem child : children) {
                createElement(doc, node, child);
            }
        }
        return node;
    }

    /**
     * Saves XML string to file
     *
     * @param xml     XML string to save
     * @param file    destination IFile
     * @param monitor IProgressMonitor
     */
    public static boolean saveXmlToFile(String xml, String absFileName) {
        // Write into file
        File file = new File(absFileName);

        try (PrintWriter writer = new PrintWriter(file)) {
            writer.write(xml);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
