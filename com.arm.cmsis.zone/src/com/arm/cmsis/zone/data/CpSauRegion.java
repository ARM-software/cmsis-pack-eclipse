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

package com.arm.cmsis.zone.data;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpMemory;
import com.arm.cmsis.pack.enums.EMemorySecurity;
import com.arm.cmsis.pack.generic.IAttributes;


/**
 *  SAU region descriptions
 */
public class CpSauRegion extends CpZoneItem implements ICpSauRegion {


	public CpSauRegion(ICpItem parent, String tag) {
		super(parent, tag);
	}


	public CpSauRegion(ICpMemory memory) {
		super(null, CmsisConstants.SAU);
		setMemory(memory);
	}

	@Override
	public boolean appendSauRegion(ICpSauRegion region) {
		if(region == null)
			return false;
		if(!hasAttribute(CmsisConstants.NSC)) {
			setMemory(region);
			return true;
		}
		if(region.isCallableAccess() != isCallableAccess())
			return false; // not the same access
		long start = region.getStart();
		if(start < 0)
			return false;

		long thisStop = getStop();
		if(thisStop + 1 != start)
			return false;

		Long size = getSize() + region.getSize();
		attributes().setAttributeHex(CmsisConstants.SIZE, size);

		String info = getInfo();
		if(!info.isEmpty()) {
			info +=", "; //$NON-NLS-1$
		}
		info += region.getInfo();
		attributes().setAttribute(CmsisConstants.INFO, info);
		return true;
	}


	private void setMemory(ICpMemory memory) {
		if(memory == null)
			return;
		attributes().setAttributeHex(CmsisConstants.START, memory.getStart());
		attributes().setAttributeHex(CmsisConstants.SIZE, memory.getSize());
		attributes().setAttribute(CmsisConstants.NSC, memory.isCallableAccess());
		attributes().setAttribute(CmsisConstants.INFO, memory.getId());
	}

	@Override
	public boolean isCallableAccess() {
		return getAttributeAsBoolean(CmsisConstants.NSC, false);
	}

	@Override
	public EMemorySecurity getSecurity() {
		if(!hasAttribute(CmsisConstants.NSC))
			return  EMemorySecurity.NOT_SPECIFIED;
		return isCallableAccess() ? EMemorySecurity.CALLABLE : EMemorySecurity.COMBINED;
	}


	@Override
	protected IAttributes getAttributesForFtlModel() {
		IAttributes ftlAttributes = super.getAttributesForFtlModel();
		// in fzone file we use "start" and "end", not "start" and "size"
		Long stop = getStop();
		ftlAttributes.setAttributeHex(CmsisConstants.END, stop);
		ftlAttributes.removeAttribute(CmsisConstants.SIZE);
		return ftlAttributes;
	}


}

