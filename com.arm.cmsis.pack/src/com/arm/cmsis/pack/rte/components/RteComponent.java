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
import com.arm.cmsis.pack.rte.RteConstants;
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
		if(hasBundle()) {
			return getParentBundle().isUseLatestVersion();
		}
		return super.isUseLatestVersion();
	}
	
	@Override
	public String getEffectiveName() {
		String name = super.getEffectiveName();
		if(name.isEmpty()) {
			IRteComponentGroup g = getParentGroup();
			if(g != null) {
				name = g.getName();
			} 
		}
		return name;
	}

	
	@Override
	public boolean setSelected(int count) {
		if(nSelected == count) {
			return false;
		}
		nSelected = count;
		return true;
	}

	@Override
	public int getMaxInstanceCount() {
		ICpComponent c = getActiveCpComponent();
		if(c != null) {
			return c.getMaxInstances();
		} 
		return 0;
	}
	
	@Override
	public void addComponent(ICpComponent cpComponent, int flags) {
		if(cpComponent.isApi()) {
			return;
		}
		if(cpComponent instanceof ICpComponentInfo) {
			addComponentInfo((ICpComponentInfo)cpComponent, flags);
			return;
		}
		
		// add variant, vendor and version items
		String variant = cpComponent.getAttribute(CmsisConstants.CVARIANT);
		IRteComponentItem variantItem = getChild(variant);
		if( variantItem == null) {
			variantItem = new RteComponentVariant(this, variant);
			addChild(variantItem);
		}
		
		// first try to get supplied vendor
		String vendor = cpComponent.getVendor();
		IRteComponentItem vendorItem = variantItem.getChild(vendor);
		if( vendorItem == null) {
			vendorItem = new RteComponentVendor(variantItem, cpComponent.getVendor());
			variantItem.addChild(vendorItem);
		}
		
		String version = cpComponent.getVersion();
		IRteComponentItem versionItem = vendorItem.getChild(version);
		if( versionItem == null) {
			versionItem = new RteComponentVersion(vendorItem, cpComponent.getVersion());
			vendorItem.addChild(versionItem);
		}
		
		versionItem.addComponent(cpComponent, flags);
		if(cpComponent.isDefaultVariant()) {
			setActiveChild(variant);
		}			
	}

	protected void addComponentInfo(ICpComponentInfo ci, int flags) {

		// calculate ignore flags
		boolean versionFixed = (flags & RteConstants.COMPONENT_IGNORE_VERSION) == 0 && ci.isVersionFixed();
		// version is fixed => variant and vendor implicitly fixed too 
		boolean vendorFixed  = versionFixed || ((flags & RteConstants.COMPONENT_IGNORE_VENDOR) == 0);  
		String variant = ci.getAttribute(CmsisConstants.CVARIANT);
		boolean variantFixed = versionFixed || ((flags & RteConstants.COMPONENT_IGNORE_VARIANT) == 0 && !variant.isEmpty());
		
		// add variant, vendor and version items
		// try to get supplied variant
		IRteComponentItem variantItem = getChild(variant);
		if(variantItem == null && !variantFixed)
			variantItem = getChild(getActiveVariant()); 
		
		if( variantItem == null) {
			if(hasChildren()) {
				ci.setEvaluationResult(EEvaluationResult.MISSING_VARIANT);
			} else {
				ci.setEvaluationResult(EEvaluationResult.MISSING);
			}
			variantItem = new RteComponentVariant(this, variant);
			addChild(variantItem);
		}
		
		// try to get supplied vendor
		String vendor = ci.getVendor();
		IRteComponentItem vendorItem = variantItem.getChild(vendor);
		if( vendorItem == null && !vendorFixed)
			vendorItem = variantItem.getActiveChild();
		if( vendorItem == null) {
			if(variantItem.hasChildren()) {
				// there are some vendors in the collection, but not what is needed 
				ci.setEvaluationResult(EEvaluationResult.MISSING_VENDOR);
			} else {
				ci.setEvaluationResult(EEvaluationResult.MISSING);
			}
			vendorItem = new RteComponentVendor(variantItem, vendor);
			variantItem.addChild(vendorItem);
		}
		
		String version = null;
		if(versionFixed) {
			version = ci.getVersion();
		}
		IRteComponentItem versionItem = vendorItem.getChild(version);
		if( versionItem == null) {
			if(vendorItem.hasChildren()) {
				// there are some versions in the collection, but not what is needed
				ci.setEvaluationResult(EEvaluationResult.MISSING_VERSION);
			} else {
				ci.setEvaluationResult(EEvaluationResult.MISSING);
			}
			versionItem = new RteComponentVersion(vendorItem, ci.getVersion());
			vendorItem.addChild(versionItem);
		}
		
		versionItem.addComponent(ci, flags);

		setSelected(ci.getInstanceCount());
		setActiveChild(variant);
		variantItem.setActiveChild(vendor);
		vendorItem.setActiveChild(version);
	}

	
	@Override
	public void addCpItem(ICpItem cpItem) {
		if(cpItem instanceof ICpComponent ) {
			addComponent((ICpComponent)cpItem, RteConstants.NONE);
		} else if (cpItem instanceof ICpTaxonomy ){
			String csub = cpItem.getAttribute(CmsisConstants.CGROUP);
			if( csub.equals(getName())) {
				if(getTaxonomy() == null) {
					fTaxonomy = cpItem;
				} 
				return;
			}
		}
	}

	
	@Override
	public void setActiveComponentInfo(ICpComponentInfo ci) {
		if(ci == null) {
			return;
		}
		addComponent(ci, RteConstants.NONE);
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
		if(hasBundle()) {
			return CmsisConstants.EMPTY_STRING;
		}
		return super.getActiveVendor();
	}
	
	@Override
	public void setActiveVendor(String vendor) {
		if(hasBundle()) {
			return;
		}
		super.setActiveVendor(vendor);
	}

	@Override
	public String getActiveVersion() {
		if(hasBundle()) {
			return CmsisConstants.EMPTY_STRING;
		}
		return super.getActiveVersion();
	}

	@Override
	public void setActiveVersion(String version) {
		if(hasBundle()) {
			return;
		}
		super.setActiveVersion(version);
	}

	@Override
	public boolean hasBundle() {
		IRteComponentBundle bundle = getParentBundle();
		if(bundle != null && !bundle.getName().isEmpty()) {
			return true;
		}
		return false;
	}
	
	
	@Override
	public Collection<IRteComponent> getSelectedComponents(	Collection<IRteComponent> components) {
		// is we are here => component is active
		if(isSelected()) {
			if(components == null) {
				components = new LinkedList<IRteComponent>();
			}
			components.add(this);
		}
		return components;
	}
	

	@Override
	public Collection<IRteComponent> getUsedComponents(Collection<IRteComponent> components) {
		// is we are here => component is active
		if(getActiveCpComponentInfo() != null) {
			if(components == null) {
				components = new LinkedList<IRteComponent>();
			}
				components.add(this);
		}
		return components;
	}

	@Override
	public EEvaluationResult findComponents(IRteDependency dependency) {
		EEvaluationResult result = super.findComponents(dependency);
		if(result == EEvaluationResult.SELECTABLE) {
			if(isSelected()) {
				result = EEvaluationResult.FULFILLED;
			}
		} else if (result.ordinal() >= EEvaluationResult.INSTALLED.ordinal()) {
			if(!isActive()) {
				result = EEvaluationResult.INACTIVE;
			}
		}
		dependency.addComponent(this, result);
		return result;
	}

	@Override
	public int getUseCount() {
		ICpComponentInfo ci = getActiveCpComponentInfo();
		if(ci != null) {
			return ci.getInstanceCount();
		}
		return 0;
	}
	
	
}
