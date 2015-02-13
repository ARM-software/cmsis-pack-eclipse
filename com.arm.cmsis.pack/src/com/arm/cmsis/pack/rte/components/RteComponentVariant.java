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
import java.util.LinkedList;
import java.util.List;

import com.arm.cmsis.pack.enums.EComponentAttribute;

/**
 * Class represent Cvariant hierarchy level, contains collection of versions.
 * Used in bundle and component levels
 */
public class RteComponentVariant extends RteComponentItem {
	public static final String ANY = "<any>"; 
	/**
	 * @param parent
	 */
	public RteComponentVariant(IRteComponentItem parent, String name) {
		super(parent, name);
		fComponentAttribute = EComponentAttribute.CVENDOR;
	}
	
	@Override
	public Collection<String> getVendorStrings() {
		IRteComponent component = getParentComponent();
		if(component != null && component.hasBundle() ) {
			return null;
		}
		
		Collection<String> keys = getKeys();
		if(keys == null)
			return null;
		List<String> vendors = new LinkedList<String>();
		vendors.addAll(keys);
		vendors.add(0, getImplicitChildName());
		
		return vendors;
	}
	
	@Override
	public String getImplicitChildName() {
		return ANY;
	}


	@Override
	public String getActiveVendor() {
		return getActiveChildName();
	}


	@Override
	public void setActiveVendor(String vendor) {
		setActiveChild(vendor);
	}

	@Override
	public boolean isUseAnyVendor() {
		return isActiveChildImplicit();
	}
	
}
