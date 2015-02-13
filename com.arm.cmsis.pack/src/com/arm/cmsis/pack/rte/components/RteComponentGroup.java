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
import java.util.Map;
import java.util.TreeMap;

import com.arm.cmsis.pack.base.CmsisConstants;
import com.arm.cmsis.pack.data.ICpComponent;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpTaxonomy;
import com.arm.cmsis.pack.enums.EComponentAttribute;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.info.ICpComponentInfo;
import com.arm.cmsis.pack.utils.AlnumComparator;

/**
 *
 */
public class RteComponentGroup extends RteComponentItem implements IRteComponentGroup{

	private Map<String, ICpComponent> fApis = null; // api collection sorted by version 
	private String fActiveApiVersion = null;
	/**
	 * @param parent
	 */
	public RteComponentGroup(IRteComponentItem parent, String name) {
		super(parent, name);
		fComponentAttribute = EComponentAttribute.CSUB;
		fbExclusive = false;
	}

	
	@Override
	public IRteComponentGroup getParentGroup() {
		return this;
	}
	
	
	@Override
	public ICpItem getCpItem() {
		ICpComponent api = getApi();
		if(api != null) {
			return api;
		}
		return super.getCpItem();
	}


	@Override
	public String getEffectiveName() {
		String name = super.getEffectiveName();
		if(fApis != null && !fApis.isEmpty())
			name += " (API)";
		return name;
	}


	@Override
	public IRteComponentItem getEffectiveItem() {
		if(fApis == null || fApis.isEmpty()) { 
			if(getChildCount() == 1) { 
				IRteComponentItem child = fChildMap.entrySet().iterator().next().getValue();
				String childName = child.getName();
				if(childName.isEmpty() || childName.equals(getName()))
					return child; 
			}
		}
		return super.getEffectiveItem();
	}

	@Override
	public void addCpItem(ICpItem cpItem) {
		if(cpItem instanceof ICpComponent) {
			ICpComponent c = (ICpComponent)cpItem;
			if( c.isApi())
				addApi(c);
			else 
				addComponent(c);
		} else if (cpItem instanceof ICpTaxonomy ){
			String csub = cpItem.attributes().getAttribute(CmsisConstants.CSUB);
			if( csub != null) {
				// add component using subName as a key    
				IRteComponentItem component = getChild(csub);
				if(component != null) {
					component.addCpItem(cpItem);
				}
			} else if(getTaxonomy() == null){
				fTaxonomy = cpItem;
			}
		}
	}

	@Override
	public void addComponent(ICpComponent cpComponent) {
		if(cpComponent.isApi()) {
			addApi(cpComponent);
			return;
		}
		String componentName = cpComponent.attributes().getAttribute("Csub", IAttributes.EMPTY_STRING);
		// add component using subName as a key    
		IRteComponentItem component = getChild(componentName);
		if(component == null) {
			component = new RteComponent(this, componentName); 
			addChild(component);
		}
		component.addComponent(cpComponent);
	}
		
	
	/**
	 * Adds an API item to this group
	 * @param cpApi ICpApi item
	 */
	protected void addApi(ICpComponent cpApi) {
		String groupName = cpApi.attributes().getAttribute(CmsisConstants.CGROUP, IAttributes.EMPTY_STRING); 
		if(!groupName.equals(getName()))
			return;

		String version = cpApi.attributes().getAttribute("Capiversion", IAttributes.EMPTY_STRING);
		ICpComponent existingApi = getApi(version);
		if(existingApi == null) { 
			if(fApis == null)
				fApis = new TreeMap<String, ICpComponent>(new AlnumComparator());
			fApis.put(version, cpApi);
		} 
		if(cpApi instanceof ICpComponentInfo) {
			ICpComponentInfo apiInfo = (ICpComponentInfo)cpApi;
			if(existingApi == null || existingApi instanceof ICpComponentInfo) {
				apiInfo.setComponent(null);
				apiInfo.setEvaluationResult(EEvaluationResult.MISSING_API);
			} else {
				apiInfo.setComponent(existingApi);
			}
		}
	}


	@Override
	public ICpComponent getApi( final String version){
		if(fApis != null) {
			return fApis.get(version);
		}
		return null;
	}

	@Override
	public ICpComponent getApi() {
		return getApi(getActiveApiVersion());
	}

	
	@Override
	public Map<String, ICpComponent> getApis() {
		return fApis;
	}

	@Override
	public String getActiveApiVersion() {
		if(fApis != null && fActiveApiVersion == null ) {
			fActiveApiVersion = fApis.entrySet().iterator().next().getKey();
		}
		return fActiveApiVersion;
	}

	@Override
	public boolean setActiveApi(String version) {
		String activeApiVersion = getActiveApiVersion();
		if(activeApiVersion.equals(version))
			return false;
		fActiveApiVersion = version;
		return true;
	}
	
	@Override
	public IRteComponentGroup getGroup(IAttributes attributes) {
		if(attributes.getAttribute(CmsisConstants.CGROUP, IAttributes.EMPTY_STRING).equals(getName()))
			return this;
		return null;
	}

	@Override
	public Collection<String> getVersionStrings() {
		if(fApis != null && !fApis.isEmpty())
			return fApis.keySet();
		return super.getVersionStrings();
	}

	@Override
	public String getActiveVersion() {
		if(fApis != null && !fApis.isEmpty())
			return getActiveApiVersion();
		return super.getActiveVersion();
	}

	@Override
	public void setActiveVersion(String version) {
		if(fApis != null && !fApis.isEmpty())
			setActiveApi(version);
		else 
			super.setActiveVersion(version);
	}
	
}
