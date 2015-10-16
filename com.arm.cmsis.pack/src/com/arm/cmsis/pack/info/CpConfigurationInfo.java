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

package com.arm.cmsis.pack.info;


import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpItem;
import com.arm.cmsis.pack.data.CpPackFilter;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPackFilter;

/**
 *
 */
public class CpConfigurationInfo extends CpItem implements ICpConfigurationInfo {

	public CpConfigurationInfo() {
		super(null, CmsisConstants.CONFIGURATION_TAG);
	}

	public CpConfigurationInfo(ICpDeviceInfo deviceInfo, ICpItem toolchainInfo) {
		super(null, CmsisConstants.CONFIGURATION_TAG);
		ICpPackFilterInfo filterInfo = new CpPackFilterInfo(this);
		addChild(filterInfo);
		// add device
		deviceInfo.setParent(this);
		addChild(deviceInfo);
		// add toolchain
		toolchainInfo.setParent(this);
		addChild(toolchainInfo);
		ICpItem apiInfos = new CpItem(this, CmsisConstants.APIS_TAG);
		addChild(apiInfos);
		ICpItem componentInfos = new CpItem(this, CmsisConstants.COMPONENTS_TAG);
		addChild(componentInfos);
	}
	
	
	@Override
	protected ICpItem createChildItem(String tag) {
		return createChildItem(this, tag);
	}
		
	public static ICpItem createChildItem(ICpItem parent, String tag) {
		switch(tag) {
		case CmsisConstants.API_TAG:
		case CmsisConstants.COMPONENT_TAG:
			return new CpComponentInfo(parent, tag);
		case CmsisConstants.DEVICE_TAG:
			return new CpDeviceInfo(parent, tag);
		case CmsisConstants.PACKAGE_TAG:
			return new CpPackInfo(parent, tag);
		case CmsisConstants.PACKAGES_TAG:
			return new CpPackFilterInfo(parent, tag);
		case CmsisConstants.FILE_TAG:
			return new CpFileInfo(parent, tag);
		default:
			break;
		}
		return new CpItem(parent, tag);
	}

	
	@Override
	public ICpDeviceInfo getDeviceInfo() {
		return (ICpDeviceInfo)getFirstChild(CmsisConstants.DEVICE_TAG);
	}

	@Override
	public ICpItem getToolChainInfo() {
		return getFirstChild(CmsisConstants.TOOLCHAIN_TAG);
	}

	@Override
	public ICpPackFilterInfo getPackFilterInfo() {
		ICpItem child = getFirstChild(CmsisConstants.PACKAGES_TAG);
		if(child instanceof ICpPackFilterInfo)
			return (ICpPackFilterInfo)child;
		return null;
	}

	@Override
	public ICpPackFilter createPackFilter() {
		ICpPackFilterInfo filterInfo = getPackFilterInfo();
		if(filterInfo != null)
			return filterInfo.createPackFilter();
		
		return new CpPackFilter();
	}

	@Override
	public ICpItem getComponentsItem() {
		return getFirstChild(CmsisConstants.COMPONENTS_TAG);
	}

	@Override
	public ICpItem getApisItem() {
		return getFirstChild(CmsisConstants.APIS_TAG);
	}

	
}
