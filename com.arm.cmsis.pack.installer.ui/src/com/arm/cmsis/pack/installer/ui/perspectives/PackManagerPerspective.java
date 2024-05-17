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
 * Eclipse Project - generation from template
 * ARM Ltd and ARM Germany GmbH - application-specific implementation
 *******************************************************************************/
package com.arm.cmsis.pack.installer.ui.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.progress.IProgressConstants;

import com.arm.cmsis.pack.installer.ui.views.BoardsView;
import com.arm.cmsis.pack.installer.ui.views.DevicesView;
import com.arm.cmsis.pack.installer.ui.views.ExamplesView;
import com.arm.cmsis.pack.installer.ui.views.PackPropertyView;
import com.arm.cmsis.pack.installer.ui.views.PacksView;

/**
 * Perspective for pack manager
 */
public class PackManagerPerspective implements IPerspectiveFactory {

    public static final String ID = "com.arm.cmsis.pack.installer.ui.perspectives.packmanager"; //$NON-NLS-1$

    IPageLayout fLayout;

    public PackManagerPerspective() {
        super();
    }

    @Override
    public void createInitialLayout(IPageLayout layout) {
        fLayout = layout;
        fLayout.setEditorAreaVisible(false);
        addViews();
    }

    private void addViews() {

        IFolderLayout bottom = fLayout.createFolder("Bottom", //$NON-NLS-1$
                IPageLayout.BOTTOM, 0.8f, fLayout.getEditorArea());
        bottom.addView(IConsoleConstants.ID_CONSOLE_VIEW);
        bottom.addView(IProgressConstants.PROGRESS_VIEW_ID);

        IFolderLayout topLeft = fLayout.createFolder("DevicesAndBoards", //$NON-NLS-1$
                IPageLayout.LEFT, 0.35f, fLayout.getEditorArea());
        topLeft.addView(DevicesView.ID);
        fLayout.addShowViewShortcut(DevicesView.ID);
        topLeft.addView(BoardsView.ID);
        fLayout.addShowViewShortcut(BoardsView.ID);

        IFolderLayout right = fLayout.createFolder("Outline", //$NON-NLS-1$
                IPageLayout.RIGHT, 0.75f, fLayout.getEditorArea());
        right.addView(PackPropertyView.ID);
        fLayout.addShowViewShortcut(PackPropertyView.ID);

        IFolderLayout topRight = fLayout.createFolder("PacksAndExamples", //$NON-NLS-1$
                IPageLayout.TOP, 0.8f, fLayout.getEditorArea());
        topRight.addView(PacksView.ID);
        fLayout.addShowViewShortcut(PacksView.ID);
        topRight.addView(ExamplesView.ID);
        fLayout.addShowViewShortcut(ExamplesView.ID);
    }

}
