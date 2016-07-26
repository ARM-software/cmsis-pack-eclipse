package com.arm.cmsis.pack.installer.ui.views;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.regex.Pattern;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.wizards.datatransfer.ExternalProjectImportWizard;

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
import com.arm.cmsis.pack.data.ICpPackFamily;
import com.arm.cmsis.pack.events.IRteEventListener;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.generic.ITreeObject;
import com.arm.cmsis.pack.installer.ui.CpInstallerPlugInUI;
import com.arm.cmsis.pack.installer.ui.IHelpContextIds;
import com.arm.cmsis.pack.installer.ui.Messages;
import com.arm.cmsis.pack.installer.utils.PackInstallerUtils;
import com.arm.cmsis.pack.rte.examples.IRteExampleItem;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.tree.TreeObjectContentProvider;
import com.arm.cmsis.pack.utils.AlnumComparator;
import com.arm.cmsis.pack.utils.Utils;

public class PackPropertyView extends ViewPart implements IRteEventListener {

	public static final String ID = "com.arm.cmsis.pack.installer.ui.views.PackPropertyView"; //$NON-NLS-1$

	// copy from org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage
	private final static String STORE_COPY_PROJECT_ID = "WizardProjectsImportPage.STORE_COPY_PROJECT_ID"; //$NON-NLS-1$

	TreeViewer fViewer;
	private Action fExpandAction;
	private Action fExpandItemAction;
	private Action fCollapseAction;
	private Action fCollapseItemAction;
	private Action fHelpAction;
	Action fDoubleClickAction;
	private Action fInstallAction;
	private Action fCopyAction;

	private ISelectionListener fViewSelectionListener;

