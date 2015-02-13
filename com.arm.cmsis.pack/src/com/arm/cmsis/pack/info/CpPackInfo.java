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

package com.arm.cmsis.pack.info;

import com.arm.cmsis.pack.data.CpItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;

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
		attributes().setAttribute("name", pack.getName());
		attributes().setAttribute("url", pack.getUrl()); 
		attributes().setAttribute("vendor", pack.getVendor());
		attributes().setAttribute("version", pack.getVersion());
	}
	
	/**
	 * @param parent parent item if any
	 */
	public CpPackInfo(ICpItem parent) {
		super(parent, "package");
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
	public String getVendor() {
		return attributes().getAttribute("vendor");
	}

	@Override
	public String getVersion() {
		return attributes().getAttribute("version");
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
	
}
