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
import java.util.Set;

/**
 * Interface to collection of pack families
 * @see ICpPackFamily
 */
public interface ICpPackCollection extends ICpPackFamily {

	/**
	 * Returns full pack collection as sorted by ID (ascending and version descending) 
	 * @return full pack collection 
	 */
	Collection<ICpPack> getPacks();
	

	/**
	 * Returns pack collection of the latest installed packs  
	 * @return collection of the latest packs 
	 */
	Collection<ICpPack> getLatestPacks();

	/**
	 * Returns set of latest packs IDs   
	 * @return set of latest pack IDs 
	 */
	Set<String> getLatestPackIDs();
	
	
	/**
	 * Returns pack collection of filtered packs according to supplied filter  
	 * @return collection of filtered packs, if filter is null, the entire collection is returned 
	 */
	Collection<ICpPack> getFilteredPacks(ICpPackFilter packFilter);
	
	
}
