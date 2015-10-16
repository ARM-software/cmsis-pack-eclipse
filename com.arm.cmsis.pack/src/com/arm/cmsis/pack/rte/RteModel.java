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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackManager;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpConditionContext;
import com.arm.cmsis.pack.data.CpPackFilter;
import com.arm.cmsis.pack.data.ICpComponent;
import com.arm.cmsis.pack.data.ICpConditionContext;
import com.arm.cmsis.pack.data.ICpDeviceItem;
import com.arm.cmsis.pack.data.ICpFile;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPackCollection;
import com.arm.cmsis.pack.data.ICpPackFilter;
import com.arm.cmsis.pack.data.ICpTaxonomy;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.enums.EVersionMatchMode;
import com.arm.cmsis.pack.info.CpComponentInfo;
import com.arm.cmsis.pack.info.CpFileInfo;
import com.arm.cmsis.pack.info.ICpComponentInfo;
import com.arm.cmsis.pack.info.ICpConfigurationInfo;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.info.ICpFileInfo;
import com.arm.cmsis.pack.info.ICpPackFilterInfo;
import com.arm.cmsis.pack.info.ICpPackInfo;
import com.arm.cmsis.pack.rte.components.IRteComponent;
import com.arm.cmsis.pack.rte.components.IRteComponentGroup;
import com.arm.cmsis.pack.rte.components.IRteComponentItem;
import com.arm.cmsis.pack.rte.components.RteComponentRoot;
import com.arm.cmsis.pack.rte.components.RteMoreClass;
import com.arm.cmsis.pack.rte.components.RteSelectedDeviceClass;
import com.arm.cmsis.pack.rte.dependencies.IRteDependencyItem;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;
import com.arm.cmsis.pack.rte.devices.RteDeviceItem;

/**
 * Default implementation of IRteModel interface 
 *    
 */
public class RteModel implements IRteModel {

	// object to store/load configuration meta data 
	protected ICpConfigurationInfo fConfigurationInfo = null;
	// filtered Packs
	protected ICpPackCollection   fAllPacks = null;
	protected Collection<ICpPack> fFilteredPacks = null;
	protected ICpPackFilter 	  fPackFilter = null;  
	private Map<String, ICpPackInfo> fUsedPackInfos = null;
	
	// selected device 
	protected ICpDeviceInfo      fDeviceInfo = null;
	// selected toolchain
	protected ICpItem		     fToolchainInfo = null;
	
	// component filter 
	protected ICpConditionContext  fComponentFilter = null;

	// filtered components tree 
	protected RteComponentRoot 	fComponentRoot = null;
	// filtered device tree 
	protected IRteDeviceItem 	fRteDevices = null;
	
	// engine to evaluate/resolve component dependencies 
	protected IRteDependencySolver fDependencySolver = null;
	

	/**
	 *  Default constructor
	 */
	public RteModel() {
		fDependencySolver = new RteDependencySolver(this);
	}

	@Override
	public void clear() {
		fAllPacks = null;
		fRteDevices = null;
		fComponentRoot = null;
		fPackFilter = null;
		fFilteredPacks = null;
		fDeviceInfo = null;
		fToolchainInfo = null;
		fConfigurationInfo = null;
	}

	
	@Override
	public ICpConfigurationInfo getConfigurationInfo() {
		return fConfigurationInfo;
	}

	@Override
	public void setConfigurationInfo(ICpConfigurationInfo info) {
		fConfigurationInfo = info;
		if(fConfigurationInfo == null) {
			clear();
			return;
		}
		fDeviceInfo = info.getDeviceInfo();
		fToolchainInfo = info.getToolChainInfo();
		fPackFilter = new CpPackFilter(info.createPackFilter());

		update();
	}
	
