/*******************************************************************************
* Copyright (c) 2014 ARM Ltd.
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/

package com.arm.cmsis.pack.data;

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
		AVAILABLE,  
		DOWNLOADED, 
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

	
}
