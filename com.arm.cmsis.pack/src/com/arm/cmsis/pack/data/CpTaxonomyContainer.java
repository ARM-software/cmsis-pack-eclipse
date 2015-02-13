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
 *
 */
public class CpTaxonomyContainer extends CpItem {

	/**
	 * @param parent
	 */
	public CpTaxonomyContainer(ICpItem parent) {
		super(parent);
	}

	/**
	 * @param parent
	 * @param tag
	 */
	public CpTaxonomyContainer(ICpItem parent, String tag) {
		super(parent, tag);
	}

	@Override
	protected ICpItem createChildItem(String tag) {
		if(tag.equals("description")) 
			return new CpTaxonomy(this, tag);
		return super.createChildItem(tag);
	}

}