	@Override
	public void update(){
		fRteDevices = null;
		collectPacks();
		filterPacks();
		resolveFilterPacks(); 
		getDevices(); // creates device tree
		resolveDevice(); 
		updateComponentFilter();
		collectComponents();
		resolveComponents();
		updateComponentInfos();
	}
	
	
	protected boolean resolveFilterPacks() {
		if(fConfigurationInfo == null)
			return false;
		
		boolean allResolved = true;
		ICpPackFilterInfo packsItem = fConfigurationInfo.getPackFilterInfo();
		if(packsItem == null) 
			return allResolved;

		Collection<? extends ICpItem> packInfos = packsItem.getChildren();
		if(packInfos == null)
			return allResolved;

		if(fAllPacks == null)
			return false;
		
		for(ICpItem item : packInfos) {
			if(!(item instanceof ICpPackInfo))
				continue;
			ICpPackInfo packInfo = (ICpPackInfo)item;
			EVersionMatchMode mode = packInfo.getVersionMatchMode();
			ICpPack pack = null;
			switch(mode){
			case FIXED:
				pack = fAllPacks.getPack(packInfo.getId());
				break;
			case EXCLUDED:
			case LATEST:
				pack = fAllPacks.getPack(packInfo.getPackFamilyId()); 
				break;
			}
			packInfo.setPack(pack);
			if(pack == null && mode !=EVersionMatchMode.EXCLUDED) {
				allResolved = false;
			}
		}		
		return allResolved;
	}
	
	private ICpPack resolvePack(ICpPackInfo pi) {
		ICpPack pack = pi.getPack();
		if(pack != null)
			return pack;
		if(fAllPacks == null)
			return null;
		pack = fAllPacks.getPack(pi.getId());
		if(pack != null)
			pi.setPack(pack);

		return pack;
	}
	
	
	protected boolean resolveDevice() {
		if(fDeviceInfo == null)
			return false;
		fDeviceInfo.setRteDevice(null);
		IRteDeviceItem rteDevice = getDevices().findItem(fDeviceInfo.attributes());
		fDeviceInfo.setRteDevice(rteDevice);
		ICpPackInfo packInfo = fDeviceInfo.getPackInfo();
		if(rteDevice == null) {
			resolvePack(packInfo);
		}
		ICpDeviceItem device = fDeviceInfo.getDevice(); 
		EEvaluationResult res = EEvaluationResult.FULFILLED;
		if(device == null) {
			if(packInfo.getPack() == null) {
				res = EEvaluationResult.FAILED;
			} else {
				res = EEvaluationResult.UNAVAILABLE_PACK;
			}
		}
		fDeviceInfo.setEvaluationResult(res);
		return device != null;
	}
	
	
	
