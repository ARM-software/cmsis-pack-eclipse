/*******************************************************************************
 * Copyright (c) 2016 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/

package com.arm.cmsis.pack.installer.ui.views;

import java.util.Collection;
import java.util.LinkedList;
import java.util.regex.Pattern;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Tree;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpEnvironmentProvider;
import com.arm.cmsis.pack.ICpPackInstaller;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpComponent;
import com.arm.cmsis.pack.data.CpItem;
import com.arm.cmsis.pack.data.ICpBoard;
import com.arm.cmsis.pack.data.ICpComponent;
import com.arm.cmsis.pack.data.ICpDeviceItem;
import com.arm.cmsis.pack.data.ICpExample;
import com.arm.cmsis.pack.data.ICpFile;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPack.PackState;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.generic.ITreeObject;
import com.arm.cmsis.pack.installer.ui.CpInstallerPlugInUI;
import com.arm.cmsis.pack.installer.ui.IHelpContextIds;
import com.arm.cmsis.pack.installer.ui.Messages;
import com.arm.cmsis.pack.installer.ui.PackInstallerViewController;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.tree.TreeObjectContentProvider;
import com.arm.cmsis.pack.utils.AlnumComparator;
import com.arm.cmsis.pack.utils.VersionComparator;

/**
 * Default implementation of the pack properties view in pack manager
 */
public class PackPropertyView extends PackInstallerView {

	public static final String ID = "com.arm.cmsis.pack.installer.ui.views.PackPropertyView"; //$NON-NLS-1$

	private Action fInstallPackAction; // install single pack
	private Action fInstallRequiredPacksAction; // install required packs
	private Action fCopyAction;


	/**
	 *
	 */
	class PackPropertyViewContentProvider extends TreeObjectContentProvider {
		private ICpItem root;

		@Override
		public Object[] getChildren(Object parentElement) {
			ICpItem item = getCpItem(parentElement);
			if (item == null) {
				return ITreeObject.EMPTY_OBJECT_ARRAY;
			}
			// root node
			if (item instanceof ICpPack) {
				return getPackChildren((ICpPack) item);
			}
			// device node
			else if (item instanceof ICpDeviceItem) {
				return getDeviceItems((ICpDeviceItem) item);
			}
			// board node
			else if (item instanceof ICpBoard) {
				return getBoardChildren((ICpBoard) item);
			}
			// example node
			else if (item instanceof ICpExample) {
				return getExampleChildren((ICpExample) item);
			}
			// examples node
			else if (CmsisConstants.EXAMPLES_TAG.equals(item.getTag())) {
				return getExamples(item);
			}
			// components node
			else if (CmsisConstants.COMPONENTS_TAG.equals(item.getTag())) {
				root = buildComponentTree(item);
				return root.getChildArray();
			}
			// required packages node
			else if (CmsisConstants.PACKAGES_TAG.equals(item.getTag())) {
				return getRequiredPacks(item);
			}

			return super.getChildren(parentElement);
		}

		@Override
		public boolean hasChildren(Object element) {
			return getChildren(element) != null && getChildren(element).length > 0;
		}

		private Object[] getPackChildren(ICpPack pack) {
			if (pack == null || pack.getPackState() == PackState.ERROR
					|| pack.getChildren() == null) {
				return ITreeObject.EMPTY_OBJECT_ARRAY;
			}

			Collection<ICpItem> result = new LinkedList<>();
			for (ICpItem child : pack.getChildren()) {
				if (CmsisConstants.DEVICES_TAG.equals(child.getTag())
						|| CmsisConstants.BOARDS_TAG.equals(child.getTag())
						|| CmsisConstants.COMPONENTS_TAG.equals(child.getTag())) {
					result.add(child);
				}
				// requirements node, here we only need the required packages
				else if (CmsisConstants.REQUIREMENTS_TAG.equals(child.getTag())) {
					result.add(child.getFirstChild(CmsisConstants.PACKAGES_TAG));
				}
				// examples node
				else if (CmsisConstants.EXAMPLES_TAG.equals(child.getTag())) {
					Object[] examples = getExamples(child);
					if (examples != null && examples.length > 0) {
						result.add(child);
					}
				}
			}
			return result.toArray();
		}

