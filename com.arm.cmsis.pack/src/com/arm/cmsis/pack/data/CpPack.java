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
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.utils.VersionComparator;

/**
 * Default implementation of ICpPack interface
 */
public class CpPack extends CpItem implements ICpPack {

	private String fileName = null;
	private String installDir = null;
	private String version = null;
	private PackState state = PackState.UNKNOWN; 
	private Map<String, ICpItem> conditions = null; // sorted map for quick access to conditions
	/**
	 * @param parent
	 */
	public CpPack(ICpItem parent) {
		super(parent);
	}

	/**
	 * @param tag
	 * @param parent
	 */
	public CpPack(ICpItem parent, String tag) {
		super(parent, tag);
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
	public String getFileName() {
		return fileName;
	}
	
	
	@Override
	public void setFileName(String fileName) {
		IPath p = new Path(fileName);
		this.fileName = p.toString();
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
	public synchronized String getInstallDir(final String packRoot) {
		if(packRoot == null || state == PackState.INSTALLED || state == PackState.GENERATED) {
		// installed and generator files are already located at their installation directories
			if(installDir == null) {
				try {
					File f = new File(getFileName());
					if(f.exists()) {

						IPath p = new Path( f.getCanonicalFile().getParent());
						installDir = p.toString() + '/';
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return installDir;
		}
		String dir = CmsisConstants.EMPTY_STRING;
		// construct installation path out of Pack properties
		// use forward slashes
		if(packRoot != null) {
			dir += packRoot + '/';
		}
		dir += getVendor() + '/' + getName() + '/' + getVersion() + '/';
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

	@Override
	public Set<String> getAllDeviceNames() {
		Set<String> devices = new HashSet<String>();
		Collection<? extends ICpItem> items = getGrandChildren(CmsisConstants.DEVICES_TAG);
		if (items == null || items.isEmpty()) {
			return devices;
		}
		for (ICpItem item : items) {
			if (!(item instanceof ICpDeviceItem)) {
				continue;
			}
			ICpDeviceItem device = (ICpDeviceItem) item;
			devices.addAll(collectDeviceNames(device));
		}
		return devices;
	}
	
	private Set<String> collectDeviceNames(ICpDeviceItem parent) {
		Set<String> ret = new HashSet<String>();
		if (parent == null || parent.getDeviceItems() == null) {
			return ret;
		}
		for (ICpItem item : parent.getDeviceItems()) {
			if (!(item instanceof ICpDeviceItem)) {
				continue;
			}
			ICpDeviceItem d = (ICpDeviceItem) item;
			ret.add(d.getName());
			ret.addAll(collectDeviceNames(d));
		}
		return ret;
	}

	@Override
	public boolean isDevicelessPack() {
		// TODO check more
		if (getGrandChildren(CmsisConstants.DEVICES_TAG) == null &&
				getGrandChildren(CmsisConstants.BOARDS_TAG) == null) {
			return true;
		} else {
			return false;
		}
	}

}
