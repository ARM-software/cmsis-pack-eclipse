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

package com.arm.cmsis.pack.info;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.generic.IAttributes;

/**
 *
 */
public class CpPackInfo extends CpItem implements ICpPackInfo {

	protected ICpPack fPack = null;

	/**
	 * Constructs Pack info from a Pack
	 * @param parent parent item if any
	 * @param pack ICpPack item to take data from
	 */
	public CpPackInfo(ICpItem parent, ICpPack pack) {
		super(parent, pack.getTag());
		fPack = pack;
		updateInfo();
	}

	public CpPackInfo(ICpItem parent, ICpPackInfo packInfo) {
		super(parent, packInfo.getTag());
		fPack = packInfo.getPack();
		attributes().setAttributes(packInfo.attributes());
	}

	public CpPackInfo(ICpItem parent, IAttributes attributes) {
		super(parent, CmsisConstants.PACKAGE_TAG);
		fPack = null;
		attributes().setAttributes(attributes);
	}


	/**
	 * @param parent parent item if any
	 */
	public CpPackInfo(ICpItem parent) {
		super(parent, CmsisConstants.PACKAGE_TAG);
	}

	/**
	 * @param parent
	 * @param tag
	 */
	public CpPackInfo(ICpItem parent, String tag) {
		super(parent, tag);
	}

	@Override
	public ICpPackInfo getPackInfo() {
		return this;
	}

	@Override
	public ICpPack getPack() {
		return fPack;
	}

	@Override
	public void setPack(ICpPack pack) {
		fPack = pack;
	}

	@Override
	public void updateInfo() {
		if(fPack != null) {
			attributes().setAttribute(CmsisConstants.NAME, fPack.getName());
			attributes().setAttribute(CmsisConstants.URL, fPack.getUrl());
			attributes().setAttribute(CmsisConstants.VENDOR, fPack.getVendor());
			attributes().setAttribute(CmsisConstants.VERSION, fPack.getVersion());
		}
	}

	@Override
	public String getVendor() {
		return getAttribute(CmsisConstants.VENDOR);
	}

	@Override
	public String getVersion() {
		return getAttribute(CmsisConstants.VERSION);
	}

	@Override
	public String getPackId() {
		return getId();
	}

	@Override
	public String constructId() {
		// construct Pack ID in the form "Vendor.Name.Version"
		return getPackFamilyId() + '.' + getVersion();
	}

	@Override
	public String getPackFamilyId() {
		return getVendor() + '.' + getName();
	}

	@Override
	public String getDescription() {
		if(fPack != null) {
			return fPack.getDescription();
		}
		return null;
	}


}
