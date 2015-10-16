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

import com.arm.cmsis.pack.DeviceVendor;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.generic.IAttributes;

/**
 *  Default implementation of ICpBoard interface
 */
public class CpBoard extends CpItem implements ICpBoard {

	public CpBoard(ICpItem parent) {
		super(parent);
	}

	public CpBoard(ICpItem parent, String tag) {
		super(parent, tag);
	}

	@Override
	public String constructId() {
		  String id = DeviceVendor.getOfficialVendorName(getVendor());

		  String name = getAttribute(CmsisConstants.NAME);
		  if(name != null && !name.isEmpty()) {
		    id += CmsisConstants.DOBLE_COLON;
		    id += name;
		  }
		  String rev = getAttribute(CmsisConstants.REVISION);
		  if(rev != null && !rev.isEmpty()) {
		    id += CmsisConstants.DOBLE_COLON;
		    id += rev;
		  }
		  return id;
	}

	@Override
	public boolean hasCompatibleDevice(IAttributes deviceAttributes) {
		Collection<? extends ICpItem> children = getChildren();
		if(children == null)
			return false;
		for(ICpItem item : children) {
			String tag = item.getTag();
			switch(tag) {
			case CmsisConstants.MOUNTED_DEVICE_TAG:
			case CmsisConstants.COMPATIBLE_DEVICE_TAG:
				if(item.attributes().matchCommonAttributes(deviceAttributes))
					return true;
			default:
				break;
			}
		}
		return false;
	}

	@Override
	public synchronized String getUrl() {
		if(fURL == null) {
			fURL = DeviceVendor.getBoardVendorUrl(getVendor());
			if(!fURL.isEmpty()) {
				fURL += '/';
				fURL += DeviceVendor.adjutsToUrl(getName());  
			}
		}
		return fURL;
	}	
	
}
