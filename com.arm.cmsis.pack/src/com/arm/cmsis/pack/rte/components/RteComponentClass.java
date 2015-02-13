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

package com.arm.cmsis.pack.rte.components;


import java.util.Collection;

import com.arm.cmsis.pack.base.CmsisConstants;
import com.arm.cmsis.pack.data.ICpComponent;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpTaxonomy;
import com.arm.cmsis.pack.enums.EComponentAttribute;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.info.ICpComponentInfo;

/**
 * Class represents Cclass component hierarchy level, contains collection of bundles  
 */
public class RteComponentClass extends RteComponentItem implements IRteComponentClass {

	/**
	 * @param parent
	 */
	public RteComponentClass(IRteComponentItem parent, String name) {
		super(parent, name);
		fComponentAttribute = EComponentAttribute.CBUNDLE;
	}
	

	@Override
	public IRteComponentClass getParentClass() {
		return this;
	}


	@Override
	public void addComponent(ICpComponent cpComponent) {
		String bundleName = cpComponent.getBundleName();
		// ensure childItem
		IRteComponentItem bundleItem = getChild(bundleName); 
		if(bundleItem == null ) {
			bundleItem = new RteComponentBundle(this, bundleName);
			addChild(bundleItem);
			if(cpComponent instanceof ICpComponentInfo) {
				ICpComponentInfo ci = (ICpComponentInfo)cpComponent;
				ci.setEvaluationResult(EEvaluationResult.MISSING_BUNDLE);
			}
		}
		bundleItem.addComponent(cpComponent);
		
		if(cpComponent instanceof ICpComponentInfo) {
			// consider error situation when components belong to different bundles  
			setActiveChild(bundleName);
		}
	}

	
	@Override
	public void addCpItem(ICpItem cpItem) {
		if (cpItem instanceof ICpTaxonomy ){
			String cgroup = cpItem.attributes().getAttribute(CmsisConstants.CGROUP);
			if( cgroup == null) {
				if(getTaxonomy() == null)
					fTaxonomy = cpItem; 
				return;
			}
		}
		super.addCpItem(cpItem);
	}

	@Override
	public Collection<String> getVariantStrings() {
		return getKeys();
	}

	@Override
	public String getActiveVariant() {
		return getActiveChildName();
	}

	@Override
	public void setActiveVariant(String variant) {
		setActiveChild(variant);
	}
}
