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
import java.util.Map;
import java.util.Set;

/**
 * Interface to filter Packs using user-defined criteria   
 */
public interface ICpPackFilter {

	/**
	 * Checks if Pack with given ID passes the filter
	 * @param packId Pack ID or pack family ID   
	 * @return true if Pack passes the filter
	 */
	boolean passes(final String packId);

	/**
	 * Checks if given Pack passes the filter
	 * @param pack Pack to check
	 * @return true if Pack passes the filter
	 */
	boolean passes(final ICpPack pack);
	
	/**
	 * Filters collection of Packs  
	 * @param packs collection of Packs to filter 
	 * @return filtered Pack collection
	 */
	Collection<ICpPack> filter(final Collection<ICpPack> packs); 
	
	
	/**
	 * Checks if pack with supplied ID  
	 * @param packId pack family ID, if pack ID with version is supplied family ID is extracted 
	 * @return
	 */
	boolean isUseLatest(final String packId);
	
	/**
	 * Set to use latest pack version from given family 
	 * @param packFamilyId pack family ID (family )
	 */
	void setUseLatest(final String packFamilyId);

	/**
	 * Checks if pack with supplied ID is excluded  
	 * @param packId pack ID or pack family ID, if ID contains version, family ID is extracted
	 * @return true if pack with supplied ID is excluded
	 */
	boolean isExcluded(final String packId);

	/**
	 * Excludes or includes given pack family  
	 * @param packId pack ID or pack family ID, if ID contains version, family ID is extracted
	 * @param excluded flag to exclude/include pack family
	 */
	void setExcluded(final String packId, boolean excluded);
	
	/**
	 * Check is to latest versions of all installed packs 
	 * @return true if only latest versions of packs should be used
	 */
	boolean isUseAllLatestPacks();
	
	/**
	 * Sets if to use latest versions of all installed packs 
	 * @param bUseLatest flag if to use only latest versions of packs
	 */
	void setUseAllLatestPacks(boolean bUseLatest);

	/**
	 * Checks if specified pack uses fixed pack version 
	 * @param packId pack family ID
	 * @return true if fixed
	 */
	boolean isFixed(final String packId); 
	
	/**
	 * Sets fixed pack version to use
	 * @param packId pack ID with version, if fixed is <code>false</code> family ID can be used 
	 * @param fixed if to use pack with fixed version 
	 */
	void setFixed(final String packId, boolean fixed);
	
	/**
	 * Sets fixed pack versions to use for given pack family
	 * @param familyId pack family ID  
	 * @param set of fixed pack versions to use or <code>null</code> to use latest  
	 */
	void setFixed(final String familyId, Set<String> fixedVersions);
	
	
	/**
	 * Returns set of fixed versions for given family<br>
	 * if returned set is empty or null, the family is excluded or uses latest version  
	 * @param packId family ID or pack ID, if ID contains version, family ID is extracted
	 * @return set of version strings   
	 */
	Set<String> getVersions(final String packId);
	
	
	/**
	 * Returns map of collection of pack versions 
	 * @return map of filtered pack versions  
	 */
	Map<String, Set<String> > getFilterdPackVersions();
	
	/**
	 * Sets latest pack IDs to the filter 
	 * @param latestPackIds set of latest Pack IDs
	 */
	void setLatestPackIDs(Set<String> latestPackIDs);
	
	
	/**
	 * Returns latest pack IDs used by filter 
	 * @return set of latest Pack IDs
	 */
	Set<String> getLatestPackIDs();
}
