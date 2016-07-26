/*******************************************************************************
* Copyright (c) 2015 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* Eclipse Project - generation from template   
* ARM Ltd and ARM Germany GmbH - application-specific implementation
*******************************************************************************/
package com.arm.cmsis.pack.refclient.ui;

import java.util.Collection;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.part.*;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.configuration.IRteConfiguration;
import com.arm.cmsis.pack.configuration.RteConfiguration;
import com.arm.cmsis.pack.data.ICpDebug;
import com.arm.cmsis.pack.data.ICpDebugConfiguration;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.events.IRteEventListener;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.info.ICpComponentInfo;
import com.arm.cmsis.pack.info.ICpConfigurationInfo;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.info.ICpFileInfo;
import com.arm.cmsis.pack.info.ICpPackInfo;
import com.arm.cmsis.pack.project.CpProjectPlugIn;
import com.arm.cmsis.pack.project.IRteProject;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.tree.TreeObjectContentProvider;


/**
 * This sample view class is an example how to show raw data of an active RteConfighuration  
 * <p>
 */

public class ConfigView extends ViewPart implements ISelectionListener, IRteEventListener {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.arm.cmsis.pack.refclient.ui.ConfigView"; //$NON-NLS-1$
	public static final String DEBUG_CONFIG = "Debug Configuration"; //$NON-NLS-1$

	IRteProject selectedProject = null;
	
	private TreeViewer viewer;
	private Action action1;
	
	class ConfigViewContentProvider extends TreeObjectContentProvider {

		public Object [] getChildren(Object parent) {
			if (parent instanceof ICpDeviceInfo) {
				ICpDeviceInfo item = (ICpDeviceInfo)parent;
				ICpItem props = item.getEffectiveProperties(); 
				if(props != null) 
					return props.getChildArray(); 
			} 

			return super.getChildren(parent);
		}
		public boolean hasChildren(Object parent) {
			if (parent instanceof ICpDeviceInfo) {
				return true;
			} 
			return super.hasChildren(parent);
		}
	}

	class ConfigViewLabelProvider extends LabelProvider implements ITableLabelProvider{
	
		@Override
		public String getColumnText(Object obj, int index){
			if(obj instanceof ICpItem) {
				ICpItem item = (ICpItem)obj;
				switch(index) {
				case 0:
					return item.getTag();
				case 1:
					return item.attributes().toString();
				default:
					break;
				}
			}
			return CmsisConstants.EMPTY_STRING;
		}

		@Override
		public Image getColumnImage(Object obj, int index){
			if(index == 0)
				return getImage(obj);
			return null;
		}

