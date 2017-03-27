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

package com.arm.cmsis.pack.data;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.utils.VersionComparator;

/**
 * Default implementation of ICpPack interface
 */
public class CpPack extends CpRootItem implements ICpPack {

	protected String version = null;
	protected PackState state = PackState.UNKNOWN;
	protected Map<String, ICpItem> conditions = null; // sorted map for quick access to conditions
	protected Set<String> deviceNames = null; // names of all declared and referenced devices
	protected Set<String> boardNames = null; // names of boards described in the pack
	protected int deviceLess = -1; // -1 means uninitialized
	protected int deprecated = -1; // -1 means uninitialized
	
	public CpPack() {
		this(NULL_CPITEM);
	}

	public CpPack(ICpItem parent) {
		this(parent, CmsisConstants.PACKAGE_TAG);
	}

	public CpPack(ICpItem parent, String tag) {
		super(parent, tag);
	}

	public CpPack(String tag, String fileName) {
		super(tag, fileName);
	}

	@Override
	public ICpPack getPack() {
		return this;
	}

	@Override
	public ICpItem createItem(ICpItem parent, String tag) {
		// in all other cases call super class
		return super.createItem(parent, tag);
	}

	@Override
	public PackState getPackState() {
		return state;
	}

	@Override
	public void setPackState(PackState state) {
		this.state = state;
	}

	@Override
	public boolean isGenerated() {
		return state == PackState.GENERATED;
	}


	@Override
	public synchronized ICpItem getCondition(String conditionId) {
		if(conditionId == null || conditionId.isEmpty()) {
			return null;
		}
		if(conditions == null) {
			// fill conditions map for quick access
			conditions = new HashMap<String, ICpItem>();
			ICpItem conditionsItem = getFirstChild(CmsisConstants.CONDITIONS_TAG);
			if(conditionsItem != null) {
				Collection<? extends ICpItem> items = conditionsItem.getChildren();
				for(ICpItem c : items) {
					conditions.put(c.getId(), c);
				}
			}
		}
		return conditions.get(conditionId);
	}


	@Override
	public synchronized ICpGenerator getGenerator(String id) {
		Collection<? extends ICpItem> generators = getGrandChildren(CmsisConstants.GENERATORS_TAG);
		if(generators != null) {
			for(ICpItem g : generators) {
				if(!(g instanceof ICpGenerator)) {
					continue;
				}

				if(id == null || id.isEmpty() || g.getId().equals(id) ) {
					return (ICpGenerator)g;
				}
			}
		}
		return null;
	}

	@Override
	public synchronized String getInstallDir(final String packRoot) {
		if(packRoot == null || getPackState() == PackState.GENERATED) {
			// installed and generator files are already located at their installation directories
			return getRootDir(true);
		}
		// construct installation path out of Pack properties, use forward slashes
		String dir = packRoot;
		dir += '/' + getVendor() + '/' + getName() + '/' + getVersion() + '/';
		return dir;
	}


	@Override
	public String getName() {
		ICpItem nameItem = getFirstChild(CmsisConstants.NAME);
		if(nameItem != null) {
			return nameItem.getText();
		}
		return CmsisConstants.EMPTY_STRING;
	}

	@Override
	public String getVendor() {
		ICpItem vendorItem = getFirstChild(CmsisConstants.VENDOR);
		if(vendorItem != null) {
			return vendorItem.getText();
		}
		return CmsisConstants.EMPTY_STRING;
	}

	@Override
	public synchronized String getVersion() {
		if( version == null) {
			ICpItem releases = getFirstChild(CmsisConstants.RELEASES_TAG);
			if(releases != null && releases.hasChildren()) {
				for(ICpItem r : releases.getChildren()){
					String v = r.getAttribute(CmsisConstants.VERSION);
					if( VersionComparator.versionCompare(v, version) > 0) {
						version = v;
					}
				}
			}
			if( version == null) {
				version = CmsisConstants.EMPTY_STRING;
			}
		}
		return version;
	}


	@Override
	public synchronized String getUrl() {
		if(fURL == null) {
			ICpItem urlItem = getFirstChild(CmsisConstants.URL);
			if(urlItem != null) {
				fURL = urlItem.getText();
			} else {
				fURL = CmsisConstants.EMPTY_STRING;
			}
		}
		return fURL;
	}

	@Override
	public String constructId() {
		// construct Pack ID in the form "Vendor.Name.Version"
		return getPackFamilyId() + '.' + getVersion();
	}

	@Override
	public String getPackFamilyId() {
		return getVendor() + '.' + getName();
	}

