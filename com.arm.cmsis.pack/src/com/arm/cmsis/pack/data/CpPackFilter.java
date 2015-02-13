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
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class CpPackFilter implements ICpPackFilter {

	private boolean fbUseAllLatestsPacks = true;
	private Map<String, Set<String> > fPackVersions = null; // filtered pack IDs (common id , set of versions)   
	private Set<String> fLatestPackIDs = null;  
	
	/**
	 * Default empty constructor 
	 */
	public CpPackFilter() {
		
	}

	@Override
	public boolean passes(final String packId) {
		if(fbUseAllLatestsPacks)
			return isLatest(packId);
		
		if(fPackVersions == null || fPackVersions.isEmpty())
			return false; // no packs to use
		
		String familyId = CpPack.familyFromId(packId);
		Set<String> versions = fPackVersions.get(familyId);
		if(versions == null)
			return false;
		
		String version  = CpPack.versionFromId(packId);		
		if(versions.isEmpty()) { // use latest
			if(version.isEmpty()) 
				return true; // family Id is supplied => check for latest is wanted
			return isLatest(packId);
		}

		return versions.contains(version);		
	}

	/**
	 * @return the fLatestPackIds
	 */
	public Set<String> getLatestPackIDs() {
		return fLatestPackIDs;
	}
	/**
	 * @param fLatestPackIds the fLatestPackIds to set
	 */
	public void setLatestPackIds(Set<String> latestPackIDs) {
		fLatestPackIDs = latestPackIDs;
	}
	
		
	public boolean isLatest(final String packId) {
		if(fLatestPackIDs != null)
			return fLatestPackIDs.contains(packId);
		return false;
	}
	
	
	@Override
	public boolean passes(ICpPack pack) {
		if(pack == null)
			return false;
		
		return passes(pack.getId());
	}

	@Override
	public Collection<ICpPack> filter(final Collection<ICpPack> packs) {
		Collection<ICpPack> filtered = new HashSet<ICpPack>(); 
		if(packs != null) {
			for(ICpPack pack : packs){
				if(passes(pack))
					filtered.add(pack);
			}
		}
		return filtered;
	}

	public boolean isUseAllLatestsPacks() {
		return fbUseAllLatestsPacks;
	}

	/**
	 * Sets filter to use latest versions of all installed packs 
	 * @param fbUseAllLatestsPacks the fbUseAllLatestsPacks to set
	 */
	public void setUseAllLatestsPacks(boolean bUseAllLatestsPacks) {
		fbUseAllLatestsPacks = bUseAllLatestsPacks;
	}

	@Override
	public boolean isUseLatest(String packId) {
		if(fbUseAllLatestsPacks)
			return true;
		if(fPackVersions == null)
			return false;
		String familyId = CpPack.familyFromId(packId);
		Set<String> versions =  fPackVersions.get(familyId);
		if(versions == null)
			return false;
		return versions.isEmpty(); // entry exists, but empty => use latest 
	}

	@Override
	public boolean isExcluded(String packId) {
		if(fPackVersions == null)
			return true;
		String familyId = CpPack.familyFromId(packId);
		Set<String> versions =  fPackVersions.get(familyId);
		if(versions == null)
			return true;
		String version = CpPack.versionFromId(packId);
		return !version.isEmpty() && !versions.contains(version); // entry exists, but empty => use latest 
	}
	
	
}
