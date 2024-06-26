/*******************************************************************************
* Copyright (c) 2021 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.data;

import com.arm.cmsis.pack.common.CmsisConstants;

/**
 *
 */
public class CpTaxonomyContainer extends CpItem {

    /**
     * @param parent
     */
    public CpTaxonomyContainer(ICpItem parent) {
        super(parent);
    }

    /**
     * @param parent
     * @param tag
     */
    public CpTaxonomyContainer(ICpItem parent, String tag) {
        super(parent, tag);
    }

    @Override
    protected ICpItem createChildItem(String tag) {
        if (tag.equals(CmsisConstants.DESCRIPTION))
            return new CpTaxonomy(this, tag);
        return super.createChildItem(tag);
    }

}
