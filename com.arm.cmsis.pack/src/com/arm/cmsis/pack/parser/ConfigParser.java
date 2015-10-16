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
import com.arm.cmsis.pack.info.ICpConfigurationInfo;

/**
 *  Simple parser to read ICpConfigurationInfo from a file 
 */
public class ConfigParser extends CpXmlParser {

	/**
	 *  Default constructor
	 */
	public ConfigParser() {
	}

	/**
	 * @param xsdFile schema file
	 */
	public ConfigParser(String xsdFile) {
		super(xsdFile);
	}

	@Override
	public ICpItem createItem(ICpItem parent, String tag) {
		if(parent != null) {
			return CpConfigurationInfo.createChildItem(parent, tag);
		}
		if(rootItem == null) {
			ICpConfigurationInfo ci = new CpConfigurationInfo() ;
			rootItem = ci;
		}
		return rootItem;
	}

}
