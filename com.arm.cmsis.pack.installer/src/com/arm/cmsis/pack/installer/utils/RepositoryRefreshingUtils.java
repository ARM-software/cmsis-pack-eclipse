/*******************************************************************************
 * Copyright (c) 2014 Liviu Ionescu.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Liviu Ionescu - initial implementation.
 *     ARM Ltd and ARM Germany GmbH - application-specific implementation
 *******************************************************************************/

package com.arm.cmsis.pack.installer.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.arm.cmsis.pack.common.CmsisConstants;

/**
 * Utility functions from GNU ARM
 */
public class RepositoryRefreshingUtils {

	private static Map<String, Long> timestamps = new HashMap<>();

	/**
	 * @param inputStream the input stream
	 * @param pdscList files list to collect .pdsc files
	 * @return the number of .pdsc files in the list that needs updating
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static int readIndex(InputStream inputStream, List<String[]> pdscList)
			throws ParserConfigurationException, SAXException, IOException {

		// Read from url to local buffer
		BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
		String line = null;

		// Insert missing root element
		StringBuilder buffer = new StringBuilder();
		buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"); //$NON-NLS-1$
		buffer.append("<root>\n"); //$NON-NLS-1$

		// Check time stamp and url of the index.pidx
		boolean timeChanged = true;
		String pidxUrl = CmsisConstants.EMPTY_STRING;
		long timestamp = 0;

		while ((line = in.readLine()) != null) {
			line = line.trim();
			if (line.startsWith("<pdsc ")) { //$NON-NLS-1$
				buffer.append(line + '\n');
			} else if (line.startsWith("<url")) { //$NON-NLS-1$
				int start = line.indexOf('>') + 1;
				int end = line.indexOf('<', start);
				pidxUrl = line.substring(start, end);
			} else if (line.startsWith("<timestamp")) { //$NON-NLS-1$
				int start = line.indexOf('>') + 1;
				int end = line.indexOf('<', start);
				timestamp = parseTime(pidxUrl, line.substring(start, end));
			}
		}
		if (!pidxUrl.isEmpty()) {
			if (timestamps.containsKey(pidxUrl) && timestamp == timestamps.get(pidxUrl)) {
				timeChanged = false;
			} else {
				timeChanged = true;
				timestamps.put(pidxUrl, timestamp);
			}
		}

		buffer.append("</root>\n"); //$NON-NLS-1$

		// Parse from local buffer
		InputSource inputSource = new InputSource(new StringReader(
				buffer.toString()));

		DocumentBuilder parser = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document document = parser.parse(inputSource);

		Element el = document.getDocumentElement();
		if (!"root".equals(el.getNodeName())) { //$NON-NLS-1$
			return 0;
		}

		int count = 0;
		List<Element> pdscElements = getChildrenElementsList(el, "pdsc"); //$NON-NLS-1$
		for (Element pdscElement : pdscElements) {

			String url = pdscElement.getAttribute(CmsisConstants.URL).trim();
			String vendor = pdscElement.getAttribute(CmsisConstants.VENDOR).trim();
			String name = pdscElement.getAttribute(CmsisConstants.NAME).trim();
			if (!name.endsWith(CmsisConstants.EXT_PDSC)) {
				name += CmsisConstants.EXT_PDSC;
			}
			if (!vendor.isEmpty()) {
				name = vendor + '.' + name;
			}
			String replacement = pdscElement.getAttribute(CmsisConstants.REPLACEMENT).trim();
			if (!replacement.isEmpty()) {
				name = replacement + CmsisConstants.EXT_PDSC;
			}
			String version = pdscElement.getAttribute(CmsisConstants.VERSION).trim();

			pdscList.add(new String[] { url, name, version });
			++count;
		}

		return timeChanged ? count : 0;

	}

	private static List<Element> getChildrenElementsList(Element el, String name) {

		NodeList nodeList = el.getChildNodes();

		// Allocate exactly the number of children
		List<Element> list = new ArrayList<Element>(nodeList.getLength());

		for (int i = 0; i < nodeList.getLength(); ++i) {
			Node node = nodeList.item(i);

			if (node.getNodeType() == Node.ELEMENT_NODE) {
				if ((name == null) || node.getNodeName().equals(name)) {
					list.add((Element) node);
				}
			}
		}
		return list;
	}

	/**
	 * Parse the time
	 * @param time should be in the format of xs:dateTime.
	 * e.g. <code> 2016-04-05T12:00:00 </code>
	 * @return true if the time stamp of the index file has changed
	 */
	private static long parseTime(String url, String time) {
		StringBuilder dateFormat = new StringBuilder("yyyy-MM-dd'T'HH:mm:ss"); //$NON-NLS-1$
		// if the date contains time zone info, add a 'Z' in the end of the date format
		int fromIndex = time.indexOf('T');
		int msIndex = time.indexOf('.'); // milliseconds index
		int timeZoneIndex = Math.max(time.indexOf('+', fromIndex), time.indexOf('-', fromIndex));
		if (timeZoneIndex < 0) {
			timeZoneIndex = time.length();
		}
		if (msIndex != -1) {
			dateFormat.append('.');
			for (int i = msIndex + 1; i < timeZoneIndex; i++) {
				dateFormat.append('S');
			}
		}
		if (timeZoneIndex > 0) {
			dateFormat.append("XXX"); //$NON-NLS-1$
		}
		try {
			Date date = new SimpleDateFormat(dateFormat.toString()).parse(time);
			return date.getTime();
		} catch (ParseException e) {
			// do nothing, just return -1 to make sure time stamp changes
			return -1;
		}
	}

}
