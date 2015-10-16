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
import java.util.Map;
import java.util.TreeMap;

import com.arm.cmsis.pack.common.CmsisConstants;
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

	protected Map<String, ICpComponent> fApis = null; // api collection sorted by version 
	protected String fActiveApiVersion = null;
	protected boolean fbUseLatesApi = true;
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
			name += " (API)"; //$NON-NLS-1$
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
			String csub = cpItem.getAttribute(CmsisConstants.CSUB);
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
		String componentName = cpComponent.getAttribute(CmsisConstants.CSUB);
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
		String groupName = cpApi.getAttribute(CmsisConstants.CGROUP); 
		if(!groupName.equals(getName()))
			return;

		ICpComponentInfo apiInfo = null;
		if(cpApi instanceof ICpComponentInfo) {
			apiInfo = (ICpComponentInfo)cpApi;
		}
		
		String version = null;
		if(apiInfo == null || apiInfo.isVersionFixed()) 
			version = cpApi.getVersion();

		ICpComponent existingApi = getApi(version);
		if(existingApi == null) { 
			if(fApis == null)
				fApis = new TreeMap<String, ICpComponent>(new AlnumComparator());
			fApis.put(cpApi.getVersion(), cpApi);
		} 
		if(apiInfo != null) {
			if(existingApi == null || existingApi instanceof ICpComponentInfo) {
				apiInfo.setComponent(null);
				apiInfo.setEvaluationResult(EEvaluationResult.MISSING_API);
			} else {
				apiInfo.setComponent(existingApi);
			}
			setActiveApi(version);
		}
	}


	@Override
	public ICpComponent getApi( final String version){
		if(fApis != null) {
			if(version == null)
				return fApis.entrySet().iterator().next().getValue();
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
	public boolean setActiveApi(final String version) {
		if(fApis == null)
			return false;
		String newVersion = version;
		if(version == null || version.equals(getDefaultVersion())) {
			newVersion = fApis.entrySet().iterator().next().getKey();
			fbUseLatesApi = true;
		} else {
			fbUseLatesApi = false;
		}
		
		String activeApiVersion = getActiveApiVersion();
		if(activeApiVersion.equals(newVersion))
			return false;
		fActiveApiVersion = newVersion;
		return true;
	}
	
	@Override
	public IRteComponentGroup getGroup(IAttributes attributes) {
		if(attributes.getAttribute(CmsisConstants.CGROUP, CmsisConstants.EMPTY_STRING).equals(getName()))
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


	@Override
	public boolean isUseLatestVersion() {
		if(fApis != null && !fApis.isEmpty())
			return fbUseLatesApi;
		return super.isUseLatestVersion();
	}
	
}
