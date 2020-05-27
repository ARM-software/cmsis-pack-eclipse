/*******************************************************************************
 * Copyright (c) 2018 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 * function parseTime() from Liviu Ionescu - initial implementation.
 *******************************************************************************/
package com.arm.cmsis.pack.parser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.utils.VersionComparator;

/**
 * Using CpXmlParser this class implements utilities to read files based on pidx schema, e.g. index.pidx, local_repository.pidx.
 *
 * In addition some routines are available to handle specific functionalities related to use cases.
 * @author ducngu01
 *
 */
public class CpPidxParser {
	public static final int NAME = 0;
	public static final int URL = 1;
	public static final int VENDOR = 2;
	public static final int VERSION = 3;
	public static final String[] PACK_IGNORE_TAGS = new String[] {
		CmsisConstants.DEVICES_TAG,
		CmsisConstants.COMPONENTS_TAG,
		CmsisConstants.BOARDS_TAG,
		CmsisConstants.CONDITIONS_TAG
	};



	/**
	 * Private constructor to prevent instantiating the utility class
	 */
	private CpPidxParser() {
		throw new IllegalStateException("CpPidxParser is a utility class"); //$NON-NLS-1$
	}

	/**
	 * Partly parsing a pdsc file to retrieve only the pack id.
	 * @param pdscFile
	 * 		name of pdsc file
	 * @return pack id specified in the pdsc file
	 */
	public static String parsePdsc(String pdscFile) {
		PdscParser pdscParser = new PdscParser();
		Set<String> tagSet = new HashSet<>();
		Collections.addAll(tagSet, PACK_IGNORE_TAGS);
		pdscParser.setIgnoreTags(tagSet);

		ICpItem root = pdscParser.parseFile(pdscFile);
		if (!(root instanceof ICpPack)) {
			return null;
		}
		ICpPack pack = (ICpPack)root;
		return pack.getPackId();
	}

	/**
	 * Read file based on pidx schema, e.g. local_repository.pidx
	 * @param pidxFile
	 *            pidx file
	 * @return a list with pack id mapped to folder where a pack is installed
	 */
	public static Map<String, String> parsePidx(String pidxFile) {
		CpXmlParser parser = new CpXmlParser();

		ICpItem rootItem = parser.parseFile(pidxFile);
		Map<String, String> map = new HashMap<>();
		if (rootItem == null) {
			return null;
		}

		Collection<ICpItem> pidx = rootItem.getChildren(CmsisConstants.PINDEX);
		if (pidx != null) {
			for (ICpItem item : pidx) {
				Collection<ICpItem> pdsc = item.getChildren(CmsisConstants.PDSC);
				for (ICpItem entry : pdsc) {
					String packId = entry.getVendor() + '.' +
									entry.getName() + '.' +
									VersionComparator.removeMetadata(entry.getVersion());
					String dir = entry.getAttribute(CmsisConstants.URL);
					if (dir.startsWith(CmsisConstants.LOCAL_FILE_URL)) {	// eliminate local file specification
						dir = dir.substring(CmsisConstants.LOCAL_FILE_URL.length());
					}
					map.put(packId, dir);
				}
			}
		}
		return map;
	}

	/**
	 * Parsers index.pidx file and puts information into the list
	 * @param pidxFile index.pidx file
	 * @param indexList	if successful list of vendor, name and version of packs
	 * @return number of pdsc files
	 */
	public static int parsePidx(String pidxFile, List<String[]> indexList) {

		CpXmlParser parser = new CpXmlParser();
		ICpItem rootItem = parser.parseFile(pidxFile);
		if (rootItem == null) {
			return 0;
		}

		String url;
		// fill indexList
		int count = 0;
		Collection<ICpItem> pidx = rootItem.getChildren(CmsisConstants.PINDEX);
		if (pidx != null) {
			for (ICpItem item : pidx) {
				Collection<ICpItem> pdsc = item.getChildren(CmsisConstants.PDSC);
				for (ICpItem entry : pdsc) {
					url = entry.getAttribute(CmsisConstants.URL);
					String vendor = entry.getVendor();
					String name = entry.getName();
					if (!name.endsWith(CmsisConstants.EXT_PDSC)) {
						name += CmsisConstants.EXT_PDSC;
					}
					name = vendor + '.' + name;
					String version = entry.getAttribute(CmsisConstants.VERSION);
					indexList.add(new String[] { url, name, version });
					count++;
				}
			}
		}

		return count;
	}


