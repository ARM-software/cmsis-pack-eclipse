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
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.arm.cmsis.pack.CpStrings;
import com.arm.cmsis.pack.data.ICpComponent;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.enums.EComponentAttribute;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.info.ICpComponentInfo;
import com.arm.cmsis.pack.item.CmsisMapItem;
import com.arm.cmsis.pack.rte.RteConstants;
import com.arm.cmsis.pack.rte.dependencies.IRteDependency;
import com.arm.cmsis.pack.utils.AlnumComparator;


/**
 *  Base class for component hierarchy items 
 */
public class RteComponentItem extends CmsisMapItem<IRteComponentItem> implements IRteComponentItem {
	
	protected String fActiveChildName = null;
	protected boolean fbActiveChildDefault = true;
	protected boolean fbExclusive = true; // default is true
	protected EComponentAttribute fComponentAttribute = EComponentAttribute.CNONE;
	protected ICpItem fTaxonomy = null;  
	
	/**
	 * Hierarchical constructor
	 * @param parent parent item
	 */
	public RteComponentItem(IRteComponentItem parent, final String name) {
		super(parent, name);
		
	}

	@Override
	public void destroy() {
		super.destroy();
		fTaxonomy = null;
	}
	
	@Override
	public boolean purge() {
		if(!hasChildren()) {
			destroy(); // children were already purged
			return true;
		}
		return super.purge();
	}
	

	@Override
	public void addComponent(ICpComponent cpComponent, int flags) {
		// default does nothing
	}
	
	@Override
	public void addComponent(ICpComponent cpComponent) {
		addComponent(cpComponent, RteConstants.NONE);
	}
	
	@Override
	public Map<String, IRteComponentItem> createMap() {
		// entities are sorted alpha-numerically in ascending order    
		return new TreeMap<String, IRteComponentItem>(new AlnumComparator(false));
	}
	
	
	@Override
	public String getItemKey(IRteComponentItem item) {
		if(item == null) {
			return null;
		}
		return item.getKey();
	}

	@Override
	public String getKey() {
		return getName();
	}
	
	@Override
	public void addCpItem(ICpItem cpItem) {
		if(!hasChildren()) {
			return;
		}
		Collection<? extends IRteComponentItem> children = getChildren();
		for(IRteComponentItem child : children) {
			child.addCpItem(cpItem);
		}
	}
	
	
	@Override
	public ICpItem getTaxonomy() {
		return fTaxonomy;
	}

	@Override
	public ICpComponent getActiveCpComponent() {
		ICpItem item = getActiveCpItem();
		if(item instanceof ICpComponent) {
			return (ICpComponent) item;
		}
		return null;
	}

	
	@Override
	public ICpItem getCpItem() {
		return null; // base class does not have associated ICpItem 
	}
		

	@Override
	public boolean isExclusive() {
		return fbExclusive;
	}

