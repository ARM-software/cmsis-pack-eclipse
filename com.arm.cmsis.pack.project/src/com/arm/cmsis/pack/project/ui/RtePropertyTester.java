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
package com.arm.cmsis.pack.project.ui;

import org.eclipse.core.expressions.PropertyTester;

import com.arm.cmsis.pack.project.utils.ProjectUtils;

public class RtePropertyTester extends PropertyTester {

    public RtePropertyTester() {
    }

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        if (property.equalsIgnoreCase("rteFile")) { //$NON-NLS-1$
            return ProjectUtils.getRteFileResource(receiver) != null;
        }
        return false;
    }
}
