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
import java.util.Map;
import java.util.TreeMap;

import com.arm.cmsis.pack.enums.EComponentAttribute;
import com.arm.cmsis.pack.utils.VersionComparator;

/**
 * Class represent Cvendor hierarchy level, contains collection of variants.
 * Direct child for bundles and components   
 */
public class RteComponentVendor extends RteComponentItem {

	public static final String LATEST = "<latest>"; 
	
	/**
	 * @param parent
	 */
	public RteComponentVendor(IRteComponentItem parent, String name) {
		super(parent, name);
		fComponentAttribute = EComponentAttribute.CVERSION;
	}

	@Override
	public Map<String, IRteComponentItem> createMap() {
		// versions are sorted in descending order  
		return new TreeMap<String, IRteComponentItem>(new VersionComparator());
	}

	@Override
	public Collection<String> getVersionStrings() {
		IRteComponent component = getParentComponent();
		if(component != null && component.hasBundle() ) {
			return null;
		}
		
		Collection<String> keys = getKeys();
		if(keys == null)
			return null;
		List<String> versions = new LinkedList<String>();
		versions.addAll(keys);
		versions.add(0, getImplicitChildName());
		return versions;
	}

	@Override
	public String getImplicitChildName() {
		return LATEST;
	}

	@Override
	public String getActiveVersion() {
		return getActiveChildName();
	}

	
	@Override
	public void setActiveVersion(String version) {
		setActiveChild(version);
	}

	@Override
	public boolean isUseLatestVersion() {
		return isActiveChildImplicit();
	}
	
}
