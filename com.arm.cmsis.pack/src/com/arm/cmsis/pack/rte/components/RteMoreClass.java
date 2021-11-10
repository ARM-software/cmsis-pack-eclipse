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

package com.arm.cmsis.pack.rte.components;

import com.arm.cmsis.pack.CpStrings;

/**
 * The class represents an artificial "more.." component class that used when
 * pack filter is in effect
 */
public class RteMoreClass extends RteComponentClass {

    public RteMoreClass(IRteComponentItem parent) {
        super(parent, "{ more... }"); //$NON-NLS-1$
    }

    @Override
    public String getDescription() {
        return CpStrings.PackFilterInInEffectComponentsFiltered;
    }

    @Override
    public boolean purge() {
        return false;
    }
}
