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
