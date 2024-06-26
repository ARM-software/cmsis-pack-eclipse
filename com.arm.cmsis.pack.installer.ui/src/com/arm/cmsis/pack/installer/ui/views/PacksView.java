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

package com.arm.cmsis.pack.installer.ui.views;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackInstaller;
import com.arm.cmsis.pack.ICpPackManager;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpItem;
import com.arm.cmsis.pack.data.CpPack;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPack.PackState;
import com.arm.cmsis.pack.data.ICpPackCollection;
import com.arm.cmsis.pack.data.ICpPackFamily;
import com.arm.cmsis.pack.generic.ITreeObject;
import com.arm.cmsis.pack.installer.ui.ButtonId;
import com.arm.cmsis.pack.installer.ui.IHelpContextIds;
import com.arm.cmsis.pack.installer.ui.Messages;
import com.arm.cmsis.pack.item.ICmsisItem;
import com.arm.cmsis.pack.ui.ColorConstants;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.tree.AdvisedCellLabelProvider;
import com.arm.cmsis.pack.ui.tree.ColumnAdvisor;
import com.arm.cmsis.pack.ui.tree.IColumnAdvisor;
import com.arm.cmsis.pack.ui.tree.TreeObjectContentProvider;
import com.arm.cmsis.pack.utils.Utils;
import com.arm.cmsis.pack.utils.VersionComparator;

/**
 * The Packs View in the Pack Manager perspective
 */
public class PacksView extends PackInstallerView {

    /**
     * The ID of the view as specified by the extension.
     */
    public static final String ID = "com.arm.cmsis.pack.installer.ui.views.PacksView"; //$NON-NLS-1$

    private static final String ROOT = "Root"; //$NON-NLS-1$
    private Action fRemovePack;
    private Action fDeletePack;
    private Action fInstallSinglePack;
    private Action fInstallRequiredPacks;
    private Action fManLocalRepo;

    private ColumLabelProviderWithImage fColumnLabelProviderWithImage;

    class PacksViewContentProvider extends TreeObjectContentProvider {

        @Override
        public Object[] getChildren(Object parentElement) {
            ICpItem item = ICpItem.cast(parentElement);
            if (item == null || item instanceof ICpPack) {
                return ITreeObject.EMPTY_OBJECT_ARRAY;
            }
            return item.getChildArray();
        }

        @Override
        public Object getParent(Object element) {
            ICpItem item = ICpItem.cast(element);
            if (item == null) {
                return null;
            }
            if (CmsisConstants.RELEASE_TAG.equals(item.getTag())) {
                ICpPack pack = item.getPack();
                if (pack == null) {
                    return null;
                }
                ICpItem parent = pack.getParent();
                if (parent instanceof ICpPackFamily) {
                    ICpPackFamily family = (ICpPackFamily) parent;
                    return family.getPreviousReleases();
                }
            }
            return item.getParent();
        }

        @Override
        public boolean hasChildren(Object element) {
            ICpItem item = ICpItem.cast(element);
            if (item == null || item instanceof ICpPack) {
                return false;
            }
            return item.hasChildren();
        }
    }

    class PacksViewColumnAdvisor extends ColumnAdvisor {
        private String packCountButtonString;

        public PacksViewColumnAdvisor(ColumnViewer columnViewer) {
            super(columnViewer);
        }

        @Override
        public CellControlType getCellControlType(Object obj, int columnIndex) {
            if (columnIndex == COLBUTTON) {
                return CellControlType.BUTTON;
            }
            return CellControlType.TEXT;
        }

        @Override
        public boolean isEnabled(Object obj, int columnIndex) {
            ICpPackInstaller packInstaller = getPackInstaller();
            if (packInstaller == null) {
                return false;
            }
            if (packInstaller.isUpdatingPacks()) {
                return false;
            }

            ButtonId buttonId = getButtonId(obj, columnIndex);
            if ((buttonId == ButtonId.BUTTON_UPTODATE) || (buttonId == ButtonId.BUTTON_OFFLINE)
                    || (buttonId == ButtonId.BUTTON_DEPRECATED)) {
                return false;
            }
            ICpItem cpItem = ICpItem.cast(obj);
            if (cpItem != null && !ROOT.equals(cpItem.getTag())) {
                if (cpItem instanceof ICpPackCollection) {
                    return false;
                } else if (cpItem instanceof ICpPackFamily) {
                    return !packInstaller.isProcessing(cpItem.getPackId());
                } else if (CmsisConstants.PREVIOUS.equals(cpItem.getTag())) {
                    return false;
                } else {
                    return !packInstaller.isProcessing(CpPack.getFullPackId(cpItem));
                }
            }
            return false;
        }

