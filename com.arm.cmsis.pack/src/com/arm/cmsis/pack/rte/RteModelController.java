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

package com.arm.cmsis.pack.rte;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackManager;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpPackFilter;
import com.arm.cmsis.pack.data.ICpDeviceItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPackCollection;
import com.arm.cmsis.pack.data.ICpPackFilter;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.enums.EVersionMatchMode;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.events.RteEventProxy;
import com.arm.cmsis.pack.generic.Attributes;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.info.ICpComponentInfo;
import com.arm.cmsis.pack.info.ICpConfigurationInfo;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.info.ICpPackFilterInfo;
import com.arm.cmsis.pack.info.ICpPackInfo;
import com.arm.cmsis.pack.rte.components.IRteComponent;
import com.arm.cmsis.pack.rte.components.IRteComponentItem;
import com.arm.cmsis.pack.rte.dependencies.IRteDependencyItem;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;
import com.arm.cmsis.pack.rte.packs.IRtePack;
import com.arm.cmsis.pack.rte.packs.IRtePackCollection;
import com.arm.cmsis.pack.rte.packs.IRtePackFamily;
import com.arm.cmsis.pack.rte.packs.RtePackCollection;

/**
 * Default implementation of IRteModel interface 
 *    
 */
public abstract class RteModelController extends RteEventProxy implements IRteModelController {

	protected IRteModel fModel = null;
	// filtered Packs
	protected ICpPackFilter 	  fSavedPackFilter = null;
	protected ICpPackFilter 	  fCurrentPackFilter = null;
	protected IRtePackCollection  fRtePackCollection = null;
	
	protected IAttributes fSavedDeviceAttributes = null;
	protected Set<String> fSavedComponentKeys = null;
	
	protected boolean fbComponentSelectionModified = false;
	protected boolean fbPackFilterModified = false;
	protected boolean fbDeviceModified = false;

	/**
	 *  Default constructor
	 */
	public RteModelController(IRteModel model) {
		fModel = model;
	}

	@Override
	public IRteModel getModel() {
		return fModel;
	}

	
	//@Override
	@Override
	public void clear() {
		if(fModel != null) {
			fModel.clear();
		}
		fModel = null;
		fSavedPackFilter = null;
		fCurrentPackFilter = null;
		fRtePackCollection = null;
		fSavedDeviceAttributes = null;
		fSavedComponentKeys = null;
	}


	@Override
	public boolean isComponentSelectionModified() {
		return fbComponentSelectionModified;
	}

	@Override
	public boolean isPackFilterModified() {
		return fbPackFilterModified;
	}
	
	@Override
	public boolean isDeviceModified() {
		return fbDeviceModified;
	}
	
	@Override
	public boolean isModified() {
		return isDeviceModified() || isPackFilterModified() || isComponentSelectionModified();
	}
	
	protected boolean checkIfComponentsModified() {
		Set<String> keys = collectComponentKeys();
		return !keys.equals(fSavedComponentKeys);
	}
	
	protected Set<String> collectComponentKeys() {
		Set<String> ids = new HashSet<String>();
		ICpConfigurationInfo info = getConfigurationInfo();
		collectComponentKeys(ids, info.getGrandChildren(CmsisConstants.COMPONENTS_TAG));
		collectComponentKeys(ids, info.getGrandChildren(CmsisConstants.APIS_TAG));
		return ids;
	}
	
	static protected void collectComponentKeys(Set<String> ids, Collection<? extends ICpItem> children) {
		if(children == null || children.isEmpty()) {
			return;
		}
		for(ICpItem child : children) {
			if(!(child instanceof ICpComponentInfo)) {
				continue;
			}
			ICpComponentInfo ci = (ICpComponentInfo)child;
			String key = ci.getName() + ':' + ci.getAttribute(CmsisConstants.INSTANCES);
			if(ci.isVersionFixed()) {
				key += ':' + ci.getVersion();
			}
			ids.add(key);
		}
	}

