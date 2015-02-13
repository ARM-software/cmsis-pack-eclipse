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

package com.arm.cmsis.pack.rte;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.arm.cmsis.pack.data.CpPack;
import com.arm.cmsis.pack.data.ICpComponent;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPackFilter;
import com.arm.cmsis.pack.utils.AlnumComparator;
import com.arm.cmsis.pack.utils.VersionComparator;

/**
 * Class that encapsulates filtered and ordered collection of Packs 
 *
 */
public class RtePackCollection {

	private Map<String, ICpPack> fPacks = null; // all packs in the collection
	private Map<String, ICpPack> fLatestPacks = null; // only the latest versions of packs (common id to package) 
	private ICpPackFilter fPackFilter = null;
	
	private Map<String, ICpComponent> fComponents = null; // sorted map of all components
	private Map<String, ICpComponent> fApis = null; // sorted map of all apis
	private Map<String, ICpItem> fTaxonomy = null; //map of taxonomy descriptions
	
	/**
	 *  Default constructor 
	 */
	public RtePackCollection() {
		fPacks = new TreeMap<String, ICpPack>(new AlnumComparator());
		fLatestPacks = new HashMap<String, ICpPack>();
	}

	public void clear() {
		fPacks = null; 
		fLatestPacks = null; // only the latest versions of packs (common id to package) 
		fPackFilter = null;
		fComponents = null;
		fTaxonomy = null;
	}
	
	
	public void setFilter(ICpPackFilter filter) {
		fPackFilter = filter;
	}

	/**
	 * @return the all packs in the collection 
	 */
	public Map<String, ICpPack> getPacks() {
		return fPacks;
	}

	/**
	 * @return latest packs in the collection 
	 */
	public Map<String, ICpPack> getLatestPacks() {
		return fLatestPacks;
	}

	/**
	 * @return pack filter 
	 */
	public ICpPackFilter getPackFilter() {
		return fPackFilter;
	}

	/**
	 * @return component collection
	 */
	public Map<String, ICpComponent> getfComponents() {
		if(fComponents == null)
			refreshCollections();
		
		return fComponents;
	}

	
	/**
	 *  Collect components from all packs  
	 */
	private void refreshCollections() {
		fComponents = new TreeMap<String, ICpComponent>(new AlnumComparator());
		fApis = new TreeMap<String, ICpComponent>(new AlnumComparator());
		fTaxonomy = new HashMap<String, ICpItem>();
		for(ICpPack pack : fPacks.values()){
			collectComponents(pack);
			collectApis(pack);
			collectTaxonomy(pack);
		}
	}


	/**
	 * Collect components from given pack 
	 * @param pack
	 */
	private void collectComponents(ICpPack pack) {
		Collection<? extends ICpItem> children = pack.getChildren("components");
		if(children == null || children.isEmpty())
			return;
		for(ICpItem item : children){
			if(item instanceof ICpComponent) { // should always work
				ICpComponent c = (ICpComponent) item;
				String id = c.getId();
				if(fComponents.containsKey(id))
					continue; // the component is already added from a newer pack
				fComponents.put(id, c);
			}
		}
	}

	/**
	 * Collect APIs from given pack
	 * @param pack
	 */
	private void collectApis(ICpPack pack) {
		Collection<? extends ICpItem> children = pack.getChildren("apis");
		if(children == null || children.isEmpty())
			return;
		for(ICpItem item : children){
			if(item instanceof ICpComponent) { // should always work
				ICpComponent a = (ICpComponent) item;
				String id = a.getId();
				if(fApis.containsKey(id))
					continue; // the component is already added from a newer pack
				fApis.put(id, a);
			}
		}
	}

	/**
	 * Collect taxonomy from given pack
	 * @param pack
	 */
	private void collectTaxonomy(ICpPack pack) {
		Collection<? extends ICpItem> children = pack.getChildren("taxonomy");
		if(children == null || children.isEmpty())
			return;
		for(ICpItem item : children){
			String id = item.getId();
			if(fTaxonomy.containsKey(id))
				continue; // the component is already added from a newer pack
			fTaxonomy.put(id, item);
		}
	}


	/**
	 * Adds Pack to collection if it passes the filter
	 * @param pack Pack to add
	 * @return true if Pack has been added, false otherwise
	 */
	public boolean addPack(ICpPack pack){
		if(pack == null)
			return false;
		if(fPackFilter == null || fPackFilter.passes(pack)) {
			String id = pack.getId();
			if(fPacks.containsKey(id))
				return false;
			fPacks.put(id, pack);
			String familyId = CpPack.familyFromId(id);
			
			ICpPack latestPack = fLatestPacks.get(familyId);
			if(latestPack == null || 
				VersionComparator.versionCompare(pack.getVersion(), latestPack.getVersion()) > 0) {
				fLatestPacks.put(familyId, pack);
			}
		
			return true;
		}
		return false;
	}
	
	/**
	 * Returns Pack with given ID
	 * @param id Pack ID with version 
	 * @return Pack if found in the collection
	 */
	public ICpPack getPack(final String id){
		String familyId = CpPack.familyFromId(id);
		if(familyId.equals(id))
			return fLatestPacks.get(familyId);
		return fPacks.get(id);
	}

	/**
	 * Returns latest Pack for given ID
	 * @param id Pack ID with or without version 
	 * @return Pack if found in the collection
	 */
	public ICpPack getLatestPack(final String id){
		String familyId = CpPack.familyFromId(id); 
		return fLatestPacks.get(familyId);
	}

	/**
	 * Checks if the pack with given ID is the latest
	 * @param id Pack ID with version 
	 * @return Pack if found in the collection
	 */
	public boolean isLatest(final String id){
		String familyId = CpPack.familyFromId(id); 
		return fLatestPacks.containsKey(familyId);
	}
	
	
	/**
	 * Returns first Pack with ID beginning with the given prefix 
	 * @param prefix Pack ID prefix 
	 * @return Pack if found in the collection
	 */
	public ICpPack getFirstPack(final String prefix){
		for(Entry<String, ICpPack> e : fPacks.entrySet()) {
			String packId = e.getKey();
			if(packId.startsWith(prefix))
				return e.getValue();
		}
		return null;
	}
	
}
