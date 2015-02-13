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

/**
 * Interface to filter Packs using user-defined criteria   
 */
public interface ICpPackFilter {

	/**
	 * Checks if Pack with given ID passes the filter
	 * @param packId Pack ID to check: Note the pack ID can be with or without version!   
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
	 * @param packId pack ID or pack family ID
	 * @return
	 */
	boolean isUseLatest(final String packId);

	/**
	 * Checks if pack with supplied ID is excluded  
	 * @param packId pack ID or pack family ID
	 * @return
	 */
	boolean isExcluded(final String packId);

	
	/**
	 * Check is to latest versions of all installed packs 
	 * @return true if only latest versions of packs should be used
	 */
	public boolean isUseAllLatestsPacks();
	
	
}
