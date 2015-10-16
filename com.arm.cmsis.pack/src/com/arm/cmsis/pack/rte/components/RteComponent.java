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
import java.util.LinkedList;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpComponent;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpTaxonomy;
import com.arm.cmsis.pack.enums.EComponentAttribute;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.info.ICpComponentInfo;
import com.arm.cmsis.pack.rte.dependencies.IRteDependency;

/**
 *  Class that represents component taxonomy level that can be selected.<br>
 *  Contain collection of component variant items.
 */
public class RteComponent extends RteComponentItem implements IRteComponent {

	protected int nSelected = 0; // number of selected instances
	
	public RteComponent(IRteComponentItem parent, String name) {
		super(parent, name);
		fComponentAttribute = EComponentAttribute.CVARIANT;
	}

	@Override
	public boolean isSelected() {
		return nSelected > 0;
	}

	@Override
	public int getSelectedCount() {
		return nSelected;
	}


	@Override
	public boolean isUseLatestVersion() {
		if(hasBundle())
			return getParentBundle().isUseLatestVersion();
		return super.isUseLatestVersion();
	}
	
	@Override
	public String getEffectiveName() {
		String name = super.getEffectiveName();
		if(name.isEmpty()) {
			IRteComponentGroup g = getParentGroup();
			if(g != null)
				name = g.getName(); 
		}
		return name;
	}

	
	@Override
	public boolean setSelected(int count) {
		if(nSelected == count)
			return false;
		nSelected = count;
		return true;
	}

	@Override
	public int getMaxInstanceCount() {
		ICpComponent c = getActiveCpComponent();
		if(c != null)
			return c.getMaxInstances(); 
		return 0;
	}
	
	@Override
	public void addComponent(ICpComponent cpComponent) {
		if(cpComponent.isApi())
			return;
		ICpComponentInfo ci = null;
		if(cpComponent instanceof ICpComponentInfo) {
			ci = (ICpComponentInfo)cpComponent;
		}
		
		// add variant, vendor and version items
		String variant = cpComponent.getAttribute(CmsisConstants.CVARIANT);
		IRteComponentItem variantItem = getChild(variant);
		if( variantItem == null) {
			variantItem = new RteComponentVariant(this, variant);
			if(ci != null){
				if(hasChildren())
					ci.setEvaluationResult(EEvaluationResult.MISSING_VARIANT);
				else
					ci.setEvaluationResult(EEvaluationResult.MISSING);
			}

			addChild(variantItem);
		}
		
		String vendor = cpComponent.getVendor();
		IRteComponentItem vendorItem = variantItem.getChild(vendor);
		if( vendorItem == null) {
			if(ci != null && variantItem.hasChildren()) {
				// there are some vendors in the collection, but not what is needed 
				ci.setEvaluationResult(EEvaluationResult.MISSING_VENDOR);
			}
			vendorItem = new RteComponentVendor(variantItem, cpComponent.getVendor());
			variantItem.addChild(vendorItem);
		}
		
		String version = null;
		if(ci == null || ci.isVersionFixed()) 
			version = cpComponent.getVersion();
		IRteComponentItem versionItem = vendorItem.getChild(version);
		if( versionItem == null) {
			if(ci != null && ci.isVersionFixed() && vendorItem.hasChildren()) {
				// there are some versions in the collection, but not what is needed
				ci.setEvaluationResult(EEvaluationResult.MISSING_VERSION);
			}
			versionItem = new RteComponentVersion(vendorItem, cpComponent.getVersion());
			vendorItem.addChild(versionItem);
		}
		
		versionItem.addComponent(cpComponent);
		if(ci != null) {
			setSelected(ci.getInstanceCount());
			setActiveChild(variant);
			variantItem.setActiveChild(vendor);
			vendorItem.setActiveChild(version);
		}
	}

	@Override
	public void addCpItem(ICpItem cpItem) {
		if(cpItem instanceof ICpComponent ) {
			addComponent((ICpComponent)cpItem);
		} else if (cpItem instanceof ICpTaxonomy ){
			String csub = cpItem.getAttribute(CmsisConstants.CGROUP);
			if( csub.equals(getName())) {
				if(getTaxonomy() == null)
					fTaxonomy = cpItem; 
				return;
			}
		}
	}

	
	@Override
	public void setActiveComponentInfo(ICpComponentInfo ci) {
		if(ci == null)
			return;
		addComponent(ci);
	}

	@Override
	public IRteComponent getParentComponent() {
		return this;
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

	@Override
	public String getActiveVendor() {
		if(hasBundle())
			return CmsisConstants.EMPTY_STRING;
		return super.getActiveVendor();
	}
	
	@Override
	public void setActiveVendor(String vendor) {
		if(hasBundle())
			return;
		super.setActiveVendor(vendor);
	}

	@Override
	public String getActiveVersion() {
		if(hasBundle())
			return CmsisConstants.EMPTY_STRING;
		return super.getActiveVersion();
	}

	@Override
	public void setActiveVersion(String version) {
		if(hasBundle())
			return;
		super.setActiveVersion(version);
	}

	@Override
	public boolean hasBundle() {
		IRteComponentBundle bundle = getParentBundle();
		if(bundle != null && !bundle.getName().isEmpty())
			return true;
		return false;
	}
	
	
	@Override
	public Collection<IRteComponent> getSelectedComponents(	Collection<IRteComponent> components) {
		// is we are here => component is active
		if(isSelected()) {
			if(components == null)
				components = new LinkedList<IRteComponent>();
				components.add(this);
		}
		return components;
	}
	

	@Override
	public Collection<IRteComponent> getUsedComponents(Collection<IRteComponent> components) {
		// is we are here => component is active
		if(getActiveCpComponentInfo() != null) {
			if(components == null)
				components = new LinkedList<IRteComponent>();
				components.add(this);
		}
		return components;
	}

	@Override
	public EEvaluationResult findComponents(IRteDependency dependency) {
		EEvaluationResult result = super.findComponents(dependency);
		if(result == EEvaluationResult.SELECTABLE) {
			if(isSelected())
				result = EEvaluationResult.FULFILLED;
		} else if (result.ordinal() >= EEvaluationResult.INSTALLED.ordinal()) {
			if(!isActive())
				result = EEvaluationResult.INACTIVE;
		}
		if(dependency.isDeny() && result == EEvaluationResult.FULFILLED)
			result = EEvaluationResult.INCOMPATIBLE;
		if(!dependency.isDeny() || result == EEvaluationResult.INCOMPATIBLE) {
			dependency.addComponent(this, result);
		}
		return result;
	}

	@Override
	public int getUseCount() {
		ICpComponentInfo ci = getActiveCpComponentInfo();
		if(ci != null)
			return ci.getInstanceCount();
		return 0;
	}
	
	
}
