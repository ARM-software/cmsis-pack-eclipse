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
import com.arm.cmsis.pack.generic.IAttributes;

/**
 * Class describing a taxonomy entry
 */
public class CpTaxonomy extends CpItem implements ICpTaxonomy {

	/**
	 * Default ICpItem constructor
	 * @param parent
	 * @param tag
	 */
	public CpTaxonomy(ICpItem parent, String tag) {
		super(parent, tag);
	}

	
	@Override
	public String constructId() {
		return constructTaxonomyId(attributes());
	}

	/**
	 * Constructs taxonomy id out of Cclass, Cgroup and Csub attributes
	 * @param e
	 * @return
	 */
	public static String constructTaxonomyId(IAttributes a) {
		if(a == null )
			return CmsisConstants.EMPTY_STRING;
		String id = a.getAttribute(CmsisConstants.CCLASS);
		id += "."; //$NON-NLS-1$
		if(a.hasAttribute(CmsisConstants.CGROUP)) {
			id += "."; //$NON-NLS-1$
			id += a.getAttribute(CmsisConstants.CGROUP);
		}

		if(a.hasAttribute(CmsisConstants.CSUB)) {
			id += "."; //$NON-NLS-1$
			id += a.getAttribute(CmsisConstants.CSUB);
		}
		return id;
	}

	@Override
	public String getDescription() {
		return getText();
	}


}
