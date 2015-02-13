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

import com.arm.cmsis.pack.base.CmsisConstants;
import com.arm.cmsis.pack.data.ICpComponent;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpTaxonomy;
import com.arm.cmsis.pack.enums.EComponentAttribute;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.info.ICpComponentInfo;
import com.arm.cmsis.pack.rte.IRteDependency;

/**
 *  Class that represents component taxonomy level that can be selected.
 *  Contain collection of component vendor items.
 *  Contains reference to instantiated component
 */
public class RteComponent extends RteComponentItem implements IRteComponent {

	protected int nSelected = 0; // number of selected instances
	
	/**
	 * @param parent
	 */
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
			return c.attributes().getAttributeAsInt("maxInstances", 1); 
		return 0;
	}
	
	@Override
	public void addComponent(ICpComponent cpComponent) {
		if(cpComponent.isApi())
			return;
		ICpComponentInfo ci = null;
		if(cpComponent instanceof ICpComponentInfo) {
			// consider error situation when components belong to different bundles  
			ci = (ICpComponentInfo)cpComponent;
		}
		
		// add variant, vendor and version items
		String variant = cpComponent.attributes().getAttribute("Cvariant", IAttributes.EMPTY_STRING);
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
			if(ci == null || ci.isVendorFixed() || variantItem.getFirstChild() == null) {
				vendorItem = new RteComponentVendor(variantItem, vendor);
				variantItem.addChild(vendorItem);
			} else {
				vendorItem = variantItem.getFirstChild();
				vendor = variantItem.getFirstChildKey();
			}
			
			if(ci != null && ci.isVendorFixed()) {
				ci.setEvaluationResult(EEvaluationResult.MISSING_VENDOR);
			}
		}
		
		String version = cpComponent.getVersion();
		IRteComponentItem versionItem = vendorItem.getChild(version);
		if( versionItem == null) {
			if(ci == null || ci.isVersionFixed() || vendorItem.getFirstChild() == null) {
				versionItem = new RteComponentVersion(vendorItem, version);
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
			String csub = cpItem.attributes().getAttribute(CmsisConstants.CGROUP, IAttributes.EMPTY_STRING);
			if( csub.equals(getName())) {
				if(getTaxonomy() == null)
					fTaxonomy = cpItem; 
				return;
			}
		}
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
			return IAttributes.EMPTY_STRING;
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
			return IAttributes.EMPTY_STRING;
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
		if(!dependency.isDeny() || result == EEvaluationResult.FULFILLED)
			dependency.addComponent(this, result);
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
