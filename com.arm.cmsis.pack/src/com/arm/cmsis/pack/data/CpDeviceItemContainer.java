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

import com.arm.cmsis.pack.common.CmsisConstants;

/**
 *
 */
public class CpDeviceItemContainer extends CpItem {

	/**
	 * @param parent
	 * @param tag
	 */
	public CpDeviceItemContainer(ICpItem parent, String tag) {
		super(parent, tag);
	}


	@Override
	protected ICpItem createChildItem(String tag) {
		if(tag.equals(CmsisConstants.FAMILY_TAG) || tag.equals(CmsisConstants.SUBFAMILY_TAG) || 
		   tag.equals(CmsisConstants.DEVICE_TAG) || tag.equals(CmsisConstants.VARIANT_TAG)){
			return new CpDeviceItem(this, tag);
		}
		return new CpDeviceProperty(this, tag);
	}

}
