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
import java.util.Map;
import java.util.TreeMap;

import com.arm.cmsis.pack.data.ICpComponent;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.info.ICpComponentInfo;
import com.arm.cmsis.pack.rte.IRteDependency;
import com.arm.cmsis.pack.utils.AlnumComparator;

/**
 * Class represent Cversion hierarchy level (the end-leaf), contains references to ICpComponents.
  */
public class RteComponentVersion extends RteComponentItem {

	protected LinkedHashSet<ICpComponent> fComponents = new LinkedHashSet<ICpComponent>();
	protected ICpComponentInfo fComponentInfo = null;
	
	
	public RteComponentVersion(IRteComponentItem parent, String name) {
		super(parent, name);
	}

	@Override
	public Map<String, IRteComponentItem> createMap() {
		// entities are sorted by packs with versions in descending order  
		return new TreeMap<String, IRteComponentItem>(new AlnumComparator());
	}

	
	@Override
	public EEvaluationResult findComponents(IRteDependency dependency) {
		if(getEntityCount() > 1)
			return EEvaluationResult.INSTALLED;
		return EEvaluationResult.SELECTABLE;
	}
	
	@Override
	public void addComponent(ICpComponent cpComponent) {
		if(cpComponent instanceof ICpComponentInfo) {
			fComponentInfo = (ICpComponentInfo)cpComponent;
			fComponentInfo.setComponent(getFirstCpComponent());
		} else if(! fComponents.contains(cpComponent)){
			fComponents.add(cpComponent);
		}
	}


	@Override
	public ICpItem getCpItem() {
		return getActiveCpComponent();
	}

	@Override
	public ICpComponent getActiveCpComponent() {
		ICpComponent cpComponent = getFirstCpComponent();
		if(cpComponent != null)
			return cpComponent;
		return fComponentInfo;
	}

	@Override
	public ICpComponentInfo getActiveCpComponentInfo() {
		return fComponentInfo;
	}

	
	protected ICpComponent getFirstCpComponent(){
		if(!fComponents.isEmpty())
			return fComponents.iterator().next();
		return null;
	}
	
	
	@Override
	public ICpComponent getApi() {
		IRteComponentGroup group = getParentGroup();
		if(group != null) {
			ICpItem cpItem = getCpItem();
			if(cpItem != null && cpItem.attributes().hasAttribute("Capiversion"))
				return group.getApi(cpItem.attributes().getAttribute("Capiversion")); // certain API version version
			else
				return group.getApi(); // active API version
		}
		return null;
	}

	
	protected int getEntityCount() {
		return fComponents.size();
	}
}
