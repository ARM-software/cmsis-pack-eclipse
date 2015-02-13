package com.arm.cmsis.pack.refclient.views;

import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackManager;
import com.arm.cmsis.pack.data.ICpComponent;
import com.arm.cmsis.pack.data.ICpDeviceItem;
import com.arm.cmsis.pack.data.ICpFile;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPackFamily;
import com.arm.cmsis.pack.events.IRteEventListener;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.generic.Attributes;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.refclient.RefClient;
import com.arm.cmsis.pack.ui.CpPlugInUI;



/**
 * This sample view class is an example how to show raw data of installed packs  
 * <p>
 */

public class PackView extends ViewPart implements IRteEventListener {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.arm.cmsis.pack.refclient.views.PackView";

	private TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	private Action expandAction;
	private Action collapseAction;
	private Action doubleClickAction;

	class ViewContentProvider implements ITreeContentProvider {
		ICpItem packs = null;

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			if(newInput != null && newInput instanceof ICpItem)
				packs = (ICpItem) newInput; 
			else
				packs = null;
		}
		public void dispose() {
			packs = null;
		}
		public Object[] getElements(Object parent) {
			return getChildren(parent);
		}
		public Object getParent(Object child) {
			if (child instanceof ICpItem) {
				return ((ICpItem)child).getParent();
			}
			return null;
		}
		public Object [] getChildren(Object parent) {
			
			if (parent instanceof ICpDeviceItem) {
				ICpDeviceItem item = (ICpDeviceItem)parent;
				if(item.getDeviceItems() != null)
					return item.getDeviceItems().toArray();
				else if(item.getEffectiveProperties("") != null)
  				return item.getEffectiveProperties("").getChildArray();
			} 
			if (parent instanceof ICpItem)
				return ((ICpItem)parent).getChildArray();
			return new Object[0];
		}
		public boolean hasChildren(Object parent) {
			if (parent instanceof ICpDeviceItem) {
				ICpDeviceItem item = (ICpDeviceItem)parent;
				return item.getDeviceItems() != null || item.getChildren() != null;
			} 
			if (parent instanceof ICpItem)
				return ((ICpItem)parent).hasChildren();
			return false;
		}
	}

	class ViewLabelProvider implements ITableLabelProvider{
	
		
		public String getColumnText(Object obj, int index){
			if(obj instanceof ICpItem) {
				ICpItem item = (ICpItem)obj;
				switch(index) {
				case 0:
					return item.getTag();
				case 1:
					return item.attributes().getAttributesAsString();
				case 2: 
					return item.getText();
				default:
					break;
				}
			}
			return IAttributes.EMPTY_STRING;
		}

		public Image getColumnImage(Object obj, int index){
			if(index == 0)
				return getImage(obj);
			return null;
		}

		public Image getImage(Object obj){
			if (obj instanceof ICpPack) {
				return CpPlugInUI.getImage(CpPlugInUI.ICON_PACKAGE);
			} else if (obj instanceof ICpPackFamily) {
				return CpPlugInUI.getImage(CpPlugInUI.ICON_PACKAGES);
			} else if (obj instanceof ICpComponent) {
				return CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT);
			} else if (obj instanceof ICpFile) {
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
			} else if (obj instanceof ICpDeviceItem) {
				ICpDeviceItem item = (ICpDeviceItem)obj;
				if(item.getDeviceItems() == null) {
					return CpPlugInUI.getImage(CpPlugInUI.ICON_DEVICE);
				}
			}
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}

		@Override
		public void addListener(ILabelProviderListener listener) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
		}
	}

	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public PackView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {

		Tree tree = new Tree(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		tree.setHeaderVisible(true);
		viewer = new TreeViewer(tree);

		TreeColumn column0 = new TreeColumn(tree, SWT.LEFT);
		tree.setLinesVisible(true);
		column0.setAlignment(SWT.LEFT);
		column0.setText("Tag");
		column0.setWidth(300);
		
		TreeColumn column1 = new TreeColumn(tree, SWT.LEFT);
		tree.setLinesVisible(true);
		column1.setAlignment(SWT.LEFT);
		column1.setText("Attributes");
		column1.setWidth(200);

		TreeColumn column2 = new TreeColumn(tree, SWT.RIGHT);
		column2.setAlignment(SWT.LEFT);
		column2.setText("Text");
		column2.setWidth(500);

		drillDownAdapter = new DrillDownAdapter(viewer);


		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		//viewer.setSorter(new NameSorter());
		refresh();
		
		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "com.arm.cmsis.pack.refclient.viewer");
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		
		RefClient.getDefault().addRteConfigListener(this);
	}

	
	protected void refresh() {

		ICpPackManager packManager = CpPlugIn.getDefault().getPackManager();
		if(packManager != null)
			viewer.setInput(packManager.getPacks());
		else 
			viewer.setInput(null);
	}
	
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				PackView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(expandAction);
		manager.add(collapseAction);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(expandAction);
		manager.add(collapseAction);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(expandAction);
		manager.add(collapseAction);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private void makeActions() {
		expandAction = new Action() {
			public void run() {
				if(viewer == null)
					return;
				viewer.expandAll();
			}
		};

		expandAction.setText("Expand All");
		expandAction.setToolTipText("Expands all nodes");
		expandAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_EXPAND_ALL));
		
		collapseAction = new Action() {
			public void run() {
				if(viewer == null)
					return;
				viewer.collapseAll();
			}
		};
		collapseAction.setText("Collapse All");
		collapseAction.setToolTipText("Collapse all expanded nodes");
		collapseAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
					getImageDescriptor(ISharedImages.IMG_ELCL_COLLAPSEALL));
		
		
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				if(obj instanceof ICpItem) {
					ICpItem item = (ICpItem)obj;
					String s = item.getName();
					s += "\n";
					Attributes a = new Attributes();
					Map<String, String> m = item.getEffectiveAttributes(null); 
					a.setAttributes(m);
					s += a.getAttributesAsString(); 
					showMessage(s);
				}
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"PackView",
			message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	@Override
	public void handleRteEvent(RteEvent event) {
		if(event.getTopic().equals(RteEvent.PACK_ALL_LOADED)) 
			refresh();
	}

	@Override
	public void dispose() {
		RefClient.getDefault().removeRteConfigListener(this);
		super.dispose();
	}
	
}