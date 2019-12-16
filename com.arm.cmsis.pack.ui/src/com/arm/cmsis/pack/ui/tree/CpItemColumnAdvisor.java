/*******************************************************************************
* Copyright (c) 2019 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.ui.tree;

import org.eclipse.jface.viewers.ColumnViewer;

import com.arm.cmsis.pack.data.ICpItem;

/**
 * 
 */
public class CpItemColumnAdvisor extends ColumnAdvisor {

	/**
	 * Simple column advisor for ICpItem 
	 * @param columnViewer
	 */
	public CpItemColumnAdvisor(ColumnViewer columnViewer) {
		super(columnViewer);
	}

	@Override
	public String getString(Object obj, int columnIndex) {
		ICpItem item = getItem(obj);
		if(item == null)
			return null;
		if(columnIndex == 0) {
			return item.getName();
		}
		return null;
	}

	public ICpItem getItem(Object obj) {
		return ICpItem.cast(obj);
	}
	
	
}
