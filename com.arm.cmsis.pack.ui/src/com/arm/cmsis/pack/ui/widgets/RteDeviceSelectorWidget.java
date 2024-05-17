/*******************************************************************************
 * Copyright (c) 2022 ARM Ltd. and others
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

import java.util.Collection;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpBoard;
import com.arm.cmsis.pack.data.ICpDeviceItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.enums.EDeviceHierarchyLevel;
import com.arm.cmsis.pack.generic.ITreeObject;
import com.arm.cmsis.pack.info.CpBoardInfo;
import com.arm.cmsis.pack.info.CpDeviceInfo;
import com.arm.cmsis.pack.info.ICpBoardInfo;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.rte.IRteModelController;
import com.arm.cmsis.pack.rte.boards.IRteBoardDeviceItem;
import com.arm.cmsis.pack.rte.boards.IRteBoardItem;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.CpStringsUI;
import com.arm.cmsis.pack.ui.IStatusMessageListener;
import com.arm.cmsis.pack.ui.StatusMessageListerenList;
import com.arm.cmsis.pack.ui.tree.BoardTreeColumnComparator;
import com.arm.cmsis.pack.ui.tree.BoardViewContentProvider;
import com.arm.cmsis.pack.ui.tree.BoardsViewColumnAdvisor;
import com.arm.cmsis.pack.ui.tree.BoardsViewLabelProvider;
import com.arm.cmsis.pack.ui.tree.TreeObjectContentProvider;
import com.arm.cmsis.pack.utils.FullDeviceName;

/**
 * Widget to select a CMSIS device
 */
public class RteDeviceSelectorWidget extends Composite {
    private Text text;
    Text txtSearch;
    private Label lblVendor;
    private Label lblDevice;
    private Label lblCpu;

    private Combo comboSecurity;
    private Combo comboFpu;
    private Combo comboMve;
    private Combo comboEndian;

    static final String[] SECURITY_VALUES = new String[] { CmsisConstants.NON_SECURE, CmsisConstants.SECURE,
            CmsisConstants.TZ_DISABLED };

    IRteDeviceItem fDevices = null;
    IRteDeviceItem fSelectedDeviceItem = null;
    private ICpDeviceItem fSelectedDevice = null;
    private ICpDeviceInfo fDeviceInfo = null;

    IRteBoardItem fBoards = null;
    IRteBoardDeviceItem fSelectedBoardItem = null;
    private ICpBoardInfo fBoardInfo = null;

    private TreeViewer treeViewerDevices;
    private TreeViewer treeViewerBoards;

    // list of listeners (e.g parent widgets to monitor events)
    StatusMessageListerenList listeners = new StatusMessageListerenList();
    String fSearchString = CmsisConstants.EMPTY_STRING;
    String fSelectedFpu = CmsisConstants.EMPTY_STRING;
    String fSelectedEndian = CmsisConstants.EMPTY_STRING;
    String fSelectedSecurity = CmsisConstants.EMPTY_STRING;
    String fSelectedMve = CmsisConstants.EMPTY_STRING;
    boolean fbShowProcessors = true;

    boolean updatingControls = false;
    boolean updatingBoardControls = false;
    private Label lblMemory;
    private Label lblPack;

    private TabFolder fTabFolder = null;
    private TabItem fTabItemBoards;
    private TabItem fTabItemDevices;

    protected IRteColumnAdvisor<IRteModelController> fBoardColumnAdvisor = null;

    private Label lblDeviceLabel;
    private Label lblVendorLabel;
    private Label lblPackLabel;
    private Label lblCpuLabel;
    private Label lblMemoryLabel;
    private Label lblSecuritylabel;
    private Label lblFpuLabel;
    private Label lblMve;
    private Label lblEndian;
    private Label lblNewLabel;
    private Label lblBoardVendorLabel;
    private Label lblBoardPackLabel;
    private Label lblBoardName;
    private Label lblBoardVendor;
    private Label lblBoardPack;
    private Label lblBoardLabel;

    BoardsViewColumnAdvisor fBoardsViewColumnAdvisor = null;
    BoardViewContentProvider fBoardViewContentProvider = null;
    BoardsViewLabelProvider fBoardsViewLabelProvider = null;
    BoardTreeColumnComparator fBoardTreeColumnComparator = null;

    private int getSelectedTabIndex() {
        return fTabFolder != null ? fTabFolder.getSelectionIndex() : -1;
    }

    private boolean isDeviceTabSelected() {
        return getSelectedTabIndex() == 1;
    }

    private boolean isBoardTabSelected() {
        return getSelectedTabIndex() == 0;
    }

