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

package com.arm.cmsis.pack.rte;

import java.util.Collection;

import com.arm.cmsis.pack.base.CmsisItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.rte.components.IRteComponentItem;

/**
 *
 */
public class RteDependencyItem extends CmsisItem implements IRteDependencyItem {

	protected EEvaluationResult fResult = EEvaluationResult.UNDEFINED;
	protected IRteComponentItem fComponentItem = null; 
	/**
	 * Default constructor 
	 */
	public RteDependencyItem() {
	}


	public RteDependencyItem(EEvaluationResult result) {
		setEvaluationResult(result);
	}

	
	/**
	 * Constructor
	 * @param component IRteComponent candidate component 
	 */
	public RteDependencyItem(IRteComponentItem componentItem) {
		fComponentItem = componentItem;
	}

	/**
	 * Constructor
	 * @param component IRteComponent candidate component 
	 */
	public RteDependencyItem(IRteComponentItem componentItem, EEvaluationResult result) {
		fComponentItem = componentItem;
		setEvaluationResult(result);
	}
	
	@Override
	public boolean isDeny() {
		// Default returns false
		return false;
	}


	@Override
	public EEvaluationResult getEvaluationResult() {
		return fResult ;
	}

	@Override
	public void setEvaluationResult(EEvaluationResult result) {
		fResult = result;
	}

	@Override
	public Collection<? extends IRteDependencyItem> getChildren() {
		return null;
	}

	@Override
	public IRteComponentItem getComponentItem() {
		return fComponentItem;
	}
	
	@Override
	public ICpItem getCpItem(){
		IRteComponentItem componentItem = getComponentItem();
		if(componentItem != null) {
			return componentItem.getActiveCpItem();
		}
		return null;
	}

	@Override
	public String getName() {
		ICpItem cpItem = getCpItem();
		if(cpItem != null)
			return cpItem.getName();
		return super.getName();
	}
	
	@Override
	public String getDescription() {
		ICpItem cpItem = getCpItem();
		if(cpItem != null)
			return cpItem.getDescription();
		return super.getDescription();
	}

	@Override
	public String getUrl() {
		ICpItem cpItem = getCpItem();
		if(cpItem != null)
			return cpItem.getUrl();
		return super.getUrl();
	}
	
}
