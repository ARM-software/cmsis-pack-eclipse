/*******************************************************************************
* Copyright (c) 2016 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.data;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 *   Default implementation of ICpRootItem interface
 */
public class CpRootItem extends CpItem implements ICpRootItem {

	private String fileName = null;

	public CpRootItem() {
		super(NULL_CPITEM);
	}
	
	public CpRootItem(ICpItem parent) {
		super(parent);
	}

	public CpRootItem(ICpItem parent, String tag) {
		super(parent, tag);
	}

	public CpRootItem(String tag, String fileName) {
		super(NULL_CPITEM, tag);
		setFileName(fileName);
	}
	
	@Override
	public void setFileName(String fileName) {
		if (fileName != null) {
			IPath p = new Path(fileName);
			this.fileName = p.toString();
		}
	}

	@Override
	public String getFileName() {
		return fileName;
	}

}
