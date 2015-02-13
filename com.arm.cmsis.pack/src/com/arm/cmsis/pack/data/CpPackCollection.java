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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.arm.cmsis.pack.utils.AlnumComparator;

/**
 *
 */
public class CpPackCollection extends CpItem implements ICpPackCollection {

	// fChildren from CpItem class is not used
	private Map<String, ICpPackFamily> fPackFamilies = null; 
	/**
	 * @param parent
	 */
	public CpPackCollection() {
		super(null);
	}


	@Override
	public ICpPack getPack(String packId) {
		if(fPackFamilies != null) {
			String familyId = CpPack.familyFromId(packId);
			ICpPackFamily f = fPackFamilies.get(familyId);
			if(f != null) {
				String version = CpPack.versionFromId(packId);
				return f.getPack(version);
			}
		}
		return null;
	}

	
	@Override
	public Collection<? extends ICpItem> getChildren() {
		if(fPackFamilies != null)
			return fPackFamilies.values();
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
		if(fPackFamilies == null)
			fPackFamilies = new TreeMap<String, ICpPackFamily>(new AlnumComparator(false, false));
		
		String familyId = pack.getPackFamilyId();
		
		ICpPackFamily family = fPackFamilies.get(familyId); 
		if(family == null) {
			family = new CpPackFamily(this, familyId);
			fPackFamilies.put(familyId, family);
		}
		family.addChild(item);
	}


	@Override
	public Collection<ICpPack> getPacks() {
		Collection<ICpPack> packs = new LinkedList<ICpPack>();
		for(ICpPackFamily f : fPackFamilies.values()) {
			Collection<? extends ICpItem> children = f.getChildren();
			if(children == null)
				continue;

			for(ICpItem item : children) {
				if(item == null || !(item instanceof ICpPack))
					continue;
				ICpPack pack = (ICpPack)item;
				packs.add(pack);
			}
		}
		return packs;
	}

	@Override
	public Collection<ICpPack> getFilteredPacks(ICpPackFilter packFilter) {
		if(packFilter == null || packFilter.isUseAllLatestsPacks())
			return getLatestPacks();

		Collection<ICpPack> packs = new LinkedList<ICpPack>();
		for(ICpPackFamily f : fPackFamilies.values()) {
			Collection<? extends ICpItem> children = f.getChildren();
			if(children == null)
				continue;
			if(packFilter != null) {
				String familyId = f.getPackFamilyId();
				if(packFilter.isExcluded(familyId)) {
					continue; // skip entire family
				} else if(packFilter.isUseLatest(familyId)) {
					ICpPack pack = f.getPack();
					packs.add(pack);
					continue;
				}
			}
			
			for(ICpItem item : children) {
				if(item == null || !(item instanceof ICpPack))
					continue;
				ICpPack pack = (ICpPack)item;
				if(packFilter == null || packFilter.passes(pack))
					packs.add(pack);
			}
		}
		return packs;
	}
	
	@Override
	public Collection<ICpPack> getLatestPacks() {
		Collection<ICpPack> latestPacks = new LinkedList<ICpPack>();
		for(ICpPackFamily f : fPackFamilies.values()) {
			ICpPack pack = f.getPack();
			if(pack != null)
				latestPacks.add(pack);
		}
		return latestPacks;
	}


	@Override
	public Set<String> getLatestPackIDs() {
		Set<String> latestPacks = new HashSet<String>(); 
		for(ICpPackFamily f : fPackFamilies.values()) {
			String packId = f.getPackId();
			if(packId != null && ! packId.isEmpty())
				latestPacks.add(packId);
		}
		return latestPacks;
	}


	@Override
	public ICpPack getPackByFilename(String pdscFile) {
		if(fPackFamilies != null) {
			for(ICpPackFamily f : fPackFamilies.values()){
				ICpPack pack = f.getPackByFilename(pdscFile); 
				if(pack != null)
					return pack;
			}
		}
		return null;
	}
}
