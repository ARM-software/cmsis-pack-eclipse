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
 *	Provides factory method to create items implementing ICpItem interface
 *  @see ICpItem
 */
public interface ICpItemFactory {
	/**
	 * Factory method to create ICpItem-derived instances
	 * @param parent item that contains this one 
	 * @param tag XML tag for the item 
	 * @return created or existing ICpItem 
	 */
	public ICpItem createItem(ICpItem parent, String tag);
}
