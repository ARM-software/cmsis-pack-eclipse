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

import com.arm.cmsis.pack.base.CmsisConstants;
import com.arm.cmsis.pack.data.ICpComponent;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.enums.EComponentAttribute;

/**
 * Class represents root of component hierarchy
 * Contains collection of groups 
 */
public class RteComponentRoot extends RteComponentItem {
	/**
	 * Default root constructor 
	 */
	public RteComponentRoot() {
		fName = "root";
		fComponentAttribute = EComponentAttribute.CCLASS;
		fbExclusive = false;
	}

	@Override
	public void addComponent(ICpComponent cpComponent) {
		String className = cpComponent.attributes().getAttribute("Cclass");
		if(className == null || className.isEmpty())
			return; 
		// ensure childItem
		IRteComponentItem classItem = getChild(className); 
		
		if(classItem == null ) {
			classItem = new RteComponentClass(this, className);
			addChild(classItem);
		}
		classItem.addComponent(cpComponent);
	}
	
	
	@Override
	public void addCpItem(ICpItem cpItem) {
		String className = cpItem.attributes().getAttribute(CmsisConstants.CCLASS);
		if(className == null || className.isEmpty())
			return; 
		// check if class exists 
		IRteComponentItem classItem = getChild(className); 
		if(classItem == null ) {
			return; // no class => no add
		}
		classItem.addCpItem(cpItem);
	}
}
