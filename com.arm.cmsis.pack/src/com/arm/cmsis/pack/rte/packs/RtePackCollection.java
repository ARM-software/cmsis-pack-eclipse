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

package com.arm.cmsis.pack.rte.packs;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpPack;
import com.arm.cmsis.pack.data.CpPackFilter;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPackCollection;
import com.arm.cmsis.pack.data.ICpPackFamily;
import com.arm.cmsis.pack.data.ICpPackFilter;
import com.arm.cmsis.pack.enums.EVersionMatchMode;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.info.CpPackFilterInfo;
import com.arm.cmsis.pack.info.CpPackInfo;
import com.arm.cmsis.pack.info.ICpPackFilterInfo;
import com.arm.cmsis.pack.info.ICpPackInfo;
import com.arm.cmsis.pack.utils.AlnumComparator;

/**
 * Class that encapsulates filtered and ordered collection of Packs 
 *
 */
public class RtePackCollection extends RtePackItem implements IRtePackCollection {

	protected ICpPackCollection fPackCollection = null;
	protected Map<String, IRtePackFamily> fPackFamilies = new TreeMap<String, IRtePackFamily>(new AlnumComparator(false, false));
	Map<String, ICpPackInfo> fUsedPacks = null; 
	
	
	boolean fbUseAllLatest = true;
	
	public RtePackCollection() {
		super(null);
	}

	@Override
	public void clear() {
		super.clear();
		fPackFamilies = null;
		fUsedPacks = null;
		fPackCollection = null;
	}

	
	@Override
	public boolean purge() {
		if(fPackFamilies != null && ! fPackFamilies.isEmpty()) {
			for (Iterator<IRtePackFamily> iterator = fPackFamilies.values().iterator(); iterator.hasNext();) {
				IRtePackFamily family = iterator.next();
				if(family.purge()) {
					iterator.remove();
				}
			}	
		}
		return false;
	}

	
	
	@Override
	public IRtePackCollection getRoot() {
		return this;
	}


	@Override
	public String getId() {
		if(fPackCollection != null)
			return fPackCollection.getId();
		return null;
	}

	@Override
	public void setPackFilterInfo(ICpPackFilterInfo packFilterInfo) {
		if(packFilterInfo == null)
			return;
		fbUseAllLatest = packFilterInfo.isUseAllLatestPacks();
		// set version match modes and selection
		Collection<? extends ICpItem> children = packFilterInfo.getChildren(); 
		if(children == null)
			return;
		for(ICpItem item : children){
			if(!(item instanceof ICpPackInfo))
				continue;
			addCpItem(item);
			ICpPackInfo packInfo = (ICpPackInfo)item;
			
			String familyId = packInfo.getPackFamilyId();
			IRtePackFamily packFamily = getRtePackFamily(familyId);
			EVersionMatchMode mode = packInfo.getVersionMatchMode();
			// first select packs with fixed version to avoid auto-selection 
			if(mode == EVersionMatchMode.FIXED){
				String version = packInfo.getVersion();
				IRtePack pack = packFamily.getRtePack(version);
				pack.setSelected(true);
			}
			packFamily.setVersionMatchMode(mode);
		}
	}
	
	@Override
	public void addCpItem(ICpItem item) {
		if(item == null)
			return;
		if(item instanceof ICpPackFilterInfo || item instanceof ICpPackCollection) {
			if(item instanceof ICpPackCollection )
				fPackCollection = (ICpPackCollection)item;
			Collection<? extends ICpItem> children = item.getChildren(); 
			if(children == null)
				return;
			for(ICpItem child : children){
				addCpItem(child);
			}
		} else { 
			addRtePackFamily(item);
		} 
	}

	protected void addRtePackFamily(ICpItem item) {
		if(item instanceof ICpPackFamily || item instanceof ICpPack || item instanceof ICpPackInfo) {
			String familyId = CpPack.familyFromId(item.getId());
			IRtePackFamily rtePackFamily = ensurePackFamily(familyId);
			rtePackFamily.addCpItem(item);
		} 
	}

	protected IRtePackFamily ensurePackFamily(String familyId) {
		IRtePackFamily rtePackFamily = getRtePackFamily(familyId);
		if(rtePackFamily == null) {
			rtePackFamily = new RtePackFamily(this, familyId);
			fPackFamilies.put(familyId, rtePackFamily);
		}
		return rtePackFamily;
	}
	
	@Override
	public ICpItem getCpItem() {
		return fPackCollection;
	}

	@Override
	public boolean isSelected() {
		for(IRtePackFamily f : fPackFamilies.values()) {
			if(f.isSelected())
				return true;
		}
		return false;
	}

