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

package com.arm.cmsis.pack.info;

import java.util.Collection;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpItem;
import com.arm.cmsis.pack.data.ICpDebugConfiguration;
import com.arm.cmsis.pack.data.ICpDeviceItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;
import com.arm.cmsis.pack.utils.Utils;

/**
 * Default implementation of ICpDeviceInfo interface
 */
public class CpDeviceInfo extends CpItem implements ICpDeviceInfo {

	protected ICpDeviceItem fDevice = null;
	protected ICpPackInfo   fPackInfo = null;
	protected EEvaluationResult fResolveResult = EEvaluationResult.UNDEFINED; 
	
	/**
	 * Constructs CpDeviceInfo from supplied ICpDeviceItem 
	 * @param parent parent ICpItem
	 * @param device IRteDeviceItem to construct from 
	 */
	public CpDeviceInfo(ICpItem parent, IRteDeviceItem device) {
		super(parent, CmsisConstants.DEVICE_TAG);
		setRteDevice(device);
	}

	
	/**
	 * Default constructor   
	 * @param parent parent ICpItem
	 */
	public CpDeviceInfo(ICpItem parent) {
		super(parent, CmsisConstants.DEVICE_TAG);
	}

	/**
	 * Constructs CpDeviceInfo from parent and tag
	 * @param parent parent ICpItem
	 * @param tag
	 */
	public CpDeviceInfo(ICpItem parent, String tag) {
		super(parent, tag);
	}

	@Override
	public ICpDeviceItem getDevice() {
		return fDevice;
	}
	

	@Override
	public ICpPack getPack() {
		if(fDevice != null)
			return fDevice.getPack(); 
		if(fPackInfo != null)
			return fPackInfo.getPack();
		return null;
	}


	@Override
	public ICpPackInfo getPackInfo() {
		return fPackInfo;  
	}
	
	
	@Override
	public void setRteDevice(IRteDeviceItem device) {
		if(device != null) {
			setDevice(device.getDevice());
			fName = device.getName();
		} else {
			fDevice = null;
		}
		updateInfo();
	}

	@Override
	public void setDevice(ICpDeviceItem device) {
		fDevice = device;
	}

	@Override
	public String getProcessorName() {
		int i = getDeviceName().indexOf(':');
		if (i >= 0) {
			return getName().substring(i + 1);
		}
		return CmsisConstants.EMPTY_STRING;
	}
	
	
	@Override
	public String getDeviceName() {
		return getName();
	}

	
	@Override
	public void updateInfo() {
		if(fDevice != null) {
			fPackInfo = new CpPackInfo(this, fDevice.getPack());
			replaceChild(fPackInfo);
			if(!attributes().hasAttributes()) {
				attributes().setAttributes(fDevice.getEffectiveAttributes(null));
				String processorName = getProcessorName();
				ICpItem props = fDevice.getEffectiveProperties(processorName);
				if(props != null) {
					ICpItem proc = props.getFirstChild(CmsisConstants.PROCESSOR_TAG);
					if(proc != null) {
						attributes().mergeAttributes(proc.attributes());
					}
					if(processorName != null && !processorName.isEmpty())
						attributes().setAttribute(CmsisConstants.PNAME, processorName);
				}
				String url = fDevice.getUrl();
				if(url != null && !url.isEmpty()) {
					ICpItem urlItem = new CpItem(this, CmsisConstants.URL);
					urlItem.setText(url);
					replaceChild(urlItem);
				}
			}
		} else {
			if(fPackInfo != null)
				fPackInfo.setPack(null);
		}
	}


	@Override
	public void addChild(ICpItem item) {
		if(item instanceof ICpPackInfo) {
			fPackInfo = (ICpPackInfo)item;
		} 
		super.addChild(item);
	}

	
	@Override
	public String getName() {
		if(fName == null || fName.isEmpty())
			fName = getDeviceName(attributes());
		return fName;
	}
	
	@Override
	public String getVersion() {
		return CmsisConstants.EMPTY_STRING;
	}

	@Override
	public String getDescription() {
		ICpItem effectiveProps = getEffectiveProperties();
		if(effectiveProps != null)
			return effectiveProps.getDescription();
		return CmsisConstants.EMPTY_STRING;
	}

	
	@Override
	public String getUrl() {
		if(fDevice != null)
			return fDevice.getUrl();
		return super.getUrl();
	}

	@Override
	public EEvaluationResult getEvaluationResult() {
		return fResolveResult;
	}

	@Override
	public void setEvaluationResult(EEvaluationResult result) {
		fResolveResult = result;
	}

	@Override
	public ICpItem getEffectiveProperties() {
		if(fDevice != null)
			return fDevice.getEffectiveProperties(getProcessorName());
		return null;
	}
	
	@Override
	public ICpDebugConfiguration getDebugConfiguration() {
		if(fDevice != null)
			return fDevice.getDebugConfiguration(getProcessorName());
		return null;
	}

	
	@Override
	public String getSummary() {
		String summary = "ARM "  + getAttribute(CmsisConstants.DCORE) + ", " + getClockSummary();  //$NON-NLS-1$//$NON-NLS-2$
		String memory = getMemorySummary();
		if(!memory.isEmpty()) {
			summary += ", " + memory; //$NON-NLS-1$
		}
		return summary;
	}


	@Override
	public String getClockSummary() {
		return Utils.getScaledClockFrequency(getAttribute(CmsisConstants.DCLOCK));
	}


	@Override
	public String getMemorySummary() {
		ICpItem effectiveProps = getEffectiveProperties();
		if(effectiveProps == null)
			return CmsisConstants.EMPTY_STRING;
		
		Collection<ICpItem> mems = effectiveProps.getChildren(CmsisConstants.MEMORY_TAG);
		if(mems == null || mems.isEmpty())
			return CmsisConstants.EMPTY_STRING;
			
		long ramSize = 0;
		long romSize = 0;
		for(ICpItem m : mems) {
			long size = m.attributes().getAttributeAsLong(CmsisConstants.SIZE, 0);
			if(size == 0) 
				continue;
			String id = m.getId();
			if(id.startsWith(CmsisConstants.IRAM))
				ramSize += size;
			else if(id.startsWith(CmsisConstants.IROM))
				romSize += size;
		}

		String summary = CmsisConstants.EMPTY_STRING;
		if (ramSize > 0) {
			summary += Utils.getMemorySizeString(ramSize) + " RAM"; //$NON-NLS-1$
		}
		if (romSize > 0) {
			if (!summary.isEmpty())
				summary += ", "; //$NON-NLS-1$
			summary += Utils.getMemorySizeString(romSize) + " ROM"; //$NON-NLS-1$
		}
		return summary;
	}

	@Override
	public Collection<ICpItem> getBooks() {
		ICpItem effectiveProps = getEffectiveProperties();
		if(effectiveProps == null)
			return null;
		return effectiveProps.getBooks();
	}
}
