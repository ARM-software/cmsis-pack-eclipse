/*******************************************************************************
* Copyright (c) 2014 ARM Ltd.
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/

package com.arm.cmsis.pack.info;

import java.util.Collection;

import com.arm.cmsis.pack.data.CpComponent;
import com.arm.cmsis.pack.data.ICpComponent;
import com.arm.cmsis.pack.data.ICpConditionContext;
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
	/**
	 * Creates info for the given component
	 * @param parent parent item if any
	 * @param component
	 */
	public CpComponentInfo(ICpItem parent, ICpComponent component, int instanceCount) {
		super(parent, component != null? component.getTag() : "component");
		setComponent(component);
		if(component != null) {
			attributes().setAttributes(component.attributes());
			attributes().setAttribute("Cvendor", component.getVendor());
			if(instanceCount > 0 ) {
				attributes().setAttribute("instances", instanceCount);
			}
		}
	}

	
	/**
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
			fPackInfo = new CpPackInfo(this, component.getPack());
			replaceChild(fPackInfo);
			setEvaluationResult(EEvaluationResult.FULFILLED);
		} else {
			if(fResolveResult == EEvaluationResult.FULFILLED || fResolveResult == EEvaluationResult.UNDEFINED)
				fResolveResult = EEvaluationResult.MISSING;
		}
	}
	
	@Override
	public int getInstanceCount() {
		return attributes().getAttributeAsInt("instances", 1);
	}

	@Override
	public Collection<ICpFile> getFilteredFiles(ICpConditionContext context) {
		if(fComponent != null)
			return fComponent.getFilteredFiles(context);
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
		if(fResolveResult == EEvaluationResult.UNDEFINED || result.ordinal() < fResolveResult.ordinal()) 
			fResolveResult = result;
	}


	@Override
	public String getDescription() {
		if(fComponent != null)
			return fComponent.getDescription();
		return "component is missing"; 
	}
	
}
