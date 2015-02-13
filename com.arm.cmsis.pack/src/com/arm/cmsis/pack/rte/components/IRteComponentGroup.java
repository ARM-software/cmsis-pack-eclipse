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

package com.arm.cmsis.pack.rte.components;

import java.util.Map;

import com.arm.cmsis.pack.data.ICpComponent;

/**
 * Interface for Cgroup level of component hierarchy.
 * Extends IRteComponentItem and adds methods to handle ICpApi 
 * @see IRteComponentItem   
 */
public interface IRteComponentGroup extends IRteComponentItem {

	/**
	 * Returns API collection as map sorted by version (from latest to oldest )
	 * @return API map  
	 */
	Map<String, ICpComponent> getApis(); 

	
	/**
	 * Returns api of specified version  
	 * @return ICpApi object if found or null
	 */
	ICpComponent getApi(String version);
	
	
	/**
	 * Returns version of active API  
	 * @return active API version or null if no active API available
	 */
	String getActiveApiVersion();

	
	/**
	 * Sets active API version 
	 * @param version to set active
	 * @return if active version has changed
	 */
	boolean setActiveApi(String version);
	
	
}
