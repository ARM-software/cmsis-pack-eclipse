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

package com.arm.cmsis.pack.data;

import java.util.Collection;

import com.arm.cmsis.pack.common.CmsisConstants;

/**
 *
 */
public class CpDeviceProperty extends CpDeviceItemContainer implements ICpDeviceProperty {
	protected ICpItem effectiveContent = null; 

	/**
	 * @param parent
	 * @param tag
	 */
	public CpDeviceProperty(ICpItem parent, String tag) {
		super(parent, tag);
	}

	@Override
	public long getDP() {
		return attributes().getAttributeAsLong(CmsisConstants.__DP, 0);
	}

	@Override
	public long getAP() {
		return attributes().getAttributeAsLong(CmsisConstants.__AP, 0);
	}
	
	@Override
	public boolean isUnique() {
		// only a few properties are not unique
		switch(fTag) {
		case CmsisConstants.BLOCK_TAG:
		case CmsisConstants.CONTROL_TAG:
		case CmsisConstants.FEATURE_TAG:
		case CmsisConstants.DESCRIPTION:
			return false;
		default: 
			break;
		}
		return true;
	}


	@Override
	public ICpItem getEffectiveContent() {
		return effectiveContent;
	}


	@Override
	public synchronized void mergeEffectiveContent(ICpItem property, String processorName) {
		attributes().mergeAttributes(property.attributes()); // always merge attributes
		if(!providesEffectiveContent())  // merge content only if property needs it 
			return;
		if(effectiveContent == null) {
			effectiveContent = new CpItem(this);
		}
		Collection<? extends ICpItem> children = getChildren();
		if(children == null || children.isEmpty())
			return;
		for(ICpItem item : children) {
			if(item instanceof ICpDeviceProperty) {
				String pname = item.getProcessorName();
				if(processorName == null || processorName.isEmpty() || pname.isEmpty() || pname.equals(processorName))
					effectiveContent.mergeProperty(item, processorName);
			}
		}						
	}


	@Override
	public boolean providesEffectiveContent() {
		// only a few properties collect the content 
		if(fTag.equals(CmsisConstants.ENVIRONMENT_TAG) || 
				fTag.equals(CmsisConstants.TRACE_TAG) || 
				fTag.equals(CmsisConstants.DEBUG_TAG) || 
				fTag.equals(CmsisConstants.SEQUENCES_TAG))
			return true;
		return false;
	}


	@Override
	public ICpItem getEffectiveParent() {
		ICpItem parent = getParent();
		if(parent != null && parent instanceof ICpDeviceProperty)
			return parent;
		return null;
	}

	
	@Override
	public boolean isAtomic() {
		// TODO Auto-generated method stub
		return attributes().getAttributeAsBoolean(CmsisConstants.ATOMIC, false);
	}

	@Override
	public String getDescription() {
		return getAttribute(CmsisConstants.INFO);
	}

	
	
	@Override
	public long getAddress() {
		return attributes().getAttributeAsLong(CmsisConstants.ADDRESS, 0);
	}

	@Override
	public long getStart() {
		return attributes().getAttributeAsLong(CmsisConstants.START, 0);
	}

	@Override
	public long getSize() {
		return attributes().getAttributeAsLong(CmsisConstants.SIZE, 0);
	}

	@Override
	public boolean isDefault() {
		return attributes().getAttributeAsBoolean(CmsisConstants.DEFAULT, false);
	}
}
