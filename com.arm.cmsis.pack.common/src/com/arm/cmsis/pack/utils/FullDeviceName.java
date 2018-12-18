/*******************************************************************************
 * Copyright (c) 2017 ARM Ltd and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/

package com.arm.cmsis.pack.utils;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.generic.IAttributes;

/**
 * Class to represent full device name represented Dname[:Pname]
 *
 */
public class FullDeviceName {
	private String fFullDeficeName = CmsisConstants.EMPTY_STRING;
	
	/**
	 * Constructs class from full device name
	 * @param fullDeviceName
	 */
	public FullDeviceName(String fullDeviceName) {
		if(fullDeviceName != null) {
			fFullDeficeName = fullDeviceName;
		} else {
			fFullDeficeName = CmsisConstants.EMPTY_STRING;
		}
	}
	
	/**
	 * Constructs class from device and processor name
	 * @param dName device name
	 * @param pName processor name
	 */
	public FullDeviceName(String dName, String pName) {
		if(dName == null) {
			fFullDeficeName = CmsisConstants.EMPTY_STRING;
			return;
		}
		fFullDeficeName = getFullDeviceName(dName, pName);
	}
	
	/**
	 * Constructs class from attributes
	 * @param attributes device attributes
	 */
	public FullDeviceName(IAttributes attributes) {
		this(getFullDeviceName(attributes));
	}

	
	@Override
	public String toString() {
		return fFullDeficeName;
	}

	@Override
	public boolean equals(Object arg0) {
		if(arg0 == null)
			return false;
		return fFullDeficeName.equals(arg0.toString());
	}

	@Override
	public int hashCode() {
		return fFullDeficeName.hashCode();
	}

	/**
	 * Return full device name
	 * @return Dname[:Pname]
	 */
	public String getFullDeviceName() {
		return fFullDeficeName;
	}

	/**
	 * Return device name
	 * @return Dname
	 */
	public String getDeviceName() {
		return extractDeviceName(fFullDeficeName);
	}

	/**
	 * Return processor name
	 * @return Pname or empty string if processor name is not found
	 */
	public String getProcessoreName() {
		return extractProcessoreName(fFullDeficeName);
	}

	/**
	 * Returns full device name in form "Dname:Pname" 
	 * @param dName device name
	 * @param pName processor name
	 * @return full device name or empty string if dName is null or empty
	 */
	static public String getFullDeviceName(String dName, String pName) {
		String fullDeviceName = dName;
		if(fullDeviceName == null || fullDeviceName.isEmpty())
			return CmsisConstants.EMPTY_STRING;
		if(pName != null && !pName.isEmpty()) {
			fullDeviceName += ':' + pName;
		}
		return fullDeviceName;
	}

	/**
	 * Returns full device name in form "Dname:Pname" 
	 * @param attributes attributes to construct full device name  
	 * @return full device name or empty string if the attributes parameter does not represent device
	 */
	static public String getFullDeviceName(IAttributes attributes) {
		return getFullDeviceName(getDeviceName(attributes), getProcessorName(attributes));
	}
	
	/**
	 * Returns "Dname" or "Dvariant" attribute of the element representing device property 
	 * @param attributes attributes to construct full device name  
	 * @return device name or an empty string if the attributes parameter does not represent device
	 */
	static public String getDeviceName(IAttributes attributes) {
		String deviceName = CmsisConstants.EMPTY_STRING;
		if(attributes.hasAttribute(CmsisConstants.DVARIANT)) {
			deviceName = attributes.getAttribute(CmsisConstants.DVARIANT);
		} else if(attributes.hasAttribute(CmsisConstants.DNAME)) {
			deviceName = attributes.getAttribute(CmsisConstants.DNAME);
		}
		return deviceName;
	}
	
	/**
	 * Returns "Pname" attribute of the element representing device property 
	 * @param attributes attributes to extract processor name  
	 * @return processor name or empty string if "Pname" attribute not found
	 */
	static public String getProcessorName(IAttributes attributes) {
		return attributes.getAttribute(CmsisConstants.PNAME, CmsisConstants.EMPTY_STRING);
	}

	
	/**
	 * Extracts device name from the full device name string
	 * @param fullDeviceName full device name Dname[:Pname]
	 * @return Dname
	 */
	public static String extractDeviceName(String fullDeviceName) {
		if(fullDeviceName == null)
			return CmsisConstants.EMPTY_STRING;
		int i = fullDeviceName.indexOf(':');
		if (i >= 0) {
			return fullDeviceName.substring(0, i);
		} 
		return fullDeviceName;
	}
	
	/**
	 * Extracts processor name from the full device name string
	 * @param fullDeviceName full device name Dname[:Pname]
	 * @return Pname or empty string if processor name is not found
	 */
	public static String extractProcessoreName(String fullDeviceName) {
		if(fullDeviceName == null)
			return CmsisConstants.EMPTY_STRING;
		int i = fullDeviceName.indexOf(':');
		if (i >= 0) {
			return fullDeviceName.substring(i + 1);
		} 
		return CmsisConstants.EMPTY_STRING;
	}
}
