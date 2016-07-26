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

import com.arm.cmsis.pack.data.ICpFile;

/**
 *  Interface representing a component's file used by configuration 
 */
public interface ICpFileInfo extends ICpFile, ICpItemInfo {

	/**
	 * Returns actual ICpFile file corresponding to this info  
	 * @return actual ICpFile represented by the info
	 */
	ICpFile getFile();
	
	/**
	 * Sets actual resolved ICpFile to this info  and optionally updates info attributes
	 * @param file ICpFile to set
	 */
	void setFile(ICpFile file);
	
	
	/**
	 * Returns parent ICpComponentInfo if this file is a child of a component or an API 
	 * @return parent ICpComponentInfo
	 */
	ICpComponentInfo getComponentInfo();
	
	
	/**
	 * Sets file version to this object, either from actual ICpFile or a stored value when the file has been copied 
	 * @param version version string to set
	 */
	void setVersion(String version);
	
	/**
	 * Compares version stored in this info to actual file version
	 * @return version difference
	 */
	int getVersionDiff(); 
}
