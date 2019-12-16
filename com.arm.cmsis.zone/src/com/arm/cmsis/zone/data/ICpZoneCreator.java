/*******************************************************************************
* Copyright (c) 2019 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.zone.data;

import com.arm.cmsis.pack.permissions.IMemoryPriviledge;
import com.arm.cmsis.pack.permissions.IMemorySecurity;

/**
 * Interface for a zone creator element
 */
public interface ICpZoneCreator extends ICpZoneItem, IMemorySecurity, IMemoryPriviledge {

	/**
	 * Retrieves name of a tool created the zone
	 * @return String
	 */
	String getTool();
	
	/**
	 * Sets tool name  
	 * @param tool tool name and version
	 */
	void setTool(String tool);
	
	
}
