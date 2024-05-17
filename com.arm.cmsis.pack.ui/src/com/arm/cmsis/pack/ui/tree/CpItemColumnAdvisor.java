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

package com.arm.cmsis.pack.ui.tree;

import org.eclipse.jface.viewers.ColumnViewer;

import com.arm.cmsis.pack.data.ICpItem;

/**
 *
 */
public class CpItemColumnAdvisor extends ColumnAdvisor {

    /**
     * Simple column advisor for ICpItem
     *
     * @param columnViewer
     */
    public CpItemColumnAdvisor(ColumnViewer columnViewer) {
        super(columnViewer);
    }

    @Override
    public String getString(Object obj, int columnIndex) {
        ICpItem item = getItem(obj);
        if (item == null)
            return null;
        if (columnIndex == 0) {
            return item.getName();
        }
        return null;
    }

    public ICpItem getItem(Object obj) {
        return ICpItem.cast(obj);
    }

}
