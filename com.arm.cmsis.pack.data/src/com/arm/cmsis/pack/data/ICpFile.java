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

import com.arm.cmsis.pack.enums.EFileCategory;
import com.arm.cmsis.pack.enums.EFileRole;

/**
 * Interface describing component's file item 
 */
public interface ICpFile extends ICpItem {
	
	/**
	 * Returns file category corresponding "category" attribute in pdsc file 
	 * @return file category as EFileCategory value
	 * @see EFileCategory
	 */
	EFileCategory getCategory();
	
	/**
	 * Returns file role corresponding "attr" attribute in pdsc file
	 * @return file role as EFileRole enum value
	 * @see EFileRole
	 */
	EFileRole getRole();
}
