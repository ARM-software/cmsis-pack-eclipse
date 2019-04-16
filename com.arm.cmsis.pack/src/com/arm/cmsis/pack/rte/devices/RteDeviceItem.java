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

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpPackIdComparator;
import com.arm.cmsis.pack.data.ICpDeviceItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.enums.EDeviceHierarchyLevel;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.item.CmsisMapItem;
import com.arm.cmsis.pack.utils.AlnumComparator;
import com.arm.cmsis.pack.utils.DeviceVendor;
import com.arm.cmsis.pack.utils.FullDeviceName;

/**
 * Default implementation of IRteDeviceItem
 */
public class RteDeviceItem extends CmsisMapItem<IRteDeviceItem> implements IRteDeviceItem {

	protected int fLevel = EDeviceHierarchyLevel.NONE.ordinal();
	protected Map<String, ICpDeviceItem> fDevices = null;
	protected Set<String> fDeviceNames = null;
	protected  boolean fbDeprecated = false;

	/**
	 *
	 */
	public RteDeviceItem() {
		fLevel = EDeviceHierarchyLevel.ROOT.ordinal();
		fName = CmsisConstants.ALL_DEVICES;
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
			// Return the latest INSTALLED pack's device
			ICpDeviceItem firstDevice = null;
			for (ICpDeviceItem device : fDevices.values()) {
				if (device.getPack().getPackState().isInstalledOrLocal()) {
					return device;
				}
				if (firstDevice == null)
					firstDevice = device;
			}
			// Otherwise return the latest pack's device
			return firstDevice;
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
	public boolean isDeprecated() {
		return fbDeprecated;
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
			boolean deprecated = pack.isDeprecated();
			String packId = pack.getId();
			if(fDevices == null) {
				fDevices = new TreeMap<String, ICpDeviceItem>(new CpPackIdComparator());
			}
			
			ICpDeviceItem device = fDevices.get(packId);
			if(device != null) {
				if(pack == device.getPack()) // not from the same pack 
					return;
				// installed/downloaded pack has preference 
				if(pack.getPackState().ordinal() > device.getPack().getPackState().ordinal()) {
					return;
				}
			}
			if(fDevices.isEmpty() || fbDeprecated )  {
				fbDeprecated = deprecated;
			} else if(deprecated) {
				return;
			}
			fDevices.put(packId, item);			
			
			if(fLevel == EDeviceHierarchyLevel.PROCESSOR.ordinal()) {
				return;
			}
			Collection<ICpDeviceItem> subItems = item.getDeviceItems();
			if(subItems != null && !subItems.isEmpty()) {
				for(ICpDeviceItem i : subItems ){
					addDevice(i);
				}
			} else if(level >= EDeviceHierarchyLevel.DEVICE.ordinal()) {
				addDeviceName(getName()); 
				if( isToAddProcessors(item)) {
					// add processor leaves
					Map<String, ICpItem> processors = item.getProcessors();
					for(Entry<String, ICpItem> e : processors.entrySet()) {
						String fullName = item.getName() + ":" + e.getKey(); //$NON-NLS-1$
						addDeviceItem(item, fullName, EDeviceHierarchyLevel.PROCESSOR.ordinal());
					}
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

	protected boolean isToAddProcessors(ICpDeviceItem item) {
		Map<String, ICpItem> processors = item.getProcessors();
		if(processors == null || processors.isEmpty())
			return false;
		if(processors.size() > 1)
			return true;
		Entry<String, ICpItem> e = processors.entrySet().iterator().next();
		String procName = e.getKey();
		if(procName != null && !procName.isEmpty()) {
			return true;
		}
		return false;
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
	public void removeDevice(ICpDeviceItem item) {
		if (item == null) {
			return;
		}

		EDeviceHierarchyLevel eLevel = item.getLevel();
		int level = eLevel.ordinal();

		if(fLevel == level || fLevel == EDeviceHierarchyLevel.PROCESSOR.ordinal()) {
			ICpPack pack = item.getPack();
			String packId = pack.getId();
			if(fDevices == null) {
				return;
			}

			fDevices.remove(packId);

			if(fLevel == EDeviceHierarchyLevel.PROCESSOR.ordinal()) {
				getParent().removeChild(this);
				return;
			}
			Collection<ICpDeviceItem> subItems = item.getDeviceItems();
			if(subItems != null && !subItems.isEmpty()) {
				for(ICpDeviceItem subItem : subItems ){
					removeDevice(subItem);
				}
			} else if(level >= EDeviceHierarchyLevel.DEVICE.ordinal() && item.getProcessorCount() > 1) {
				// add processor leaves
				Map<String, ICpItem> processors = item.getProcessors();
				for(Entry<String, ICpItem> e : processors.entrySet()) {
					String procName = item.getName() + ":" + e.getKey(); //$NON-NLS-1$
					removeDeviceItem(item, procName, EDeviceHierarchyLevel.PROCESSOR.ordinal());
				}
			}
			if (fDevices.size() == 0) {
				removeDeviceName(fName);
				getParent().removeChild(this);
			}
			return;
		} else if(fLevel == EDeviceHierarchyLevel.ROOT.ordinal()) {
			IRteDeviceItem d = findItem(item.getName(), item.getVendor(), false);
			if (d != null) {
				d.removeDevice(item);
				IRteDeviceItem p = d.getParent();
				while (p != null && p.getLevel() > EDeviceHierarchyLevel.ROOT.ordinal()) {
					if (p.getChildren() == null ||
							p.getChildren().isEmpty()) {
						IRteDeviceItem pp = p.getParent();
						pp.removeChild(p);
						p = pp;
					} else {
						break;
					}
				}
			}
			return;
		} else if(fLevel > level) {// should not happen if algorithm is correct
			return;
		}

		removeDeviceItem(item, item.getName(), level);
	}

	protected void removeDeviceItem(ICpDeviceItem item, String itemName, int level) {
		IRteDeviceItem di = getChild(itemName);
		if (di != null) {
			di.removeDevice(item);
		}
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
					}
					return dti;
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
		String deviceName = FullDeviceName.getFullDeviceName(attributes);
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
		vendor = DeviceVendor.getOfficialVendorName(vendor);
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
		if(device != null) {
			return device.getDoc(); // TODO: return a collection of documents
		}
		return null;
	}

	@Override
	public Set<String> getAllDeviceNames() {
		if(fDeviceNames == null ){
			fDeviceNames = new HashSet<String>();
			if(isDevice())
				addDeviceName(getDevice().getName());
			if (fChildMap != null) {
				for (IRteDeviceItem item : fChildMap.values()) {
					fDeviceNames.addAll(item.getAllDeviceNames());
				}
			}
		} 
		return fDeviceNames;
	}

	@Override
	public String getVendorName() {
		if (fLevel == EDeviceHierarchyLevel.VENDOR.ordinal()) {
			return fName;
		}
		if (getParent() != null) {
			return getParent().getVendorName();
		}
		return CmsisConstants.EMPTY_STRING;
	}

	@Override
	public void addDeviceName(String name) {
		if (fDeviceNames != null ) 
			fDeviceNames.add(name);
		if (getParent() != null) {
			getParent().addDeviceName(name);
		}
	}

	
	@Override
	public void removeDeviceName(String name) {
		if (fDeviceNames != null)
			fDeviceNames.remove(name);
		
		if (getParent() != null) {
			getParent().removeDeviceName(name);
		}
	}


}