	/**
	 * Extracts version from id string
	 * @param id Pack ID string
	 * @return version string if found, null otherwise
	 */
	public static String versionFromId(final String id){
		if(id == null) {
			return CmsisConstants.EMPTY_STRING;
		}
		int pos = id.indexOf('.'); // find first separator
		if(pos > 0 ) {
			pos = id.indexOf('.', pos+1); // find second separator
			if(pos > 0 )
			{
				return id.substring(pos+1); // the rest is version (is any)
			}
		}
		return CmsisConstants.EMPTY_STRING;
	}

	/**
	 * Returns Pack family ID : pack ID without version, i.e. the form Vendor.Name
	 * @param  id Pack ID string
	 * @return family pack ID
	 */
	public static String familyFromId(final String id){
		if(id == null) {
			return CmsisConstants.EMPTY_STRING;
		}
		int pos = id.indexOf('.'); // find first separator
		if(pos > 0 ) {
			pos = id.indexOf('.', pos+1); // find second separator
			if(pos > 0 )
			{
				return id.substring(0, pos); // extract prefix (strip version)
			}
		}
		return id; // id has already no version
	}

	public static String constructPackId(IAttributes packAttributes) {
		String vendor = packAttributes.getAttribute(CmsisConstants.VENDOR);
		String name = packAttributes.getAttribute(CmsisConstants.NAME);
		String version = packAttributes.getAttribute(CmsisConstants.VERSION);
		String packId = vendor + '.' + name;
		if (CmsisConstants.FIXED.equals(packAttributes.getAttribute(CmsisConstants.VERSION_MODE))) { // use fixed version of the pack
			packId += '.' + version;
		} else { // use latest compatible version of the pack
			ICpPackCollection allPacks = CpPlugIn.getPackManager().getPacks();
			if (allPacks == null) {
				return CmsisConstants.EMPTY_STRING;
			}
			String familyId = CpPack.familyFromId(packId);
			Collection<? extends ICpItem> packs = allPacks.getPacksByPackFamilyId(familyId);
			if (packs == null) {
				return CmsisConstants.EMPTY_STRING;
			}
			ICpItem latestPack = packs.iterator().next();
			String latestVersion = latestPack.getVersion();
			int verCmp = VersionComparator.versionCompare(latestVersion, version);
			if (isPackFamilyId(packId) && verCmp >= 0 && verCmp < 4) { // compatible
				packId += '.' + latestVersion;
			} else {
				packId += '.' + version;
			}
		}
		return packId;
	}

	public static boolean isPackFamilyId(String id) {
		return id.split("\\.").length == 2; //$NON-NLS-1$
	}

	@Override
	public Set<String> getAllDeviceNames() {
		if (deviceNames == null) {
			deviceNames = new HashSet<String>();
			if(isDevicelessPack()) {
				return deviceNames;
			}

			collectDeviceNames(getGrandChildren(CmsisConstants.DEVICES_TAG), deviceNames);
			collectDeviceNames(getGrandChildren(CmsisConstants.BOARDS_TAG), deviceNames);
		}
		return deviceNames;
	}

	protected void collectDeviceNames(Collection<? extends ICpItem> items, Set<String> deviceNames) {
		if (items == null || items.isEmpty()) {
			return;
		}
		for (ICpItem item : items) {
			if (item instanceof ICpDeviceItem) {
				ICpDeviceItem d = (ICpDeviceItem) item;
				deviceNames.add(d.getName());
				collectDeviceNames(d, deviceNames);
			}
		}
	}


	protected void collectDeviceNames(ICpDeviceItem parent, Set<String> deviceNames) {
		if (parent == null || parent.getDeviceItems() == null) {
			return;
		}
		for (ICpItem item : parent.getDeviceItems()) {
			if (!(item instanceof ICpDeviceItem)) {
				continue;
			}
			ICpDeviceItem d = (ICpDeviceItem) item;
			deviceNames.add(d.getName());
			collectDeviceNames(d, deviceNames);
		}
	}


	@Override
	public Set<String> getBoardNames() {
		if( boardNames == null) {
			boardNames = new HashSet<String>();
			Collection<? extends ICpItem> items = getGrandChildren(CmsisConstants.DEVICES_TAG);
			if (items == null || items.isEmpty()) {
				return boardNames;
			}
			for (ICpItem item : items) {
				if (!(item instanceof ICpBoard)) {
					continue;
				}
				boardNames.add(item.getName());
			}
		}
		return boardNames;
	}

