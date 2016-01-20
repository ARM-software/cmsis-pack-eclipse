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

import java.util.Set;

/**
 * Represents CMISIS Pack meta data read from pdsc file
 * Provides access method to underlying structure of the Pack  
 */
public interface ICpPack extends ICpItem {

	/**
	 * Describes Pack state:  
	 * <dl>
	 * 	<dd>INSTALLED pack is locally installed   
		<dd>AVAILABLE pack is available for download
		<dd>DOWNLOADED pack is downloaded, but not installed
		<dd>GENERATED generator Pack read from gpdsc file
		<dd>UNKNOWN   Pack state is not defined
	 * </dl>
	 * 
	 *
	 */
	enum PackState {
		INSTALLED,	
		DOWNLOADED, 
		AVAILABLE,  
		GENERATED,
		UNKNOWN
	}
	
	
	/**
	 * Sets pack state
	 * @return <code>PackState<code> of the Pack
	 * @see PackState
	 */
	PackState getPackState();
	
	/** 
	 * Returns pack state
	 * @param state PackState to set
	 * @see PackState
	 */
	void setPackState(PackState state);
	
	/**
	 * Returns absolute pdsc/gpdsc file name of the pack 
	 * @return absolute file name of the pack
	 */
	String getFileName();

	/**
	 * Returns absolute path to directory where pack is or must be installed 
	 * @return absolute path to the pack
	 */
	String getInstallDir(String packRoot);

	/**
	 * Set absolute pdsc file name of the pack 
	 * @param fileName absolute pdsc file name of the pack 
	 */
	void setFileName(String fileName);

	/**
	 * Returns condition corresponding to supplied ID 
	 * @param conditionId id of the condition to find
	 * @return condition as ICpItem or null if condition with such id does not exist in the pack
	 */
	ICpItem getCondition(String conditionId);

	/**
	 * Get all the Devices contained in this pack
	 * @return A set of {@link ICpDeviceItem} names
	 */
	Set<String> getAllDeviceNames();
	
	/**
	 * Check if this pack is generic or not
	 * @return true if this pack is generic
	 */
	boolean isDevicelessPack();

}