		@Override
		public Image getImage(Object obj){
			if (obj instanceof ICpPackInfo) {
				return CpPlugInUI.getImage(CpPlugInUI.ICON_PACKAGE);
			} else if (obj instanceof ICpComponentInfo) {
				ICpComponentInfo ci = (ICpComponentInfo) obj;
				if(ci.isApi())
					return CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT_ERROR);
				return CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT);
			} else if (obj instanceof ICpFileInfo) {
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
			} else if (obj instanceof ICpDeviceInfo) {
				return CpPlugInUI.getImage(CpPlugInUI.ICON_DEVICE);
			} else if(obj instanceof ICpItem) {
				ICpItem item = (ICpItem) obj;
				if(item.getTag().equals(CmsisConstants.BOOK_TAG))
					return  CpPlugInUI.getImage(CpPlugInUI.ICON_BOOK);
			}
			return null;
		}
	}


	/**
	 * The constructor.
	 */
	public ConfigView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		Tree tree = new Tree(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		viewer = new TreeViewer(tree);

		TreeColumn column0 = new TreeColumn(tree, SWT.LEFT);
		column0.setAlignment(SWT.LEFT);
		column0.setText("Tag"); //$NON-NLS-1$
		column0.setWidth(100);
		
		TreeColumn column1 = new TreeColumn(tree, SWT.LEFT);
		column1.setAlignment(SWT.LEFT);
		column1.setText("Attributes");  //$NON-NLS-1$
		column1.setWidth(600);
		
		viewer.setContentProvider(new ConfigViewContentProvider());
		viewer.setLabelProvider(new ConfigViewLabelProvider());
		
		
		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), ID);
		makeActions();
		hookContextMenu();
		contributeToActionBars();
		
		CpProjectPlugIn.getRteProjectManager().addListener(this);
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
		
		ISelection sel = getSite().getWorkbenchWindow().getSelectionService().getSelection();
		updateView(sel);
	}
	


	public void dispose() {
	// important: We need do unregister our listener when the view is disposed
		getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
		CpProjectPlugIn.getRteProjectManager().removeListener(this);
		selectedProject = null;
		super.dispose();
	}


	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ConfigView.this.fillContextMenu(manager);
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
		manager.add(action1);
		manager.add(new Separator());
	}

	void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				String msg = accessRteInfo(selectedProject); 
				showMessage(DEBUG_CONFIG, msg);
			}
		};
		action1.setText("Debug configuration"); //$NON-NLS-1$
		action1.setToolTipText("Show brief debug configuration info"); //$NON-NLS-1$
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
	}

	void showMessage(String title, String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			title, 	message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		updateView(selection);
	}


	void updateView(ISelection selection) {
		IProject p = CpPlugInUI.getProjectFromSelection(selection);
		IRteProject rteProject = CpProjectPlugIn.getRteProjectManager().getRteProject(p);
		
		if(rteProject == null || rteProject == selectedProject)
			return;
		
		updateViewer(rteProject);
 
	}
	static public String accessRteInfo(IProject project) {
		// I. IRteProject from IProject
		IRteProject rteProject = CpProjectPlugIn.getRteProjectManager().getRteProject(project);
		if(rteProject == null)
			return CmsisConstants.EMPTY_STRING;
		return accessRteInfo(rteProject);
	}
	
	static public String accessRteInfo(IRteProject rteProject) {
		// I. IRteConfiguration from IRteProject
		if(rteProject == null)
			return CmsisConstants.EMPTY_STRING;
		IRteConfiguration rteConf = rteProject.getRteConfiguration();
		if(rteConf == null)
			return CmsisConstants.EMPTY_STRING;

		// II. Device information
		ICpDeviceInfo deviceInfo = rteConf.getDeviceInfo();
		// a) name, endian, FPU from device info attributes : 
		String fpu = deviceInfo.getAttribute(CmsisConstants.DFPU);
		// b) get effective properties for selected processor 
		ICpItem effectiveProps = deviceInfo.getEffectiveProperties();
		// c) get specific properties, i.e. flash algorithms
		Collection<ICpItem> flashAlgos = effectiveProps.getChildren(CmsisConstants.ALGORITHM_TAG);
		for(ICpItem a : flashAlgos ) {
			@SuppressWarnings("unused")
			String fileName = a.getAbsolutePath(a.getName());
			Long size = a.attributes().getAttributeAsLong(CmsisConstants.SIZE, 0);
		}
		// III. Component information
		// a) direct access to 3 components:  CMSIS Core, Device Startup, CMSIS RTOS, e.g.:
		ICpComponentInfo rtosComponent = rteConf.getCmsisRtosComponent();
		// b) other components from configuration info
		ICpConfigurationInfo confInfo = rteConf.getConfigurationInfo();
		Collection<ICpItem> components = confInfo.getChildren(CmsisConstants.COMPONENTS_TAG);
		
		// IV. Files
		Map<String, ICpFileInfo> projectFiles = rteConf.getProjectFiles();
		Map<String, String> headers = rteConf.getHeaders(); 
		
		// V. Debug configuration
		
		ICpDebugConfiguration debugConfig = rteConf.getDebugConfiguration();
		String msg = DEBUG_CONFIG + ": "; //$NON-NLS-1$
		if(debugConfig != null) {
			msg +=  debugConfig.attributes().toString(); 
			int nUnits = debugConfig.getPunitsCount();
			for(int i = 0; i < nUnits; i++) {
				ICpDebug debugItem = debugConfig.getDebugItem(i);
				msg +="\n"; //$NON-NLS-1$
				msg += debugItem.getTag() + ": " + debugItem.attributes().toString(); //$NON-NLS-1$
			}
		}
		return msg;
	}
	
	IRteConfiguration getRteConfiguration() {
		if(selectedProject == null)
			return null;
		return selectedProject.getRteConfiguration();
	}
	
	void updateViewer(IRteProject rteProject) {
		selectedProject = rteProject;
		
		IRteConfiguration rteConf = getRteConfiguration();
		if(rteConf != null)
			viewer.setInput(rteConf.getConfigurationInfo());
		else
			viewer.setInput(null);
	}

	@Override
	public void handle(RteEvent event) {
		IRteProject rteProject = null;
		if(event.getTopic().equals(RteEvent.PROJECT_REMOVED)) {
			if(event.getData() != selectedProject)
				return;
		} else if(event.getTopic().equals(RteEvent.PROJECT_UPDATED)) {
			rteProject = (IRteProject)event.getData();
			if(rteProject != selectedProject) {
				return;
			} 
		}
		
		final IRteProject rteProjectToSet = rteProject;
		Display.getDefault().asyncExec (new Runnable () {
		      public void run () {
		    	  updateViewer(rteProjectToSet);
		      }
		   });
		
	}
	
}