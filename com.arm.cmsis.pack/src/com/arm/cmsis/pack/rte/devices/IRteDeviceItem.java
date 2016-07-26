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
import java.util.Set;

import com.arm.cmsis.pack.data.ICpDeviceItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.item.ICmsisMapItem;

/**
 * Interface base element for Device tree elements: vendor, family, sub-family, device, variant, processor
 * This hierarchy is similar to ICpDeviceItem one, but works across Packs
 */
public interface IRteDeviceItem extends ICmsisMapItem<IRteDeviceItem>  {

	/**
	 * Returns hierarchy level of this item: root, vendor family, sub-family, device, variant or processor
	 * @return item hierarchy level as int
	 * @see ICpDeviceItem.Level
	 */
	int getLevel();

	/**
	 * Adds device (family, sub-family device or variant) recursively
	 * Here we keep the invariant that all the devices are from: <br>
	 * 1. latest installed pack <br>
	 * 2. latest pack, if there is no pack that contains this device is installed
	 * @param item device item to add
	 */
	void addDevice(ICpDeviceItem item);

	/**
	 * Adds devices (family, sub-family device or variant) from supplied pack
	 * @param pack IcpPack to add devices from
	 */
	void addDevices(ICpPack pack);

	/**
	 * Removes device (family, sub-family device or variant) recursively
	 * @param item
	 */
	void removeDevice(ICpDeviceItem item);

	/**
	 * Removes device (family, sub-family device or variant) from supplied pack
	 * @param pack IcpPack to add devices from
	 */
	void removeDevices(ICpPack pack);

	/**
	 * Returns first device stored in the item
	 * @return first ICpDeviceItem stored in the item
	 */
	ICpDeviceItem getDevice();

	/**
	 * Returns processor name associated with the item (for end-leaves only)
	 * @return processor name if associated with the item
	 */
	String getProcessorName();

	/**
	 * Returns effective properties of the first device stored in the item or properties with associated processor
	 * @return effective device properties if any
	 * @see #getProcessorName()
	 */
	ICpItem getEffectiveProperties();

	/**
	 * Checks if this item is an end leaf that represent a device that can be selected
	 * @return true if this item represents an end-leaf device
	 */
	boolean isDevice();


	/**
	 * Returns sorted collection of device items
	 * @return sorted collection of devices stored in this item
	 */
	public Collection<ICpDeviceItem> getDevices();

	/**
	 * Searches device tree for given device name and optional vendor name
	 * @param deviceName device name to search
	 * @param vendor device vendor to search
	 * @param onlyDevice set to true if only searching for device or variant (no family, subfamily)
	 * @return IRteDeviceItem if found or null otherwise
	 */
	IRteDeviceItem findItem(final String deviceName, final String vendor, final boolean onlyDevice);

	/**
	 * Searches device tree for given attributes device name and optional vendor name
	 * @param attributes device attributes to search for
	 * @return IRteDeviceItem if found or null otherwise
	 */
	IRteDeviceItem findItem(final IAttributes attributes);

	/**
	 * Returns vendor item in parent chain
	 * @return parent vendor item
	 */
	IRteDeviceItem getVendorItem();


	/**
	 * Returns vendor searching from root
	 * @param vendor device vendor to search
	 * @return vendor item
	 */
	IRteDeviceItem getVendorItem(final String vendor);

	/**
	 * Get All the Pack IDs contained in this IRteDeviceItem
	 * @return A set of Pack IDs
	 */
	Set<String> getAllPackIds();

	/**
	 * Get All the {@link ICpDeviceItem} contained in this IRteDeviceItem
	 * @return A set of {@link ICpDeviceItem} Names
	 */
	Set<String> getAllDeviceNames();

	/**
	 * Get the vendor name of this IRteDeviceItem
	 * @return vendor name of this IRteDeviceItem
	 */
	String getVendorName();

}