        @Override
        public Color getBgColor(Object obj, int columnIndex) {
            if (getCellControlType(obj, columnIndex) == CellControlType.BUTTON) {
                ICpItem item = ICpItem.cast(obj);
                if (item != null) {
                    if (CmsisConstants.GENERIC.equals(item.getTag())
                            || CmsisConstants.DEVICE_SPECIFIC.equals(item.getTag())) {
                        return null;
                    }
                    return ColorConstants.COLOR_BUTTON_TOP;
                }
            }
            return null;
        }

        @Override
        public boolean isEmpty(Object obj, int columnIndex) {
            if (getCellControlType(obj, columnIndex) == CellControlType.BUTTON) {
                ICpItem item = ICpItem.cast(obj);
                if (item != null) {
                    String tag = item.getTag();
                    if (CmsisConstants.PREVIOUS.equals(tag) || CmsisConstants.GENERIC.equals(tag)
                            || CmsisConstants.DEVICE_SPECIFIC.equals(tag)) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public Image getImage(Object obj, int columnIndex) {
            if (getCellControlType(obj, columnIndex) == CellControlType.BUTTON) {
                switch (getButtonId(obj, columnIndex)) {
                case BUTTON_UPTODATE: // the latest pack is installed
                    if (obj instanceof ICpPackFamily) {
                        ICpPackFamily pf = (ICpPackFamily) obj;
                        if (!CpPlugIn.getPackManager().isRequiredPacksInstalled(pf.getPack())) {
                            return CpPlugInUI.getImage(CpPlugInUI.ICON_RTE_SUB_WARNING);
                        }

                        if (CpPlugIn.getPackManager().isLocalRepository(pf.getPack())) {
                            return CpPlugInUI.getImage(CpPlugInUI.ICON_RTE_INSTALLED_LOCAL_REPO);
                        }
                    }
                    return CpPlugInUI.getImage(CpPlugInUI.ICON_RTE);

                case BUTTON_UPDATE:
                case BUTTON_UPDATE_PLUS:
                case BUTTON_OFFLINE:
                case BUTTON_DEPRECATED:
                case BUTTON_RESOLVE:
                    return CpPlugInUI.getImage(CpPlugInUI.ICON_RTE_WARNING);
                case BUTTON_INSTALL:
                case BUTTON_INSTALL_PLUS:
                    return CpPlugInUI.getImage(CpPlugInUI.ICON_RTE_INSTALL);
                case BUTTON_UNPACK:
                case BUTTON_UNPACK_PLUS:
                    return CpPlugInUI.getImage(CpPlugInUI.ICON_RTE_UNPACK);
                case BUTTON_REMOVE:
                case BUTTON_DELETE:
                case BUTTON_DELETE_ALL:
                case BUTTON_REPOSITORY:
                    if (getButtonId(obj, columnIndex) == ButtonId.BUTTON_REPOSITORY) {
                        if (CpPlugIn.getPackManager().isLocalRepository((ICpPack) obj)) {
                            return CpPlugInUI.getImage(CpPlugInUI.ICON_RTE_INSTALLED_LOCAL_REPO);
                        }
                    }
                    return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_DELETE);
                default:
                    break;
                }
            }
            return null;
        }

        @Override
        public String getString(Object element, int index) {
            if (getCellControlType(element, index) != CellControlType.BUTTON) {
                return CmsisConstants.EMPTY_STRING;
            }

            ButtonId buttonId = getButtonId(element, index);
            switch (buttonId) {
            case BUTTON_1PACK:
            case BUTTON_PACKS:
                return packCountButtonString;
            default:
                return getButtonString(buttonId);
            }
        }

        private ButtonId getPackCountButtonId(int count) {
            if (count == 1) {
                packCountButtonString = Messages.PackInstallerView_Bt1Pack;
                return ButtonId.BUTTON_1PACK;
            } else if (count > 1) {
                packCountButtonString = count + Messages.PackInstallerView_BtPacks;
                return ButtonId.BUTTON_PACKS;
            }
            return ButtonId.BUTTON_UNDEFINED;
        }

        @Override
        public String getTooltipText(Object obj, int columnIndex) {
            ICpItem item = ICpItem.cast(obj);
            if (item != null) {
                String toolTip = getActionTooltip(item);
                return toolTip != null ? toolTip : getNameTooltip(item);
            }
            return null;
        }

        @Override
        protected void handleMouseUp(MouseEvent e) {
            if (e.button == 1) {
                ICpPackInstaller packInstaller = getPackInstaller();
                if (packInstaller == null) {
                    return;
                }

                Point pt = new Point(e.x, e.y);
                ViewerCell cell = getViewer().getCell(pt);
                if (cell == null) {
                    return;
                }

                int colIndex = cell.getColumnIndex();
                Object element = cell.getElement();
                if (getCellControlType(element, colIndex) != CellControlType.BUTTON || !isEnabled(element, colIndex)
                        || !isButtonPressed(element, colIndex)) {
                    return;
                }

                ICpItem cpItem = ICpItem.cast(element);
                if (cpItem == null || ROOT.equals(cpItem.getTag())) {
                    return;
                }

                String packId = CpPack.getFullPackId(cpItem);
                switch (getButtonId(element, colIndex)) {
                case BUTTON_INSTALL:
                case BUTTON_INSTALL_PLUS:
                case BUTTON_UPDATE:
                case BUTTON_UPDATE_PLUS:
                case BUTTON_UNPACK:
                case BUTTON_UNPACK_PLUS:
                    packInstaller.installPack(packId);
                    break;
                case BUTTON_REMOVE:
                    packInstaller.removePack((ICpPack) cpItem, false);
                    break;
                case BUTTON_DELETE:
                    packInstaller.removePack((ICpPack) cpItem, true);
                    break;
                case BUTTON_RESOLVE:
                    packInstaller.installRequiredPacks((ICpPack) cpItem);
                    break;
                case BUTTON_DELETE_ALL:
                    for (Iterator<? extends ICpItem> iter = cpItem.getChildren().iterator(); iter.hasNext();) {
                        ICpPack pack = (ICpPack) iter.next();
                        packInstaller.removePack(pack, true);
                    }
                    break;
                case BUTTON_REPOSITORY:
                    Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                    PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(shell,
                            "com.arm.cmsis.pack.ui.CpManLocalRepoPage", null, null); //$NON-NLS-1$
                    dialog.open();
                    break;
                default:
                    break;
                }

                setButtonPressed(null, COLBUTTON, null);
                this.control.redraw();
            }
        }

        protected ButtonId getButtonId(Object element, int index) {
            ButtonId buttonId = ButtonId.BUTTON_UNDEFINED;

            if (getCellControlType(element, index) != CellControlType.BUTTON) {
                return buttonId;
            }
            if ((element instanceof ICpPackFamily) && !(element instanceof ICpPackCollection)) {
                ICpPackFamily packFamily = ((ICpPackFamily) element);
                if (CmsisConstants.ERRORS.equals(packFamily.getTag())) {
                    return ButtonId.BUTTON_DELETE_ALL;
                }
                ICpPack latestPack = packFamily.getPack();
                if (latestPack == null) {
                    return buttonId;
                }
                Collection<? extends ICpItem> releases = latestPack.getReleases();
                if (releases == null) {
                    return ButtonId.BUTTON_DELETE;
                }
                if (latestPack.isDeprecated()) {
                    return ButtonId.BUTTON_DEPRECATED;
                }

                if (latestPack.getPackState().isInstalledOrLocal()) {
                    return ButtonId.BUTTON_UPTODATE;
                }
                boolean bPackInstalled = false, bPackOffline = false;
                for (ICpPack pack : packFamily.getPacks()) {
                    if (pack.getPackState().isInstalledOrLocal()) {
                        bPackInstalled = true;
                        ICpItem urlItem = pack.getFirstChild(CmsisConstants.URL);
                        if (urlItem == null || !Utils.isValidURL(urlItem.getText())) {
                            bPackOffline = true;
                        }
                        break;
                    }
                }
                if (!bPackInstalled) {
                    if (CpPlugIn.getPackManager().isRequiredPacksInstalled(latestPack)) {
                        return ButtonId.BUTTON_INSTALL;
                    }
                    return ButtonId.BUTTON_INSTALL_PLUS;
                } else if (bPackOffline) {
                    return ButtonId.BUTTON_OFFLINE;
                } else {
                    if (CpPlugIn.getPackManager().isRequiredPacksInstalled(latestPack)) {
                        return ButtonId.BUTTON_UPDATE;
                    }
                    return ButtonId.BUTTON_UPDATE_PLUS;
                }
            } else if (element instanceof ICpPack) {
                ICpPack pack = (ICpPack) element;
                PackState state = pack.getPackState();
                boolean requiredPacksInstalled = CpPlugIn.getPackManager().isRequiredPacksInstalled(pack);
                if (state.isInstalledOrLocal()) {
                    if (requiredPacksInstalled) {
                        if (state == PackState.LOCAL) {
                            return ButtonId.BUTTON_REPOSITORY;
                        }
                        return ButtonId.BUTTON_REMOVE;
                    }
                    return ButtonId.BUTTON_RESOLVE;
                } else if (state == PackState.DOWNLOADED) {
                    if (requiredPacksInstalled) {
                        return ButtonId.BUTTON_UNPACK;
                    }
                    return ButtonId.BUTTON_UNPACK_PLUS;
                } else if (state == PackState.ERROR) {
                    return ButtonId.BUTTON_DELETE;
                } else if (state == PackState.AVAILABLE) {
                    if (requiredPacksInstalled) {
                        return ButtonId.BUTTON_INSTALL;
                    }
                    return ButtonId.BUTTON_INSTALL_PLUS;
                }
            } else if (element instanceof ICpItem) {
                ICpItem item = ICpItem.cast(element);
                if (CmsisConstants.GENERIC.equals(item.getTag())) {
                    int count = item.getChildCount();
                    buttonId = getPackCountButtonId(count);
                } else if (CmsisConstants.DEVICE_SPECIFIC.equals(item.getTag())) {
                    int count = fViewController.getFilter().getFilteredDevicePackFamilies().size();
                    buttonId = getPackCountButtonId(count);
                } else if (!CmsisConstants.PREVIOUS.equals(item.getTag())) {
                    if (item.hasAttribute(CmsisConstants.DEPRECATED)) {
                        buttonId = ButtonId.BUTTON_DEPRECATED;
                    } else {
                        buttonId = ButtonId.BUTTON_INSTALL;
                    }
                }
            }

            return buttonId;
        }

    }

    class DescriptionColumnAdvisor extends ColumnAdvisor {

        public DescriptionColumnAdvisor(ColumnViewer columnViewer) {
            super(columnViewer);
        }

        @Override
        public String getString(Object obj, int columnIndex) {
            ICpItem item = ICpItem.cast(obj);
            if (item == null) {
                return null;
            }
            // Add selection string on the first line
            if (item instanceof ICpPackCollection) {
                if (CmsisConstants.DEVICE_SPECIFIC.equals(item.getTag())) {
                    String filterString = fViewController.getFilter().getFilterString();
                    return filterString == null ? null : filterString + Messages.PacksView_Selected;
                } else if (CmsisConstants.GENERIC.equals(item.getTag())) {
                    return Messages.PacksView_GenericPacksDescription;
                }
            }

            if (item instanceof ICpPackFamily) {
                ICpPackFamily packFamily = (ICpPackFamily) item;
                if (CmsisConstants.ERRORS.equals(packFamily.getTag())) {
                    return Messages.PacksView_CannotLoadPdscFiles;
                }
                return formatDescription(item.getDescription());
            }

            if (CmsisConstants.RELEASE_TAG.equals(item.getTag())) {
                return formatDescription(item.getText());
            } else if (CmsisConstants.PREVIOUS.equals(item.getTag())) {
                return item.getPackFamilyId() + Messages.PacksView_PreviousPackVersions;
            }

            ICpPack pack = item.getPack();
            if (pack.getPackState() != PackState.ERROR) {
                for (ICpItem cpItem : pack.getGrandChildren(CmsisConstants.RELEASES_TAG)) {
                    if (cpItem.getAttribute(CmsisConstants.VERSION).equals(pack.getVersion())) {
                        String desc = cpItem.getText();
                        desc = desc.replace("\n", ""); //$NON-NLS-1$ //$NON-NLS-2$
                        return formatDescription(desc);
                    }
                }
            } else {
                return Messages.PacksView_Location + pack.getFileName();
            }
            return null;
        }

        // Due to the multi-line table cell in Linux, we
        // only use the first line when OS is Linux
        private String formatDescription(String description) {
            boolean isWinOS = CmsisConstants.WIN.equals(Utils.getHostType());
            return isWinOS ? description : description.split("\\r?\\n")[0]; //$NON-NLS-1$
        }

        @Override
        public CellControlType getCellControlType(Object obj, int columnIndex) {
            if (obj instanceof ICpPack && columnIndex == COLDESC) {
                ICpPack pack = (ICpPack) obj;
                ICpItem repo = pack.getFirstChild(CmsisConstants.REPOSITORY);
                if (repo != null) {
                    return CellControlType.URL;
                }
            }
            return CellControlType.TEXT;
        }

        @Override
        public String getUrl(Object obj, int columnIndex) {
            return getUrlText(obj, columnIndex);
        }

        private String getUrlText(Object obj, int columnIndex) {
            if (obj instanceof ICpPack && getCellControlType(obj, columnIndex) == CellControlType.URL) {
                ICpPack pack = (ICpPack) obj;
                String repo = pack.getRepositoryUrl();
                if (!repo.isEmpty()) {
                    return repo;
                }
                return pack.getDownloadUrl();
            }
            return super.getUrl(obj, columnIndex);
        }

        @Override
        public String getTooltipText(Object obj, int columnIndex) {
            String tt = getUrlText(obj, columnIndex);
            if (tt == null) {
                tt = super.getTooltipText(obj, columnIndex);
            }
            return tt;
        }

    }

    class PackTreeColumnComparator extends PackInstallerTreeColumnComparator {

        private VersionComparator versionComparator;

        public PackTreeColumnComparator(TreeViewer viewer, IColumnAdvisor advisor) {
            super(viewer, advisor);
            versionComparator = new VersionComparator();
        }

        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            ICpItem cp1 = (ICpItem) e1;
            ICpItem cp2 = (ICpItem) e2;

            // Error packs should always be shown on top
            // Never switch the Device Specific and Generic row
            if (CmsisConstants.ERRORS.equals(cp1.getTag())) {
                return -1;
            } else if (CmsisConstants.ERRORS.equals(cp2.getTag())) {
                return 1;
            } else if (cp1 instanceof ICpPackCollection && cp2 instanceof ICpPackCollection) {
                return 0;
            }

            // For this view we only sort ICpPackFamily
            if (!(e1 instanceof ICpPackFamily || e1 instanceof ICpPack)
                    || !(e2 instanceof ICpPackFamily || e2 instanceof ICpPack)) {
                return 0;
            }

            // if this is not the 1st column, use default sorting
            if (getColumnIndex() != 0) {
                return super.compare(viewer, e1, e2);
            }

            // regular comparison
            int result = alnumComparator.compare(cp1.getPackFamilyId(), cp2.getPackFamilyId());
            if (result == 0) {
                if (cp1.hasAttribute(CmsisConstants.VERSION) && cp2.hasAttribute(CmsisConstants.VERSION)) {
                    result = versionComparator.compare(cp1.getAttribute(CmsisConstants.VERSION),
                            cp2.getAttribute(CmsisConstants.VERSION));
                } else {
                    result = 0;
                }
            }
            return bDescending ? -result : result;
        }
    }

