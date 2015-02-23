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

package com.arm.cmsis.pack.rte;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackManager;
import com.arm.cmsis.pack.data.CpConditionContext;
import com.arm.cmsis.pack.data.CpItem;
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
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.events.IRteEventProxy;
import com.arm.cmsis.pack.info.CpComponentInfo;
import com.arm.cmsis.pack.info.CpConfigurationInfo;
import com.arm.cmsis.pack.info.CpFileInfo;
import com.arm.cmsis.pack.info.CpPackInfo;
import com.arm.cmsis.pack.info.ICpComponentInfo;
import com.arm.cmsis.pack.info.ICpConfigurationInfo;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.info.ICpFileInfo;
import com.arm.cmsis.pack.info.ICpPackInfo;
import com.arm.cmsis.pack.rte.components.IRteComponent;
import com.arm.cmsis.pack.rte.components.IRteComponentGroup;
import com.arm.cmsis.pack.rte.components.IRteComponentItem;
import com.arm.cmsis.pack.rte.components.RteComponentRoot;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;
import com.arm.cmsis.pack.rte.devices.RteDeviceItem;

/**
 * Default implementation of IRteConfiguration interface 
 *    
 */
public class RteConfiguration implements IRteConfiguration {

	// object to store/load configuration meta data 
	protected ICpConfigurationInfo fConfigurationInfo = null;
	
	// filtered Packs
	protected ICpPackFilter 	  fPackFilter = null;  
	protected Collection<ICpPack> fFilteredPacks = null;

	
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

	// instance data -> project 
	// private IRteProjectData
	
	// event proxy to notify GUI
	protected IRteEventProxy       fRteEventProxy = null;
	
	protected boolean tbResolvingComponents = false;
	
		

	/**
	 *  Default constructor
	 */
	public RteConfiguration() {
		// default uses "use the latest available components" filter
		fPackFilter = new CpPackFilter();
		fDependencySolver = new RteDependencySolver(this);
	}

	
	@Override
	public IRteEventProxy getRteEventProxy() {
		return fRteEventProxy;
	}

	@Override
	public void setRteEventProxy(IRteEventProxy rteEventProxy) {
		fRteEventProxy = rteEventProxy;
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
		ICpDeviceInfo deviceInfo = info.getDeviceInfo();
		ICpItem ti = info.getToolchainInfo(); 
		setFilterAttributes(deviceInfo, ti);
		
		// resolve components and select them
		EEvaluationResult result = EEvaluationResult.FULFILLED; 
		EEvaluationResult res = resolveComponents(info.getChildren("components"));
		if(res.ordinal() < result.ordinal())
			result = res;
		res = resolveComponents(info.getChildren("apis"));
		if(res.ordinal() < result.ordinal())
			result = res;
		evaluateDependencies();
	}

	private EEvaluationResult resolveComponents(Collection<? extends ICpItem> children) {
		EEvaluationResult result = EEvaluationResult.FULFILLED;
		if(children == null || children.isEmpty())
			return result;
		for(ICpItem item : children){
			if(item instanceof ICpComponentInfo) { // skip doc and description items 
				ICpComponentInfo ci = (ICpComponentInfo) item;
				if(ci.isApi())
					fComponentRoot.addCpItem(ci);
				else
					fComponentRoot.addComponent(ci);
				EEvaluationResult res = ci.getEvaluationResult();
				if(res.ordinal() < result.ordinal())
					result = res;
			}
		}
		return result;
	}



