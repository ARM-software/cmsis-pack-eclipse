/*******************************************************************************
 * Copyright (c) 2022 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/

package com.arm.cmsis.pack.ui.tree;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpBoard;
import com.arm.cmsis.pack.rte.boards.IRteBoardItem;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;
import com.arm.cmsis.pack.ui.CpStringsUI;

public class BoardTreeColumnComparator extends TreeColumnComparator {
    private static final String MOUNTED_DEVICES = CmsisConstants.MOUNTED_DEVICES;
    private static final String COMPATIBLE_DEVICES = CmsisConstants.COMPATIBLE_DEVICES;

    public BoardTreeColumnComparator(TreeViewer viewer, IColumnAdvisor advisor) {
        super(viewer, advisor, 0);
    }

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {

        if (getColumnIndex() != 0) {
            return super.compare(viewer, e1, e2);
        }

        int result = 0;
        if ((e1 instanceof IRteBoardItem) && (e2 instanceof IRteBoardItem)) {

            IRteBoardItem cp1 = (IRteBoardItem) e1;
            IRteBoardItem cp2 = (IRteBoardItem) e2;

            String title1 = getBoardTitle(cp1.getBoard());
            String title2 = getBoardTitle(cp2.getBoard());

            result = alnumComparator.compare(title1, title2);

        } else if ((e1 instanceof IRteDeviceItem) && (e2 instanceof IRteDeviceItem)) {
            IRteDeviceItem d1 = (IRteDeviceItem) e1;
            IRteDeviceItem d2 = (IRteDeviceItem) e2;
            if (MOUNTED_DEVICES.equals(d1.getName()) || COMPATIBLE_DEVICES.equals(d1.getName())) {
                return 0;
            }
            result = alnumComparator.compare(d1.getName(), d2.getName());
        }
        return bDescending ? -result : result;
    }

    String getBoardTitle(ICpBoard cpBoard) {
        if (cpBoard == null) {
            return CmsisConstants.EMPTY_STRING;
        }
        String boardTitle = cpBoard.getName();
        String revision = cpBoard.getRevision();
        if (!revision.isEmpty()) {
            boardTitle += " (" + revision + ')'; //$NON-NLS-1$
        }
        if (cpBoard.isDeprecated()) {
            boardTitle += ' ' + CpStringsUI.DeprecatedBoard;
        }
        return boardTitle;
    }
}