	@Override
	public void reloadPacks() {
		collectPacks();
		fRtePackCollection.setPackFilterInfo(fModel.getConfigurationInfo().getPackFilterInfo());
		update();
	}
	
	protected void collectPacks() {
		ICpPackCollection allPacks = null;
		ICpPackManager pm  = CpPlugIn.getPackManager();
		if(pm != null) {
			allPacks = pm.getInstalledPacks();
		}
		fRtePackCollection = new RtePackCollection();
		if(allPacks != null) {
			fRtePackCollection.addCpItem(allPacks); 
		}
	}
	

	@Override
	public void setConfigurationInfo(ICpConfigurationInfo info) {
		if(info == null) {
			clear();
			return;
		}

		fSavedPackFilter = new CpPackFilter(info.createPackFilter());
		fCurrentPackFilter = new CpPackFilter(fSavedPackFilter);
		fSavedDeviceAttributes = new Attributes(info.getDeviceInfo().attributes());
		collectPacks();

		fRtePackCollection.setPackFilterInfo(info.getPackFilterInfo()); 
		fModel.setConfigurationInfo(info); // will update used packs
		fRtePackCollection.setUsedPacks(getUsedPackInfos());
		fSavedComponentKeys = collectComponentKeys(); // initial update
	}

	@Override
	public void updateConfigurationInfo() {
		if(getConfigurationInfo() == null) {
			return;
		}
		if(setPackFilter(fCurrentPackFilter)) {
			update();
		} else {
			updateComponentInfos();
		}
	}

	
	@Override
	public void updateComponentInfos() {
		fModel.updateComponentInfos();
		fRtePackCollection.setUsedPacks(getUsedPackInfos());
	}
	
	public void update() {
		update(RteConstants.NONE);
	}
	
	@Override
	public void update(int flags) {
		updateComponentInfos();
		updatePackFilterInfo();
		fModel.update(flags);
		fRtePackCollection.setUsedPacks(getUsedPackInfos());
		emitRteEvent(RteEvent.CONFIGURATION_MODIFIED, this);
	}


	@Override
	public void commit() {
		updateConfigurationInfo();

		fModel.getComponents().purge();
		fRtePackCollection.purge();
		fSavedPackFilter = new CpPackFilter(getPackFilter());
		fCurrentPackFilter = new CpPackFilter(fSavedPackFilter);
		fSavedDeviceAttributes = new Attributes(getDeviceInfo().attributes());
		fSavedComponentKeys = collectComponentKeys();
		fbComponentSelectionModified = false;
		fbPackFilterModified = false;
		fbDeviceModified = false;
		emitRteEvent(RteEvent.CONFIGURATION_COMMITED, this);
	}
	

	@Override
	public IRtePackCollection getRtePackCollection() {
		return fRtePackCollection;
	}

	@Override
	public void selectComponent(IRteComponent component, int nInstances) {
		fModel.selectComponent(component, nInstances);
		evaluateComponentDependencies();
	}

	@Override
	public void selectActiveVariant(IRteComponentItem item, String variant) {
		if(item != null) {
			item.setActiveVariant(variant);
			evaluateComponentDependencies();
		}
	}

	@Override
	public void selectActiveVendor(IRteComponentItem item, String vendor) {
		if(item != null) {
			item.setActiveVendor(vendor);
			evaluateComponentDependencies();
		}
	}


	@Override
	public void selectActiveVersion(IRteComponentItem item, String version) {
		if(item != null) {
			item.setActiveVersion(version);
			evaluateComponentDependencies();
		}
	}

	protected void emitComponentSelectionModified() {
		updateComponentInfos();
		fbComponentSelectionModified = checkIfComponentsModified();
		emitRteEvent(RteEvent.COMPONENT_SELECTION_MODIFIED, this);
	}

	protected void emitPackFilterModified() {
		fCurrentPackFilter = fRtePackCollection.createPackFiler();
		fbPackFilterModified = !fSavedPackFilter.equals(fCurrentPackFilter);
		emitRteEvent(RteEvent.FILTER_MODIFIED, this);
	}

	
	@Override
	public EEvaluationResult resolveComponentDependencies() {
		EEvaluationResult res = fModel.resolveComponentDependencies(); 
		emitComponentSelectionModified();
		return res;
	}