	@Override
	public void apply() {
		fConfigurationInfo = new CpConfigurationInfo();
		// store pack filter
		ICpItem packs = new CpItem(fConfigurationInfo, "packages");
		fConfigurationInfo.addChild(packs);
		Set<ICpPack> selectedPacks = new HashSet<ICpPack>();
		// TODO: add filtered packages 
		// add device
		fConfigurationInfo.addChild(fDeviceInfo);
		// add toolchain
		fConfigurationInfo.addChild(fToolchainInfo);
		ICpItem apiInfos = new CpItem(fConfigurationInfo, "apis");
		fConfigurationInfo.addChild(apiInfos);

		ICpItem componentInfos = new CpItem(fConfigurationInfo, "components");
		fConfigurationInfo.addChild(componentInfos);
		
		Set<ICpComponent> selectedApis = new HashSet<ICpComponent>();

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
				Collection<ICpFile> filteredFiles = c.getFilteredFiles(fComponentFilter);
				for(ICpFile f : filteredFiles) {
					ICpFileInfo fi = new CpFileInfo(ci, f);
					ci.addChild(fi);
				}
				ICpPack pack = c.getPack();
				selectedPacks.add(pack);
			}
			componentInfos.addChild(ci);

			IRteComponentGroup g = component.getParentGroup();
			//	 collect used APIs  
			ICpComponent api = g.getApi();
			if(api != null) {
				selectedApis.add(api);
			}
		}
		
		for(ICpComponent api : selectedApis){
			ICpComponentInfo ai = null;
			if(api instanceof ICpComponentInfo) {
				ai = (ICpComponentInfo)api;
				ai.setParent(apiInfos);
			} else {
				ai = new CpComponentInfo(apiInfos, api, 1);
				Collection<ICpFile> filteredFiles = api.getFilteredFiles(fComponentFilter);
				for(ICpFile f : filteredFiles) {
					ICpFileInfo fi = new CpFileInfo(ai, f);
					ai.addChild(fi);
				}
				ICpPack pack = api.getPack();
				if(pack != null)
					selectedPacks.add(pack);
			}
			apiInfos.addChild(ai);
		}
		
		for(ICpPack pack: selectedPacks){
			ICpPackInfo pi = new CpPackInfo(packs, pack);
			packs.addChild(pi);
		}
		
		emitEvent("com.arm.comsis.pack.rte.configuration.saved", this);
	}

	@Override
	public void clear() {
		fRteDevices = null;
		fFilteredPacks = null;
	}
	
	public void collectPacks() {
		fFilteredPacks = null;
		ICpPackCollection packs = null;
		ICpPackManager pm  = CpPlugIn.getDefault().getPackManager();
		if(pm != null)
			packs = pm.getPacks();
		if(packs != null)
			fFilteredPacks = packs.getFilteredPacks(fPackFilter);
	}
	
	
	/**
	 * Sets pack filter for the configuration
	 * @param filter ICpPackFilter to set
	 */
	void setPackFilter(ICpPackFilter filter) {
		clear();
		fPackFilter = filter;
	}
	
	@Override
	public ICpPackFilter getPackFilter() {
		return fPackFilter;
	}


	@Override
	public ICpDeviceInfo getDeviceInfo() {
		return fDeviceInfo;
	}

	
	@Override
	public void setDeviceInfo(ICpDeviceInfo deviceInfo) {
		fDeviceInfo = deviceInfo;
		updateConditionContext();
		collectPackData();
		emitModifyEvent();
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
	public void setToolchainInfo(ICpItem toolchainInfo) {
		fToolchainInfo = toolchainInfo;
		updateConditionContext();
		collectPackData();
		emitModifyEvent();
	}


	@Override
	public void setFilterAttributes(ICpDeviceInfo deviceInfo, ICpItem toolchainInfo) {
		fDeviceInfo = deviceInfo;
		fToolchainInfo = toolchainInfo;
		updateConditionContext();
		collectPackData();
		emitModifyEvent();
	}


	/**
	 * Returns hierarchical collection of devices available for this target 
	 * @return root of device tree as IRteDeviceItem  
	 */
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
	protected void updateConditionContext() {
		fComponentFilter = new CpConditionContext();
		collectPacks();
		getDevices(); 
		if(fDeviceInfo != null) {
			fComponentFilter.setAttributes(fDeviceInfo.attributes().getAttributesAsMap());
			if(fDeviceInfo.getDevice() == null) {
				IRteDeviceItem rteDevice = getDevices().findItem(fDeviceInfo.attributes());
				ICpDeviceItem cpDevice = null;  
				if(rteDevice != null) {
					cpDevice = rteDevice.getDevice();
				} else {
					// TODO set error state: device not found
				}
				fDeviceInfo.setDevice(cpDevice);
			}
		}
		if(fToolchainInfo != null) {
			fComponentFilter.mergeAttributes(fToolchainInfo.attributes());
		}
		
		fComponentFilter.resetResult();
	}


	/**
	 *  Collects filtered packs and and components from the filtered packs   
	 */
	protected void collectPackData() {
		collectPacks();
		collectComponents();
	}

	/**
	 *  Builds filtered components tree 
	 */
	protected void collectComponents() {
		fComponentRoot = new RteComponentRoot(); 
		// create component tree
		if(fFilteredPacks == null || fFilteredPacks.isEmpty())
			return;
		// first add components
		for(ICpPack pack : fFilteredPacks ){
			Collection<? extends ICpItem> children = pack.getChildren("components");
			collectComponents(children);
		}
		// then add APIs and taxonomy items
		for(ICpPack pack : fFilteredPacks ){
			Collection<? extends ICpItem> children = pack.getChildren("apis");
			collectCpItems(children);
			
			children = pack.getChildren("taxonomy");
			collectCpItems(children);
		}
	}
	
	/**
	 * @param children
	 */
	protected void collectCpItems( Collection<? extends ICpItem> children) {
		if(children == null || children.isEmpty())
			return;
		for(ICpItem item : children){
			if(item instanceof ICpTaxonomy || item instanceof ICpComponent) {
				fComponentRoot.addCpItem(item); // add directly to group
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
			if(item.getTag().equals("bundle")){
				// insert bundle implicitly since its components can be filtered out 
				collectComponents(item.getChildren());
			} else if(item instanceof ICpComponent) { // skip doc and description items 
				ICpComponent c = (ICpComponent) item;
				EEvaluationResult res = c.evaluate(fComponentFilter);
				if(res.ordinal() < EEvaluationResult.FULFILLED.ordinal())
					continue; // filtered out
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
	public void selectComponent(IRteComponent component, int nInstances) {
		if(component != null) {
			component.setSelected(nInstances);
			evaluateDependencies();
		}
	}

	
	@Override
	public void selectActiveChild(IRteComponentItem item, String childName) {
		if(item != null) {
			item.setActiveChild(childName);
			evaluateDependencies();
		}
	}
	

	@Override
	public void selectActiveVariant(IRteComponentItem item, String variant) {
		if(item != null) {
			item.setActiveVariant(variant);
			evaluateDependencies();
		}
	}

	@Override
	public void selectActiveVendor(IRteComponentItem item, String vendor) {
		if(item != null) {
			item.setActiveVendor(vendor);
			evaluateDependencies();
		}
	}


	@Override
	public void selectActiveVersion(IRteComponentItem item, String version) {
		if(item != null) {
			item.setActiveVersion(version);
			evaluateDependencies();
		}
	}


	protected void emitEvent(final String topic, Object data) {
		if(fRteEventProxy != null) {
			fRteEventProxy.processRteEvent(new RteEvent(topic, data));
		}
	}
	
	protected void emitModifyEvent() {
		emitEvent(RteEvent.CONFIGURATION_MODIFIED, this);
	}

	
	@Override
	public EEvaluationResult evaluateDependencies() {
		EEvaluationResult result = fDependencySolver.evaluateDependencies();
		if(!tbResolvingComponents)
			emitModifyEvent(); 
		return result;

	}

	@Override
	public EEvaluationResult getEvaluationResult(IRteComponentItem item) {
		return fDependencySolver.getEvaluationResult(item);
	}

	
	@Override
	public EEvaluationResult resolveDependencies() {
		EEvaluationResult res = EEvaluationResult.UNDEFINED; 
		if(tbResolvingComponents) 
			return res;
		tbResolvingComponents = true;
		res = fDependencySolver.resolveDependencies();
		emitModifyEvent();
		tbResolvingComponents = false;
		return res;
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
	
	
}
