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
import com.arm.cmsis.pack.data.CpRootItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPackFilter;
import com.arm.cmsis.pack.utils.Utils;

/**
 * The class inplementing ICpConfigurationInfo interface 
 */
public class CpConfigurationInfo extends CpRootItem implements ICpConfigurationInfo {

	public CpConfigurationInfo() {
		super(NULL_CPITEM, CmsisConstants.CONFIGURATION_TAG);
	}

	public CpConfigurationInfo(ICpDeviceInfo deviceInfo, ICpItem toolchainInfo, boolean createDefaultTags) {
		this();
		if (createDefaultTags) {
			ICpPackFilterInfo filterInfo = new CpPackFilterInfo(this);
			addChild(filterInfo);
		}
		// add device
		deviceInfo.setParent(this);
		addChild(deviceInfo);
		// add toolchain
		toolchainInfo.setParent(this);
		addChild(toolchainInfo);
		if (createDefaultTags) {
			getComponentsItem();
		}
	}

	public CpConfigurationInfo(String tag, String fileName) {
		super(tag, fileName);
	}

	
	@Override
	public ICpPack getPack() {
		// return DFP taken from device info
		ICpDeviceInfo di = getDeviceInfo();
		if(di == null )
			return null;
		return di.getPack();
	}
	

	@Override
	public String getDfpPath() {
		ICpPack pack = getPack();
		if(pack != null)
			return pack.getDir(true);
		return null;
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
		if(child instanceof ICpPackFilterInfo) {
			return (ICpPackFilterInfo)child;
		}
		return null;
	}

	@Override
	public ICpPackFilter createPackFilter() {
		ICpPackFilterInfo filterInfo = getPackFilterInfo();
		if(filterInfo != null) {
			return filterInfo.createPackFilter();
		}

		return new CpPackFilter();
	}

	@Override
	public ICpItem getComponentsItem() {
		ICpItem componentInfos  = getFirstChild(CmsisConstants.COMPONENTS_TAG);
		if(componentInfos == null) {
			componentInfos = new CpItem(this, CmsisConstants.COMPONENTS_TAG);
			addChild(componentInfos);
		}
		return componentInfos;
	}

	@Override
	public ICpItem getApisItem() {
		ICpItem apiInfos  = getFirstChild(CmsisConstants.APIS_TAG);
		if(apiInfos == null) {
			apiInfos = new CpItem(this, CmsisConstants.APIS_TAG);
			addChild(apiInfos);
		}
		return apiInfos;
	}

	@Override
	public String getName() {
		if(fileName != null && !fileName.isEmpty())
			return Utils.extractBaseFileName(fileName);
		return super.getName();
	}

	
}