		private Object[] getRequiredPacks(ICpItem packs) {
			if (packs == null || packs.getChildren() == null) {
				return ITreeObject.EMPTY_OBJECT_ARRAY;
			}
			return packs.getChildArray();
		}

		private Object[] getDeviceItems(ICpDeviceItem device) {
			if (device.getDeviceItems() != null) {
				return device.getDeviceItems().toArray();
			}
			return ITreeObject.EMPTY_OBJECT_ARRAY;
		}

		private Object[] getBoardChildren(ICpBoard board) {
			// Currently we show nothing under the board item
			return ITreeObject.EMPTY_OBJECT_ARRAY;
		}

		private Object[] getExamples(ICpItem examples) {
			if (examples == null || examples.getChildren() == null) {
				return ITreeObject.EMPTY_OBJECT_ARRAY;
			}

			Collection<ICpExample> result = new LinkedList<>();
			ICpEnvironmentProvider envProvider = CpPlugIn.getEnvironmentProvider();
			for (ICpItem item : examples.getChildren()) {
				if (!(item instanceof ICpExample)) {
					continue;
				}
				ICpExample example = (ICpExample) item;
				if(envProvider == null || !envProvider.isSupported(example)) {
					continue;
				}
				result.add(example);
			}
			return result.toArray();
		}

		private Object[] getExampleChildren(ICpExample example) {
			if (example == null || example.getChildren() == null) {
				return ITreeObject.EMPTY_OBJECT_ARRAY;
			}

			Collection<ICpItem> result = new LinkedList<>();
			for (ICpItem item : example.getChildren()) {
				// Only show the board item under the example item
				if (item instanceof ICpBoard) {
					result.add(item);
					break;
				}
			}
			return result.toArray();
		}

		/**
		 * Build the Compoenent Tree of this pack
		 * @param parent the default root of this tree
		 * @return the root of this component tree
		 */
		protected ICpItem buildComponentTree(ICpItem parent) {
			ICpItem newRoot = new CpItem(null, parent.getTag());
			if (parent.getChildren() == null) {
				return newRoot;
			}
			for (ICpItem child : parent.getChildren()) {
				if (CmsisConstants.BUNDLE_TAG.equals(child.getTag())) {
					for (ICpItem gchild : child.getChildren()) {
						if (!CmsisConstants.COMPONENT_TAG.equals(gchild.getTag())) {
							continue;
						}
						String id = gchild.getId();
						addChild(id, newRoot);
					}
				} else if (CmsisConstants.COMPONENT_TAG.equals(child.getTag())) {
					String id = child.getId();
					addChild(id, newRoot);
				}

			}
			return newRoot;
		}

		/**
		 * Add leaf to the parent node
		 * @param id ID of the component to be added as a child of parent, used to check if the node with this ID already exists
		 * @param parent parent node to which child node should be added
		 */
		protected void addChild(String id, ICpItem parent) {
			String path = formalizeId(id);
			ICpItem node = findNode(path, parent);
			if (node == null) {
				int lastDot = path.lastIndexOf('.');
				String parentPath = path.substring(0, lastDot);
				node = buildPath(parentPath, parent);
				node.addChild(new CpComponent(node, path.substring(lastDot + 1)));
			}
		}

		/**
		 * Turn the ID to the format: a.b.c to find the path to the child
		 * @param id ID of the component
		 * @return formalized ID
		 */
		private String formalizeId(String id) {
			int begin = id.lastIndexOf(CmsisConstants.DOUBLE_COLON) + 2;
			int end = id.lastIndexOf('(');
			if (end == -1) {
				end = id.lastIndexOf(':');
			}
			return id.substring(begin, end);
		}

