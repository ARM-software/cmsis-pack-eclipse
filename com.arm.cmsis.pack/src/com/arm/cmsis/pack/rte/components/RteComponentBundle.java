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

import com.arm.cmsis.pack.data.ICpComponent;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.info.ICpComponentInfo;

/**
 * Class represent Cbundle component hierarchy level, contains collection of component vendors 
 */
public class RteComponentBundle extends RteComponentVariant implements IRteComponentBundle{

	/**
	 * @param parent
	 */
	public RteComponentBundle(IRteComponentItem parent, String name) {
		super(parent, name);
	}

	@Override
	public void addComponent(ICpComponent cpComponent) {
		ICpComponentInfo ci = null;
		if(cpComponent instanceof ICpComponentInfo) {
			// consider error situation when components belong to different bundles  
			ci = (ICpComponentInfo)cpComponent;
		}
		
		// create bundle vendor and version items
		String vendor = IAttributes.EMPTY_STRING;
		String version = IAttributes.EMPTY_STRING;
		if(!getName().isEmpty()) { // bundle or component in a bundle
			vendor = cpComponent.getVendor();
			version = cpComponent.getVersion();
		} 
		
		IRteComponentItem vendorItem = getChild(vendor);
		if( vendorItem == null) {
			if(ci == null || ci.isVendorFixed() || getFirstChild() == null) {
				vendorItem = new RteComponentVendor(this, vendor);
				addChild(vendorItem);
			} else {
				vendorItem = getFirstChild();
				vendor = getFirstChildKey();
			}
		}

		IRteComponentItem versionItem = vendorItem.getChild(version);
		if( versionItem == null) {
			if(ci == null || ci.isVersionFixed() || getFirstChild() == null) {
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
		versionItem.addComponent(cpComponent);
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
