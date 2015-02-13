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

package com.arm.cmsis.pack.data;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.utils.VersionComparator;

/**
 * Default implementation of ICpPack interface
 */
public class CpPack extends CpItem implements ICpPack {

	private String fileName = null;
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
		this.fileName = fileName;
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
	public ICpItem getCondition(String conditionId) {
		if(conditionId == null || conditionId.isEmpty())
			return null;
		if(conditions == null) {
			// fill conditions map for quick access
			conditions = new HashMap<String, ICpItem>();
			ICpItem conditionsItem = getFirstChild("conditions");
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
	public String getInstallDir(final String packRoot) {
		
		if(packRoot == null || state == PackState.INSTALLED || state == PackState.GENERATED) {
		// installed and generator files are already located at their installation directories
			try {
				File f = new File(getFileName());
				if(f.exists())
					return f.getCanonicalFile().getParent() + File.separator;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		String dir = IAttributes.EMPTY_STRING;
		// construct installation path out of Pack properties
		if(packRoot != null) {
			dir += packRoot + File.pathSeparator;
		}
		dir += getVendor() + File.pathSeparator + getVersion();
		return dir;
	}
	

	@Override
	public String getName() {
		ICpItem nameItem = getFirstChild("name");
		if(nameItem != null)
			return nameItem.getText();
		return IAttributes.EMPTY_STRING;
	}

	@Override
	public String getVendor() {
		ICpItem vendorItem = getFirstChild("vendor");
		if(vendorItem != null)
			return vendorItem.getText();
		return IAttributes.EMPTY_STRING;
	}

	@Override
	public String getVersion() {
		if( version == null) {
			ICpItem releases = getFirstChild("releases");
			if(releases != null && releases.hasChildren()) {
				for(ICpItem r : releases.getChildren()){
					String v = r.attributes().getAttribute("version");
					if( VersionComparator.versionCompare(v, version) > 0)
						version = v;
				}
			}
			if( version == null)
				version = IAttributes.EMPTY_STRING;
		}
		return version;
	}

	
	@Override
	public String getUrl() {
		if(fURL == null) {
			ICpItem urlItem = getFirstChild("url");
			if(urlItem != null)
				fURL = urlItem.getText();
			else 
				fURL = IAttributes.EMPTY_STRING;
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
		if(id == null)
			return IAttributes.EMPTY_STRING;
		int pos = id.indexOf('.'); // find first separator
		if(pos > 0 ) {
			pos = id.indexOf('.', pos+1); // find second separator
			if(pos > 0 )
				return id.substring(pos+1); // the rest is version (is any)
		}
		return IAttributes.EMPTY_STRING;
	}
	
	/**
	 * Returns Pack family ID : pack ID without version, i.e. the form Vendor.Name  
	 * @param  id Pack ID string
	 * @return family pack ID  
	 */
	public static String familyFromId(final String id){
		if(id == null)
			return IAttributes.EMPTY_STRING;
		int pos = id.indexOf('.'); // find first separator
		if(pos > 0 ) {
			pos = id.indexOf('.', pos+1); // find second separator
			if(pos > 0 )
				return id.substring(0, pos); // extract prefix (strip version)
		}
		return id; // id has already no version
	}
}
