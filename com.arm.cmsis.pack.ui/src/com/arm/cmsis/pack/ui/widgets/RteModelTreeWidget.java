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
package com.arm.cmsis.pack.ui.widgets;

import org.eclipse.swt.widgets.Tree;

import com.arm.cmsis.pack.rte.IRteModelController;

public abstract class RteModelTreeWidget extends RteTreeWidget<IRteModelController> {

    @Override
    public void refresh() {
        if (fTreeViewer != null) {
            Tree tree = fTreeViewer.getTree();
            if (tree != null && !tree.isDisposed()) {
                fTreeViewer.refresh();
            }
        }
    }
}
