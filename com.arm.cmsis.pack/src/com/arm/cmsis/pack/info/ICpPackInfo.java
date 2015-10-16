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

package com.arm.cmsis.pack.info;

import com.arm.cmsis.pack.data.ICpPack;

/**
 * Interface describing pack meta data 
 */
public interface ICpPackInfo extends ICpItemInfo {
	
	/**
	 * Sets actual pack to this info
	 * @param pack actual CMSISA pack
	 */
	void setPack(ICpPack pack);
	
}
