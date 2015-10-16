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