	@Override
	public void updateComponentInfos() {
	
		if(fConfigurationInfo == null)
			return;
		
		ICpItem apiInfos = fConfigurationInfo.getApisItem();
		apiInfos.clear();
		ICpItem componentInfos = fConfigurationInfo.getComponentsItem();
		componentInfos.clear();

		fUsedPackInfos = new HashMap<String, ICpPackInfo>();
		ICpPackInfo devicePackInfo = fDeviceInfo.getPackInfo();
		addUsedPackInfo(devicePackInfo.getPackInfo());

		Map<ICpComponent, EVersionMatchMode> selectedApis = new HashMap<ICpComponent, EVersionMatchMode>();
		Collection<IRteComponent> selectedComponents = getSelectedComponents();
		for(IRteComponent component : selectedComponents){
			ICpComponent c = component.getActiveCpComponent();
			if(c == null)
				continue;
			ICpComponentInfo ci = null;
			if(c instanceof ICpComponentInfo) {
				ci = (ICpComponentInfo)c;
				ci.setParent(componentInfos);
			} else { 
				ci = new CpComponentInfo(componentInfos, c, component.getSelectedCount());
				collectFilteredFiles(ci);
			}
			EVersionMatchMode versionMode = component.isUseLatestVersion() ? 
					EVersionMatchMode.LATEST: EVersionMatchMode.FIXED; 
			ci.setVersionMatchMode(versionMode);

			componentInfos.addChild(ci);
			component.setActiveComponentInfo(ci);
			addUsedPackInfo(ci.getPackInfo());

			IRteComponentGroup g = component.getParentGroup();
			//	 collect used APIs  
			ICpComponent api = g.getApi();
			if(api != null) {
				EVersionMatchMode vmm = EVersionMatchMode.LATEST;
				if(!g.isUseLatestVersion())
					vmm = EVersionMatchMode.FIXED;
				selectedApis.put(api, vmm);
			}
		}
		
		for(Entry<ICpComponent, EVersionMatchMode> e : selectedApis.entrySet()){
			ICpComponent api = e.getKey();
			EVersionMatchMode versionMode = e.getValue();
			ICpComponentInfo ai = null;
			if(api instanceof ICpComponentInfo) {
				ai = (ICpComponentInfo)api;
				ai.setParent(apiInfos);
			} else {
				ai = new CpComponentInfo(apiInfos, api, 1);
				collectFilteredFiles(ai);
				ICpPackInfo pi = ai.getPackInfo();
				if(!fUsedPackInfos.containsKey(pi.getId()))
					fUsedPackInfos.put(pi.getId(), pi);
			}
			ai.setVersionMatchMode(versionMode);
			apiInfos.addChild(ai);
			addUsedPackInfo(ai.getPackInfo());
		}
	}
	
	protected void addUsedPackInfo(ICpPackInfo packInfo) {
		String packId = packInfo.getId();
		if(fPackFilter.isFixed(packId))
			packInfo.setVersionMatchMode(EVersionMatchMode.FIXED);
		else 
			packInfo.setVersionMatchMode(EVersionMatchMode.LATEST);
		
		if(!fUsedPackInfos.containsKey(packId))
			fUsedPackInfos.put(packId, packInfo);
	}

	
	void collectFilteredFiles(ICpComponentInfo ci){
		ICpComponent c = ci.getComponent();
		if(c == null)
			return;
		Collection<? extends ICpItem> allFiles = c.getGrandChildren(CmsisConstants.FILES_TAG);
		Collection<ICpItem> filtered = fComponentFilter.filterItems(allFiles); // filter by device & toolchain 
		filtered = fDependencySolver.filterItems(filtered); // filter by selection
		for(ICpItem item : filtered) {
			if(item instanceof ICpFile) {
				ICpFile f = (ICpFile)item;
				ICpFileInfo fi = ci.getFileInfo(f);
				if(fi != null) {
					fi.setFile(f); // do not change stored attributes
				} else {
					fi = new CpFileInfo(ci, f); // new apply or resolved component has additional file(s) 
					ci.addChild(fi);
				}
			}
		}
	}
	
	
	void resolveComponents() {
		if(fConfigurationInfo == null) {
			return;
		}
		// resolve components and select them
		EEvaluationResult result = EEvaluationResult.FULFILLED; 
		EEvaluationResult res = resolveComponents(fConfigurationInfo.getGrandChildren(CmsisConstants.COMPONENTS_TAG));
		if(res.ordinal() < result.ordinal())
			result = res;
		res = resolveComponents(fConfigurationInfo.getGrandChildren(CmsisConstants.APIS_TAG));
		if(res.ordinal() < result.ordinal())
			result = res;
		evaluateComponentDependencies();
	}
	
