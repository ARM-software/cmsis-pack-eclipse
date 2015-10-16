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

package com.arm.cmsis.pack.configuration;

import com.arm.cmsis.pack.build.settings.BuildSettings;
import com.arm.cmsis.pack.generic.IAttributes;

/**
 *  This class extends generic BuildSettings to provide device attributes 
 */
public class RteBuildSettings extends BuildSettings {
	protected IAttributes deviceAttributes = null;
	

	public RteBuildSettings() {
	}
	
	
	@Override
	public void clear() {
		// TODO Auto-generated method stub
		super.clear();
	}



	@Override
	public String getDeviceAttribute(String key) {
		if(deviceAttributes == null)
			return null;
		return deviceAttributes.getAttribute(key);
	}

	
	public void setDeviceAttributes(IAttributes deviceAttributes) {
		this.deviceAttributes = deviceAttributes;
	}

	
	
}
