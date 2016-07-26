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


/**
 * Base interface for pack container family or collection
 * 
 */
public interface ICpPackGroup extends ICpItem {
	/**
	 * Returns pack of specified version if any
	 * @param packId pack ID or version string to get pack
	 * @return ICpPack object or null if not found
	 */
	ICpPack getPack(final String packId);

	
	/**
	 * Searches for pack loaded from given pdsc file 
	 * @param pdscFile file name corresponding to pack
	 * @return ICpPack object or null if not found
	 */
	ICpPack getPackByFilename(final String pdscFile);
	
	/**
	 * Returns collection of packs in the group sorted by ID (ascending and version descending) 
	 * @return collection of ICpPack objects 
	 */
	Collection<ICpPack> getPacks();

}