    /**
     * Create the composite.
     *
     * @param parent
     * @param style
     */
    public RteDeviceSelectorWidget(Composite parent, boolean bShowProcessors) {
        super(parent, SWT.NONE);
        fbShowProcessors = bShowProcessors;

        GridLayout gridLayout = new GridLayout(5, false);
        gridLayout.horizontalSpacing = 9;
        setLayout(gridLayout);

        lblDeviceLabel = new Label(this, SWT.NONE);
        lblDeviceLabel.setText(CpStringsUI.RteDeviceSelectorWidget_DeviceLabel);

        lblDevice = new Label(this, SWT.NONE);
        lblDevice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        new Label(this, SWT.NONE);

        lblCpuLabel = new Label(this, SWT.NONE);
        lblCpuLabel.setText(CpStringsUI.RteDeviceSelectorWidget_CPULabel);

        lblCpu = new Label(this, SWT.NONE);
        lblCpu.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        lblVendorLabel = new Label(this, SWT.NONE);
        lblVendorLabel.setText(CpStringsUI.RteDeviceSelectorWidget_VendorLabel);

        lblVendor = new Label(this, SWT.NONE);
        lblVendor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        new Label(this, SWT.NONE);

        lblMemoryLabel = new Label(this, SWT.NONE);
        lblMemoryLabel.setText(CpStringsUI.RteDeviceSelectorWidget_lblMemory);

        lblMemory = new Label(this, SWT.NONE);
        lblMemory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        lblPackLabel = new Label(this, SWT.NONE);
        lblPackLabel.setText(CpStringsUI.RteDeviceSelectorWidget_lblPack_text);

        lblPack = new Label(this, SWT.NONE);
        lblPack.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        new Label(this, SWT.NONE);

        lblSecuritylabel = new Label(this, SWT.NONE);
        lblSecuritylabel.setText(CpStringsUI.RteDeviceSelectorWidget_lblSecurityLabel_text);

        comboSecurity = new Combo(this, SWT.READ_ONLY);
        comboSecurity.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                if (updatingControls) {
                    return;
                }
                int index = comboSecurity.getSelectionIndex();
                fSelectedSecurity = securityIndexToString(index);
            }
        });
        comboSecurity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        lblBoardLabel = new Label(this, SWT.NONE);
        lblBoardLabel.setText(CpStringsUI.RteDeviceSelectorWidget_lblBoardLabel_text);

        lblBoardName = new Label(this, SWT.NONE);
        lblBoardName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        new Label(this, SWT.NONE);

        lblFpuLabel = new Label(this, SWT.NONE);
        lblFpuLabel.setText(CpStringsUI.RteDeviceSelectorWidget_FPULabel);
        comboFpu = new Combo(this, SWT.READ_ONLY);
        comboFpu.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                if (updatingControls) {
                    return;
                }
                int index = comboFpu.getSelectionIndex();
                fSelectedFpu = fpuIndexToString(index);
            }
        });
        comboFpu.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        lblBoardVendorLabel = new Label(this, SWT.NONE);
        lblBoardVendorLabel.setText(CpStringsUI.RteDeviceSelectorWidget_lblBoardVendorLabel);

        lblBoardVendor = new Label(this, SWT.NONE);
        lblBoardVendor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        new Label(this, SWT.NONE);

        lblMve = new Label(this, SWT.NONE);
        lblMve.setText(CpStringsUI.RteDeviceSelectorWidget_lblMve_text);

        comboMve = new Combo(this, SWT.READ_ONLY);
        comboMve.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        comboMve.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                if (updatingControls) {
                    return;
                }
                int index = comboMve.getSelectionIndex();
                fSelectedMve = mveIndexToString(index);
                if (fSelectedMve.equals(CmsisConstants.FP_MVE) && !fSelectedFpu.equals(CmsisConstants.DP_FPU)) {
                    updateFpu();
                }
            }
        });

        lblBoardPackLabel = new Label(this, SWT.NONE);
        lblBoardPackLabel.setText(CpStringsUI.RteDeviceSelectorWidget_lblBoardPackLabel);

        lblBoardPack = new Label(this, SWT.NONE);
        lblBoardPack.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        new Label(this, SWT.NONE);

        lblEndian = new Label(this, SWT.NONE);
        lblEndian.setText(CpStringsUI.RteDeviceSelectorWidget_Endian);

        comboEndian = new Combo(this, SWT.READ_ONLY);
        comboEndian.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                if (updatingControls) {
                    return;
                }
                fSelectedEndian = adjustEndianString(comboEndian.getText());
            }
        });
        comboEndian.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        // Folder with tabItems
        fTabFolder = new TabFolder(this, SWT.NONE);
        fTabFolder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (isDeviceTabSelected() && fDeviceInfo != null && fDevices != null) {
                    IRteDeviceItem item = fDevices.findItem(fDeviceInfo.attributes());
                    if (item != null) {
                        selectDeviceItem(item);
                    }
                }
            }
        });

        GridData gridTabFolder = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 2);
        gridTabFolder.minimumWidth = 240;
        gridTabFolder.heightHint = 200;
        fTabFolder.setLayoutData(gridTabFolder);

        fTabItemBoards = new TabItem(fTabFolder, SWT.NONE);
        fTabItemBoards.setText(CpStringsUI.RteDeviceSelectorWidget_tabItemBoards);

        fTabItemDevices = new TabItem(fTabFolder, SWT.NONE);

        fTabItemDevices.setText(CpStringsUI.RteDeviceSelectorWidget_tabItemDevices);

        // Devices
        treeViewerDevices = new TreeViewer(fTabFolder, SWT.BORDER);
        treeViewerDevices.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                handleDeviceTreeSelectionChanged(event);
            }
        });
        Tree treeDevices = treeViewerDevices.getTree();
        fTabItemDevices.setControl(treeDevices);

        treeViewerDevices.setContentProvider(new RteDeviceContentProvider());
        treeViewerDevices.setLabelProvider(new RteDeviceLabelProvider());
        treeViewerDevices.addFilter(new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                if (fSearchString.isEmpty() || fSearchString.equals("*")) { //$NON-NLS-1$
                    return true;
                }
                if (element instanceof IRteDeviceItem) {
                    // first check if parent elements (vendor, family, etc. match the pattern)
                    IRteDeviceItem deviceItem = (IRteDeviceItem) element;
                    for (IRteDeviceItem parent = deviceItem.getParent(); parent != null; parent = parent.getParent()) {
                        if (parent.getName().contains(fSearchString)) {
                            return true;
                        }
                    }
                    return findDeviceItem(deviceItem, fSearchString) != null;
                }
                return false;
            }
        });

        fTabFolder.setSelection(fTabItemDevices);

        // Boards
        treeViewerBoards = new TreeViewer(fTabFolder, SWT.BORDER);
        treeViewerBoards.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                handleBoardTreeSelectionChanged(event);
            }
        });
        Tree treeBoards = treeViewerBoards.getTree();
        fTabItemBoards.setControl(treeBoards);

        fBoardsViewColumnAdvisor = new BoardsViewColumnAdvisor(treeViewerBoards);
        treeViewerBoards.setContentProvider(fBoardViewContentProvider = new BoardViewContentProvider(false));
        treeViewerBoards.setComparator(
                fBoardTreeColumnComparator = new BoardTreeColumnComparator(treeViewerBoards, fBoardsViewColumnAdvisor));
        treeViewerBoards.setLabelProvider(fBoardsViewLabelProvider = new BoardsViewLabelProvider(false));

        treeViewerBoards.addFilter(new ViewerFilter() {
            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                if (fSearchString.isEmpty() || fSearchString.equals("*")) { //$NON-NLS-1$
                    return true;
                }
                if (element instanceof IRteBoardItem) {
                    IRteBoardItem boardItem = (IRteBoardItem) element;
                    return findBoardItem(boardItem, fSearchString) != null;
                }
                if (element instanceof IRteDeviceItem) {
                    IRteDeviceItem deviceItem = (IRteDeviceItem) element;
                    return findDeviceItem(deviceItem, fSearchString) != null;
                }
                return false;
            }
        });

        lblNewLabel = new Label(this, SWT.NONE);
        lblNewLabel.setText(CpStringsUI.RteDeviceSelectorWidget_Description);
        new Label(this, SWT.NONE);

        text = new Text(this, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        GridData gd_text = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 2);
        gd_text.widthHint = 240;
        gd_text.minimumWidth = 200;
        text.setLayoutData(gd_text);
        text.setEditable(false);

        Label lblSearch = new Label(this, SWT.NONE);
        lblSearch.setText(CpStringsUI.RteDeviceSelectorWidget_SearchLabel);

        txtSearch = new Text(this, SWT.BORDER);
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                setSearchText(txtSearch.getText());
            }
        });
        txtSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        txtSearch.setToolTipText(CpStringsUI.RteDeviceSelectorWidget_SearchTooltip);

        setTabList(new Control[] { comboSecurity, comboFpu, comboEndian, fTabFolder, txtSearch });

        addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                fSelectedDeviceItem = null;
                fDevices = null;
                fSelectedBoardItem = null;
                fBoards = null;
                listeners.removeAllListeners();
                listeners = null;
            }
        });
    }

    static IRteDeviceItem getDeviceTreeItem(Object obj) {
        if (obj instanceof IRteDeviceItem) {
            return (IRteDeviceItem) obj;
        }
        return null;
    }

    IRteBoardItem getBoardDeviceTreeItem(Object obj) {
        if (obj instanceof IRteBoardItem) {
            return (IRteBoardItem) obj;
        }
        return null;

    }

    public class RteDeviceLabelProvider extends LabelProvider {
        @Override
        public Image getImage(Object element) {
            IRteDeviceItem item = getDeviceTreeItem(element);
            if (item != null) {
                if (item.getLevel() == EDeviceHierarchyLevel.VENDOR.ordinal()) {
                    return CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT);
                } else if (!isEndLeaf(item)) {
                    return CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT_CLASS);
                }
                return CpPlugInUI.getImage(CpPlugInUI.ICON_DEVICE);
            }
            return null;
        }

        @Override
        public String getText(Object element) {
            IRteDeviceItem item = getDeviceTreeItem(element);
            if (item != null) {
                return item.getName();
            }
            return CmsisConstants.EMPTY_STRING;
        }
    }

    public class RteDeviceContentProvider extends TreeObjectContentProvider {

        @Override
        public Object[] getChildren(Object parent) {
            IRteDeviceItem item = getDeviceTreeItem(parent);
            if (item != null && item.hasChildren() && !isEndLeaf(item)) {
                return super.getChildren(item);
            }
            return ITreeObject.EMPTY_OBJECT_ARRAY;
        }

        @Override
        public boolean hasChildren(Object parent) {
            IRteDeviceItem item = getDeviceTreeItem(parent);
            if (item == null)
                return false;
            if (!item.hasChildren())
                return false;
            if (isEndLeaf(item))
                return false;
            return true;
        }
    }

    public boolean isShowProcessors() {
        return fbShowProcessors;
    }

    public void setShowProcessors(boolean bShow) {
        fbShowProcessors = bShow;
    }

    protected IRteDeviceItem findDeviceItem(IRteDeviceItem item, String substring) {
        if (item == null) {
            return null;
        }
        if (substring.isEmpty() || substring.equals(CmsisConstants.ASTERISK))
            return item;

        if (item.getName().contains(substring))
            return item;

        if (substring.contains(CmsisConstants.ASTERISK))
            return item.getFirstChild(substring);

        Collection<? extends IRteDeviceItem> children = item.getChildren();
        if (children == null) {
            return null;
        }

        for (IRteDeviceItem device : children) {
            IRteDeviceItem matchingItem = findDeviceItem(device, substring);
            if (matchingItem != null) {
                return matchingItem;
            }
        }

        return null;
    }

    protected IRteBoardDeviceItem findBoardItem(IRteBoardDeviceItem item, String substring) {
        if (item == null) {
            return null;
        }
        if (substring.isEmpty() || substring.equals(CmsisConstants.ASTERISK))
            return item;

        if (item.getName().contains(substring))
            return item;

        if (substring.contains(CmsisConstants.ASTERISK))
            return item.getFirstChild(substring);

        Collection<? extends IRteBoardDeviceItem> children = item.getChildren();
        if (children == null) {
            return null;
        }

        for (IRteBoardDeviceItem di : children) {
            IRteBoardDeviceItem matchingItem = findBoardItem(di, substring);
            if (matchingItem != null) {
                return matchingItem;
            }
        }

        return null;
    }

    boolean setSearchText(String s) {
        if (fSearchString.equals(s)) {
            return false;
        }
        fSearchString = s;

        if (isDeviceTabSelected()) {
            // Search must be a substring of an existing value
            if (fDevices == null || !fDevices.hasChildren()) {
                return false;
            }

            IRteDeviceItem deviceItem = null;
            if (!fSearchString.isEmpty() && !fSearchString.equals("*")) { //$NON-NLS-1$
                if (fSelectedDeviceItem != null
                        && findDeviceItem(fSelectedDeviceItem, fSearchString) == fSelectedDeviceItem) {
                    deviceItem = fSelectedDeviceItem;
                } else {
                    deviceItem = findDeviceItem(fDevices, fSearchString);
                }
            }
            refreshTree();
            if (deviceItem != null) {
                selectDeviceItem(deviceItem);
                treeViewerDevices.setExpandedState(deviceItem, true);
            }
        } else if (isBoardTabSelected()) {
            if (fBoards == null || !fBoards.hasChildren()) {
                return false;
            }
            IRteBoardDeviceItem boardItem = null;
            if (!fSearchString.isEmpty() && !fSearchString.equals("*")) { //$NON-NLS-1$
                if (fSelectedBoardItem != null
                        && findBoardItem(fSelectedBoardItem, fSearchString) == fSelectedBoardItem) {
                    boardItem = fSelectedBoardItem;
                } else {
                    boardItem = findBoardItem(fBoards, fSearchString);
                }
            }
            refreshBoardTree();
            if (boardItem != null) {
                selectBoardItem(boardItem);
                treeViewerBoards.setExpandedState(boardItem, true);
            }
        }
        return true;
    }

    private void refreshTree() {
        treeViewerDevices.refresh();
    }

    private void refreshBoardTree() {
        treeViewerBoards.refresh();
    }

    /**
     * Sets collection of the available devices
     *
     * @param devices IRteDeviceItem root of device tree
     */
    public void setDevices(IRteDeviceItem devices) {
        this.fDevices = devices;
        treeViewerDevices.setInput(devices);
    }

    /**
     * Sets collection of the available boards
     *
     * @param boards IRteBoardItem root of board tree
     */
    public void setBoards(IRteBoardItem boards) {
        this.fBoards = boards;
        treeViewerBoards.setInput(boards);
    }

    /**
     * Returns selected device item of device tree
     *
     * @return selected IRteDeviceItem
     */
    public IRteDeviceItem getSelectedDeviceItem() {
        if (fSelectedDevice != null) {
            return fSelectedDeviceItem;
        }
        return null;
    }

    protected void handleDeviceTreeSelectionChanged(SelectionChangedEvent event) {
        updateDeviceItem(getSelectedItem());
    }

    protected void updateDeviceItem(IRteDeviceItem selectedItem) {
        if (selectedItem == fSelectedDeviceItem) {
            return;
        }
        fSelectedDeviceItem = selectedItem;
        updateDeviceInfo();
        updateControls();

        if (fSelectedBoardItem != null && isDeviceTabSelected() && isEndLeaf(fSelectedDeviceItem)) {
            // Update board info if needed
            if (isMountedDevice()) {
                updateBoardItem(fSelectedBoardItem);// Note: fSelectedBoardItem will be null.
            } else {
                IRteBoardDeviceItem parentBoard = fSelectedBoardItem; // Save board for next device selection.
                updateBoardItem(null);// Note: fSelectedBoardItem will be null.
                fSelectedBoardItem = parentBoard;
            }
        }
    }

    /**
     * Check if currently selected device is a mounted device on the selected board
     *
     * @return true if device is mounted
     */
    protected boolean isMountedDevice() {
        if (fSelectedBoardItem == null || fSelectedDevice == null) {
            return false;
        }

        if (fSelectedBoardItem.getRteDeviceLeaf() == fSelectedDeviceItem) {
            return true;
        }

        ICpBoard board = fSelectedBoardItem.getBoard();
        if (board != null && fDeviceInfo != null) {
            return board.hasMountedDevice(fDeviceInfo.attributes());
        }
        return false;
    }

    private void updateDeviceInfo() {
        if (fSelectedDeviceItem != null && isEndLeaf(fSelectedDeviceItem)) {
            fSelectedDevice = fSelectedDeviceItem.getDevice();
            fDeviceInfo = null;
            ICpItem props = fSelectedDeviceItem.getEffectiveProperties();
            if (props != null) {
                fDeviceInfo = new CpDeviceInfo(null, fSelectedDevice, fSelectedDeviceItem.getName());
            }
        } else {
            fSelectedDevice = null;
            fDeviceInfo = null;
        }
    }

    protected void handleBoardTreeSelectionChanged(SelectionChangedEvent event) {
        updateBoardItem(getSelectedBoardDeviceItem());
    }

    protected void updateBoardItem(IRteBoardDeviceItem selectedItem) {
        if (selectedItem == fSelectedBoardItem && fBoardInfo != null) {
            return;
        }
        fSelectedBoardItem = selectedItem;

        updateBoardInfo();
        updateBoardControls();

        IRteDeviceItem deviceItem = fSelectedBoardItem != null ? fSelectedBoardItem.getRteDeviceLeaf() : null;
        if (deviceItem != fSelectedDeviceItem && isBoardTabSelected()) {
            updateDeviceItem(deviceItem);
        }
    }

    private void updateBoardInfo() {
        if (fSelectedBoardItem != null && fSelectedBoardItem.getBoard() != null) {
            fBoardInfo = new CpBoardInfo(null, fSelectedBoardItem.getBoard());
        } else {
            fBoardInfo = null;
        }
    }

    protected void updateControls() {
        updatingControls = true;
        String description = CmsisConstants.EMPTY_STRING;
        String vendorName = CmsisConstants.EMPTY_STRING;
        String deviceName = CmsisConstants.EMPTY_STRING;
        String cpu = CmsisConstants.EMPTY_STRING;
        String pack = CmsisConstants.EMPTY_STRING;
        String mem = CmsisConstants.EMPTY_STRING;
        String message = null;

        if (fSelectedDeviceItem != null) {
            IRteDeviceItem vendorItem = fSelectedDeviceItem.getVendorItem();
            if (vendorItem != null) {
                vendorName = vendorItem.getName();
            }
            if (isEndLeaf(fSelectedDeviceItem)) {
                deviceName = fSelectedDeviceItem.getName();
            }
            description = fSelectedDeviceItem.getDescription();
        } else {
            message = "Device is not installed. Please install the pack first, or select a new device"; //$NON-NLS-1$
        }

        if (fDeviceInfo != null) {
            String clock = fDeviceInfo.getClockSummary();
            pack = fDeviceInfo.getPackId();
            cpu = CmsisConstants.ARM + CmsisConstants.SPACE + fDeviceInfo.getAttribute(CmsisConstants.DCORE)
                    + CmsisConstants.SPACE + clock;
            mem = fDeviceInfo.getMemorySummary();
            // this happens in the import process, when the device is not installed
            if (deviceName.isEmpty()) {
                deviceName = fDeviceInfo.getFullDeviceName();
            }
            if (vendorName.isEmpty()) {
                vendorName = fDeviceInfo.getVendor();
            }

            lblVendor.setText(vendorName);
            lblDevice.setText(deviceName);
            text.setText(description);
            lblCpu.setText(cpu);
            lblPack.setText(pack);
            lblMemory.setText(mem);

            updateSecurity();
            updateMve();
            updateFpu();
            updateEndian();
        } else {
            message = CpStringsUI.RteDeviceSelectorWidget_NoDeviceSelected;
            cleanDeviceControls();
        }

        updateStatus(message);
        updatingControls = false;
    }

    protected void updateBoardControls() {
        updatingBoardControls = true;
        String boardName = CmsisConstants.EMPTY_STRING;
        String boardVendor = CmsisConstants.EMPTY_STRING;
        String boardPack = CmsisConstants.EMPTY_STRING;
        String message = null;

        if (fBoardInfo == null && fDeviceInfo == null) { // New project will be created.
            message = CpStringsUI.RteDeviceSelectorWidget_NoBoardSelected;
            updateStatus(message);
        } else {
            if (fBoardInfo == null)
                cleanBoardControls();

            else {
                boardName = fBoardInfo.getName();
                boardVendor = fBoardInfo.getVendor();
                boardPack = fBoardInfo.getPackId();

                lblBoardName.setText(boardName);
                if (boardVendor != null)
                    lblBoardVendor.setText(boardVendor);
                lblBoardPack.setText(boardPack);
            }
        }
        if (message != null)
            updateStatus(message);

        updatingBoardControls = false;
    }

    protected void cleanDeviceControls() {
        lblDevice.setText(CmsisConstants.EMPTY_STRING);
        lblVendor.setText(CmsisConstants.EMPTY_STRING);
        lblPack.setText(CmsisConstants.EMPTY_STRING);
        lblCpu.setText(CmsisConstants.EMPTY_STRING);
        lblMemory.setText(CmsisConstants.EMPTY_STRING);
        text.setText(CmsisConstants.EMPTY_STRING);
    }

    protected void cleanBoardControls() {
        lblBoardName.setText(CmsisConstants.EMPTY_STRING);
        lblBoardVendor.setText(CmsisConstants.EMPTY_STRING);
        lblBoardPack.setText(CmsisConstants.EMPTY_STRING);
    }

    /**
     * Returns IRteDeviceItem currently selected in the tree
     */
    private IRteDeviceItem getSelectedItem() {
        IStructuredSelection sel = (IStructuredSelection) treeViewerDevices.getSelection();
        if (sel.size() == 1) {
            Object o = sel.getFirstElement();
            if (o instanceof IRteDeviceItem) {
                return (IRteDeviceItem) o;
            }
        }
        return null;
    }

    /**
     * Returns object (either IRteBoardItem or IRteBoardDeviceItem) currently
     * selected in the boards tree
     *
     * @return
     */
    private IRteBoardDeviceItem getSelectedBoardDeviceItem() {
        IStructuredSelection sel = (IStructuredSelection) treeViewerBoards.getSelection();
        if (sel.size() == 1) {
            Object o = sel.getFirstElement();
            if (o instanceof IRteBoardDeviceItem) {
                return (IRteBoardDeviceItem) o;
            }
        }
        return null;
    }

    private void updateEndian() {
        String endian = null;
        if (fDeviceInfo != null) {
            endian = fDeviceInfo.getAttribute(CmsisConstants.DENDIAN);
        }

        String[] endianStrings = getEndianStrings(endian);
        comboEndian.setItems(endianStrings);

        endian = adjustEndianString(fSelectedEndian);
        int index = comboEndian.indexOf(endian);
        if (index < 0 || index >= endianStrings.length) {
            index = 0;
        }

        comboEndian.select(index);
        comboEndian.setEnabled(endianStrings.length > 1);
    }

    private String[] getEndianStrings(String endian) {
        if (endian != null) {
            switch (endian) {
            case CmsisConstants.BIGENDIAN:
                return new String[] { CmsisConstants.BIGENDIAN };
            case CmsisConstants.CONFIGENDIAN:
            case "*": //$NON-NLS-1$
                return new String[] { CmsisConstants.LITTLENDIAN, CmsisConstants.BIGENDIAN };
            case CmsisConstants.LITTLENDIAN:
            default:
                return new String[] { CmsisConstants.LITTLENDIAN };
            }
        }
        return new String[] { CmsisConstants.EMPTY_STRING };
    }

    String adjustEndianString(String endian) {
        if (endian != null) {
            if (endian.equals(CmsisConstants.BIGENDIAN) || endian.equals(CmsisConstants.LITTLENDIAN)) {
                return endian;
            }
        }
        return CmsisConstants.LITTLENDIAN;
    }

    private void updateFpu() {
        String fpu = null;
        if (fDeviceInfo != null) {
            fpu = fDeviceInfo.getAttribute(CmsisConstants.DFPU);
            if (fSelectedFpu.isEmpty())
                fSelectedFpu = fpu;
        }

        if (fSelectedMve.equals(CmsisConstants.FP_MVE)) {
            fSelectedFpu = CmsisConstants.DP_FPU;
        }

        String[] fpuStrings = getFpuStrings(fpu);
        int index = fpuStringToIndex(fSelectedFpu);
        if (index < 0 || index >= fpuStrings.length) {
            index = fpuStringToIndex(fpu);
        }
        if (index < 0) {
            index = 0;
        }

        comboFpu.setItems(fpuStrings);
        comboFpu.select(index);
        comboFpu.setEnabled(fpuStrings.length > 1);
    }

    private void updateMve() {
        int index = -1;
        String mve = null;
        if (fDeviceInfo != null && fDeviceInfo.getCoreArchitecture().isARMv8_1()) {
            mve = fDeviceInfo.getAttribute(CmsisConstants.DMVE);
            if (fSelectedMve.isEmpty())
                fSelectedMve = mve;
            index = mveStringToIndex(fSelectedMve);
        }

        if (index < 0) {
            comboMve.removeAll();
            comboMve.setEnabled(false);
        } else {
            String[] mveStrings = getMveStrings(mve);
            if (index >= mveStrings.length) {
                index = mveStringToIndex(mve);
            }
            comboMve.setItems(mveStrings);
            comboMve.setEnabled(mveStrings.length > 1);
            comboMve.select(index);
        }
    }

    private String[] getFpuStrings(String fpu) {
        if (fpu != null) {
            switch (fpu) {
            case "1": //$NON-NLS-1$
            case CmsisConstants.FPU:
            case CmsisConstants.SP_FPU:
                return new String[] { CpStringsUI.RteDeviceSelectorWidget_none,
                        CpStringsUI.RteDeviceSelectorWidget_SinglePrecision };
            case CmsisConstants.DP_FPU:
                return new String[] { CpStringsUI.RteDeviceSelectorWidget_none,
                        CpStringsUI.RteDeviceSelectorWidget_SinglePrecision,
                        CpStringsUI.RteDeviceSelectorWidget_DoublePrecision };
            default:
                return new String[] { CpStringsUI.RteDeviceSelectorWidget_none };
            }
        }
        return new String[] { CmsisConstants.EMPTY_STRING };
    }

    private String[] getMveStrings(String mve) {
        if (mve != null) {
            switch (mve) {
            case CmsisConstants.MVE:
                return new String[] { CpStringsUI.RteDeviceSelectorWidget_none,
                        CpStringsUI.RteDeviceSelectorWidget_Integer };
            case CmsisConstants.FP_MVE:
                return new String[] { CpStringsUI.RteDeviceSelectorWidget_none,
                        CpStringsUI.RteDeviceSelectorWidget_Integer,
                        CpStringsUI.RteDeviceSelectorWidget_IntegerAndFloat };
            default:
                return new String[] { CpStringsUI.RteDeviceSelectorWidget_none };
            }
        }
        return new String[] { CmsisConstants.EMPTY_STRING };
    }

    String mveIndexToString(int index) {
        switch (index) {
        case -1:
            return null;
        case 1:
            return CmsisConstants.MVE;
        case 2:
            return CmsisConstants.FP_MVE;
        default:
            break;
        }
        return CmsisConstants.NO_MVE;
    }

    private int mveStringToIndex(String mve) {
        if (mve != null) {
            switch (mve) {
            case CmsisConstants.MVE:
                return 1;
            case CmsisConstants.FP_MVE:
                return 2;
            default:
                return 0;
            }
        }
        return -1;
    }

    private void updateSecurity() {
        int index = -1;
        if (fDeviceInfo != null && CmsisConstants.TZ.equals(fDeviceInfo.getAttribute(CmsisConstants.DTZ))) {
            index = securityStringToIndex(fSelectedSecurity);
            if (index < 0) {
                String security = fDeviceInfo.getAttribute(CmsisConstants.DSECURE);
                index = securityStringToIndex(security);
            }
            if (index < 0)
                index = 0;
        }

        if (index < 0) {
            comboSecurity.removeAll();
            comboSecurity.setEnabled(false);
        } else {
            comboSecurity.setItems(SECURITY_VALUES);
            comboSecurity.setEnabled(true);
            comboSecurity.select(index);
        }

    }

    private int securityStringToIndex(String security) {
        if (security != null) {
            switch (security) {
            case CmsisConstants.NON_SECURE:
                return 0;
            case CmsisConstants.SECURE:
                return 1;
            case CmsisConstants.TZ_DISABLED:
                return 2;
            }
        }
        return -1;
    }

    String securityIndexToString(int index) {
        if (comboSecurity.isEnabled()) {
            switch (index) {
            case 0:
                return CmsisConstants.NON_SECURE;
            case 1:
                return CmsisConstants.SECURE;
            case 2:
                return CmsisConstants.TZ_DISABLED;
            }
        }
        return CmsisConstants.EMPTY_STRING;
    }

    String fpuIndexToString(int index) {
        switch (index) {
        case 1:
            return CmsisConstants.SP_FPU;
        case 2:
            return CmsisConstants.DP_FPU;
        default:
            break;
        }
        return CmsisConstants.NO_FPU;
    }

    private int fpuStringToIndex(String fpu) {
        if (fpu != null) {
            switch (fpu) {
            case "1": //$NON-NLS-1$
            case CmsisConstants.FPU:
            case CmsisConstants.SP_FPU:
                return 1;
            case CmsisConstants.DP_FPU:
                return 2;
            default:
                return 0;
            }
        }
        return -1;
    }

    private void updateStatus(String message) {
        // notify listeners
        listeners.notifyListeners(message);
    }

    public void addListener(IStatusMessageListener listener) {
        listeners.addListener(listener);
    }

    public void removeListener(IStatusMessageListener listener) {
        listeners.removeListener(listener);
    }

    /**
     * Returns device info for selected device
     *
     * @return ICpDeviceInfo
     */
    public ICpDeviceInfo getDeviceInfo() {
        if (fDeviceInfo != null) {
            int index = comboFpu.getSelectionIndex();
            String fpu = fpuIndexToString(index);
            fDeviceInfo.setAttribute(CmsisConstants.DFPU, fpu);

            String endian = comboEndian.getText();
            if (endian == null || endian.isEmpty()) {
                endian = CmsisConstants.LITTLENDIAN;
            }
            fDeviceInfo.setAttribute(CmsisConstants.DENDIAN, endian);

            index = comboSecurity.getSelectionIndex();
            if (index >= 0) {
                String security = securityIndexToString(index);
                fDeviceInfo.setAttribute(CmsisConstants.DSECURE, security);
            } else {
                fDeviceInfo.removeAttribute(CmsisConstants.DSECURE);
            }

            index = comboMve.getSelectionIndex();
            if (index >= 0) {
                String mve = mveIndexToString(index);
                fDeviceInfo.setAttribute(CmsisConstants.DMVE, mve);
                if (fSelectedMve.equals(CmsisConstants.FP_MVE)) {
                    fDeviceInfo.setAttribute(CmsisConstants.DFPU, CmsisConstants.FP_MVE);
                }
            } else {
                fDeviceInfo.removeAttribute(CmsisConstants.DMVE);
            }

        }
        return fDeviceInfo;
    }

    public void setDeviceInfo(ICpDeviceInfo deviceInfo) {
        fDeviceInfo = deviceInfo;
        if (fDeviceInfo != null) {
            fSelectedEndian = fDeviceInfo.getAttribute(CmsisConstants.DENDIAN);
            fSelectedSecurity = fDeviceInfo.getAttribute(CmsisConstants.DSECURE);
            fSelectedMve = fDeviceInfo.getAttribute(CmsisConstants.DMVE);
            if (fSelectedMve.equals(CmsisConstants.FP_MVE)) {
                fSelectedFpu = CmsisConstants.DP_FPU;
            } else {
                fSelectedFpu = fDeviceInfo.getAttribute(CmsisConstants.DFPU);
            }

            // set initial selection
            IRteDeviceItem item = fDevices.findItem(fDeviceInfo.attributes());
            if (item != null) {
                selectDeviceItem(item); // It will call handleDeviceTreeSelectionChanged method automatically.
            } else {
                String message = NLS.bind(CpStringsUI.RteDeviceSelectorWidget_DeviceNotFound,
                        FullDeviceName.getFullDeviceName(deviceInfo.attributes()));
                updateStatus(message);
            }
        } else {
            updateStatus(CpStringsUI.RteDeviceSelectorWidget_NoDeviceSelected);
        }
    }

    /**
     * Selects given device item in devices tree
     *
     * @param item device item to select
     */
    public void selectDeviceItem(IRteDeviceItem item) {
        if (item != null) {
            Object[] path = item.getHierachyPath();
            TreePath tp = new TreePath(path);
            TreeSelection ts = new TreeSelection(tp);
            treeViewerDevices.setSelection(ts, true);
        }
    }

    /**
     * Selects given board item in boards tree
     *
     * @param item device item to select
     */
    public void selectBoardItem(IRteBoardDeviceItem item) {
        if (item != null) {
            Object[] path = item.getHierachyPath();
            TreePath tp = new TreePath(path);
            TreeSelection ts = new TreeSelection(tp);
            treeViewerBoards.setSelection(ts, true);
        }
    }

    protected boolean isEndLeaf(IRteDeviceItem item) {
        if (item == null)
            return false;

        if (item.getLevel() < EDeviceHierarchyLevel.DEVICE.ordinal()) {
            return false;
        }
        if (!item.hasChildren()) {
            return true;
        }
        IRteDeviceItem child = item.getFirstChild();
        if (!isShowProcessors() && child.getLevel() == EDeviceHierarchyLevel.PROCESSOR.ordinal())
            return true;

        return false;
    }

    public ICpBoardInfo getBoardInfo() {
        return fBoardInfo;
    }

    public void setBoardInfo(ICpBoardInfo boardInfo) {
        fBoardInfo = boardInfo;
        if (fBoardInfo != null) {
            ICpBoard board = fBoardInfo.getBoard();
            if (board != null) {
                // set initial selection
                IRteBoardItem item = fBoards.findBoard(board.getId());
                if (item != null)
                    selectBoardItem(item); // It will call handleBoardTreeSelectionChanged method automatically.
            }
            if (fTabFolder != null && fTabItemBoards != null) {
                fTabFolder.setSelection(fTabItemBoards);
            }
        }
    }
}