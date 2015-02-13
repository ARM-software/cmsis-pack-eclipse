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
 * Interface for pack family: collection of pack with the same name, but different versions
 * 
 */
public interface ICpPackFamily extends ICpItem {
	/**
	 * Returns pack of specified version if any
	 * @param version pack ID or version string to get pack
	 * @return ICpPack object or null if not found
	 */
	ICpPack getPack(final String packId);

	
	/**
	 * RSearches for pack loaded from given pdsc file 
	 * @param pdscFile file name corresponding to pack
	 * @return ICpPack object or null if not found
	 */
	ICpPack getPackByFilename(final String pdscFile);
	
}
