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

import java.util.LinkedHashSet;

import com.arm.cmsis.pack.base.CmsisConstants;
import com.arm.cmsis.pack.data.ICpComponent;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.enums.EComponentAttribute;

/**
 *  RClass represent a bundle version  
 */
public class RteComponentBundleVersion extends RteComponentItem {

	protected LinkedHashSet<ICpItem> fBundles = new LinkedHashSet<ICpItem>();

	public RteComponentBundleVersion(IRteComponentItem parent, String name) {
		super(parent, name);
		fComponentAttribute = EComponentAttribute.CGROUP;
		fbExclusive = false;
	}

	@Override
	public ICpItem getCpItem() {
		if(!fBundles.isEmpty())
			fBundles.iterator().next();
		return null;
	}

	@Override
	public void addComponent(ICpComponent cpComponent) {
		
		ICpItem bundle = cpComponent.getParent("bundle");
		if(bundle != null && !fBundles.contains(bundle)) {
			fBundles.add(bundle);
		}
		
		String groupName = cpComponent.attributes().getAttribute(CmsisConstants.CGROUP);
		IRteComponentItem groupItem = getChild(groupName); 
		if(groupItem == null) {
			groupItem = new RteComponentGroup(this, groupName);
			addChild(groupItem);
		}
		groupItem.addComponent(cpComponent);
	}

	
	@Override
	public void addCpItem(ICpItem cpItem) {
		String groupName = cpItem.attributes().getAttribute(CmsisConstants.CGROUP);
		if(groupName == null || groupName.isEmpty())
			return; 
		// check if class exists 
		IRteComponentItem groupItem = getChild(groupName); 
		if(groupItem == null ) {
			return; // no group => no add
		}
		groupItem.addCpItem(cpItem);
	}
}
