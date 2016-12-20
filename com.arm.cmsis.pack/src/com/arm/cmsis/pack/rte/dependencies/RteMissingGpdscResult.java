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

package com.arm.cmsis.pack.rte.dependencies;

import java.io.File;

import com.arm.cmsis.pack.CpStrings;
import com.arm.cmsis.pack.rte.components.IRteComponentItem;

/**
 *  The class represent a result of missing gpdsc file required by a configuration  
 */
public class RteMissingGpdscResult extends RteDependencyResult {
	protected String fGpdscFile  = null; 
	protected boolean fExists = false; 
			
	public RteMissingGpdscResult(IRteComponentItem componentItem, String filename) {
		super(componentItem);
		fGpdscFile = filename;
		File f = new File (filename);
		fExists = f.exists();
	}

	@Override
	public String getDescription() {
		String descr = CpStrings.Required_Gpdsc_File + ' ' + fGpdscFile + ' ';
		if(!fExists)
			descr += CpStrings.IsMissing;
		else 
			descr += CpStrings.Failed_To_Load;
		return descr;
	}

	@Override
	public boolean isMaster() {
		return true;
	}
	
	
}
