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

package com.arm.cmsis.pack.rte.components;

import java.util.Collection;

import com.arm.cmsis.pack.enums.EComponentAttribute;

/**
 * Class represent Cvariant hierarchy level, contains collection of versions.
 * Used in bundle and component levels
 */
public class RteComponentVariant extends RteComponentItem {
	/**
	 * @param parent
	 */
	public RteComponentVariant(IRteComponentItem parent, String name) {
		super(parent, name);
		fComponentAttribute = EComponentAttribute.CVENDOR;
	}
	
	@Override
	public Collection<String> getVendorStrings() {
		IRteComponent component = getParentComponent();
		if(component != null && component.hasBundle() ) {
			return null;
		}
		return getKeys();
	}
	
	@Override
	public String getActiveVendor() {
		return getActiveChildName();
	}


	@Override
	public void setActiveVendor(String vendor) {
		setActiveChild(vendor);
	}
}
