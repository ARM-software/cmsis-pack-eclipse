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
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;


import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpPack.PackState;
import com.arm.cmsis.pack.utils.VersionComparator;

/**
 * Default implementation of ICpPackFamily interface
 */
public class CpPackFamily extends CpItem implements ICpPackFamily {

	protected Map<String, ICpPack> fPacks = null;
	protected ICpItem fPreviousReleases = null; // container for previous releases

	public CpPackFamily(ICpItem parent) {
		super(parent);
	}

	public CpPackFamily(ICpItem parent, String familyId) {
		super(parent, familyId);
		fId = familyId;
	}

	@Override
	public ICpPack getPack() {
		// get the latest pack
		if(fPacks != null && !fPacks.isEmpty()) {
			return fPacks.entrySet().iterator().next().getValue();
		}
		return null;
	}

	@Override
	public String getPackId() {
		// get the latest pack ID
		ICpPack pack = getPack();
		if(pack != null) {
			return pack.getId();
		}
		return null;
	}


	@Override
	public String getPackFamilyId() {
		return getId();
	}

	@Override
	public ICpPack getPack(final String packId) {
		if(fPacks != null) {
			if(packId.equals(getId())) {
				return getPack();
			}
			// id or version ?
			String familyId = CpPack.familyFromId(packId);
			if(familyId.equals(getId())) {
				String version = CpPack.versionFromId(packId);
				fPacks.get(version);
			}
			return fPacks.get(packId); // supplied packId is in fact version
		}
		return null;
	}

	@Override
	public Collection<ICpPack> getPacks() {
		if(fPacks != null) {
			return fPacks.values();
		}
		return null;
	}
	
	
	@Override
	public Collection<? extends ICpItem> getChildren() {
		if(fPacks != null) {
			return fPacks.values();
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
		cachedChildArray = null;
		fPreviousReleases = null;
		ICpPack pack = (ICpPack)item;
		if(fPacks == null) {
			fPacks = new TreeMap<String, ICpPack>(new VersionComparator());
		}
		if (pack.getParent() == null) {
			pack.setParent(this);
		}
		if (pack.getPackState() == PackState.ERROR) {
			fPacks.put(pack.getTag(), pack);
		} else {
			String version = pack.getVersion();
			ICpPack inserted =  fPacks.get(version);
			if(inserted == null || inserted.getPackState().ordinal() > pack.getPackState().ordinal()) {
				fPacks.put(version, pack);
			}
		}
	}

	@Override
	public ICpPack getPackByFilename(String pdscFile) {
		if(fPacks != null) {
			for(ICpPack pack : fPacks.values()){
				String fileName = pack.getFileName();
				if(fileName != null && fileName.equals(pdscFile)) {
					return pack;
				}
			}
		}
		return null;
	}

	@Override
	public String getDescription() {
		ICpPack pack = getPack();
		if(pack != null) {
			return pack.getDescription();
		}
		return null;
	}

	@Override
	public synchronized String getUrl() {
		ICpPack pack = getPack();
		if(pack != null) {
			return pack.getUrl();
		}
		return null;
	}

	@Override
	public ICpItem getPreviousReleases() {
		if(fPreviousReleases == null) {
			Collection<? extends ICpItem> previousReleases = collectPreviousReleases();
			if(previousReleases != null && !previousReleases.isEmpty()){
				fPreviousReleases = new CpItem(this, CmsisConstants.PREVIOUS);
				for(ICpItem item : previousReleases) {
					fPreviousReleases.addChild(item);
				}
			}
		}
		return fPreviousReleases;
	}
	

	protected Collection<? extends ICpItem> collectPreviousReleases() {
		ICpPack pack = getPack();
		if(pack == null) {
			return null;  
		}
		Collection<? extends ICpItem> releases = pack.getReleases();
		if (releases == null) {
			return null;
		}
		Map<String, ICpItem> previousReleases = new TreeMap<String, ICpItem>(new VersionComparator());
		for(ICpItem item : releases) {
			String version = item.getAttribute(CmsisConstants.VERSION);
			if(fPacks == null || !fPacks.containsKey(version))
				previousReleases.put(version, item);
		}
		return previousReleases.values();
	}

	@Override
	protected Object[] createChildArray() {
		fPreviousReleases = null;
		Collection<ICpItem> children = new LinkedList<ICpItem>();
		if(fPacks != null )
			children.addAll(fPacks.values());
		ICpItem previousReleases = getPreviousReleases(); // refresh previous release info
		if(previousReleases != null){
			children.add(fPreviousReleases);
		}
		if(!children.isEmpty())
			return children.toArray();
		return EMPTY_OBJECT_ARRAY;
	}	
	
}

