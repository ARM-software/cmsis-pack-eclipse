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

import java.util.Collection;

/**
 *
 */
public class CpDeviceProperty extends CpDeviceItemContainer implements ICpDeviceProperty {
	protected ICpItem effectiveContent = null; 

	/**
	 * @param parent
	 * @param tag
	 */
	public CpDeviceProperty(ICpItem parent, String tag) {
		super(parent, tag);
	}


	@Override
	public boolean isUnique() {
		// only a few properties are not unique
		if(fTag.equals("feature") || fTag.equals("block") || fTag.equals("control") || fTag.equals("description"))
			return false;
		return true;
	}


	@Override
	public ICpItem getEffectiveContent() {
		return effectiveContent;
	}


	@Override
	public void mergeEffectiveContent(ICpItem property) {
		attributes().mergeAttributes(property.attributes()); // always merge attributes
		if(!providesEffectiveContent())  // merge content only if property needs it 
			return;
		if(effectiveContent == null) {
			effectiveContent = new CpItem(this);
		}
		Collection<? extends ICpItem> children = getChildren();
		if(children == null || children.isEmpty())
			return;
		for(ICpItem item : children) {
			if(item instanceof ICpDeviceProperty) {
				effectiveContent.mergeProperty(item);
			}
		}						
	}


	@Override
	public boolean providesEffectiveContent() {
		// only a few properties collect the content 
		if(fTag.equals("environment") || fTag.equals("trace") || fTag.equals("debug"))
			return true;
		return false;
	}


	@Override
	public ICpItem getEffectiveParent() {
		ICpItem parent = getParent();
		if(parent != null && parent instanceof ICpDeviceProperty)
			return parent;
		return null;
	}

}
