/*******************************************************************************
* Copyright (c) 2017 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/
package com.arm.cmsis.zone.svd;

import com.arm.cmsis.pack.data.ICpItem;

/**
 * Base item for SVD file parser  
 *   
 */
public interface ISvdItem extends ICpItem {

	/**
	 * Returns SVD root item 
	 * @return ISvdRoot
	 */
	ISvdRoot getSvdRoot();
	
	
}
