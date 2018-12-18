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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPackFamily;
import com.arm.cmsis.pack.enums.EVersionMatchMode;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.info.ICpPackInfo;
import com.arm.cmsis.pack.utils.VersionComparator;

/**
 * 
 */
public class RtePackFamily extends RtePackItem implements IRtePackFamily {

	protected String fId = null;
	protected Map<String, IRtePack> fPacks = new TreeMap<String, IRtePack>(new VersionComparator());
	protected EVersionMatchMode fVersionMatchMode = EVersionMatchMode.EXCLUDED; // until pack filter says other way
	protected ICpPackFamily fPackFamily = null; 
	
	public RtePackFamily(IRtePackItem parent, ICpPackFamily packFamily) {
		this(parent, packFamily.getId());
		addCpItem(packFamily);
	}
	
	public RtePackFamily(IRtePackItem parent, String id) {
		super(parent);
		fId = id;
	}

	@Override
	public void clear() {
		super.clear();
		fPacks = null;
		fPackFamily = null;
		fId = null;
	}

	
	@Override
	public boolean purge() {
		if(fPacks != null && ! fPacks.isEmpty()) {
			for (Iterator<IRtePack> iterator = fPacks.values().iterator(); iterator.hasNext();) {
				IRtePack pack = iterator.next();
				if(pack.purge()) {
					iterator.remove();
				}
			}	
		}
		
		if(fPacks == null || fPacks.isEmpty()) {
			clear();
			return true;
		}
		return false;
	}

	@Override
	public String getId() {
		return fId;
	}

	@Override
	public boolean isSelected() {
		for(IRtePack pack : fPacks.values()){
			if(pack.isSelected())
				return true;
		}
		return false;
	}

	protected boolean isExplicitlySelected() {
		for(IRtePack pack : fPacks.values()){
			if(pack.isExplicitlySelected())
				return true;
		}
		return false;
	}

	
	
	@Override
	public IRtePackFamily getFamily() {
		return this;
	}


	@Override
	public boolean isUsed() {
		for(IRtePack pack : fPacks.values()){
			if(pack.isUsed())
				return true;
		}
		return false;
	}

	@Override
	public ICpPack getPack() {
		IRtePack rtePack = getLatestRtePack();
		if(rtePack != null )
			return rtePack.getPack();
		return null;
	}

	@Override
	public ICpPackInfo getPackInfo() {
		IRtePack rtePack = getLatestRtePack();
		if(rtePack != null )
			return rtePack.getPackInfo();
		return null;
	}

	@Override
	public boolean isInstalled() {
		if(fPacks.isEmpty())
			return false;
		for(IRtePack pack : fPacks.values()){
			if(!pack.isInstalled() && pack.isSelected())
				return false;
		}
		return true;
	}

	@Override
	public boolean isExcluded() {
		return getVersionMatchMode() == EVersionMatchMode.EXCLUDED;
	}
	
	@Override
	public EVersionMatchMode getVersionMatchMode() {
		if(isUseAllLatestPacks())
			return EVersionMatchMode.LATEST;
		return fVersionMatchMode;
	}

	
	@Override
	public void setVersionMatchMode(EVersionMatchMode mode) {
		fVersionMatchMode = mode;
		if(mode == EVersionMatchMode.FIXED && !isExplicitlySelected()){
			IRtePack pack= getLatestRtePack();
			if(pack != null)
				pack.setSelected(true); 
		}
	}
	
	@Override
	public void updateVersionMatchMode() {
		if(fVersionMatchMode != EVersionMatchMode.LATEST) {
			if(isExplicitlySelected())
				fVersionMatchMode = EVersionMatchMode.FIXED;
			else 
				fVersionMatchMode = EVersionMatchMode.EXCLUDED;
		}
	}

	
	@Override
	public IRtePack getLatestRtePack() {
		if(fPacks.isEmpty())
			return null;
		return fPacks.entrySet().iterator().next().getValue();
	}

	@Override
	public IRtePack getRtePack(String version) {
		if(version == null || version.isEmpty())
			return getLatestRtePack();
		return fPacks.get(version);
	}
	
	@Override
	public IRtePackItem getFirstChild() {
		return getLatestRtePack();
	}

	@Override
	public void addCpItem(ICpItem item) {
		if(item instanceof ICpPackFamily) {
			fPackFamily = (ICpPackFamily)item;
			Collection<? extends ICpItem> children = fPackFamily.getChildren(); 
			if(children == null)
				return;
			for(ICpItem child : children) {
				addCpItem(child);
			}
		} else if(item instanceof ICpPack || item instanceof ICpPackInfo) {
			addRtePackItem(item);
		}
	}

	protected void addRtePackItem(ICpItem item) {
		String version = item.getVersion();
		IRtePack rtePack = getRtePack(version);
		if(rtePack == null) { 
			rtePack = new RtePack(this, item);
			fPacks.put(version,  rtePack);
		} else {
			rtePack.addCpItem(item);
		}
	}

	@Override
	public ICpItem getCpItem() {
		return fPackFamily;
	}

	@Override
	public Object[] getChildArray() {
		return fPacks.values().toArray();
	}
	
	@Override
	public boolean hasChildren() {
		return !fPacks.isEmpty();
	}
	
	@Override
	public int getChildCount() {
		return fPacks.size();
	}

	@Override
	public String getVersion() {
		switch(getVersionMatchMode()){
		case FIXED:
			break;
		case LATEST:
			IRtePackItem rtePack = getLatestRtePack();
			if(rtePack != null)
				return rtePack.getVersion();
		case EXCLUDED:
		default:
			return CmsisConstants.EMPTY_STRING;
		}

		String version = CmsisConstants.EMPTY_STRING;  
		for(IRtePack pack : fPacks.values()){
			if(pack.isSelected()) {
				if(!version.isEmpty())
					version += " ,"; //$NON-NLS-1$
				version += pack.getVersion();
			}
		}
		return version;
	}

	@Override
	public Set<String> getSelectedVersions() {
		Set<String> versions = new HashSet<String>();  
		for(IRtePack pack : fPacks.values()){
			if(pack.isSelected()) {
				String version = VersionComparator.removeMetadata(pack.getVersion()); 
				versions.add(version);
			}
		}	
		return versions;
	}

	@Override
	public Collection<IRtePack> getSelectedPacks() {
		Collection<IRtePack> packs = new LinkedList<IRtePack>();  
		for(IRtePack pack : fPacks.values()){
			if(pack.isSelected())
				packs.add(pack);
		}	
		return packs;
	}

	@Override
	public IAttributes getAttributes() {
		IRtePack p = getLatestRtePack();
		if(p != null)
			return p.getAttributes();
		return null;
	}
	
	
}