	@Override
	public ICpPackFilter getPackFilter() {
		return fModel.getPackFilter();
	}

	@Override
	public boolean setPackFilter(ICpPackFilter filter) {
		return fModel.setPackFilter(filter);
	}

	@Override
	public ICpDeviceItem getDevice() {
		return fModel.getDevice();
	}

	@Override
	public ICpDeviceInfo getDeviceInfo() {
		return fModel.getDeviceInfo();
	}

	@Override
	public void setDeviceInfo(ICpDeviceInfo deviceInfo) {
		boolean changed = false;
		int updateFlags = RteConstants.NONE;  
		if(getDeviceInfo() == null) {
			changed = true;
		} else {
			changed = !getDeviceInfo().attributes().equals(deviceInfo.attributes());
			if(changed)
				updateFlags = RteConstants.COMPONENT_IGNORE_ALL;
		}
		
		if(changed) {
			fbDeviceModified = !fSavedDeviceAttributes.equals(deviceInfo.attributes());
			fModel.setDeviceInfo(deviceInfo);
			update(updateFlags);
		}
	}

	@Override
	public ICpItem getToolchainInfo() {
		return fModel.getToolchainInfo();
	}

	@Override
	public ICpConfigurationInfo getConfigurationInfo() {
		return fModel.getConfigurationInfo();
	}


	@Override
	public IRteComponentItem getComponents() {
		return fModel.getComponents();
	}

	@Override
	public EEvaluationResult evaluateComponentDependencies() {
		EEvaluationResult res = fModel.evaluateComponentDependencies();
		emitComponentSelectionModified();
		return res;
	}

	@Override
	public EEvaluationResult getEvaluationResult() {
		return fModel.getEvaluationResult();
	}
	
	@Override
	public EEvaluationResult getEvaluationResult(IRteComponentItem item) {
		return fModel.getEvaluationResult(item);
	}

	@Override
	public void setEvaluationResult(EEvaluationResult result) {
		fModel.setEvaluationResult(result);
	}

	@Override
	public Collection<IRteComponent> getSelectedComponents() {
		return fModel.getSelectedComponents();
	}

	@Override
	public Collection<IRteComponent> getUsedComponents() {
		return fModel.getUsedComponents();
	}

	@Override
	public Map<String, ICpPackInfo> getUsedPackInfos() {
		return fModel.getUsedPackInfos();
	}

	@Override
	public Collection<? extends IRteDependencyItem> getDependencyItems() {
		return fModel.getDependencyItems();
	}


	//@Override
	public void updatePackFilterInfo() {
		ICpPackFilterInfo packFilterInfo = fRtePackCollection.createPackFilterInfo();
		ICpConfigurationInfo confInfo = getConfigurationInfo();
		packFilterInfo.setParent(confInfo);
		confInfo.replaceChild(packFilterInfo);
	}

	@Override
	public void selectPack(IRtePack pack, boolean select) {
		if(pack != null) {
			pack.setSelected(select);
			IRtePackFamily family = pack.getFamily();
			if(family != null) {
				family.updateVersionMatchMode();
			}
			emitPackFilterModified();
		}
	}

	@Override
	public void setVesrionMatchMode(IRtePackFamily packFamily, EVersionMatchMode mode) {
		if(packFamily != null) {
			packFamily.setVersionMatchMode(mode);
			emitPackFilterModified();
		}
	}
	

	@Override
	public boolean isUseAllLatestPacks() {
		return fRtePackCollection.isUseAllLatestPacks();
	}

	@Override
	public void setUseAllLatestPacks(boolean bUseLatest) {
		fRtePackCollection.setUseAllLatestPacks(bUseLatest);
		emitPackFilterModified();
	}

	@Override
	public IRteDeviceItem getDevices() {
		return fModel.getDevices();
	}
	
}