	ICpItem getCpItem(Object obj) {
		if (obj instanceof ICpItem) {
			return (ICpItem) obj;
		}
		return null;
	}

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
			if (item instanceof ICpPack) {
				return getPackChildren((ICpPack) item);
			} else if (item instanceof ICpDeviceItem) {
				return getDeviceItems((ICpDeviceItem) item);
			} else if (item instanceof ICpBoard) {
				return getBoardChildren((ICpBoard) item);
			} else if (CmsisConstants.EXAMPLES_TAG.equals(item.getTag())) {
				return getExamples(item);
			} else if (item instanceof ICpExample) {
				return getExampleChildren((ICpExample) item);
			} else if (CmsisConstants.COMPONENTS_TAG.equals(item.getTag())) {
				root = buildComponentTree(item);
				return root.getChildArray();
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
				} else if (CmsisConstants.EXAMPLES_TAG.equals(child.getTag())) {
					Object[] examples = getExamples(child);
					if (examples != null && examples.length > 0) {
						result.add(child);
					}
				}
			}
			return result.toArray();
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

	class OutlineViewLabelProvider extends ColumnLabelProvider {
		@Override
		public Image getImage(Object obj) {
			ICpItem item = getCpItem(obj);
			if (item == null) {
				return null;
			}
			boolean installed = item.getPack() == null ? false : item.getPack().getPackState() == PackState.INSTALLED;
			if (obj instanceof ICpPack) {
				if (installed) {
					return CpPlugInUI.getImage(CpPlugInUI.ICON_PACKAGE);
				}
				return CpPlugInUI.getImage(CpPlugInUI.ICON_PACKAGE_GREY);
			} else if (obj instanceof ICpPackFamily) {
				if (installed) {
					return CpPlugInUI.getImage(CpPlugInUI.ICON_PACKAGES);
				}
				return CpPlugInUI.getImage(CpPlugInUI.ICON_PACKAGES_GREY);
			} else if (obj instanceof ICpComponent) {
				ICpComponent c = (ICpComponent) obj;
				if(c.getMaxInstances() > 1) {
					return CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT);
				}
				return CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT);
			} else if (obj instanceof ICpFile) {
				return CpPlugInUI.getImage(CpPlugInUI.ICON_FILE);
			} else if (obj instanceof ICpBoard) {
				if (installed) {
					return CpPlugInUI.getImage(CpPlugInUI.ICON_BOARD);
				}
				return CpPlugInUI.getImage(CpPlugInUI.ICON_BOARD_GREY);
			} else if (obj instanceof ICpDeviceItem) {
				ICpDeviceItem di = (ICpDeviceItem) obj;
				if (di.getDeviceItems() == null) {
					if (installed) {
						return CpPlugInUI.getImage(CpPlugInUI.ICON_DEVICE);
					}
					return CpPlugInUI.getImage(CpPlugInUI.ICON_DEPRDEVICE);
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
			default:
				break;
			}

			return null;//  CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT);
		}

		@Override
		public String getText(Object element) {
			ICpItem item = getCpItem(element);
			if (item == null) {
				return CmsisConstants.EMPTY_STRING;
			}
			if (item instanceof ICpComponent) {
				return format(item.getTag());
			} else if (CmsisConstants.RELEASE_TAG.equals(item.getTag()) && item.hasAttribute(CmsisConstants.VERSION)) {
				return format(item.getAttribute(CmsisConstants.VERSION));
			}
			return format(item.getId());
		}

		private String format(String string) {
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
	}

	@Override
	public void createPartControl(Composite parent) {

		fViewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		fViewer.setContentProvider(new PackPropertyViewContentProvider());
		fViewer.setLabelProvider(new OutlineViewLabelProvider());
		fViewer.setSorter(new NameSorter());

		PlatformUI.getWorkbench().getHelpSystem().setHelp(fViewer.getControl(), IHelpContextIds.PACK_PROPERTIES_VIEW);

		hookViewSelection();
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();

		CpInstallerPlugInUI.registerViewPart(this);

		CpPlugIn.addRteListener(this);
	}

	private void hookViewSelection() {

		fViewSelectionListener = new ISelectionListener() {

			@Override
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {

				if (part instanceof PacksView && !fViewer.getControl().isDisposed()) {
					fireSelectionChanged(part, selection);
				}
				if (part instanceof ExamplesView && !fViewer.getControl().isDisposed()) {
					fireSelectionChanged(part, selection);
				}
			}
		};
		getSite().getPage().addSelectionListener(fViewSelectionListener);
	}

	protected void fireSelectionChanged(IWorkbenchPart part, ISelection selection) {
		ICpPack pack = getPackItem(selection);
		if (pack != null) {
			ICpItem proot = new CpItem(null);
			proot.addChild(pack);
			fViewer.setAutoExpandLevel(2);
			fViewer.setInput(proot);
		}
	}

	private ICpPack getPackItem(ISelection sel) {
		IStructuredSelection selection = (IStructuredSelection) sel;
		if (selection == null || selection.isEmpty()) {
			return null;
		}
		Object obj = selection.getFirstElement();
		if (obj instanceof ICpPack) {
			return (ICpPack) obj;
		} else if (obj instanceof ICpPackFamily) {
			return ((ICpPackFamily) obj).getPack();
		} else if (obj instanceof IRteExampleItem) {
			IRteExampleItem item = (IRteExampleItem) obj;
			return item.getExample().getPack();
		}
		return null;
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

	private void makeActions() {
		fExpandAction = new Action() {
			@Override
			public void run() {
				if (fViewer == null) {
					return;
				}
				fViewer.expandAll();
			}
		};
		fExpandAction.setText(Messages.ExpandAll);
		fExpandAction.setToolTipText(Messages.ExpandAllNodes);
		fExpandAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_EXPAND_ALL));

		fExpandItemAction = new Action() {
			@Override
			public void run() {
				if (fViewer == null) {
					return;
				}
				ISelection selection = fViewer.getSelection();
				if (selection == null) {
					return;
				}
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				fViewer.expandToLevel(obj, AbstractTreeViewer.ALL_LEVELS);
			}
		};
		fExpandItemAction.setText(Messages.ExpandSelected);
		fExpandItemAction.setToolTipText(Messages.ExpandSelectedNode);
		fExpandItemAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_EXPAND_ALL));

		fCollapseAction = new Action() {
			@Override
			public void run() {
				if (fViewer == null) {
					return;
				}
				fViewer.collapseAll();
			}
		};
		fCollapseAction.setText(Messages.CollapseAll);
		fCollapseAction.setToolTipText(Messages.CollapseAllNodes);
		fCollapseAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_COLLAPSE_ALL));

		fCollapseItemAction = new Action() {
			@Override
			public void run() {
				if (fViewer == null) {
					return;
				}
				ISelection selection = fViewer.getSelection();
				if (selection == null) {
					return;
				}
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				fViewer.collapseToLevel(obj, AbstractTreeViewer.ALL_LEVELS);
			}
		};
		fCollapseItemAction.setText(Messages.CollapseSelected);
		fCollapseItemAction.setToolTipText(Messages.CollapseSelectedNode);
		fCollapseItemAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_COLLAPSE_ALL));

		fHelpAction = new Action(Messages.Help, IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				fViewer.getControl().notifyListeners(SWT.Help, new Event());
			}
		};
		fHelpAction.setToolTipText(Messages.PackPropertyView_HelpForPackPropertiesView);
		fHelpAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_HELP));

		fDoubleClickAction = new Action() {
			@Override
			public void run() {
				ISelection selection = fViewer.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				if (fViewer.getExpandedState(obj)) {
					fViewer.collapseToLevel(obj, AbstractTreeViewer.ALL_LEVELS);
				} else if (fViewer.isExpandable(obj)) {
					fViewer.expandToLevel(obj, 1);
				}
			}
		};

		fInstallAction = new Action() {
			@Override
			public void run() {
				ICpExample example = getExampleItem();
				if (example == null) {
					return;
				}
				ICpPackInstaller packInstaller = CpPlugIn.getPackManager().getPackInstaller();
				packInstaller.installPack(example.getPackId());
			}
		};

		fCopyAction = new Action() {
			@Override
			public void run() {
				ICpExample cpExample = getExampleItem();
				copyExample(cpExample.getAbsolutePath(cpExample.getFolder()));
				PackInstallerUtils.clearReadOnly(
						ResourcesPlugin.getWorkspace().getRoot().getLocation()
						.append(Utils.extractBaseFileName(cpExample.getFolder())).toFile(),
						CmsisConstants.EMPTY_STRING);
			}
		};
		fCopyAction.setText(Messages.PackPropertyView_CopyAction);
		fCopyAction.setToolTipText(Messages.PackPropertyView_CopyTooltip);
		fCopyAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_RTE));

	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				PackPropertyView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(fViewer.getControl());
		fViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, fViewer);
	}

	void fillContextMenu(IMenuManager manager) {
		if (fViewer.getSelection() == null || fViewer.getSelection().isEmpty()) {
			manager.add(fExpandAction);
			manager.add(fCollapseAction);
		} else {
			manager.add(fExpandItemAction);
			manager.add(fCollapseItemAction);
		}
		manager.add(new Separator());
		ICpExample example = getExampleItem();
		if (example != null) {
			if (example.getPack().getPackState() == PackState.INSTALLED
					|| example.getPack().getPackState() == PackState.GENERATED) {
				manager.add(fCopyAction);
				fCopyAction.setEnabled(copyButtonEnabled(example));
			} else {
				if (example.getPack().getPackState() == PackState.DOWNLOADED) {
					fInstallAction.setText(Messages.PackPropertyView_UnpackAction);
					fInstallAction.setToolTipText(Messages.PackPropertyView_UnpackTooltip);
					fInstallAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_RTE_UNPACK));
				} else {
					fInstallAction.setText(Messages.PackPropertyView_InstallAction);
					fInstallAction.setToolTipText(Messages.PackPropertyView_InstallTooltip);
					fInstallAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_RTE_INSTALL));
				}
				manager.add(fInstallAction);
			}
		}
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void hookDoubleClickAction() {
		fViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				fDoubleClickAction.run();
			}
		});
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(fExpandAction);
		manager.add(fCollapseAction);
		manager.add(fHelpAction);
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(fExpandAction);
		manager.add(fCollapseAction);
		manager.add(fHelpAction);
	}

	private boolean copyButtonEnabled(ICpExample example) {
		if (example == null) {
			return false;
		}
		return true;
	}

	void copyExample(String examplePath) {
		IImportWizard wizard = new ExternalProjectImportWizard(examplePath);
		IDialogSettings settings = wizard.getDialogSettings();
		boolean bCopySaved = false;
		if (settings != null) {
			bCopySaved = settings.getBoolean(STORE_COPY_PROJECT_ID);
			settings.put(STORE_COPY_PROJECT_ID, true);
		}
		wizard.init(PlatformUI.getWorkbench(), new TreeSelection());

		WizardDialog dialog = new WizardDialog(fViewer.getControl().getShell(), wizard);

		File exampleFolder = new File(examplePath);
		PackInstallerUtils.clearReadOnly(exampleFolder, ".project"); //$NON-NLS-1$

		if (dialog.open() == Window.OK) {

			IPerspectiveDescriptor persDescription = PlatformUI.getWorkbench().getPerspectiveRegistry()
					.findPerspectiveWithId("org.eclipse.cdt.ui.CPerspective"); //$NON-NLS-1$

			if (persDescription != null) {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().setPerspective(persDescription);
			}
		}

		if (settings != null) {
			settings.put(STORE_COPY_PROJECT_ID, bCopySaved);
		}

		PackInstallerUtils.setReadOnly(exampleFolder);
	}

	public Composite getComposite() {
		return fViewer.getTree();
	}

	@Override
	public void setFocus() {
		fViewer.getControl().setFocus();
	}

	@Override
	public void handle(RteEvent event) {
		if (RteEvent.PACKS_RELOADED.equals(event.getTopic())) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					fViewer.setInput(null);
				}
			});
		}
	}

}