	@Override
	public String getActiveChildName() {
		if(isExclusive() && hasChildren() ) {
			if(fActiveChildName == null || !hasChild(fActiveChildName)) {
				fActiveChildName = getFirstChildKey();
			}
		}
		return fActiveChildName;
	}

	
	@Override
	public boolean isSelected() {
		IRteComponent component = getParentComponent();
		if(component != null) {
			return component.isSelected() && isActive();
		}
		
		IRteComponentItem activeChild = getActiveChild();
		if(activeChild != null) {
			return activeChild.isSelected();
		}
		if(!hasChildren()) {
			return false;
		}
		Collection<? extends IRteComponentItem> children = getChildren();
		for(IRteComponentItem child : children) {
			if(child.isSelected()) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean isActive() {
		IRteComponentItem parent = getParent();
		if(parent == null)
		 {
			return true; // root is always active
		}
		if(parent.isExclusive()){
			if(parent.getActiveChild() != this) {
				return false;
			}
		}
		return parent.isActive();
	}

	@Override
	public IRteComponentItem getActiveChild() {
		if(hasChildren() && isExclusive()) {
			String activeChildName = getActiveChildName();
			if(fActiveChildName != null) {
				return fChildMap.get(activeChildName);
			}
		}
		return null;
	}

	@Override
	public boolean setActiveChild(final String name) {
		if(isExclusive()) {
			String newName = name;
			String defaultName = getDefaultChildName();
			
			if(name == null || (defaultName != null && name.equals(defaultName))) {
				newName = getFirstChildKey();
				fbActiveChildDefault = defaultName != null;
			} else {
				fbActiveChildDefault = false;
			}
			String activeChildName = getActiveChildName();
			if(activeChildName.equals(newName)) {
				return false;
			}
			fActiveChildName = newName;
		}
		return true;
	}

	@Override
	public ICpItem getActiveCpItem() {
		ICpItem cpItem = getCpItem();
		if(cpItem != null) {
			return cpItem;
		}
		IRteComponentItem activeChild = getActiveChild();
		if(activeChild != null) {
			return activeChild.getActiveCpItem();
		}
		return null;
	}
	
	@Override
	public ICpComponentInfo getActiveCpComponentInfo() {
		IRteComponentItem activeChild = getActiveChild();
		if(activeChild != null) {
			return activeChild.getActiveCpComponentInfo();
		}
		return null;
	}

	@Override
	public IRteComponentItem getActiveItem() {
		ICpItem cpItem = getCpItem();
		if(cpItem != null) {
			return this;
		}
		IRteComponentItem activeChild = getActiveChild();
		if(activeChild != null) {
			return activeChild.getActiveItem();
		}
		return null;
	}

	@Override
	public ICpComponent getApi() {
		// default gets parent group active API
		IRteComponentGroup group = getParentGroup();
		if(group != null) {
			return group.getApi();
		}
		return null;
	}

	@Override
	public IRteComponentGroup getParentGroup() {
		IRteComponentItem parent = getParent();
		if(parent != null) {
			return parent.getParentGroup();
		}
		return null;
	}
	

	@Override
	public IRteComponentClass getParentClass() {
		IRteComponentItem parent = getParent();
		if(parent != null) {
			return parent.getParentClass();
		}
		return null;
	}

	@Override
	public IRteComponentGroup getGroup(IAttributes attributes) {
		if(attributes != null) {
			String childKey = attributes.getAttribute(getKeyAttributeString());
			if(childKey != null && !childKey.isEmpty()) {
				IRteComponentItem child = getChild(childKey);
				if(child != null) {
					return child.getGroup(attributes);
				}
			}
		}
		return null;
	}
	

	@Override
	public IRteComponent getParentComponent() {
		IRteComponentItem parent = getParent();
		if(parent != null) {
			return parent.getParentComponent();
		}
		return null;
	}

	@Override
	public IRteComponentBundle getParentBundle() {
		IRteComponentItem parent = getParent();
		if(parent != null) {
			return parent.getParentBundle();
		}
		return null;
	}
	

	@Override
	public Map<String, ? extends IRteComponentItem> getEffectiveChildMap() {
		if(isExclusive()) {
			IRteComponentItem activeChild = getActiveChild();
			if(activeChild != null) {
				return activeChild.getEffectiveChildMap();
			}
			return null;
		}
		return super.getEffectiveChildMap();
	}

	@Override
	public Object[] getEffectiveChildArray() {
		if(isExclusive()) {
			IRteComponentItem activeChild = getActiveChild();
			if(activeChild != null) {
				return activeChild.getEffectiveChildArray();
			}
			return null;
		}
		return super.getEffectiveChildArray();
	}

	@Override
	public IRteComponentItem getEffectiveParent() {
		IRteComponentItem parent = getParentComponent();
		if(parent != null && parent != this) {
			IRteComponentItem g = getParentGroup();
			if(g.getEffectiveItem() == parent) {
				return g;
			}
			return parent;
		}
		
		parent = getParentGroup();
		if(parent != null && parent != this && parent.getEffectiveItem() != this) {
			return parent;
		}
		
		parent = getParentClass();
		if(parent != null && parent.getEffectiveItem() != this) {
			return parent;
		}
		return getParent();
	}

	
	@Override
	public IRteComponentItem getEffectiveHierarchyItem() {
		IRteComponentItem component = getParentComponent();
		if(component != null) {
			IRteComponentItem g = getParentGroup();
			if(g.getEffectiveItem() == component) {
				return g;
			}
			return component;
		}
		IRteComponentItem parent = getParentGroup();
		if(parent != null) {
			return parent;
		}
		
		parent = getParentClass();
		if(parent != null) {
			return parent;
		}

		return super.getEffectiveHierarchyItem();
	}

	@Override
	public EComponentAttribute getKeyAttribute() {
		return fComponentAttribute;
	}
	
	
	@Override
	public String getKeyAttributeString() {
		return getKeyAttribute().toString();
	}

	
	@Override
	public String getKeyAttributeValue(IAttributes attributes) {
		if(attributes == null) {
			return null;
		}
		return attributes.getAttribute(getKeyAttributeString());
	}

	@Override
	public String getUrl() {
		ICpItem cpItem = getActiveCpItem();
		if(cpItem != null) {
			String url = cpItem.getUrl();
			if(url != null && !url.isEmpty()) {
				return url;
			}
		}
		cpItem = getTaxonomy();
		if(cpItem != null) {
			return cpItem.getUrl();
		}
		return null;
	}

	@Override
	public String getDoc() {
		return getUrl();
	}
	
	@Override
	public String getDescription() {
		ICpItem cpItem = getActiveCpItem();
		if(cpItem != null) {
			String descr = cpItem.getDescription();
			if(descr != null && !descr.isEmpty()) {
				return descr;
			}
		}
		cpItem = getTaxonomy();
		if(cpItem != null) {
			return cpItem.getDescription();
		}
		return null;
	}

	@Override
	public Collection<String> getVariantStrings() {
		// default returns null
		return null;
	}

	@Override
	public Collection<String> getVendorStrings() {
		IRteComponentItem activeChild = getActiveChild();
		if(activeChild != null) {
			return activeChild.getVendorStrings();
		}
		return null;
	}


	@Override
	public Collection<String> getVersionStrings() {
		IRteComponentItem activeChild = getActiveChild();
		if(activeChild != null) {
			return activeChild.getVersionStrings();
		}
		return null;
	}

	@Override
	public String getActiveVariant() {
		IRteComponentItem activeChild = getActiveChild();
		if(activeChild != null) {
			return activeChild.getActiveVariant();
		}
		return null;
	}
	
	@Override
	public void setActiveVariant(String variant) {
		IRteComponentItem activeChild = getActiveChild();
		if(activeChild != null) {
			activeChild.setActiveVariant(variant);
		}
	}

	
	@Override
	public String getActiveVendor() {
		IRteComponentItem activeChild = getActiveChild();
		if(activeChild != null) {
			return activeChild.getActiveVendor();
		}
		return null;
	}
	
	@Override
	public void setActiveVendor(String vendor) {
		IRteComponentItem activeChild = getActiveChild();
		if(activeChild != null) {
			activeChild.setActiveVendor(vendor);
		}
	}
	
	@Override
	public void setActiveVersion(String version) {
		IRteComponentItem activeChild = getActiveChild();
		if(activeChild != null) {
			activeChild.setActiveVersion(version);
		}
	}


	@Override
	public String getActiveVersion() {
		IRteComponentItem activeChild = getActiveChild();
		if(activeChild != null) {
			return activeChild.getActiveVersion();
		}
		return null;
	}

	@Override
	public boolean isUseLatestVersion() {
		IRteComponentItem activeChild = getActiveChild();
		if(activeChild != null) {
			return activeChild.isUseLatestVersion();
		}
		return false;
	}

		
	@Override
	public String getDefaultChildName() {
		// base does not have default child name
		return null;
	}

	@Override
	public boolean isActiveChildDefault() {
		return fbActiveChildDefault && getDefaultChildName() != null;
	}

	@Override
	public Collection<IRteComponent> getSelectedComponents(	Collection<IRteComponent> components) {
		if(components == null) {
			components = new LinkedList<IRteComponent>();
		}
		
		IRteComponentItem activeChild = getActiveChild();
		if(activeChild != null) {
			return activeChild.getSelectedComponents(components);
		}
		if(hasChildren()) {
			Collection<? extends IRteComponentItem> children = getChildren();
			for(IRteComponentItem child : children) {
				child.getSelectedComponents(components);
			}
		}
		return components;
	}

	
	@Override
	public Collection<IRteComponent> getUsedComponents(	Collection<IRteComponent> components) {
		if(components == null) {
			components = new LinkedList<IRteComponent>();
		}
		
		IRteComponentItem activeChild = getActiveChild();
		if(activeChild != null) {
			return activeChild.getUsedComponents(components);
		}
		if(hasChildren()) {
			Collection<? extends IRteComponentItem> children = getChildren();
			for(IRteComponentItem child : children) {
				child.getUsedComponents(components);
			}
		}
		return components;
	}

	
	@Override
	public EEvaluationResult findComponents(IRteDependency dependency) {
		EEvaluationResult result = EEvaluationResult.MISSING;
		if(dependency == null) {
			return result;
		}

		Map<String, ? extends IRteComponentItem> children = getChildMap();
		if(children == null) {
			return result;
		}
		boolean bIncompatible = false;
		
		String keyPattern = getKeyAttributeValue( dependency.getCpItem().attributes());
		int depFlags = dependency.getFlags(); 
		// check if we can ignore Cvarinat, Cversion, Cvendor, CBundle attribute values
		boolean ignore = depFlags == 0 ? false : (depFlags | RteConstants.flagForAttribute(getKeyAttributeString())) != 0;
		
		boolean matchFound = false;
		// first evaluate active item
		IRteComponentItem activeChild = getActiveChild();
		if(activeChild != null){
			if(ignore || matchKey(keyPattern, activeChild.getName())){
				EEvaluationResult res = activeChild.findComponents(dependency);
				if(res == EEvaluationResult.FULFILLED) {
					return res;
				} else if(res != EEvaluationResult.IGNORED ) {
					result = res;
				} 
				matchFound = true;
			} else if(activeChild.isSelected()) {
				bIncompatible = true;
			}
		}
  
		for(Entry<String, ? extends IRteComponentItem> e : children.entrySet()) {
			IRteComponentItem child = e.getValue();
			if(child == activeChild) {
				continue;
			}
			if(matchKey(keyPattern, e.getKey())){
				matchFound = true;
				EEvaluationResult res= child.findComponents(dependency);
				if(res != EEvaluationResult.IGNORED && res.ordinal() > result.ordinal()) {
					result = res;
				}
			}
		}
		if(!matchFound ){
			result = EEvaluationResult.valueOf(result, getKeyAttribute()); 
			dependency.addStopItem(this, result);
		} else if(bIncompatible && result.ordinal() >= EEvaluationResult.INCOMPATIBLE.ordinal()) {
			result = EEvaluationResult.valueOf(EEvaluationResult.INCOMPATIBLE, getKeyAttribute());
			dependency.addStopItem(this, result);
		}
		return result;
	}


	@Override
	public boolean matchKey(String pattern, String key) {
		if(pattern == null) {
			return true;
		}
		
		EComponentAttribute keyAttribute = getKeyAttribute();
		if(keyAttribute == null) {
			return true;
		}
				
		return keyAttribute.match(pattern, key);
	}

	@Override
	public String getDefaultVersion() {
		return CpStrings.RteComponentVersionLatest;
	}
	
	@Override
	public String toString() {
		return getName();
	}

}
