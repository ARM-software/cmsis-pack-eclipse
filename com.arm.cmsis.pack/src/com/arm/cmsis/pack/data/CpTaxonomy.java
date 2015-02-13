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

import com.arm.cmsis.pack.generic.IAttributes;

/**
 * Class describing a taxonomy entry
 */
public class CpTaxonomy extends CpItem implements ICpTaxonomy {

	/**
	 * Default ICpItem constructor
	 * @param parent
	 * @param tag
	 */
	public CpTaxonomy(ICpItem parent, String tag) {
		super(parent, tag);
	}

	
	@Override
	public String constructId() {
		return constructTaxonomyId(attributes());
	}

	/**
	 * Constructs taxonomy id out of Cclass, Cgroup and Csub attributes
	 * @param e
	 * @return
	 */
	public static String constructTaxonomyId(IAttributes a) {
		if(a == null )
			return IAttributes.EMPTY_STRING;
		String id = a.getAttribute("Cclass");
		id += ".";
		if(a.hasAttribute("Cgroup")) {
			id += ".";
			id += a.getAttribute("Cgroup");
		}

		if(a.hasAttribute("Csub")) {
			id += ".";
			id += a.getAttribute("Csub");
		}
		return id;
	}

	@Override
	public String getDescription() {
		return getText();
	}


}
