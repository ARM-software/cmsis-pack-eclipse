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

import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.info.CpConfigurationInfo;

/**
 *  Simple parser to read ICpConfigurationInfo from a file 
 */
public class CpConfigParser extends CpXmlParser {

	/**
	 *  Default constructor
	 */
	public CpConfigParser() {
	}

	/**
	 * @param xsdFile schema file
	 */
	public CpConfigParser(String xsdFile) {
		super(xsdFile);
	}

	@Override
	public ICpItem createItem(ICpItem parent, String tag) {
		if(parent != null) {
			return CpConfigurationInfo.createChildItem(parent, tag);
		}
		return super.createItem(parent, tag);
	}

	@Override
	public ICpItem createRootItem(String tag) {
		return new CpConfigurationInfo(tag, xmlFile);
	}
	
}
