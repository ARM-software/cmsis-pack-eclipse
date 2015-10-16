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

package com.arm.cmsis.pack.rte.components;


import java.util.Collection;

import com.arm.cmsis.pack.common.CmsisConstants;
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
		ICpComponentInfo ci = null;
		if(cpComponent instanceof ICpComponentInfo) {
			ci = (ICpComponentInfo)cpComponent;
		}
		// ensure childItem
		IRteComponentItem bundleItem = getChild(bundleName); 
		if(bundleItem == null ) {
			if(ci != null && hasChildren()) {
				// there are some bundles, but not what is needed 
				ci.setEvaluationResult(EEvaluationResult.MISSING_BUNDLE);
			}
			bundleItem = new RteComponentBundle(this, bundleName);
			addChild(bundleItem);
		}
		bundleItem.addComponent(cpComponent);
		
		if(ci != null) {
			setActiveChild(bundleName);
		}
	}

	
	@Override
	public void addCpItem(ICpItem cpItem) {
		if (cpItem instanceof ICpTaxonomy ){
			String cgroup = cpItem.getAttribute(CmsisConstants.CGROUP);
			if( cgroup == null || cgroup.isEmpty()) {
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