    class ColumLabelProviderWithImage extends ColumnLabelProvider {
        @Override
        public String getText(Object obj) {
            ICpItem item = ICpItem.cast(obj);
            if (item != null && !ROOT.equals(item.getTag())) {
                String date = CpPack.getCpItemDate(item);
                String appendDate = CmsisConstants.EMPTY_STRING;
                if (!date.isEmpty()) {
                    appendDate = " (" + date + ')'; //$NON-NLS-1$
                }
                // added spaces at last of text as a workaround to show the complete text in the
                // views
                if (item.hasAttribute(CmsisConstants.VERSION)) {
                    return item.getAttribute(CmsisConstants.VERSION) + appendDate + ' ';
                } else if (item instanceof ICpPack) {
                    ICpPack pack = (ICpPack) item;
                    if (pack.getPackState() != PackState.ERROR) {
                        return item.getVersion() + appendDate + ' ';
                    }
                    String tag = item.getTag();
                    ICpItem parent = item.getParent();
                    if (parent != null && parent.getTag().equals(CmsisConstants.ERRORS)) {
                        tag = Utils.extractFileName(tag);
                    }
                    return tag + ' ';
                } else {
                    return item.getTag() + ' ';
                }
            }
            return null;
        }

        @Override
        public Image getImage(Object obj) {
            ICpItem item = ICpItem.cast(obj);
            if (item == null || ROOT.equals(item.getTag())) {
                return null;
            }
            if (obj instanceof ICpPack) {
                ICpPack pack = (ICpPack) obj;
                if (pack.getPackState().isInstalledOrLocal()) {
                    return CpPlugInUI.getImage(CpPlugInUI.ICON_PACKAGE);
                }
                return CpPlugInUI.getImage(CpPlugInUI.ICON_PACKAGE_GREY);
            } else if ((obj instanceof ICpPackFamily) && !(obj instanceof ICpPackCollection)) {
                ICpPackFamily packFamily = (ICpPackFamily) obj;
                if (CmsisConstants.ERRORS.equals(packFamily.getTag())) {
                    return CpPlugInUI.getImage(CpPlugInUI.ICON_WARNING);
                }

                for (ICpPack pack : packFamily.getPacks()) {
                    if (pack.getPackState().isInstalledOrLocal()) {
                        return CpPlugInUI.getImage(CpPlugInUI.ICON_PACKAGES);
                    }
                }
                return CpPlugInUI.getImage(CpPlugInUI.ICON_PACKAGES_GREY);
            } else if (CmsisConstants.RELEASE_TAG.equals(item.getTag())) {
                return CpPlugInUI.getImage(CpPlugInUI.ICON_PACKAGE_GREY);
            }

            return CpPlugInUI.getImage(CpPlugInUI.ICON_ITEM);
        }

