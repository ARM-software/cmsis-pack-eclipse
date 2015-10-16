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

import java.util.Collection;

import com.arm.cmsis.pack.CpStrings;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpComponent;
import com.arm.cmsis.pack.data.ICpComponent;
import com.arm.cmsis.pack.data.ICpFile;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.enums.EEvaluationResult;

/**
 * Default implementation of ICpComponentInfo interface
 */
public class CpComponentInfo extends CpComponent implements ICpComponentInfo {

	protected ICpComponent fComponent = null;
	protected EEvaluationResult fResolveResult = EEvaluationResult.UNDEFINED;
	protected ICpPackInfo  fPackInfo = null;
	protected int fInstanceCount = -1;
	/**
	 * Creates info for the given component
	 * @param parent parent item if any
	 * @param component real component
	 * @param instanceCount component selection count  
	 */
	public CpComponentInfo(ICpItem parent, ICpComponent component, int instanceCount) {
		super(parent, component);
		fComponent = component;
		fInstanceCount = instanceCount;
		setEvaluationResult(EEvaluationResult.FULFILLED);
		updateInfo();
	}

	
	/**
	 * Constructor for parser
	 * @param parent parent item if any
	 * @param tag XML tag associated with the item 
	 */
	public CpComponentInfo(ICpItem parent, String tag) {
		super(parent, tag);
	}

	@Override
	public ICpComponent getComponent() {
		return fComponent;
	}
	
	
	@Override
	public void setComponent(ICpComponent component) {
		fComponent = component;
		if(component != null) {
			setEvaluationResult(EEvaluationResult.FULFILLED);
			fPackInfo.setPack(component.getPack());
		} else {
			if(fResolveResult == EEvaluationResult.FULFILLED || fResolveResult == EEvaluationResult.UNDEFINED)
				fResolveResult = EEvaluationResult.MISSING;
		}
	}
	
	
	@Override
	public void updateInfo() {
		if(fComponent != null) {
			fPackInfo = new CpPackInfo(this, fComponent.getPack());
			replaceChild(fPackInfo);
			attributes().setAttributes(fComponent.attributes());
			attributes().removeAttribute(CmsisConstants.CONDITION); // not needed in info 
			attributes().setAttribute(CmsisConstants.CVENDOR, fComponent.getVendor());
			attributes().setAttribute(CmsisConstants.CVERSION, fComponent.getVersion());
			if(fComponent.isDeviceDependent()) {
				deviceDependent = 1;
				attributes().setAttribute(CmsisConstants.DEVICE_DEPENDENT, true);
			} else {
				attributes().removeAttribute(CmsisConstants.DEVICE_DEPENDENT);
				deviceDependent = 0;
			}
				
			if(fInstanceCount > 1 ) {
				attributes().setAttribute(CmsisConstants.INSTANCES, fInstanceCount);
			} else {
				attributes().removeAttribute(CmsisConstants.INSTANCES);
			}
		}
	}


	@Override
	public int getInstanceCount() {
		if(fInstanceCount < 0)
			fInstanceCount = attributes().getAttributeAsInt(CmsisConstants.INSTANCES, 1);
		return fInstanceCount;
	}

	
	@Override
	public ICpFileInfo getFileInfo(ICpFile f) {
		if(f == null)
			return null;
		String name = f.getName();
		Collection<? extends ICpItem> children = getChildren();
		if(children == null) {
			return null;
		}
		for(ICpItem item : children) {
			if(item instanceof ICpFileInfo && item.getName().equals(name))
				return (ICpFileInfo)item;
		}
		return null;
	}


	@Override
	public ICpPack getPack() {
		if(fComponent != null)// TODO Auto-generated method stub
			return fComponent.getPack();
		else if(fPackInfo != null)
			return fPackInfo.getPack();
		return null;
	}

	
	@Override
	public String getPackId() {
		ICpPack pack = getPack();
		if(pack != null)
			return pack.getId();
		if(fPackInfo != null)
			return fPackInfo.getPackId();
		return null;
	}


	@Override
	public String getPackFamilyId() {
		ICpPack pack = getPack();
		if(pack != null)
			return pack.getPackFamilyId();
		if(fPackInfo != null)
			return fPackInfo.getPackFamilyId();
		return null; 
	}


	@Override
	public ICpPackInfo getPackInfo() {
		return fPackInfo;
	}


	@Override
	public void addChild(ICpItem item) {
		if(item instanceof ICpPackInfo) {
			fPackInfo = (ICpPackInfo)item;
		} 
		super.addChild(item);
	}


	@Override
	protected ICpItem createChildItem(String tag) {
		return CpConfigurationInfo.createChildItem(this, tag);
	}


	@Override
	public EEvaluationResult getEvaluationResult() {
		return fResolveResult;
	}


	@Override
	public void setEvaluationResult(EEvaluationResult result) {
	//	if(fResolveResult == EEvaluationResult.UNDEFINED || result.ordinal() < fResolveResult.ordinal()) 
			fResolveResult = result;
	}


	@Override
	public String getDescription() {
		if(fComponent != null)
			return fComponent.getDescription();
		return CpStrings.CpComponentInfo_ComponentMissing; 
	}
	
}
