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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.utils.AlnumComparator;

/**
 *  Class to collect pack families
 */
public class CpPackCollection extends CpItem implements ICpPackCollection {

	// fChildren from CpItem class is not used
	private Map<String, ICpPackFamily> fPackFamilies = null;
	private Set<String> fLatestPackIDs = null;

	public CpPackCollection() {
		super(null, CmsisConstants.PACKAGES_TAG);
	}

	@Override
	public ICpPack getPack(String packId) {
		if(fPackFamilies != null) {
			String familyId = CpPack.familyFromId(packId);
			ICpPackFamily f = fPackFamilies.get(familyId);
			if(f != null) {
				String version = CpPack.versionFromId(packId);
				if(version == null || version.isEmpty()) {
					return f.getPack();
				}
				return f.getPack(version);
			}
		}
		return null;
	}

	
	@Override
	public Collection<? extends ICpItem> getChildren() {
		if(fPackFamilies != null) {
			return fPackFamilies.values();
		}
		return null;
	}


	@Override
	public ICpItem getFirstChild(String packId) {
		return getPack(packId);
	}


	@Override
	public void addChild(ICpItem item) {
		if(item == null) {
			return;
		}
		if(!(item instanceof ICpPack)) {
			return;
		}
		ICpPack pack = (ICpPack)item;
		if(fPackFamilies == null) {
			fPackFamilies = new TreeMap<String, ICpPackFamily>(new AlnumComparator(false, false));
		}
		
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
			if(children == null) {
				continue;
			}

			for(ICpItem item : children) {
				if(item == null || !(item instanceof ICpPack)) {
					continue;
				}
				ICpPack pack = (ICpPack)item;
				packs.add(pack);
			}
		}
		return packs;
	}

	@Override
	public Collection<ICpPack> getFilteredPacks(ICpPackFilter packFilter) {
		if(packFilter == null || packFilter.isUseAllLatestPacks()) {
			return getLatestPacks();
		}

		Collection<ICpPack> packs = new LinkedList<ICpPack>();
		for(ICpPackFamily f : fPackFamilies.values()) {
			Collection<? extends ICpItem> children = f.getChildren();
			if(children == null) {
				continue;
			}
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
				if(item == null || !(item instanceof ICpPack)) {
					continue;
				}
				ICpPack pack = (ICpPack)item;
				if(packFilter == null || packFilter.passes(pack)) {
					packs.add(pack);
				}
			}
		}
		return packs;
	}
	
	@Override
	public Collection<ICpPack> getLatestPacks() {
		Collection<ICpPack> latestPacks = new LinkedList<ICpPack>();
		for(ICpPackFamily f : fPackFamilies.values()) {
			ICpPack pack = f.getPack();
			if(pack != null) {
				latestPacks.add(pack);
			}
		}
		return latestPacks;
	}


	@Override
	public synchronized Set<String> getLatestPackIDs() {
		if(fLatestPackIDs == null) {
			fLatestPackIDs = new HashSet<String>(); 
			for(ICpPackFamily f : fPackFamilies.values()) {
				String packId = f.getPackId();
				if(packId != null && !packId.isEmpty()) {
					fLatestPackIDs.add(packId);
				}
			}
		}
		return fLatestPackIDs;
	}


	@Override
	public ICpPack getPackByFilename(String pdscFile) {
		if(fPackFamilies != null) {
			for(ICpPackFamily f : fPackFamilies.values()){
				ICpPack pack = f.getPackByFilename(pdscFile); 
				if(pack != null) {
					return pack;
				}
			}
		}
		return null;
	}

	@Override
	public Set<String> getContainedPackVersions() {
		throw new RuntimeException("This method should not be called from this class"); //$NON-NLS-1$
	}

	@Override
	public Collection<? extends ICpItem> getPacksByPackFamilyId(String packFamilyId) {
		return fPackFamilies.get(packFamilyId).getChildren();
	}
}
