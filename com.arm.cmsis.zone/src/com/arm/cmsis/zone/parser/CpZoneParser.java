/*******************************************************************************
* Copyright (c) 2017 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.zone.parser;

import java.io.File;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpRootItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.enums.ESeverity;
import com.arm.cmsis.pack.parser.CpXmlParser;
import com.arm.cmsis.zone.data.CpResourceZone;
import com.arm.cmsis.zone.data.CpRootZone;
import com.arm.cmsis.zone.data.ICpRootZone;
import com.arm.cmsis.zone.error.CmsisZoneError;

/**
 *  Parser for system zone files
 */
public class CpZoneParser extends CpXmlParser {

	public static final String[] AZONE_IGNORE_TAGS = new String[] {
			CmsisConstants.DEVICE_TAG,
			CmsisConstants.RESOURCES
		};


	protected ICpRootZone aZone = null;

	public CpZoneParser() {
	}

	public CpZoneParser(String xsdFile) {
		super(xsdFile);
	}

	@Override
	protected void processItem(ICpItem item) {
		if(item != null) {
			item.initItem();
		}
	}

	@Override
	public ICpItem createRootItem(String tag) {
		ICpItem root = new CpRootItem(CmsisConstants.EMPTY_STRING, getXmlFile()); // pseudo-root
		ICpRootZone rootZone = null;
		if(CmsisConstants.RZONE.equals(tag)) {
			rootZone = new CpResourceZone(root, tag);
		} else if(tag.equals(CmsisConstants.AZONE)) {
			aZone = rootZone = new CpRootZone(root, tag);
		} else {
			rootZone = new CpRootZone(root, tag);
		}
		root.addChild(rootZone);
		return rootZone;
	}


	@Override
	public ICpItem parseFile(String file) {
		ICpItem root = super.parseFile(file);
		if(aZone != null && aZone == root){
			String rZoneFile = aZone.getResourceFileName();
			parseResourceZoneFile(rZoneFile); // will append resources to azone
		}
		return root;
	}

	protected static boolean fileExists(String file) {
		if(file == null || file.isEmpty()) {
			return false;
		}
		File f = new File(file);
		return f.exists() && !f.isDirectory();
	}

	protected void parseResourceZoneFile(String file){
		if(!fileExists(file)) {
			CmsisZoneError err = new CmsisZoneError(ESeverity.Error, CmsisZoneError.Z404);
			err.setFile(getXmlFile());
			err.setDetail(file);
			addError(err);
			if(aZone != null) {
				aZone.addError(err);
			}
			return;
		}

		CpZoneParser rParser = new CpZoneParser();
		rParser.setExplicitRoot(aZone); // to append resources to azone
		rParser.parseFile(file);
		if(rParser.getSevereErrorCount() > 0 ) {
			CmsisZoneError err = new CmsisZoneError(ESeverity.Error, CmsisZoneError.Z405);
			err.setFile(getXmlFile()); // we do set an azone file, the rest will refer to rzone one
			addError(err);
			addErrors(rParser);
			if(aZone != null) {
				aZone.addErrors(this);
			}
		}
	}

	@Override
	public String writeToXmlString(ICpItem root) {
		if(root == null)
			return null;
		String tag = root.getTag();
		if(CmsisConstants.AZONE.equals(tag)){
			setIgnoreTagsFromArray(AZONE_IGNORE_TAGS);
		} else {
			setIgnoreTags(null);
		}
		root.setAttribute(CpXmlParser.SCHEMALOCATION, tag + CmsisConstants.DOT_XSD);

		String result = super.writeToXmlString(root);
		setIgnoreTags(null);
		return result;
	}

	/**
	 * Writes full xml string, ignores no tags
	 * @param root ICpItem to write
	 * @return xml String
	 */
	public String writeToFullXmlString(ICpItem root) {
		if(root == null)
			return null;
		String tag = root.getTag();
		root.setAttribute(CpXmlParser.SCHEMALOCATION, tag + CmsisConstants.DOT_XSD);

		String result = super.writeToXmlString(root);
		setIgnoreTags(null);
		return result;
	}

}
