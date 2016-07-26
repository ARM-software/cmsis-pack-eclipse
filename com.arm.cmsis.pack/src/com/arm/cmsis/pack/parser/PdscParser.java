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


import com.arm.cmsis.pack.data.CpPack;
import com.arm.cmsis.pack.data.ICpItem;

/**
 * Class to parse pdsc files 
 */
public class PdscParser extends CpXmlParser  {
	

	public PdscParser() {
	}

	public PdscParser(String xsdFile) {
		super(xsdFile);
	}
	
	@Override
	public ICpItem createRootItem(String tag) {
		return new CpPack(tag, xmlFile);
	}
}
