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
 * Represents CMISIS Pack meta data read from pdsc file
 * Provides access method to underlying structure of the Pack
 */
public interface ICpPack extends ICpRootItem {

	/**
	 * Describes Pack state:
	 * <dl>
	 * 	<dd>INSTALLED  pack is locally installed
		<dd>DOWNLOADED pack is download, but not installed
		<dd>AVAILABLE  pack is available for download
		<dd>GENERATED  generator pack read from gpdsc file
		<dd>UNKNOWN    pack state is not defined
		<dd>ERROR 	   pack has error
	 * </dl>
	 *
	 *
	 */
	enum PackState {
		INSTALLED,
		DOWNLOADED,
		AVAILABLE,
		GENERATED,
		UNKNOWN,
		ERROR
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
	 * Returns absolute path to directory where pack is or must be installed
	 * @return absolute path to the pack
	 */
	String getInstallDir(String packRoot);

	/**
	 * Returns condition corresponding to supplied ID
	 * @param conditionId id of the condition to find
	 * @return condition as ICpItem or null if condition with such id does not exist in the pack
	 */
	ICpItem getCondition(String conditionId);

	/**
	 * Returns generator corresponding to supplied name if any
	 * @param name name of the generator find
	 * @return generator as ICpItem or null if not found in the pack
	 */
	ICpGenerator getGenerator(String name);


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

	/**
	 * @return true if this pack is the latest version
	 */
	boolean isLatest();

	/**
	 * Returns collection of Pack releases (from latest to oldest)
	 * @return collection of ICpItem representing pack releases
	 */
	Collection<? extends ICpItem> getReleases();


}
