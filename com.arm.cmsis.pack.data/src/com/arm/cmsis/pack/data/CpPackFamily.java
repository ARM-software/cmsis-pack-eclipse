/*******************************************************************************
* Copyright (c) 2015 - 2020 ARM Ltd. and others
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
import com.arm.cmsis.pack.generic.IAttributes;
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
		if(!getPackMap().isEmpty()) {
			return getPackMap().entrySet().iterator().next().getValue();
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
				getPackMap().get(version);
			}
			return getPackMap().get(packId); // supplied packId is in fact version
		}
		return null;
	}

	@Override
	public ICpPack getPack(IAttributes attributes) {
		String familyId = attributes.getAttribute(CmsisConstants.VENDOR) +
				"." + attributes.getAttribute(CmsisConstants.NAME); //$NON-NLS-1$
		if (!familyId.equals(getId())) {
			return null;
		}
		Collection<ICpPack> packs = getPacks();
		if (packs == null) {
			return null;
		}
		return getPackByVersionRange(attributes.getAttribute(CmsisConstants.VERSION));
	}

	@Override
	public ICpPack getPackByVersionRange(String versionRange) {
		if(versionRange == null || versionRange.isEmpty())
			return getPack();// returns latest version

		for (ICpPack pack : getPacks()) {
			if (VersionComparator.matchVersionRange(pack.getVersion(), versionRange)) {
				return pack;
			}
		}
		return null;
	}



	protected Map<String, ICpPack> getPackMap() {
		if(fPacks == null) {
			fPacks = new TreeMap<>(new VersionComparator());
		}
		return fPacks;
	}


	@Override
	public Collection<ICpPack> getPacks() {
		return getPackMap().values();
	}


	@Override
	public Collection<? extends ICpItem> getChildren() {
		return getPacks();
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
		if (pack.getParent() == null) {
			pack.setParent(this);
		}
		if (pack.getPackState() == PackState.ERROR) {
			getPackMap().put(pack.getTag(), pack);
		} else {
			String version = pack.getVersion();
			ICpPack inserted =  getPackMap().get(version);
			if(inserted == null || inserted.getPackState().ordinal() > pack.getPackState().ordinal()) {
				getPackMap().put(version, pack);
			}
		}
	}

	@Override
	public ICpPack getPackByFilename(String pdscFile) {
		if(fPacks != null) {
			for(ICpPack pack : getPackMap().values()){
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
			return EMPTY_LIST;
		}
		Collection<? extends ICpItem> releases = pack.getReleases();
		if (releases == null || releases.isEmpty()) {
			return EMPTY_LIST;
		}
		Map<String, ICpItem> previousReleases = new TreeMap<>(new VersionComparator());
		for(ICpItem item : releases) {
			String version = item.getAttribute(CmsisConstants.VERSION);
			if(fPacks == null || !getPackMap().containsKey(version)) {
				previousReleases.put(version, item);
			}
		}
		return previousReleases.values();
	}

	@Override
	protected Object[] createChildArray() {
		fPreviousReleases = null;
		Collection<ICpItem> children = new LinkedList<>();
		children.addAll(getPacks());
		ICpItem previousReleases = getPreviousReleases(); // refresh previous release info
		if(previousReleases != null){
			children.add(fPreviousReleases);
		}
		if(!children.isEmpty()) {
			return children.toArray();
		}
		return EMPTY_OBJECT_ARRAY;
	}

}

