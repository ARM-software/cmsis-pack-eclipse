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
import java.util.LinkedList;

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
		    id += CmsisConstants.DOUBLE_COLON;
		    id += name;
		  }
		  // TODO: commented out for easy filtering of examples
		  //String rev = getAttribute(CmsisConstants.REVISION);
		  //if(rev != null && !rev.isEmpty()) {
		  //  id += CmsisConstants.DOBLE_COLON;
		  //  id += rev;
		  //}
		  return id;
	}

	@Override
	public boolean hasCompatibleDevice(IAttributes deviceAttributes) {
		Collection<? extends ICpItem> children = getChildren();
		if(children == null) {
			return false;
		}
		for(ICpItem item : children) {
			String tag = item.getTag();
			switch(tag) {
			case CmsisConstants.MOUNTED_DEVICE_TAG:
			case CmsisConstants.COMPATIBLE_DEVICE_TAG:
				String dName = item.getAttribute(CmsisConstants.DNAME);
				if(!dName.isEmpty()) {
					if(deviceAttributes.containsValue(dName)) { // covers Dvariant
						return true;
					}
				} 
				if(!dName.isEmpty() || item.hasAttribute(CmsisConstants.DFAMILY) || item.hasAttribute(CmsisConstants.DSUBFAMILY)) {
					if(item.attributes().matchAttributes(deviceAttributes, CmsisConstants.D_ATTRIBUTE_PREFIX))
						return true;
				}
					
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

	// TODO: check later
	@Override
	public Collection<ICpItem> getMountedDevices() {
		return getDevices(CmsisConstants.MOUNTED_DEVICE_TAG);
	}

	// TODO: check later
	@Override
	public Collection<ICpItem> getCompatibleDevices() {
		return getDevices(CmsisConstants.COMPATIBLE_DEVICE_TAG);
	}

	// TODO: check later
	private Collection<ICpItem> getDevices(final String requiredTag) {

		Collection<ICpItem> devices = new LinkedList<>();
		Collection<? extends ICpItem> children = getChildren();
		if (children == null) {
			return devices;
		}

		for (ICpItem item : children) {
			String tag = item.getTag();
			if (tag.equals(requiredTag)) {
				devices.add(item);
			}
		}

		return devices;
	}
	
}