	private EEvaluationResult resolveComponents(Collection<? extends ICpItem> children) {
		EEvaluationResult result = EEvaluationResult.FULFILLED;
		if(children == null || children.isEmpty())
			return result;
		for(ICpItem item : children){
			if(item instanceof ICpComponentInfo) { // skip doc and description items 
				ICpComponentInfo ci = (ICpComponentInfo) item;
				ci.setComponent(null);
				ci.setEvaluationResult(EEvaluationResult.UNDEFINED);
				if(ci.isApi())
					fComponentRoot.addCpItem(ci);
				else
					fComponentRoot.addComponent(ci);
				EEvaluationResult res = ci.getEvaluationResult();
				if(ci.getComponent() == null) {
					ICpPackInfo pi = ci.getPackInfo();
					if(resolvePack(pi) != null ){
						if(fPackFilter.isExcluded(pi.getId()))
							ci.setEvaluationResult(EEvaluationResult.UNAVAILABLE_PACK);
						else 
							ci.setEvaluationResult(EEvaluationResult.UNAVAILABLE);
					}
				}
				if(res.ordinal() < result.ordinal())
					result = res;
			}
		}
		return result;
	}

	
	public void collectPacks() {
		fAllPacks = null;
		ICpPackManager pm  = CpPlugIn.getPackManager();
		if(pm != null)
			fAllPacks = pm.getPacks();
	}
	
	public void filterPacks() {
		fFilteredPacks = null;
		if(fAllPacks != null) {
			fPackFilter.setLatestPackIDs(fAllPacks.getLatestPackIDs());
			fFilteredPacks = fAllPacks.getFilteredPacks(fPackFilter);
		}
	}

	@Override
	public Map<String, ICpPackInfo> getUsedPackInfos() {
		return fUsedPackInfos;
	}
	
	
	@Override
	public ICpPackFilter getPackFilter() {
		return fPackFilter;
	}

	@Override
	public boolean setPackFilter(ICpPackFilter filter) {
		if(filter.equals(fPackFilter))
			return false;
		fPackFilter = new CpPackFilter(filter);
		return true;
	}
	
	@Override
	public ICpDeviceInfo getDeviceInfo() {
		return fDeviceInfo;
	}

	
	@Override
	public void setDeviceInfo(ICpDeviceInfo deviceInfo) {
		fDeviceInfo = deviceInfo;
		fConfigurationInfo.replaceChild(deviceInfo);
	}

	@Override
	public ICpDeviceItem getDevice() {
		if(fDeviceInfo != null)
			return fDeviceInfo.getDevice();
		return null;
	}
	
	@Override
	public ICpItem getToolchainInfo() {
		return fToolchainInfo;
	}


	@Override
	public IRteDeviceItem getDevices(){
		if(fRteDevices == null){
			fRteDevices = RteDeviceItem.createTree(fFilteredPacks);
		}
		return fRteDevices;
	}

	@Override
	public IRteComponentItem getComponents() {
		return fComponentRoot;
	}

	/**
	 *  Updates component filter by setting new device information
	 */
	protected void updateComponentFilter() {
		fComponentFilter = new CpConditionContext();
		if(fDeviceInfo != null) {
			fComponentFilter.setAttributes(fDeviceInfo.attributes().getAttributesAsMap());
			fComponentFilter.removeAttribute(CmsisConstants.URL); // this attribute is not needed for filtering
		}
		if(fToolchainInfo != null) {
			fComponentFilter.mergeAttributes(fToolchainInfo.attributes());
		}
		fComponentFilter.resetResult();
	}

