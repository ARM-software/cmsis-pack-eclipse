package com.arm.cmsis.pack.data;

/*******************************************************************************
 * Copyright (c) 2016 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/

/**
 * Interface for ICpItem-based tree root element (ICpPack, ICpConfigurationInfo, etc.)  
 */
public interface ICpRootItem extends ICpItem {
	
	/**
	 * Set absolute pdsc file name of the pack 
	 * @param fileName absolute pdsc file name of the pack 
	 */
	void setFileName(String fileName);

	/**
	 * Returns absolute file name of the root item 
	 * @return absolute file name of the root item 
	 */
	String getFileName();
	
}
