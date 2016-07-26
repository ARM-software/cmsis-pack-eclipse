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
public class CpMemory extends CpDeviceProperty implements ICpMemory {

	public CpMemory(ICpItem parent, String tag) {
		super(parent, tag);
	}
	
	@Override
	public String constructId() {
		String id = getAttribute(CmsisConstants.ID);
		if(id.isEmpty())
			id = getAttribute(CmsisConstants.NAME); 
		return id;
	}

	@Override
	public boolean isStartup() {
		return attributes().getAttributeAsBoolean(CmsisConstants.STARTUP, false);
	}

	@Override
	public String getAccess() {
		String access = getAttribute(CmsisConstants.ACCESS);
		if(access.isEmpty()) {
			access += READ_ACCESS;
			String id = attributes().getAttribute(CmsisConstants.ID);
			if(id != null) {
				if(id.startsWith(CmsisConstants.IRAM))
					access += WRITE_ACCESS;
			} else {
				 // actually error situation, should not happen by correctly written pdsc
			}
			access += EXECUTE_ACCESS;
		}
		return access;
	}
	

	@Override
	public boolean isAccess(char access) {
		return getAccess().indexOf(access) >= 0;
	}

	@Override
	public boolean isRAM() {
		String access = getAccess();
		return access.indexOf(READ_ACCESS) >= 0 && access.indexOf(WRITE_ACCESS) >= 0 && access.indexOf(EXECUTE_ACCESS) >= 0;   
	}

	@Override
	public boolean isROM() {
		String access = getAccess();
		return access.indexOf(WRITE_ACCESS) < 0 && access.indexOf(READ_ACCESS) >= 0 && access.indexOf(EXECUTE_ACCESS) >= 0;   
	}
	
	
	@Override
	public boolean isReadAccess() {
		return isAccess(READ_ACCESS);
	}

	@Override
	public boolean isWriteAccess() {
		return isAccess(WRITE_ACCESS);
	}

	@Override
	public boolean isExecuteAccess() {
		return isAccess(EXECUTE_ACCESS);
	}

	@Override
	public boolean isSecureAccess() {
		return isAccess(SECURE_ACCESS);
	}

	@Override
	public boolean isNonSecureAccess() {
		return isAccess(NON_SECURE_ACCESS) && !isAccess(SECURE_ACCESS);
	}

	@Override
	public boolean isCallableAccess() {
		return isAccess(CALLABLE_ACCESS);
	}

	@Override
	public boolean isPeripheralAccess() {
		return isAccess(PERIPHERAL_ACCESS);
	}
}
