/*******************************************************************************
* Copyright (c) 2016 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.repository;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.generic.Attributes;
import com.arm.cmsis.pack.generic.IAttributes;

/**
 * Default implementation of {@link ICpRepository}
 */
public class CpRepository implements ICpRepository {

	protected IAttributes fAttributes;

	public CpRepository(String repoAttr) {
		fAttributes = new Attributes();
		fAttributes.setAttributes(repoAttr);
	}

	public CpRepository(String type, String name, String url) {
		fAttributes = new Attributes();
		fAttributes.setAttribute(CmsisConstants.REPO_TYPE, type);
		fAttributes.setAttribute(CmsisConstants.REPO_NAME, name);
		fAttributes.setAttribute(CmsisConstants.REPO_URL, url);
	}

	@Override
	public String getType() {
		return fAttributes.getAttribute(CmsisConstants.REPO_TYPE, CmsisConstants.EMPTY_STRING);
	}

	@Override
	public String getName() {
		return fAttributes.getAttribute(CmsisConstants.REPO_NAME, CmsisConstants.EMPTY_STRING);
	}

	@Override
	public String getUrl() {
		return fAttributes.getAttribute(CmsisConstants.REPO_URL, CmsisConstants.EMPTY_STRING);
	}

	@Override
	public int getAttrCount() {
		if (fAttributes.hasAttributes()) {
			return fAttributes.getAttributesAsMap().size();
		}
		return 0;
	}

	@Override
	public String toString() {
		return fAttributes.toString();
	}

}
