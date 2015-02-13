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

package com.arm.cmsis.pack.ui;

import java.util.Collection;

import org.eclipse.core.runtime.ListenerList;

import com.arm.cmsis.pack.data.ICpDeviceItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPackFilter;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.events.IRteConfigurationProxy;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.events.IRteEventListener;
import com.arm.cmsis.pack.events.IRteEventProxy;
import com.arm.cmsis.pack.info.ICpConfigurationInfo;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.rte.IRteConfiguration;
import com.arm.cmsis.pack.rte.IRteDependencyItem;
import com.arm.cmsis.pack.rte.components.IRteComponent;
import com.arm.cmsis.pack.rte.components.IRteComponentItem;

/**
 *
 */
public class RteConfigurationProxy implements IRteConfigurationProxy {

	/**
	 * "Real" configuration: can be RteConfiguration or another proxy 
	 */
	private IRteConfiguration fConfiguration = null; 
	private ListenerList fRteListeners = new ListenerList();
	
	/**
	 *  Constructs proxy for given configuration object 
	 */
	public RteConfigurationProxy(IRteConfiguration configuration) {
		fConfiguration = configuration;
		if(fConfiguration != null)
			fConfiguration.setRteEventProxy(this);
	}


	@Override
	public void setRteEventProxy(IRteEventProxy rteEventProxy) {
		// TODO: provide a layer for tier proxies 
		if(fConfiguration != null)
			fConfiguration.setRteEventProxy(rteEventProxy);
	}


	@Override
	public IRteEventProxy getRteEventProxy() {
		if(fConfiguration != null)
			return fConfiguration.getRteEventProxy();
		return null;
	}

	@Override
	public void clear() {
		if(fConfiguration != null)
			fConfiguration.clear();
	}


	@Override
	public ICpPackFilter getPackFilter() {
		if(fConfiguration != null)
			return fConfiguration.getPackFilter();
		return null;
	}


	@Override
	public ICpDeviceItem getDevice() {
		if(fConfiguration != null)
			return fConfiguration.getDevice();
		return null;
	}


	@Override
	public ICpDeviceInfo getDeviceInfo() {
		if(fConfiguration != null)
			return fConfiguration.getDeviceInfo();
		return null; 
	}


	@Override
	public void setDeviceInfo(ICpDeviceInfo deviceInfo) {
		if(fConfiguration != null)
			fConfiguration.setDeviceInfo(deviceInfo);
	}


	@Override
	public IRteComponentItem getComponents() {
		if(fConfiguration != null)
			return fConfiguration.getComponents();
		return null;
	}


	@Override
	public EEvaluationResult evaluateDependencies() {
		if(fConfiguration != null)
			return fConfiguration.evaluateDependencies();
		return EEvaluationResult.UNDEFINED;
	}

	@Override
	public Collection<IRteComponent> getSelectedComponents() {
		if(fConfiguration != null)
			return fConfiguration.getSelectedComponents();
		return null;
	}


	@Override
	public Collection<IRteComponent> getUsedComponents() {
		if(fConfiguration != null)
			return fConfiguration.getUsedComponents();
		return null;
	}


	@Override
	public void selectComponent(IRteComponent component, int nInstances) {
		if(fConfiguration != null)
			fConfiguration.selectComponent(component, nInstances);
	}


	@Override
	public void selectActiveChild(IRteComponentItem item, String childName) {
		if(fConfiguration != null)
			fConfiguration.selectActiveChild(item, childName);
	}


	@Override
	public void selectActiveVariant(IRteComponentItem item, String variant) {
		if(fConfiguration != null)
			fConfiguration.selectActiveVariant(item, variant);
	}


	@Override
	public void selectActiveVendor(IRteComponentItem item, String vendor) {
		if(fConfiguration != null)
			fConfiguration.selectActiveVendor(item, vendor);
	}


	@Override
	public void selectActiveVersion(IRteComponentItem item, String version) {
		if(fConfiguration != null)
			fConfiguration.selectActiveVersion(item, version);
	}


	@Override
	public void processRteEvent(RteEvent event) {
		fireRteEvent(event);
	}

	@Override
	public void addRteEventListener(IRteEventListener listener) {
		fRteListeners.add(listener);
	}


	@Override
	public void removeRteEventListener(IRteEventListener listener) {
		fRteListeners.remove(listener);
	}

	@Override
	public void fireRteEvent(RteEvent event) {
		for (Object obj : fRteListeners.getListeners()) {
			IRteEventListener listener = (IRteEventListener) obj;
			try {
				listener.handleRteEvent(event);
			} catch (Exception ex) {
				removeRteEventListener(listener);
			}
		} 
		
	}

	@Override
	public EEvaluationResult getEvaluationResult(IRteComponentItem item) {
		if(fConfiguration != null)
			return fConfiguration.getEvaluationResult(item);
		return EEvaluationResult.UNDEFINED;
	}


	@Override
	public EEvaluationResult getEvaluationResult() {
		if(fConfiguration != null)
			return fConfiguration.getEvaluationResult();
		return EEvaluationResult.UNDEFINED;
	}


	@Override
	public void setEvaluationResult(EEvaluationResult result) {
		if(fConfiguration != null)		
			fConfiguration.setEvaluationResult(result);
		
	}

	@Override
	public Collection<? extends IRteDependencyItem> getDependencyItems() {
		if(fConfiguration != null)
			return fConfiguration.getDependencyItems();
		return null;
	}


	@Override
	public EEvaluationResult resolveDependencies() {
		if(fConfiguration != null)
			return fConfiguration.resolveDependencies();
		return null;
	}


	@Override
	public ICpConfigurationInfo getConfigurationInfo() {
		if(fConfiguration != null)
			return fConfiguration.getConfigurationInfo();
		return null;
	}

	
	@Override
	public void setConfigurationInfo(ICpConfigurationInfo info) {
		if(fConfiguration != null)
			fConfiguration.setConfigurationInfo(info);
	}


	@Override
	public ICpItem getToolchainInfo() {
		if(fConfiguration != null)
			return fConfiguration.getToolchainInfo();
		return null;
	}


	@Override
	public void setToolchainInfo(ICpItem toolchainInfo) {
		if(fConfiguration != null)
			fConfiguration.setToolchainInfo(toolchainInfo);
	}


	@Override
	public void setFilterAttributes(ICpDeviceInfo deviceInfo, ICpItem toolchainInfo) {
		if(fConfiguration != null)
			fConfiguration.setFilterAttributes(deviceInfo, toolchainInfo);
	}


	@Override
	public void apply() {
		if(fConfiguration != null)
			fConfiguration.apply();
	}
}
