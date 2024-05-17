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

package com.arm.cmsis.pack.repository;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.generic.AttributedItem;

/**
 * Default implementation of {@link ICpRepository}
 */
public class CpRepository extends AttributedItem implements ICpRepository {

    public CpRepository(String repoAttr) {
        attributes().setAttributes(repoAttr);
    }

    public CpRepository(String type, String name, String url) {
        attributes().setAttribute(CmsisConstants.REPO_TYPE, type);
        attributes().setAttribute(CmsisConstants.REPO_NAME, name);
        attributes().setAttribute(CmsisConstants.REPO_URL, url);
    }

    @Override
    public String getType() {
        return attributes().getAttribute(CmsisConstants.REPO_TYPE, CmsisConstants.EMPTY_STRING);
    }

    @Override
    public String getName() {
        return attributes().getAttribute(CmsisConstants.REPO_NAME, CmsisConstants.EMPTY_STRING);
    }

    @Override
    public String getUrl() {
        return attributes().getAttribute(CmsisConstants.REPO_URL, CmsisConstants.EMPTY_STRING);
    }

    @Override
    public int getAttrCount() {
        if (attributes().hasAttributes()) {
            return attributes().getAttributesAsMap().size();
        }
        return 0;
    }

    @Override
    public String toString() {
        return attributes().toString();
    }

}
