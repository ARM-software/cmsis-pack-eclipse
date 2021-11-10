/*******************************************************************************
* Copyright (c) 2021 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* Ivor Williams - Initial API and implementation: https://dzone.com/articles/javaswt-click-table-column
* ARM Ltd and ARM Germany GmbH - application-specific implementation
*******************************************************************************/

package com.arm.cmsis.pack.installer.ui.views;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import com.arm.cmsis.pack.ui.tree.IColumnAdvisor;
import com.arm.cmsis.pack.ui.tree.TreeColumnComparator;

/**
 * The Tree comparator used to sort the rows of each column
 */
public class PackInstallerTreeColumnComparator extends TreeColumnComparator {

    public PackInstallerTreeColumnComparator(TreeViewer viewer, IColumnAdvisor advisor) {
        super(viewer, advisor, 0);
    }

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {

        int index = getColumnIndex();
        if (index == 1) {
            String str1 = columnAdvisor.getString(e1, index);
            String str2 = columnAdvisor.getString(e2, index);
            int result = alnumComparator.compare(str1, str2); // not case sensitive
            return bDescending ? -result : result;
        }
        return super.compare(viewer, e1, e2);
    }
}
