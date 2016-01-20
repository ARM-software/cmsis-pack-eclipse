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

package com.arm.cmsis.pack.rte.devices;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.arm.cmsis.pack.DeviceVendor;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpItem;
import com.arm.cmsis.pack.data.CpPack;
import com.arm.cmsis.pack.data.ICpDeviceItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.enums.EDeviceHierarchyLevel;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.item.CmsisMapItem;
import com.arm.cmsis.pack.utils.AlnumComparator;

/**
 * Default implementation of IRteDeviceItem 
 */
public class RteDeviceItem extends CmsisMapItem<IRteDeviceItem> implements IRteDeviceItem {

	private int fLevel = EDeviceHierarchyLevel.NONE.ordinal();
	private Map<String, ICpDeviceItem> fDevices = null;

	/**
	 * 
	 */
	public RteDeviceItem() {
		fLevel = EDeviceHierarchyLevel.ROOT.ordinal();
		fName = "All Devices"; //$NON-NLS-1$
	}

	/**
	 * @param parent
	 */
	public RteDeviceItem(String name, int level, IRteDeviceItem parent) {
		super(parent);
		fLevel = level;
		fName= name;
	}


	@Override
	protected Map<String, IRteDeviceItem> createMap() {
		// create TreeMap with Alpha-Numeric case-insensitive ascending sorting 
		return new TreeMap<String, IRteDeviceItem>(new AlnumComparator(false, false));
	}

	/**
	 * Creates device tree from list of Packs 
	 * @param packs collection of packs to use 
	 * @return device tree as root IRteDeviceItem 
	 */
	public static IRteDeviceItem createTree(Collection<ICpPack> packs){
		IRteDeviceItem root = new RteDeviceItem(); 
		if(packs == null || packs.isEmpty()) {
			return root;
		}
		for(ICpPack pack : packs) {
			Collection<? extends ICpItem> devices = pack.getGrandChildren(CmsisConstants.DEVICES_TAG);
			if(devices == null) {
				continue;
			}
			for(ICpItem item : devices) {
				if(!(item instanceof ICpDeviceItem)) {
					continue;
				}
				ICpDeviceItem deviceItem = (ICpDeviceItem)item;
				root.addDevice(deviceItem);
			}
		}
		return root;
	}


	@Override
	public int getLevel() {
		return fLevel;
	}

	@Override
	public Collection<ICpDeviceItem> getDevices() {
		if(fDevices != null) {
			return fDevices.values();
		}
		return null;
	}

	@Override
	public ICpDeviceItem getDevice() {
		if(fDevices != null && ! fDevices.isEmpty()) {
			return fDevices.entrySet().iterator().next().getValue();
		}
		return null;
	}


	@Override
	public String getProcessorName() {
		int i = getName().indexOf(':');
		if (i >= 0) {
			return getName().substring(i + 1);
		}
		return CmsisConstants.EMPTY_STRING;
	}

	@Override
	public ICpItem getEffectiveProperties() {
		ICpDeviceItem device = getDevice();
		if(device != null){
			String processorName = getProcessorName();
			return device.getEffectiveProperties(processorName);
		}
		return null;
	}


	@Override
	public boolean isDevice() {
		if(getLevel() < EDeviceHierarchyLevel.DEVICE.ordinal()) {
			return false;
		}
		if(hasChildren()) {
			return false;
		}
		return getDevice() != null;
	}

	@Override
	public void addDevice(ICpDeviceItem item) {
		if(item == null) {
			return;
		}

		EDeviceHierarchyLevel eLevel = item.getLevel();
		int level = eLevel.ordinal();

		if(fLevel == level || fLevel == EDeviceHierarchyLevel.PROCESSOR.ordinal()) {
			ICpPack pack = item.getPack();
			String packId = pack.getId();
			if(fDevices == null) {
				fDevices = new TreeMap<String, ICpDeviceItem>(new AlnumComparator());
			}

			ICpDeviceItem device = fDevices.get(packId);
			if(device == null) {
				fDevices.put(packId,item);
			}
			if(fLevel == EDeviceHierarchyLevel.PROCESSOR.ordinal()) {
				return;
			}
			Collection<ICpDeviceItem> subItems = item.getDeviceItems();
			if(subItems != null && !subItems.isEmpty()) {
				for(ICpDeviceItem i : subItems ){
					addDevice(i);
				}
			} else if(level >= EDeviceHierarchyLevel.DEVICE.ordinal() && item.getProcessorCount() > 1) {
				// add processor leaves
				Map<String, ICpItem> processors = item.getProcessors();
				for(Entry<String, ICpItem> e : processors.entrySet()) {
					String procName = item.getName() + ":" + e.getKey(); //$NON-NLS-1$
					addDeviceItem(item, procName, EDeviceHierarchyLevel.PROCESSOR.ordinal());
				}
			}
			return;
		} else if(fLevel == EDeviceHierarchyLevel.ROOT.ordinal()) {
			String vendorName = DeviceVendor.getOfficialVendorName(item.getVendor());
			addDeviceItem(item, vendorName, EDeviceHierarchyLevel.VENDOR.ordinal());
			return;
		} else if(fLevel > level) {// should not happen if algorithm is correct
			return;
		}

		// other cases
		addDeviceItem(item, item.getName(), level);
	}

