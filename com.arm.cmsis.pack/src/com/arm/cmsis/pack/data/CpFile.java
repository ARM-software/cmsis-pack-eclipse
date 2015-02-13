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

import com.arm.cmsis.pack.enums.EFileCategory;
import com.arm.cmsis.pack.enums.EFileRole;

/**
 *  Defautt implementation of ICpFile interface 
 */
public class CpFile extends CpItem implements ICpFile {

	protected EFileRole role = null;
	protected EFileCategory category = null;
	
	/**
	 * @param parent
	 * @param tag
	 */
	public CpFile(ICpItem parent, String tag) {
		super(parent, tag);
	}

	@Override
	public EFileCategory getCategory() {
		if(category == null){
			category = EFileCategory.fromString(getAttribute("category"));
		}
		return category;
	}

	@Override
	public EFileRole getRole() {
		if(role == null) {
			role = EFileRole.fromString(getAttribute("attr"));
		}
		return role;
	}

	
}
