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
import com.arm.cmsis.pack.data.ICpPack;

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
	public ICpItem createItem(ICpItem parent, String tag) {
		if(parent != null) {
			return parent.createItem(parent, tag);
		}
		if(rootItem == null) {
			ICpPack pack = new CpPack(parent, tag) ;
			pack.setFileName(xmlFile);
			rootItem = pack;
		}
		return rootItem;
	}
}