		/**
		 * Find if the node already exists under the parent node
		 * @param path formed like a.b.c, each segment represents a child node
		 * @param parent the parent node
		 * @return the child node if it has the formed ID like a.b.c, null if it does not exist
		 */
		private ICpItem findNode(String path, ICpItem parent) {
			String[] segments = path.split(Pattern.quote(".")); //$NON-NLS-1$
			ICpItem p = parent;
			for (String segment : segments) {
				if (p.getFirstChild(segment) != null) {
					p = p.getFirstChild(segment);
				} else {
					return null;
				}
			}
			return p;
		}

		/**
		 * Build the child nodes along the path like a.b.c, each segment represents a child node
		 * @param path formed like a.b.c
		 * @param parent the parent node
		 * @return the leaf node on the path under parent node
		 */
		private ICpItem buildPath(String path, ICpItem parent) {
			String[] segments = path.split(Pattern.quote(".")); //$NON-NLS-1$
			ICpItem p = parent;
			for (String segment : segments) {
				if (p.getFirstChild(segment) == null || !p.getFirstChild(segment).hasChildren()) {
					p.addChild(new CpComponent(p, segment));
				}
				p = p.getFirstChild(segment);
			}
			return p;
		}
	}

	class PackPropertyViewLabelProvider extends ColumnLabelProvider {
		@Override
		public Image getImage(Object obj) {
			ICpItem item = getCpItem(obj);
			if (item == null) {
				return null;
			}
			ICpPack pack = item.getPack();
			boolean installed = pack != null ? pack.getPackState() == PackState.INSTALLED : false;
			// root node
			if (pack != null && item == pack) {
				if (installed) {
					if (pack.isRequiredPacksInstalled()) {
						return CpPlugInUI.getImage(CpPlugInUI.ICON_PACKAGE);
					}
					return CpPlugInUI.getImage(CpPlugInUI.ICON_PACKAGE_RED);
				}
				return CpPlugInUI.getImage(CpPlugInUI.ICON_PACKAGE_GREY);
			}
			// Component node
			else if (item instanceof ICpComponent) {
				ICpComponent c = (ICpComponent) item;
				if(c.getMaxInstances() > 1) {
					return CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT);
				}
				return CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT);
			}
			// File node
			else if (item instanceof ICpFile) {
				return CpPlugInUI.getImage(CpPlugInUI.ICON_FILE);
			}
			// Board node
			else if (item instanceof ICpBoard) {
				if (item.isDeprecated()) {
					return CpPlugInUI.getImage(CpPlugInUI.ICON_BOARD_DEPR);
				}
				if (installed) {
					return CpPlugInUI.getImage(CpPlugInUI.ICON_BOARD);
				}
				return CpPlugInUI.getImage(CpPlugInUI.ICON_BOARD_GREY);
			}
			// Device node
			else if (item instanceof ICpDeviceItem) {
				ICpDeviceItem di = (ICpDeviceItem) item;
				if (di.getDeviceItems() == null) {
					if (di.isDeprecated()) {
						return CpPlugInUI.getImage(CpPlugInUI.ICON_DEVICE_DEPR);
					}
					if (installed) {
						return CpPlugInUI.getImage(CpPlugInUI.ICON_DEVICE);
					}
					return CpPlugInUI.getImage(CpPlugInUI.ICON_DEVICE_GREY);
				}
				return CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT_CLASS);
			}
			switch (item.getTag()) {
			case CmsisConstants.COMPONENTS_TAG:
				return CpPlugInUI.getImage(CpPlugInUI.ICON_RTE);
			case CmsisConstants.DEVICES_TAG:
				return CpPlugInUI.getImage(CpPlugInUI.ICON_DEVICE);
			case CmsisConstants.BOARDS_TAG:
				return CpPlugInUI.getImage(CpPlugInUI.ICON_BOARD);
			case CmsisConstants.BUNDLE_TAG:
				return CpPlugInUI.getImage(CpPlugInUI.ICON_RTE);
			case CmsisConstants.COMPATIBLE_DEVICE_TAG:
			case CmsisConstants.MOUNTED_DEVICE_TAG:
				return CpPlugInUI.getImage(CpPlugInUI.ICON_DEVICE);
			case CmsisConstants.EXAMPLES_TAG:
				return CpPlugInUI.getImage(CpPlugInUI.ICON_EXAMPLE);
			case CmsisConstants.PACKAGES_TAG:
				if (pack != null && pack.getPackState() == PackState.INSTALLED
				&& !pack.isRequiredPacksInstalled()) {
					return CpPlugInUI.getImage(CpPlugInUI.ICON_PACKAGES_RED);
				}
				return CpPlugInUI.getImage(CpPlugInUI.ICON_PACKAGES);
			case CmsisConstants.PACKAGE_TAG:
				String familyId = item.getAttribute(CmsisConstants.VENDOR) + '.'
				+ item.getAttribute(CmsisConstants.NAME);
				if (isRequiredPackInstalled(familyId,
						item.getAttribute(CmsisConstants.VERSION))) {
					return CpPlugInUI.getImage(CpPlugInUI.ICON_PACKAGE);
				}
				return CpPlugInUI.getImage(CpPlugInUI.ICON_PACKAGE_RED);
			default:
				break;
			}

			return null;
		}

		@Override
		public String getText(Object element) {
			ICpItem item = getCpItem(element);
			if (item == null) {
				return CmsisConstants.EMPTY_STRING;
			}
			if (item instanceof ICpComponent) {
				return capitalizeInitChar(item.getTag());
			}
			// required package node
			else if (!(item instanceof ICpPack) && CmsisConstants.PACKAGE_TAG.equals(item.getTag())) {
				return item.getAttribute(CmsisConstants.VENDOR) + '.'
						+ item.getAttribute(CmsisConstants.NAME) + '.'
						+ '[' + item.getAttribute(CmsisConstants.VERSION) + ']';
			}
			return capitalizeInitChar(item.getId());
		}

		private String capitalizeInitChar(String string) {
			if (string.isEmpty()) {
				return string;
			}
			char c = Character.toUpperCase(string.charAt(0));
			return String.valueOf(c) + string.substring(1);
		}

	}

	class NameSorter extends ViewerSorter {

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			if (!(e1 instanceof ICpItem) || !(e2 instanceof ICpItem)) {
				return 0;
			}
			ICpItem i1 = (ICpItem) e1;
			ICpItem i2 = (ICpItem) e2;
			return new AlnumComparator(false).compare(i1.getId(), i2.getId());
		}
	}

	public PackPropertyView() {
		fViewController = CpInstallerPlugInUI.getViewController();
	}

	@Override
	public boolean isFilterClient() {
		return false;
	}

	@Override
	protected String getHelpContextId() {
		return IHelpContextIds.PACK_PROPERTIES_VIEW;
	}


	@Override
	public void createTreeColumns() {

		fViewer.setContentProvider(new PackPropertyViewContentProvider());
		fViewer.setLabelProvider(new PackPropertyViewLabelProvider());
		fViewer.setSorter(new NameSorter());
		Tree tree = fViewer.getTree();
		tree.setHeaderVisible(false);
		tree.setLinesVisible(false);

	}


	@Override
	protected void refresh() {
		ICpPack pack = fViewController.getSelectedPack();
		if (pack != null) {
			ICpItem proot = new CpItem(null);
			proot.addChild(pack);
			fViewer.setAutoExpandLevel(2);
			fViewer.setInput(proot);
		} else {
			fViewer.setInput(null);
		}
	}

	ICpExample getExampleItem() {
		IStructuredSelection selection = (IStructuredSelection) fViewer.getSelection();
		if (selection == null || selection.isEmpty()) {
			return null;
		}
		Object obj = selection.getFirstElement();
		if (obj instanceof ICpExample) {
			return (ICpExample) obj;
		}
		return null;
	}

	/**
	 * Check if the required pack is installed
	 * @param familyId family ID of the required pack
	 * @param versionRange version range of the required pack
	 * @return true if the required pack with the version range is installed
	 */
	boolean isRequiredPackInstalled(String familyId, String versionRange) {
		Collection<ICpPack> installedPacks = CpPlugIn.getPackManager().getInstalledPacks().getPacksByPackFamilyId(familyId);
		if (installedPacks == null) {
			return false;
		}
		for (ICpPack installedPack : installedPacks) {
			if (VersionComparator.matchVersionRange(installedPack.getVersion(), versionRange)) {
				return true;
			}
		}
		return false;
	}


	@Override
	protected void makeActions() {

		super.makeActions();

		fInstallPackAction = new Action() {
			@Override
			public void run() {
				ICpPack pack = fViewController.getSelectedPack();
				if (pack == null) {
					return;
				}
				ICpPackInstaller packInstaller = CpPlugIn.getPackManager().getPackInstaller();
				if(packInstaller != null) {
					packInstaller.installPack(pack.getPackId());
				}
			}
		};

		fInstallRequiredPacksAction = new Action() {
			@Override
			public void run() {
				ICpPack pack = fViewController.getSelectedPack();
				if (pack == null) {
					return;
				}
				ICpPackInstaller packInstaller = CpPlugIn.getPackManager().getPackInstaller();
				if(packInstaller != null) {
					packInstaller.installRequiredPacks(pack);
				}
			}
		};

		fCopyAction = new Action() {
			@Override
			public void run() {
				ICpExample cpExample = getExampleItem();
				if(cpExample != null) {
					CpInstallerPlugInUI.getViewController().copyExample(cpExample);
				}
			}
		};
		fCopyAction.setText(Messages.PackPropertyView_CopyAction);
		fCopyAction.setToolTipText(Messages.PackPropertyView_CopyTooltip);
		fCopyAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_RTE));
	}


	@Override
	protected void fillContextMenu(IMenuManager manager) {
		super.fillContextMenu(manager);

		manager.add(new Separator());
		ICpPackInstaller packInstaller = CpPlugIn.getPackManager().getPackInstaller();
		if(packInstaller == null) {
			return;
		}

		ICpExample example = getExampleItem();
		ICpPack pack = null;
		PackState packState = PackState.UNKNOWN;
		if (example != null) {
			pack = example.getPack();
			packState = pack.getPackState();
			if (packState == PackState.INSTALLED) {
				manager.add(fCopyAction);
				fCopyAction.setEnabled(true);
				return;
			}
		} else {
			pack = fViewController.getSelectedPack();
		}
		if (pack == null) {
			return;
		}
		packState = pack.getPackState();

		if (packState == PackState.DOWNLOADED) {
			fInstallPackAction.setText(Messages.PackPropertyView_UnpackAction);
			fInstallPackAction.setToolTipText(Messages.PackPropertyView_UnpackTooltip);
			fInstallPackAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_RTE_UNPACK));
			manager.add(fInstallPackAction);
		} else if (packState == PackState.AVAILABLE) {
			fInstallPackAction.setText(Messages.PackPropertyView_InstallAction);
			fInstallPackAction.setToolTipText(Messages.PackPropertyView_InstallTooltip);
			fInstallPackAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_RTE_INSTALL));
			manager.add(fInstallPackAction);
		} else if (!pack.isRequiredPacksInstalled()) {
			fInstallRequiredPacksAction.setText(Messages.PackInstallerView_InstallRequiredPacks);
			fInstallRequiredPacksAction.setToolTipText(Messages.PackInstallerView_InstallRequiredPacksToolTip);
			fInstallRequiredPacksAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_RTE_INSTALL));
			manager.add(fInstallRequiredPacksAction);
		}
	}



	@Override
	public void handleRteEvent(RteEvent event) {
		String topic = event.getTopic();
		switch(topic) {
		case RteEvent.PACKS_RELOADED:
		case PackInstallerViewController.INSTALLER_UI_PACK_CHANGED:
			refresh();
		default:
			super.handleRteEvent(event);
		}
	}
}