	protected void addDeviceItem(ICpDeviceItem item, final String itemName, final int level) {
		IRteDeviceItem di = getChild(itemName);
		if(di == null ) {
			di = new RteDeviceItem(itemName, level, this);
			addChild(di);
		}
		di.addDevice(item);
	}


	@Override
	public IRteDeviceItem findItem(final String deviceName, final String vendor, final boolean onlyDevice) {
		if(fLevel == EDeviceHierarchyLevel.ROOT.ordinal() && vendor != null && !vendor.isEmpty()) {
			String vendorName = DeviceVendor.getOfficialVendorName(vendor);
			IRteDeviceItem dti = getChild(vendorName);
			if(dti != null) {
				return dti.findItem(deviceName, vendorName, onlyDevice);
			}
		} else {
			// check if device item can be found directly on this level
			IRteDeviceItem dti = getChild(deviceName); 
			if(dti != null) {
				if (!onlyDevice) {
					if (deviceName.contains("*")) { //$NON-NLS-1$
						// TODO: find a better criterion in this case
						return dti.getParent();
					} else {
						return dti;
					}
				} else if (dti.getLevel() > EDeviceHierarchyLevel.SUBFAMILY.ordinal()) {
					return dti;
				}
			}
			// search in children
			Collection<? extends IRteDeviceItem> children = getChildren(); 
			if(children == null) {
				return null;
			}
			for(IRteDeviceItem child : children){
				dti = child.findItem(deviceName, vendor, onlyDevice);
				if(dti != null) {
					if (!onlyDevice) {
						return dti;
					} else if (dti.getLevel() > EDeviceHierarchyLevel.SUBFAMILY.ordinal()) {
						return dti;
					}
				}
			}
		}
		return null;
	}


	@Override
	public IRteDeviceItem findItem(final IAttributes attributes) {
		String deviceName = CpItem.getDeviceName(attributes);
		if(deviceName == null || deviceName.isEmpty()) {
			return null;
		}
		String vendor = attributes.getAttribute(CmsisConstants.DVENDOR);
		return findItem(deviceName, vendor, true);
	}

	@Override
	public IRteDeviceItem getVendorItem() {
		if(getLevel() == EDeviceHierarchyLevel.VENDOR.ordinal()) {
			return this;
		} else if(getLevel() > EDeviceHierarchyLevel.VENDOR.ordinal()) { 
			if(getParent() != null) {
				return getParent().getVendorItem();
			}
		}  
		return null;
	}

	@Override
	public IRteDeviceItem getVendorItem(String vendor) {
		if(getLevel() == EDeviceHierarchyLevel.ROOT.ordinal()) {
			return getChild(vendor);
		}
		IRteDeviceItem root = getRoot();
		if(root != null) {
			return root.getVendorItem(vendor);
		}
		return null;
	}

	@Override
	public String getDescription() {
		ICpDeviceItem deviceItem = getDevice();
		if(deviceItem != null) {
			String description = deviceItem.getDescription();
			if(description != null && !description.isEmpty()) {
				return description;
			}
		}
		if(getParent() != null) {
			return getParent().getDescription();
		} 
		return CmsisConstants.EMPTY_STRING;
	}

	@Override
	public String getUrl() {
		ICpDeviceItem device = getDevice();
		if(device != null) {
			return device.getUrl();
		}
		return null;
	}

	@Override
	public String getDoc() {
		ICpDeviceItem device = getDevice();
		if(device != null)
		 {
			return device.getDoc(); // TODO: return a collection of documents
		}
		return null;
	}

	@Override
	public Set<String> getAllPackIds() {
		if (fLevel == EDeviceHierarchyLevel.PROCESSOR.ordinal()) {
			Set<String> ret = new HashSet<String>();
			if (fDevices != null) {
				for (String id : fDevices.keySet()) {
					ret.add(CpPack.familyFromId(id));
				}
			}
			return ret;
		}
		Set<String> ret = new HashSet<String>();
		if (fChildMap != null) {
			for (IRteDeviceItem item : fChildMap.values()) {
				ret.addAll(item.getAllPackIds());
			}
		}
		if (fDevices != null) {
			for (String id : fDevices.keySet()) {
				ret.add(CpPack.familyFromId(id));
			}
		}
		return ret;
	}

	@Override
	public Set<String> getAllDeviceNames() {
		Set<String> ret = new HashSet<String>();
		if (fChildMap != null) {
			for (IRteDeviceItem item : fChildMap.values()) {
				ret.addAll(item.getAllDeviceNames());
			}
		}
		if (fDevices != null) {
			for (ICpDeviceItem device : fDevices.values()) {
				ret.add(device.getName());
			}
		}
		return ret;
	}
}