	/**
	 *  Builds filtered components tree 
	 */
	protected void collectComponents() {
		fComponentRoot = new RteComponentRoot(); 

		// add  artificial class items:
		// selected device  
		RteSelectedDeviceClass devClass = new RteSelectedDeviceClass(fComponentRoot, fDeviceInfo);
		fComponentRoot.addChild(devClass);

		// create component tree
		if(fFilteredPacks == null || fFilteredPacks.isEmpty())
			return;
		//  device pack has precedence, always collect its components, APIs and taxonomy first
		ICpPack devicePack = null; 	 
		ICpDeviceItem device = fDeviceInfo.getDevice();
		if(device != null)
			devicePack = device.getPack(); 
		
		Collection<? extends ICpItem> children;
		// first add components
		if(devicePack != null) {
			children = devicePack.getGrandChildren(CmsisConstants.COMPONENTS_TAG);
			collectComponents(children);
		}
		for(ICpPack pack : fFilteredPacks ){
			if(pack == devicePack)
				continue;
			children = pack.getGrandChildren(CmsisConstants.COMPONENTS_TAG);
			collectComponents(children);
		}
		// then add APIs and taxonomy items
		if(devicePack != null) {
			children = devicePack.getGrandChildren(CmsisConstants.APIS_TAG);
			collectCpItems(children);
			children = devicePack.getGrandChildren(CmsisConstants.TAXONOMY_TAG);
			collectCpItems(children);
		}
		for(ICpPack pack : fFilteredPacks ){
			if(pack == devicePack)
				continue;
			children = pack.getGrandChildren(CmsisConstants.APIS_TAG);
			collectCpItems(children);
			
			children = pack.getGrandChildren(CmsisConstants.TAXONOMY_TAG);
			collectCpItems(children);
		}
		
		// "more.." when filter is effect
		if(!fPackFilter.isUseAllLatestPacks()) {
			RteMoreClass more = new RteMoreClass(fComponentRoot);
			fComponentRoot.addChild(more);
		}
	}
	
	/**
	 * Adds collection members to the hierarchy  
	 * @param children
	 */
	protected void collectCpItems( Collection<? extends ICpItem> children) {
		if(children == null || children.isEmpty())
			return;
		for(ICpItem item : children){
			if(item instanceof ICpTaxonomy || item instanceof ICpComponent) {
				fComponentRoot.addCpItem(item); 
			}
		}
	}

	/**
	 * Collect components from given pack 
	 * @param pack
	 */
	protected void collectComponents(Collection<? extends ICpItem> children) {
		if(children == null || children.isEmpty())
			return;
		for(ICpItem item : children){
			if(item.getTag().equals(CmsisConstants.BUNDLE_TAG)){
				// insert bundle implicitly since its components can be filtered out 
				collectComponents(item.getChildren());
			} else if(item instanceof ICpComponent) { // skip doc and description items 
				ICpComponent c = (ICpComponent) item;
				EEvaluationResult res = c.evaluate(fComponentFilter);
				if(res.ordinal() < EEvaluationResult.FULFILLED.ordinal()) {
					continue; // filtered out
				}
				fComponentRoot.addComponent(c);
			}
		}
	}

	@Override
	public Collection<IRteComponent> getSelectedComponents() {
		if(fComponentRoot != null) {
			return fComponentRoot.getSelectedComponents(new LinkedHashSet<IRteComponent>());
		}
		return null;
	}

	@Override
	public Collection<IRteComponent> getUsedComponents() {
		if(fComponentRoot != null) {
			return fComponentRoot.getUsedComponents(new LinkedHashSet<IRteComponent>());
		}
		return null;
	}

	
	@Override
	public EEvaluationResult evaluateComponentDependencies() {
		return fDependencySolver.evaluateDependencies();

	}
	
	@Override
	public EEvaluationResult resolveComponentDependencies() {
		return fDependencySolver.resolveDependencies();
	}

	@Override
	public EEvaluationResult getEvaluationResult(IRteComponentItem item) {
		return fDependencySolver.getEvaluationResult(item);
	}

	
	@Override
	public Collection<? extends IRteDependencyItem> getDependencyItems() {
		return fDependencySolver.getDependencyItems(); 
	}

	@Override
	public EEvaluationResult getEvaluationResult() {
		return fDependencySolver.getEvaluationResult();
	}

	@Override
	public void setEvaluationResult(EEvaluationResult result) {
		fDependencySolver.setEvaluationResult(result);
	}

	@Override
	public void selectComponent(IRteComponent component, int nInstances) {
		if(component != null) {
			component.setSelected(nInstances);
		}
	}
	
}
