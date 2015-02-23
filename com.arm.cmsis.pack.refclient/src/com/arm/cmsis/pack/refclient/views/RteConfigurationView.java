package com.arm.cmsis.pack.refclient.views;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.arm.cmsis.pack.data.ICpComponent;
import com.arm.cmsis.pack.data.ICpDeviceItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.events.IRteConfigurationProxy;
import com.arm.cmsis.pack.events.IRteEventListener;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.info.ICpConfigurationInfo;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.info.ICpFileInfo;
import com.arm.cmsis.pack.info.ICpPackInfo;
import com.arm.cmsis.pack.refclient.RefClient;
import com.arm.cmsis.pack.ui.CpPlugInUI;

public class RteConfigurationView extends ViewPart implements IRteEventListener{

	private TreeViewer viewer;
	IRteConfigurationProxy confProxy = null;
	ICpConfigurationInfo confData = null;
	
	private static ICpItem getCpItem(Object o) {
		if(o instanceof ICpItem)
			return (ICpItem)o;
		return null;
	}
	
	class ViewContentProvider implements ITreeContentProvider {
		
		
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
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
			ICpItem item = getCpItem(parent);
			if(item != null) {
				if (item instanceof ICpDeviceInfo) {
					ICpDeviceItem di = ((ICpDeviceInfo)parent).getDevice();
					String processor = item.getProcessorName();
					return di.getEffectiveProperties(processor).getChildArray();
				} else { 
					return ((ICpItem)parent).getChildArray();
				}
			}
			return new Object[0];
		}
		public boolean hasChildren(Object parent) {
			ICpItem item = getCpItem(parent);
			if(item != null) {
				if (item instanceof ICpDeviceInfo) {
					ICpDeviceItem di = ((ICpDeviceInfo)parent).getDevice();
					if(di == null)
						return false;
					String processor = item.getProcessorName();
					return di.getEffectiveProperties(processor).hasChildren();
				} else { 
					return ((ICpItem)parent).hasChildren();
				}
			}
			return false;
		}
	}

	class ViewLabelProvider implements ITableLabelProvider{
	
		
		public String getColumnText(Object obj, int index){
			ICpItem item = getCpItem(obj);
			if(item != null) {
				switch(index) {
				case 0:
					return item.getName();
				case 1:
					return item.attributes().getAttributesAsString();
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
			ICpItem item = getCpItem(obj);
			if (item != null) {
				String tag = item.getTag();
				if (obj instanceof ICpPackInfo) {
					return CpPlugInUI.getImage(CpPlugInUI.ICON_PACKAGE);
				} else if (obj instanceof ICpComponent) {
					return CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT);
				} else if (obj instanceof ICpFileInfo) {
					return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
				} else if (obj instanceof ICpDeviceInfo) {
					return CpPlugInUI.getImage(CpPlugInUI.ICON_DEVICE);
				} else if (tag.equals("packages")) {
					return CpPlugInUI.getImage(CpPlugInUI.ICON_PACKAGES);
				} 
				
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
			}
			return null;
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

	
	public RteConfigurationView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		Tree tree = new Tree(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		tree.setHeaderVisible(true);
		viewer = new TreeViewer(tree);

		TreeColumn column0 = new TreeColumn(tree, SWT.LEFT);
		tree.setLinesVisible(true);
		column0.setAlignment(SWT.LEFT);
		column0.setText("Item");
		column0.setWidth(300);
		
		TreeColumn column1 = new TreeColumn(tree, SWT.LEFT);
		tree.setLinesVisible(true);
		column1.setAlignment(SWT.LEFT);
		column1.setText("Details");
		column1.setWidth(500);

		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		refresh();
		
		RefClient.getDefault().addRteConfigListener(this);
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	/**
	 * 
	 */
	protected void refresh() {
		IRteConfigurationProxy conf = RefClient.getDefault().getActiveRteConfiguration();
		if(confProxy == conf)
			return;
		if(confProxy != null)
			confProxy.removeRteEventListener(this);
		
		confProxy = conf;
		confData = null;
		if(confProxy != null) {
			conf.addRteEventListener(this);
			confData = conf.getConfigurationInfo();
		}
		viewer.setInput(confData);
		viewer.expandAll();
	}

	/* (non-Javadoc)
	 * @see com.arm.cmsis.pack.events.IRteEventListener#handleRteEvent(com.arm.cmsis.pack.events.RteEvent)
	 */
	@Override
	public void handleRteEvent(RteEvent event) {
		if(event.getTopic().equals(RteEvent.CONFIGURATION_APPLIED) || 
		   event.getTopic().equals(RteEvent.CONFIGURATION_ACTIVATED)) 
			refresh();
	}

	@Override
	public void dispose() {
		RefClient.getDefault().removeRteConfigListener(this);
		if(confProxy != null)
			confProxy.removeRteEventListener(this);
		confProxy = null;
		confData = null;
		
		super.dispose();
	}
	
}
