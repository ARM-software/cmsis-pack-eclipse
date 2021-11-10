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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;

import com.arm.cmsis.pack.events.IRteController;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.CpStringsUI;
import com.arm.cmsis.pack.ui.tree.OverlayImage;
import com.arm.cmsis.pack.ui.tree.OverlayImage.OverlayPos;

public abstract class RteTreeWidget<TController extends IRteController> extends RteWidget<TController> {

    protected TreeViewer fTreeViewer = null;
    protected IRteColumnAdvisor<TController> fColumnAdvisor = null;

    protected Action expandAll = null;
    protected Action expandAllSelected = null;
    protected Action collapseAll = null;

    protected Object tContextMenuObject = null; // object under cursor
    protected int tContextMenuColumn = -1; // column index under cursor
    protected Point tContextMenuPoint = new Point(-1, -1);

    @Override
    public void destroy() {
        super.destroy();
        fTreeViewer = null;
        fColumnAdvisor = null;
        expandAll = null;
        expandAllSelected = null;
        collapseAll = null;
    }

    /**
     * Return the tree viewer embedded in this widget
     *
     * @return
     */
    public TreeViewer getViewer() {
        return fTreeViewer;
    }

    /**
     * Returns underlying tree control
     *
     * @return
     */
    public Tree getTree() {
        TreeViewer viewer = getViewer();
        if (viewer != null)
            return viewer.getTree();
        return null;
    }

    /**
     * Returns number of selected elements in the tree
     *
     * @return selection count
     */
    public int getSelectionCount() {
        Tree tree = getTree();
        if (tree != null)
            return tree.getSelectionCount();
        return 0;
    }

    /**
     * Returns Composite that should be used as focus widget
     *
     * @return widget to set focus to
     */
    @Override
    public Composite getFocusWidget() {
        return getTree();
    }

    /**
     * Sets an RTE model controller to be used by the widget
     *
     * @param modelController IRteController controller to use
     */
    @Override
    public void setModelController(TController modelController) {
        super.setModelController(modelController);
        if (fColumnAdvisor != null)
            fColumnAdvisor.setModelController(modelController);
    }

    /**
     * Returns Column adviser
     *
     * @return IColumnAdvisor
     */
    public IRteColumnAdvisor<TController> getColumnAdvisor() {
        return fColumnAdvisor;
    }

    /**
     * Sets column adviser
     *
     * @param columnAdvisor IColumnAdvisor
     */
    public void setColumnAdvisor(IRteColumnAdvisor<TController> columnAdvisor) {
        fColumnAdvisor = columnAdvisor;
    }

    protected void hookContextMenu() {
        if (fTreeViewer == null)
            return;
        final MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        makeActions();
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(manager -> contextMenuAboutToShow(menuMgr));
        Menu menu = menuMgr.createContextMenu(fTreeViewer.getControl());
        fTreeViewer.getControl().setMenu(menu);
    }

    protected void contextMenuAboutToShow(IMenuManager manager) {
        updateContextMenuValues();
        fillContextMenu(manager);
    }

    protected void fillContextMenu(IMenuManager manager) {
        if (tContextMenuPoint.y > 0 || tContextMenuColumn > 0) {
            if (isExpandAllSelectedSupported() && expandAllSelected != null) {
                manager.add(expandAllSelected);
            }
            manager.add(expandAll);
            manager.add(collapseAll);
        }
    }

    protected void updateContextMenuValues() {
        tContextMenuColumn = -1;
        tContextMenuObject = null;
        tContextMenuPoint = new Point(-1, -1);
        if (fTreeViewer == null)
            return;

        Control control = fTreeViewer.getControl();
        if (control == null)
            return;
        Point pt = control.toControl(Display.getDefault().getCursorLocation());
        tContextMenuPoint = pt;
        ViewerCell cell = fTreeViewer.getCell(pt);
        if (cell != null) {
            tContextMenuColumn = cell.getColumnIndex();
            tContextMenuObject = cell.getElement();
            return;
        }

        IStructuredSelection sel = fTreeViewer.getStructuredSelection();
        if (sel != null) {
            tContextMenuObject = sel.getFirstElement();
        }

        // we need column information even if clicked outside (e.g. on header), use the
        // first item
        cell = fTreeViewer.getCell(new Point(pt.x, 1));
        tContextMenuColumn = cell == null ? -1 : cell.getColumnIndex();
    }

    protected void makeActions() {
        expandAll = new Action() {
            @Override
            public void run() {
                if (fTreeViewer == null) {
                    return;
                }
                fTreeViewer.expandAll();
            }
        };

        expandAll.setText(CpStringsUI.ExpandAll);
        expandAll.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_EXPAND_ALL));

        collapseAll = new Action() {
            @Override
            public void run() {
                if (fTreeViewer == null) {
                    return;
                }
                fTreeViewer.collapseAll();
            }
        };
        collapseAll.setText(CpStringsUI.CollapseAll);
        collapseAll.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_COLLAPSE_ALL));

        if (!isExpandAllSelectedSupported())
            return;
        expandAllSelected = new Action() {
            @Override
            public void run() {
                expandAllSelected();
            }
        };
        expandAllSelected.setText(CpStringsUI.ExpandAllSelected);

        OverlayImage overlayImage = new OverlayImage(CpPlugInUI.getImage(CpPlugInUI.ICON_EXPAND_ALL),
                CpPlugInUI.getImage(CpPlugInUI.CHECKEDOUT_OVR), OverlayPos.TOP_RIGHT);
        expandAllSelected.setImageDescriptor(overlayImage);
    }

    public boolean isExpandAllSelectedSupported() {
        return false; // default does not support it
    }

    protected void expandAllSelected() {
        // default does nothing
    }
}