        @Override
        public String getToolTipText(Object element) {
            ICpItem item = ICpItem.cast(element);
            if (item != null) {
                return getNameTooltip(item);
            }
            return null;
        }
    }

    /**
     * Get the tooltip for the Pack column
     *
     * @param item the tree item
     * @return the tooltip or null if no tooltip needed
     */
    String getNameTooltip(ICpItem item) {
        if (item == null) {
            return null;
        }
        if (item instanceof ICpPackFamily) {
            if (CmsisConstants.ERRORS.equals(item.getTag())) {
                StringBuilder sb = new StringBuilder(Messages.PacksView_DeleteAllTooltip);
                for (ICpItem child : item.getChildren()) {
                    ICpPack pack = (ICpPack) child;
                    sb.append("\n  " + pack.getFileName()); //$NON-NLS-1$
                }
                return sb.toString();
            }
            return getPackToolTip(item.getPack());
        } else if (item instanceof ICpPack) {
            return getPackToolTip((ICpPack) item);
        } else if (CmsisConstants.RELEASE_TAG.equals(item.getTag())) {
            return getReleaseToolTip(item, null);
        }

        return null;
    }

    protected String getPackToolTip(ICpPack pack) {

        if (pack.getPackState() == PackState.ERROR) {
            return Messages.PacksView_Delete_ + pack.getFileName();
        }
        return getReleaseToolTip(pack.getThisRelease(), pack);
    }

