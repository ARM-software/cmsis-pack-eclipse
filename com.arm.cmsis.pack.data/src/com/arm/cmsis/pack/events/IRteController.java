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

package com.arm.cmsis.pack.events;

import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.generic.ICommitable;

/**
 *  Interface represents a controller to edit underlying configuration.</br>
 */
public interface IRteController extends ICommitable, IRteEventProxy{
	
	/**
	 * Returns model-specific data info
	 * @return model-specific data info
	 */
	ICpItem getDataInfo();
	
	/**
	 * Sets model-specific data info
	 * @param info model-specific data info
	 */
	void setDataInfo(ICpItem info);
	
	/**
	 * Updates model-specific data info
	 */
	void updateDataInfo();
	
	/**
	 * Opens an URL in a browser or associated system editor 
	 * @param url URL to open
	 * @return null if successfully opened, otherwise reason why operation failed
	 */
	String openUrl(String url);

	
}
