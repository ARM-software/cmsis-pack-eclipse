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

package com.arm.cmsis.zone.data;

import java.util.Map;

import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.generic.Attributes;
import com.arm.cmsis.pack.generic.IAttributedItem;
import com.arm.cmsis.pack.generic.IAttributes;

/**
 *  Peripheral's slot (channel or pin) item 
 */
public class CpSlot extends CpResourceItem implements ICpSlot {
	protected IAttributes fOriginalAttributes = null; 
	
	public CpSlot(ICpItem parent, String tag) {
		super(parent, tag);
	}


	@Override
	public IAttributes getOriginalAttributes() {
		if(fOriginalAttributes == null) {
			fOriginalAttributes = new Attributes(attributes());
		}
		return fOriginalAttributes;
	}


	@Override
	public boolean updateAttribute(String key, String value) {
		getOriginalAttributes(); // ensure stored original attributes
		return super.updateAttribute(key, value);
	}


	@Override
	public boolean updateAttributes(IAttributes newAttributes) {
		getOriginalAttributes(); // ensure stored original attributes
		return super.updateAttributes(newAttributes);
	}


	@Override
	public boolean updateAttributes(IAttributedItem item) {
		getOriginalAttributes(); // ensure stored original attributes
		return super.updateAttributes(item);
	}


	@Override
	public boolean updateAttributes(Map<String, String> attributes) {
		getOriginalAttributes(); // ensure stored original attributes
		return super.updateAttributes(attributes);
	}
	
	
}