    protected String getReleaseToolTip(ICpItem release, ICpPack thisPack) {

        StringBuilder sb = new StringBuilder();
        // version
        sb.append(NLS.bind(Messages.PacksView_Version, fColumnLabelProviderWithImage.getText(release)));

        String url = release.hasAttribute(CmsisConstants.URL) ? release.getAttribute(CmsisConstants.URL)
                : release.getPack().getUrl();
        if (!url.isEmpty()) {
            sb.append(Messages.PacksView_Url).append(": ").append(url).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (thisPack != null) {
            String repo = thisPack.getRepositoryUrl();
            if (!repo.isEmpty()) {
                sb.append(Messages.PacksView_Repository).append(": ").append(repo).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        String tag = release.getAttribute(CmsisConstants.TAG);
        if (!tag.isEmpty()) {
            sb.append(Messages.PacksView_Tag).append(": ").append(tag).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (release.hasAttribute(CmsisConstants.DEPRECATED)) {
            sb.append(NLS.bind(Messages.PacksView_DeprecatedOn, release.getAttribute(CmsisConstants.DEPRECATED)));
        }
        if (release.hasAttribute(CmsisConstants.REPLACEMENT)) {
            sb.append(NLS.bind(Messages.PacksView_ReplacedBy, release.getAttribute(CmsisConstants.REPLACEMENT)));
        }

        if (thisPack != null) {
            sb.append(getRequiredPacksTooltip(thisPack.getRequiredPacks()));
        }
        // release notes
        sb.append(Messages.PacksView_ReleaseNotes).append(":\n      ").append(release.getText()); //$NON-NLS-1$
        return sb.toString();
    }

    /**
     * Get the tooltip for the Action column
     *
     * @param item the tree item
     * @return the tooltip or null if no tooltip needed
     */
    String getActionTooltip(ICpItem item) {
        if (!(item instanceof ICpPack)) {
            return null;
        }
        ICpPack pack = (ICpPack) item;
        if (!pack.getPackState().isInstalledOrLocal() || CpPlugIn.getPackManager().isRequiredPacksInstalled(pack)) {
            return null;
        }

        String reqPacksTooltip = getRequiredPacksTooltip(pack.getRequiredPacks());
        if (reqPacksTooltip.isEmpty())
            return null;

        return Messages.PacksView_ResolveRequiredPacks + reqPacksTooltip;
    }

    /**
     * Returns tooltip portion with required packs
     *
     * @param requiredPacks collection of required packs
     * @return tooltip string
     */
    private String getRequiredPacksTooltip(Collection<? extends ICpItem> requiredPacks) {
        StringBuilder sb = new StringBuilder();
        if (requiredPacks != null && !requiredPacks.isEmpty()) {
            sb.append(Messages.PacksView_RequiredPacks);
            for (ICpItem reqPack : requiredPacks) {
                sb.append(CmsisConstants.SPACES4);
                sb.append(reqPack.getVendor()).append(CmsisConstants.DOT).append(reqPack.getName());
                String versionRange = reqPack.getAttribute(CmsisConstants.VERSION);
                if (!versionRange.isEmpty()) {
                    sb.append(".[").append(versionRange).append(']'); //$NON-NLS-1$
                }
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    /**
     * The constructor.
     */
    public PacksView() {
        fColumnLabelProviderWithImage = new ColumLabelProviderWithImage();
    }

    @Override
    protected boolean hasManagerCommands() {
        return true;
    }

    @Override
    public void createTreeColumns() {
        fTree.setInitialText(Messages.PacksView_SearchPack);

        // ------ Start Setting ALL Columns for the Packs View
        // ------ First Column
        TreeViewerColumn column0 = new TreeViewerColumn(fViewer, SWT.LEFT);
        column0.getColumn().setText(CmsisConstants.PACK_TITLE);
        column0.getColumn().setWidth(250);
        column0.setLabelProvider(fColumnLabelProviderWithImage);

        // ------ Second Column
        TreeViewerColumn column1 = new TreeViewerColumn(fViewer, SWT.LEFT);
        column1.getColumn().setText(CmsisConstants.ACTION_TITLE);
        column1.getColumn().setWidth(90);
        PacksViewColumnAdvisor columnAdvisor = new PacksViewColumnAdvisor(fViewer);
        column1.setLabelProvider(new AdvisedCellLabelProvider(columnAdvisor, COLBUTTON));

        // ------ Third Column
        TreeViewerColumn column2 = new TreeViewerColumn(fViewer, SWT.LEFT);
        column2.getColumn().setText(CmsisConstants.DESCRIPTION_TITLE);
        column2.getColumn().setWidth(400);
        DescriptionColumnAdvisor descColAdvisor = new DescriptionColumnAdvisor(fViewer);
        column2.setLabelProvider(new AdvisedCellLabelProvider(descColAdvisor, COLDESC));
        // ------ End Setting ALL Columns for the Packs View

        fViewer.setContentProvider(new PacksViewContentProvider());
        fViewer.setComparator(new PackTreeColumnComparator(fViewer, columnAdvisor));
        fViewer.setAutoExpandLevel(2);
    }

    @Override
    protected void refresh() {
        if (CpPlugIn.getDefault() == null) {
            return;
        }
        ICpPackManager packManager = CpPlugIn.getPackManager();
        if (packManager != null) {
            ICpItem root = new CpItem(null, ROOT);
            ICpPackFamily errorPacks = packManager.getErrorPacks();
            if (errorPacks != null && errorPacks.getChildCount() > 0) {
                root.addChild(errorPacks);
            }
            root.addChild(packManager.getDevicePacks());
            root.addChild(packManager.getGenericPacks());
            fViewer.setInput(root);
        } else {
            fViewer.setInput(null);
        }
    }

    @Override
    protected void handleFilterChanged() {
        super.handleFilterChanged();
        ICpItem root = (ICpItem) fViewer.getInput();
        if (root != null && fViewer.isExpandable(root.getFirstChild())) {
            fViewer.expandToLevel(root.getFirstChild(), 1);
        }
    }

    @Override
    protected void makeActions() {

        fManLocalRepo = new Action() {
            @Override
            public void run() {
                Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(shell,
                        "com.arm.cmsis.pack.ui.CpManLocalRepoPage", null, null); //$NON-NLS-1$
                dialog.open();
                super.run();
            }

        };

        fManLocalRepo.setText(Messages.PacksView_ActionManageLocalRepo + "..."); //$NON-NLS-1$
        fManLocalRepo.setToolTipText(Messages.PacksView_ActionManageLocalRepo + "..."); //$NON-NLS-1$
        fManLocalRepo.setImageDescriptor(
                ImageDescriptor.createFromImage(CpPlugInUI.getImage(CpPlugInUI.ICON_RTE_INSTALLED_LOCAL_REPO)));

        fRemovePack = new Action() {
            @Override
            public void run() {
                ICpPackInstaller packInstaller = getPackInstaller();
                if (packInstaller != null) {
                    packInstaller.removePack(getPackItem(), false);
                }
            }
        };
        fRemovePack.setText(Messages.PacksView_Remove);
        fRemovePack.setToolTipText(Messages.PacksView_RemoveSelectedPack);
        fRemovePack.setImageDescriptor(
                PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));

        fDeletePack = new Action() {
            @Override
            public void run() {
                ICpPackInstaller packInstaller = getPackInstaller();
                if (packInstaller != null) {
                    packInstaller.removePack(getPackItem(), true);
                }
            }
        };
        fDeletePack.setToolTipText(Messages.PacksView_DeleteSelectedPack);
        fDeletePack.setImageDescriptor(
                PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));

        fInstallSinglePack = new Action() {
            @Override
            public void run() {
                ICpPackInstaller packInstaller = getPackInstaller();
                ICpPack pack = getPackItem();
                if (packInstaller != null && pack != null) {
                    packInstaller.installPack(pack.getId(), false);
                }
            }
        };
        fInstallSinglePack.setToolTipText(Messages.PacksView_InstallSinglePackTooltip);

        fInstallRequiredPacks = new Action() {
            @Override
            public void run() {
                ICpPackInstaller packInstaller = getPackInstaller();
                if (packInstaller != null) {
                    packInstaller.installRequiredPacks(getPackItem());
                }
            }
        };
        fInstallRequiredPacks.setText(Messages.PackInstallerView_InstallRequiredPacks);
        fInstallRequiredPacks.setToolTipText(Messages.PackInstallerView_InstallRequiredPacksToolTip);
        fInstallRequiredPacks.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_RTE_WARNING));

        super.makeActions();

    }

    @Override
    protected void fillContextMenu(IMenuManager manager) {
        super.fillContextMenu(manager);
        ICpPackInstaller packInstaller = getPackInstaller();
        if (packInstaller == null) {
            return;
        }
        manager.add(new Separator());

        ICpPack pack = getPackItem();
        if (pack != null) {
            switch (pack.getPackState()) {
            case LOCAL:
                manager.add(fManLocalRepo);
                break;
            case INSTALLED:
                manager.add(fRemovePack);
                manager.add(fDeletePack);
                fDeletePack.setText(Messages.PacksView_RemovePlusDelete);
                if (!CpPlugIn.getPackManager().isRequiredPacksInstalled(pack)) {
                    manager.add(new Separator());
                    manager.add(fInstallRequiredPacks);
                }
                break;
            case DOWNLOADED:
                manager.add(fDeletePack);
                fDeletePack.setText(Messages.PacksView_Delete);
                if (!CpPlugIn.getPackManager().isRequiredPacksInstalled(pack)) {
                    manager.add(new Separator());
                    manager.add(fInstallSinglePack);
                    fInstallSinglePack.setText(Messages.PacksView_UnpackSinglePack);
                    fInstallSinglePack.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_RTE_UNPACK));
                }
                break;
            case AVAILABLE:
                if (!CpPlugIn.getPackManager().isRequiredPacksInstalled(pack)) {
                    manager.add(new Separator());
                    manager.add(fInstallSinglePack);
                    fInstallSinglePack.setText(Messages.PacksView_InstallSinglePack);
                    fInstallSinglePack.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_RTE_INSTALL));
                }
                if (isLocalPack(pack)) {
                    manager.add(new Separator());
                    manager.add(fDeletePack);
                    fDeletePack.setText(Messages.PacksView_Delete);
                }
                break;
            default:
                break;
            }

            if (packInstaller.isProcessing(pack.getId())) {
                fManLocalRepo.setEnabled(false);
                fRemovePack.setEnabled(false);
                fDeletePack.setEnabled(false);
                fInstallSinglePack.setEnabled(false);
                fInstallRequiredPacks.setEnabled(false);
            }
        }
    }

    /**
     * Returns selected item if it represents an ICpPack
     *
     * @return ICpPack
     */
    ICpPack getPackItem() {
        ICmsisItem item = getSelectedItem();
        if (item instanceof ICpPack) {
            return (ICpPack) item;
        }
        return null;
    }

    @Override
    protected String getHelpContextId() {
        return IHelpContextIds.PACKS_VIEW;
    }

    protected boolean isLocalPack(ICpPack pack) {
        String localPdscFileName = CpPlugIn.getPackManager().getCmsisPackLocalDir() + '/' + pack.getPackFamilyId()
                + CmsisConstants.EXT_PDSC;
        File localPdscFile = new File(localPdscFileName);
        return localPdscFile.exists();
    }

    @Override
    protected void enableActions(boolean en) {
        if (fRemovePack != null)
            fRemovePack.setEnabled(en);
        if (fDeletePack != null)
            fDeletePack.setEnabled(en);
        if (fInstallSinglePack != null)
            fInstallSinglePack.setEnabled(en);
        if (fInstallRequiredPacks != null)
            fInstallRequiredPacks.setEnabled(en);
        if (fManLocalRepo != null)
            fManLocalRepo.setEnabled(en);
        super.enableActions(en);
    }
}