	@Override
	public boolean isDevicelessPack() {
		if(deviceLess < 0) {
			// TODO check more
			if (getId().contains("ARM") ||  //$NON-NLS-1$
					(getGrandChildren(CmsisConstants.DEVICES_TAG) == null &&
					getGrandChildren(CmsisConstants.BOARDS_TAG) == null)) {
				deviceLess = 1;
			}else {
				deviceLess = 0;
			}
		}
		return deviceLess == 1;
	}

	@Override
	public boolean isLatest() {
		return (getParent() != null && getParent().getFirstChild() == this);
	}

	@Override
	public boolean isDeprecated() {
		if(deprecated < 0 ) {
			Collection<? extends ICpItem> releases = getReleases();
			if (releases != null) {
				ICpItem latestRelease = releases.iterator().next();
				deprecated = latestRelease.hasAttribute(CmsisConstants.DEPRECATED) ? 1 : 0; 
			} else {
				deprecated = 0;
			}
		}
		return deprecated == 1;
	}
	
	
	@Override
	public Collection<? extends ICpItem> getReleases() {
		return getGrandChildren(CmsisConstants.RELEASES_TAG);
	}

	@Override
	public Collection<? extends ICpItem> getRequiredPacks() {
		ICpItem requirements = getFirstChild(CmsisConstants.REQUIREMENTS_TAG);
		if (requirements == null) {
			return null;
		}
		return requirements.getGrandChildren(CmsisConstants.PACKAGES_TAG);
	}

	@Override
	public boolean isRequiredPacksInstalled() {
		Collection<? extends ICpItem> requiredPacks = getRequiredPacks();
		if (requiredPacks == null) {
			return true;
		}

		for (ICpItem requiredPack : requiredPacks) {
			ICpPackCollection installedPacks = CpPlugIn.getPackManager().getInstalledPacks();
			if (installedPacks == null || installedPacks.getPack(requiredPack.attributes()) == null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Replace Vendor.Pack.Version with Vendor/Pack/Version
	 *
	 * @param fullPackId the pack's id
	 * @return the relative installation directory. e.g. ARM/CMSIS/4.5.0
	 */
	public static String getPackRelativeInstallDir(String fullPackId) {
		int iv = fullPackId.indexOf('.');
		if (iv == -1) {
			return fullPackId;
		}
		String vendor = fullPackId.substring(0, iv);
		int ip = fullPackId.indexOf('.', iv + 1);
		if (ip == -1) {
			return fullPackId;
		}
		String pack = fullPackId.substring(iv + 1, ip);
		String version = fullPackId.substring(ip + 1);
		return vendor + File.separator + pack + File.separator + version;
	}

	/**
	 * Get the Full Pack ID of this {@link ICpItem}
	 *
	 * @param cpItem the cmsis pack item
	 * @return a String like Vendor.Pack.Version
	 */
	public static String getFullPackId(ICpItem cpItem) {
		return cpItem.getPackFamilyId() + "." + getCpItemVersion(cpItem); //$NON-NLS-1$
	}

	/**
	 * Return this {@link ICpItem}'s release date
	 *
	 * @param cpItem the cmsis pack item
	 * @return a string of the date or an empty string
	 */
	public static String getCpItemDate(ICpItem cpItem) {
		if (cpItem == null || cpItem instanceof ICpPackCollection) {
			return CmsisConstants.EMPTY_STRING;
		}

		String date = CmsisConstants.EMPTY_STRING;
		if (CmsisConstants.RELEASE_TAG.equals(cpItem.getTag())) {
			date = cpItem.getAttribute(CmsisConstants.DATE);
		} else {
			String version = cpItem.getPack().getVersion();
			Collection<? extends ICpItem> releases = cpItem.getPack().getGrandChildren(CmsisConstants.RELEASES_TAG);
			if (releases == null) {
				return date;
			}
			for (ICpItem release : releases) {
				if (release.getAttribute(CmsisConstants.VERSION).equals(version)) {
					date = release.getAttribute(CmsisConstants.DATE);
					break;
				}
			}
		}

		return date;
	}

	/**
	 * Get the items' pack version (either this pack or a specified release)
	 *
	 * @param cpItem the cmsis pack item
	 * @return pack version or an empty string
	 */
	public static String getCpItemVersion(ICpItem cpItem) {
		if (cpItem == null) {
			return CmsisConstants.EMPTY_STRING;
		}

		String installingVersion = CmsisConstants.EMPTY_STRING;

		if (cpItem.getTag().equals(CmsisConstants.RELEASE_TAG)) {
			installingVersion = cpItem.getAttribute(CmsisConstants.VERSION);
		} else {
			installingVersion = cpItem.getPack().getVersion();
		}

		return installingVersion;
	}

}
