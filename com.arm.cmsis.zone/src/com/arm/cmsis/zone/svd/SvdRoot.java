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

package com.arm.cmsis.zone.svd;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.utils.AlnumComparator;

/* 
 * Base SVD item
 */
public class SvdRoot extends SvdItem implements ISvdRoot {

	protected Map<String, ISvdPeripheral> fPeripheralMap = null; 

	public SvdRoot(ICpItem parent) {
		super(parent);
	}
	
	@Override
	public ISvdRoot getSvdRoot() {
		return this;
	}

	@Override
	public ISvdPeripheral getPeripheral(String name) {
		return getPeripheralMap().get(name);
	}

	
	@Override
	public Map<String, ISvdPeripheral> getPeripheralMap() {
		if(fPeripheralMap == null) 
			collectPeripherals(); 
		return fPeripheralMap;
	}

	protected void collectPeripherals() {
		fPeripheralMap = new TreeMap<>(new AlnumComparator(false));
		
		ICpItem peripherals = getFirstChild(CmsisConstants.PERIPHERALS);
		if(peripherals == null)
			return;

		Collection<? extends ICpItem> children = peripherals.getChildren();
		if(children == null || children.isEmpty())
			return;
		for(ICpItem child : children) {
			if(child instanceof ISvdPeripheral) {
				ISvdPeripheral p = (ISvdPeripheral)child;
				String name = p.getName();
				fPeripheralMap.put(name, p);
			}
		}
	}
	
}
