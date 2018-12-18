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
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.info.ICpComponentInfo;

/**
 * Class represent Cbundle component hierarchy level, contains collection of component vendors 
 */
public class RteComponentBundle extends RteComponentVariant implements IRteComponentBundle{

	public RteComponentBundle(IRteComponentItem parent, String name) {
		super(parent, name);
	}

	@Override
	public void addComponent(ICpComponent cpComponent, int flags) {
		ICpComponentInfo ci = null;
		if(cpComponent instanceof ICpComponentInfo) {
			// consider error situation when components belong to different bundles  
			ci = (ICpComponentInfo)cpComponent;
		}
		
		// create bundle vendor and version items
		String vendor = CmsisConstants.EMPTY_STRING;
		String version = CmsisConstants.EMPTY_STRING;
		
		if(!getName().isEmpty()) { // bundle or component in a bundle
			vendor = cpComponent.getVendor();
			if(ci == null || ci.isVersionFixed()) {
				version = cpComponent.getBundleVersion();
				if(version.isEmpty())
					version = null;
			} else { 
				version = null;
			}
		} 
		
		IRteComponentItem vendorItem = getChild(vendor);
		if( vendorItem == null) {
			vendorItem = new RteComponentVendor(this, vendor);
			addChild(vendorItem);
		}

		IRteComponentItem versionItem = vendorItem.getChild(version);
		if( versionItem == null) {
			if(ci == null || ci.isVersionFixed() || vendorItem.getFirstChild() == null) {
				versionItem = new RteComponentBundleVersion(vendorItem, version);
				vendorItem.addChild(versionItem);
			} else {
				versionItem = vendorItem.getFirstChild();
				version = vendorItem.getFirstChildKey();
			}

			if(ci != null && ci.isVersionFixed()) {
				ci.setEvaluationResult(EEvaluationResult.MISSING_VERSION);
			}
		}
		versionItem.addComponent(cpComponent, flags);
		if(ci != null){
			setActiveChild(vendor);
			vendorItem.setActiveChild(version);
		}
	}
	
	@Override
	public IRteComponentBundle getParentBundle() {
		return this;
	}
	
	@Override
	public Collection<String> getVendorStrings() {
		if(getName().isEmpty())
			return null;
		return super.getVendorStrings(); 
	}

	@Override
	public Collection<String> getVersionStrings() {
		if(getName().isEmpty())
			return null;
		return super.getVersionStrings();
	}
}
