/*******************************************************************************
* Copyright (c) 2021 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.generic;

/**
 * Default simple implementation of {@link IAttributedItem} interface
 */
public class AttributedItem implements IAttributedItem {

    private IAttributes fAttributes = null;

    @Override
    public IAttributes attributes() {
        if (fAttributes == null)
            fAttributes = createAttributes();
        return fAttributes;
    }
}