	/**
	 *
	 * @param item
	 * 	a pair of pack id as key and folder as value
	 * @return
	 * 	pdsc file name
	 */
	public static String getPackId(Entry<String, String> item) {
		return parsePdsc(getPdscFileName(item));
	}

	/**
	 *
	 * @param item
	 * 		a pair of pack id as key and folder as value
	 * @return
	 * 		pdsc file name constructed by folder (not as url, i.e. without file://localhost/) and pack id
	 */
	public static String getPdscFileName(Entry<String, String> item) {
		String fileName = item.getValue();
		String packId = item.getKey();
		int second = packId.indexOf('.', packId.indexOf('.') +1);
		fileName += packId.substring(0, second) + CmsisConstants.EXT_PDSC;
		if (fileName.startsWith(CmsisConstants.LOCAL_FILE_URL)) {
			fileName = fileName.substring(CmsisConstants.LOCAL_FILE_URL.length());
		}
		return fileName;
	}

	/**
	 * Look for local repositories specified in .local\local_repository.pidx
	 * @param localDir
	 * 		.local folder of the current rte
	 * @return
	 * 	a list of pdsc files (locating in local repositories)
	 */
	public static Collection<String> getLocalRepositoryFileNames(String localDir) {
		Collection<String> fileNames = new LinkedList<>();
		localDir += '/' + CmsisConstants.LOCAL_REPOSITORY_PIDX;
		Map<String, String> map = parsePidx(localDir);

		if (map != null) {
			for (Entry<String, String> entry :map.entrySet()) {
				String key = entry.getKey();
				String fileName = entry.getValue();
				int second = key.indexOf('.', key.indexOf('.') +1);
				fileName += key.substring(0, second) + CmsisConstants.EXT_PDSC;
				fileNames.add(fileName);
			}
		}

		return fileNames;
	}

	/**
	 * Create a new local repository file overwriting the previous one.
	 * @param xmlFile
	 * 	name of the local repository file
	 * @param map
	 * 	list of pack id mapped to repository folder
	 * @param url
	 * current rte root folder
	 */
	public static void createPidxFile(String xmlFile, Map<String, String> map, String url) {

		CpXmlParser parser = new CpXmlParser();

		// setup tag index
		ICpItem rootItem = parser.createItem(null, CmsisConstants.INDEX);
		rootItem.setAttribute(CpXmlParser.SCHEMAVERSION, "1.1.0"); //$NON-NLS-1$
		rootItem.setAttribute("xmlns:xsi", CpXmlParser.SCHEMAINSTANCE); //$NON-NLS-1$
		rootItem.setAttribute(CpXmlParser.SCHEMALOCATION, "PackIndex.xsd"); //$NON-NLS-1$

		// setup tag vendor
		ICpItem item = parser.createItem(rootItem, CmsisConstants.VENDOR);
		item.setText("local repository"); //$NON-NLS-1$
		rootItem.addChild(item);

		// setup tag url
		item = parser.createItem(rootItem, CmsisConstants.URL);
		item.setText(url);
		rootItem.addChild(item);

		// setup tag timestamp
		Date today = Calendar.getInstance().getTime();
		SimpleDateFormat timestamp = new SimpleDateFormat(CpXmlParser.DATETIMEFORMAT);
		item = parser.createItem(rootItem, CmsisConstants.TIMESTAMP);
		item.setText(timestamp.format(today));
		rootItem.addChild(item);

		// setup tag pindex
		ICpItem pindex = parser.createItem(rootItem, CmsisConstants.PINDEX);
		rootItem.addChild(pindex);

		if (map != null) {
			for (Entry<String, String> entry : map.entrySet()) {
				item = parser.createItem(pindex, CmsisConstants.PDSC);
				pindex.addChild(item);
				String packId = entry.getKey();
				int first = packId.indexOf('.');
				int second = packId.indexOf('.', first+1);
				item.setAttribute(CmsisConstants.NAME, packId.substring(first+1, second));
				item.setAttribute(CmsisConstants.URL, CmsisConstants.LOCAL_FILE_URL + entry.getValue());
				item.setAttribute(CmsisConstants.VENDOR, packId.substring(0, first));
				item.setAttribute(CmsisConstants.VERSION, packId.substring(second+1, packId.length()));
			}
		}

		parser.writeToXmlFile(rootItem, xmlFile);
	}
}
