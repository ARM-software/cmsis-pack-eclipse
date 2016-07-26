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

import java.util.LinkedHashSet;

import com.arm.cmsis.pack.common.CmsisConstants;
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
			return fBundles.iterator().next();
		return null;
	}

	@Override
	public void addComponent(ICpComponent cpComponent, int flags) {
		
		ICpItem bundle = cpComponent.getParent(CmsisConstants.BUNDLE_TAG);
		if(bundle != null && !fBundles.contains(bundle)) {
			fBundles.add(bundle);
		}
		
		String groupName = cpComponent.getAttribute(CmsisConstants.CGROUP);
		IRteComponentItem groupItem = getChild(groupName); 
		if(groupItem == null) {
			groupItem = new RteComponentGroup(this, groupName);
			addChild(groupItem);
		}
		groupItem.addComponent(cpComponent, flags);
	}

	
	@Override
	public void addCpItem(ICpItem cpItem) {
		String groupName = cpItem.getAttribute(CmsisConstants.CGROUP);
		if(groupName == null || groupName.isEmpty())
			return; 
		// check if group exists 
		IRteComponentItem groupItem = getChild(groupName); 
		if(groupItem == null ) {
			return; // no group => no add
		}
		groupItem.addCpItem(cpItem);
	}
}