	@Override
	public boolean isUsed() {
		for(IRtePackFamily f : fPackFamilies.values()) {
			if(f.isUsed())
				return true;
		}
		return false;
	}

	@Override
	public boolean isInstalled() {
		if(fPackFamilies.isEmpty())
			return false;
		for(IRtePackFamily f : fPackFamilies.values()) {
			if(!f.isInstalled())
				return false;
		}
		return true;
	}
	
	@Override
	public boolean isExcluded() {
		return false;
	}

	@Override
	public Object[] getChildArray() {
		return fPackFamilies.values().toArray();
	}
	
	@Override
	public boolean hasChildren() {
		return !fPackFamilies.isEmpty();
	}
	
	@Override
	public int getChildCount() {
		return fPackFamilies.size();
	}


	@Override
	public boolean isUseAllLatestPacks() {
		return fbUseAllLatest;
	}

	@Override
	public void setUseAllLatestPacks(boolean bUseLatest) {
		fbUseAllLatest = bUseLatest;
		if(fbUseAllLatest) 
			return;
		// select all used packs as fixed and all other as excluded
		for(IRtePackFamily f : fPackFamilies.values()) {
			if(f.isUsed())
				f.setVersionMatchMode(EVersionMatchMode.FIXED);
			else
				f.setVersionMatchMode(EVersionMatchMode.EXCLUDED);
		}
	}

	@Override
	public IRtePackFamily getRtePackFamily(String familyId) {
		return fPackFamilies.get(familyId);
	}

	@Override
	public String getVersion() {
		return CmsisConstants.EMPTY_STRING;
	}

	@Override
	public IRtePackItem getFirstChild() {
		if(!fPackFamilies.isEmpty())
			return fPackFamilies.entrySet().iterator().next().getValue();
		return null;
	}
	
	@Override
	public ICpPackFilter createPackFiler() {
		CpPackFilter filter = new CpPackFilter();
		filter.setUseAllLatestPacks(fbUseAllLatest);
		if(fbUseAllLatest)
			return filter;
		
		for(IRtePackFamily f : fPackFamilies.values()) {
			String familyId = f.getId();
			Set<String> versions = null;
			switch(f.getVersionMatchMode()){
			case FIXED:
				versions = f.getSelectedVersions();
			case LATEST:
				filter.setFixed(familyId, versions);
				break;
			case EXCLUDED:
				filter.setExcluded(familyId, true);
				break;
			default:
				break;
			}
		}
		return filter;
	}

	@Override
	public ICpPackFilterInfo createPackFilterInfo() {
		CpPackFilterInfo filterInfo = new CpPackFilterInfo(null);
		filterInfo.setUseAllLatestPacks(fbUseAllLatest);
		if(!fbUseAllLatest) {
			for(IRtePackFamily f : fPackFamilies.values()) {
				EVersionMatchMode mode = f.getVersionMatchMode(); 
				switch(mode){
				case EXCLUDED:
					continue;
				case LATEST:
					ICpPackInfo info = new CpPackInfo(filterInfo, f.attributes());
					info.setVersionMatchMode(mode);
					filterInfo.addChild(info);
					continue;
				case FIXED:
					break;
				}
				Collection<IRtePack> packs = f.getSelectedPacks();
				for(IRtePack p : packs){
					ICpPackInfo info = null;
					ICpPack pack = p.getPack();
					if(pack != null)
						info = new CpPackInfo(filterInfo, pack);
					else
						info = new CpPackInfo(filterInfo, p.attributes());
					info.setVersionMatchMode(mode);
					filterInfo.addChild(info);
				}
			}
		}
		return filterInfo;
	}

	@Override
	public void setUsedPacks(Map<String, ICpPackInfo> usedPackInfos) {
		fUsedPacks = usedPackInfos;
		// ensure all items exist
		if(fUsedPacks != null && !fUsedPacks.isEmpty()) {
			for(Entry<String, ICpPackInfo> e : usedPackInfos.entrySet()){
				addCpItem(e.getValue());
			}
		}
	}


	@Override
	public boolean isPackUsed(String id) {
		if(fUsedPacks == null || fUsedPacks.isEmpty())
			return false;
		return fUsedPacks.containsKey(id);
	}

	@Override
	public IAttributes attributes() {
		return null;
	}
	
	@Override
	public Collection<IRtePackFamily> getRtePackFamilies() {
		return fPackFamilies.values();
	}

	@Override
	public Collection<IRtePackFamily> getUsedRtePackFamilies() {

		List<IRtePackFamily> usedFamilies = new LinkedList<>(); 
		for(IRtePackFamily f : fPackFamilies.values()) {
			if(f.isUsed()) {
				usedFamilies.add(f);
			}
		}
		return usedFamilies;
	}
}
