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

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.arm.cmsis.pack.utils.VersionComparator;

/**
 * Default implementation of ICpPackFamily interface
 */
public class CpPackFamily extends CpItem implements ICpPackFamily {

	Map<String, ICpPack> fPacks = null;
	
	/**
	 * @param parent
	 */
	public CpPackFamily(ICpItem parent) {
		super(parent);
	}

	/**
	 * @param parent
	 * @param tag
	 */
	public CpPackFamily(ICpItem parent, String tag) {
		super(parent, tag);
	}

	@Override
	public ICpPack getPack() {
		// get the latest pack
		if(fPacks != null && !fPacks.isEmpty())
			return fPacks.entrySet().iterator().next().getValue();
		return null;
	}

	@Override
	public String getPackId() {
		// get the latest pack ID
		if(fPacks != null && !fPacks.isEmpty())
			return fPacks.entrySet().iterator().next().getKey();
		return null; // return 
	}

	
	@Override
	public String getPackFamilyId() {
		return getId();
	}

	@Override
	public ICpPack getPack(final String packId) {
		if(fPacks != null) {
			// id or version ?
			String version = CpPack.versionFromId(packId);
			if(version.isEmpty())
				version = packId;
			return fPacks.get(version);
		}
		return null;
	}

	@Override
	public Collection<? extends ICpItem> getChildren() {
		if(fPacks != null)
			return fPacks.values();
		return null;
	}

	@Override
	public ICpItem getFirstChild(String packId) {
		return getPack(packId);
	}

	@Override
	public void addChild(ICpItem item) {
		if(item == null)
			return;
		if(!(item instanceof ICpPack))
			return;
		ICpPack pack = (ICpPack)item;
		if(fPacks == null) {
			fPacks = new TreeMap<String, ICpPack>(new VersionComparator());
		}
		if(pack.getParent() == null)
			pack.setParent(this);
		fPacks.put(pack.getVersion(), pack);
	}

	@Override
	public ICpPack getPackByFilename(String pdscFile) {
		if(fPacks != null) {
			for(ICpPack pack : fPacks.values()){
				String fileName = pack.getFileName(); 
				if(fileName != null && fileName.equals(pdscFile))
					return pack;
			}
		}
		return null;
	}
}

