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

import java.util.Collection;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;

/**
 *  Class containing CMSIS zones
 */
public class CpZoneContainer extends CpResourceItem implements ICpZoneContainer {

	public CpZoneContainer(ICpItem parent) {
		this(parent, CmsisConstants.ZONES);
	}

	
	public CpZoneContainer(ICpItem parent, String tag) {
		super(parent, tag);
	}

	@Override
	protected ICpItem createChildItem(String tag) {
		switch (tag) {
		case CmsisConstants.ZONE:
			return new CpZone(this, tag);
		default:
			break;
		}
		return super.createChildItem(tag);
	}
	

	@Override
	public ICpZone getZone(String zoneName) {
		ICpItem item = getFirstChild(zoneName);
		if(item instanceof ICpZone) {
			return (ICpZone)item;
		}
		return null;
	}

	@Override
	public Collection<ICpZone> getZones() {
		return getChildrenOfType(ICpZone.class);
	}


	@Override
	public ICpItem toFtlModel(ICpItem ftlParent) {
		Collection<ICpZone> zones = getZones();
		if(zones == null || zones.isEmpty())
			return null;
		for(ICpZone z: zones ){
			ICpItem zoneItem = z.toFtlModel(ftlParent);
			if(ftlParent != null)
				ftlParent.addChild(zoneItem);
		}

		return null; //  does not include itself
	}
}
