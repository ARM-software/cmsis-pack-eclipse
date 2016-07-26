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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.DeviceVendor;
import com.arm.cmsis.pack.ICpEnvironmentProvider;
import com.arm.cmsis.pack.ICpPackInstaller;
import com.arm.cmsis.pack.ICpPackManager;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpBoard;
import com.arm.cmsis.pack.data.ICpExample;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack.PackState;
import com.arm.cmsis.pack.events.IRteEventListener;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.installer.ui.CpInstallerPlugInUI;
import com.arm.cmsis.pack.installer.ui.IHelpContextIds;
import com.arm.cmsis.pack.installer.ui.Messages;
import com.arm.cmsis.pack.installer.utils.PackInstallerUtils;
import com.arm.cmsis.pack.rte.examples.IRteExampleItem;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.tree.AdvisedCellLabelProvider;
import com.arm.cmsis.pack.ui.tree.ColumnAdvisor;
import com.arm.cmsis.pack.ui.tree.TreeObjectContentProvider;
import com.arm.cmsis.pack.utils.AlnumComparator;
import com.arm.cmsis.pack.utils.Utils;

public class ExamplesView extends ViewPart implements IRteEventListener {

	public static final String ID = "com.arm.cmsis.pack.installer.ui.views.ExamplesView"; //$NON-NLS-1$

	private static final int COLBUTTON = 1;

	FilteredTree fTree;
	TreeViewer fViewer;

	Action fShowInstOnlyAction;
	private Action fHelpAction;

	PacksExamplesViewFilter fPacksExamplesViewFilter;

	ViewerFilter[] fExamplesViewFilters;

	ICpPackInstaller fPackInstaller;

	private ISelectionListener fViewSelectionListener;

	private ExamplesViewColumnAdvisor fColumnAdvisor;

	IRteExampleItem getRteExampleItem(Object obj) {
		if (obj instanceof IRteExampleItem) {
			return (IRteExampleItem) obj;
		}
		return null;
	}

	class ExamplesViewColumnAdvisor extends ColumnAdvisor {

		public ExamplesViewColumnAdvisor(ColumnViewer columnViewer) {
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
			if (getCellControlType(obj,
					columnIndex) == CellControlType.BUTTON) {
				IRteExampleItem example = getRteExampleItem(obj);
				if (example != null) {
					ICpExample e = example.getExample();
					if (e == null|| fPackInstaller.isProcessing(e.getPackId())) {
						return false;
					} else if (!CpInstallerPlugInUI.isOnline() 	&& e.getPack().getPackState() == PackState.AVAILABLE) {
						return false;
					} else {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public Image getImage(Object obj, int columnIndex) {
			if (getCellControlType(obj,
					columnIndex) == CellControlType.BUTTON) {
				switch (getString(obj, columnIndex)) {
					case CmsisConstants.BUTTON_COPY :
						return CpPlugInUI.getImage(CpPlugInUI.ICON_RTE);
					case CmsisConstants.BUTTON_INSTALL :
						return CpPlugInUI.getImage(CpPlugInUI.ICON_RTE_INSTALL);
					default :
						break;
				}
			}
			return null;
		}

		@Override
		public String getString(Object obj, int index) {
			if (getCellControlType(obj, index) == CellControlType.BUTTON) {
				IRteExampleItem item = getRteExampleItem(obj);
				if (item != null) {
					if (item.getExample().getPack()
							.getPackState() == PackState.INSTALLED
							|| item.getExample().getPack()
									.getPackState() == PackState.GENERATED) {
						return CmsisConstants.BUTTON_COPY;
					}
					return CmsisConstants.BUTTON_INSTALL;
				}
			}
			return CmsisConstants.EMPTY_STRING;
		}

		@Override
		public String getTooltipText(Object obj, int columnIndex) {
			if (getCellControlType(obj,
					columnIndex) == CellControlType.BUTTON) {
				IRteExampleItem item = getRteExampleItem(obj);
				if (item != null) {
					if (item.getExample().getPack()
							.getPackState() != PackState.INSTALLED) {
						StringBuilder str = new StringBuilder(
								Messages.ExamplesView_CopyExampleInstallPack)
										.append(item.getExample().getPackId());
						return str.toString();
					}
					return constructExampleTooltipText(item.getExample());
				}
			}
			return null;
		}

		@Override
		protected void handleMouseUp(MouseEvent e) {
			Point pt = new Point(e.x, e.y);
			ViewerCell cell = getViewer().getCell(pt);

			if (cell == null) {
				return;
			}

			int colIndex = cell.getColumnIndex();
			Object element = cell.getElement();
			if (getCellControlType(element, colIndex) != CellControlType.BUTTON
					|| !isEnabled(element, colIndex)
					|| !isButtonPressed(element, colIndex)) {
				return;
			}

			IRteExampleItem example = getRteExampleItem(element);
			if (example != null) {
				switch (getString(element, colIndex)) {
					case CmsisConstants.BUTTON_COPY :
						ICpExample cpExample = example.getExample();
						copyExample(cpExample);
						PackInstallerUtils.clearReadOnly(ResourcesPlugin
								.getWorkspace().getRoot().getLocation()
								.append(Utils.extractBaseFileName(
										cpExample.getFolder()))
								.toFile(), CmsisConstants.EMPTY_STRING);
						break;
					case CmsisConstants.BUTTON_INSTALL :
						fPackInstaller
								.installPack(example.getExample().getPackId());
					default :
						break;
				}
			}

			setButtonPressed(null, COLBUTTON, null);
			this.control.redraw();
		}

	} /// end of ColumnAdviser

	void copyExample(ICpExample cpExample) {
		ICpEnvironmentProvider envProvider = CpPlugIn.getEnvironmentProvider();
		if(envProvider == null) {
			return;
		}
		IAdaptable copyResult = envProvider.copyExample(cpExample);
		if(copyResult == null) {
			return;
		}
		IProject project = (IProject) copyResult.getAdapter(IProject.class);
		if (project == null) {
			return;
		}

		IWorkbench wb = PlatformUI.getWorkbench();
		if (wb != null) {
			IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
			if (window != null) {
				IPerspectiveDescriptor persDescription = wb.getPerspectiveRegistry().findPerspectiveWithId("org.eclipse.cdt.ui.CPerspective"); //$NON-NLS-1$
				IWorkbenchPage page = window.getActivePage();
				if (page != null && persDescription != null) {
					page.setPerspective(persDescription);
					try {
						String rteConf = project.getName()	+ '.' + CmsisConstants.RTECONFIG;
						IResource r = project.findMember(rteConf);
						if(r != null && r.exists() && r.getType() == IResource.FILE) 
							IDE.openEditor(page, project.getFile(rteConf));
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	String constructExampleTooltipText(ICpExample example) {
		ICpBoard b = example.getBoard();
		String line1 = NLS.bind(Messages.ExamplesView_Board, b.getName(),
				b.getVendor());
		StringBuilder lb2 = new StringBuilder(Messages.ExamplesView_Device);
		for (ICpItem device : b.getMountedDevices()) {
			String vendorName = DeviceVendor
					.getOfficialVendorName(device.getVendor());
			String deviceName = CmsisConstants.EMPTY_STRING;
			if (device.hasAttribute(CmsisConstants.DFAMILY)) {
				deviceName = device.getAttribute(CmsisConstants.DFAMILY);
			} else if (device.hasAttribute(CmsisConstants.DSUBFAMILY)) {
				deviceName = device.getAttribute(CmsisConstants.DSUBFAMILY);
			} else if (device.hasAttribute(CmsisConstants.DNAME)) {
				deviceName = device.getAttribute(CmsisConstants.DNAME);
			} else if (device.hasAttribute(CmsisConstants.DVARIANT)) {
				deviceName = device.getAttribute(CmsisConstants.DVARIANT);
			}
			if (!deviceName.isEmpty()) {
				lb2.append(deviceName).append(" (").append(vendorName) //$NON-NLS-1$
						.append("), "); //$NON-NLS-1$
			}
		}
		if (lb2.lastIndexOf(",") >= 0) { //$NON-NLS-1$
			lb2.deleteCharAt(lb2.lastIndexOf(",")); //$NON-NLS-1$
		}
		String line2 = lb2.append(System.lineSeparator()).toString();
		String line3 = NLS.bind(Messages.ExamplesView_Pack,
				example.getPackId());
		String line4 = example.getDescription();
		return line1 + line2 + line3 + line4;
	}

	class ExampleTreeColumnComparator extends TreeColumnComparator {

		private final AlnumComparator alnumComparator;

		public ExampleTreeColumnComparator(TreeViewer viewer,
				ColumnAdvisor advisor) {
			super(viewer, advisor);
			alnumComparator = new AlnumComparator(false, false);
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {

			Tree tree = treeViewer.getTree();
			if (getColumnIndex() != 0) {
				return super.compare(viewer, e1, e2);
			}

			ICpExample cp1 = ((IRteExampleItem) e1).getExample();
			ICpExample cp2 = ((IRteExampleItem) e2).getExample();

			int result = alnumComparator.compare(cp1.getId(), cp2.getId());
			return tree.getSortDirection() == SWT.DOWN ? -result : result;
		}
	}

	public ExamplesView() {
		fPackInstaller = CpPlugIn.getPackManager().getPackInstaller();
	}

	@Override
	public void createPartControl(Composite parent) {
		fPacksExamplesViewFilter = PacksExamplesViewFilter.getInstance();
		PatternFilter patternFilter = new PatternFilter() {
			@Override
			protected boolean isLeafMatch(final Viewer viewer,
					final Object element) {
				TreeViewer treeViewer = (TreeViewer) viewer;
				boolean isMatch = false;
				ColumnLabelProvider labelProvider = (ColumnLabelProvider) treeViewer
						.getLabelProvider(0);
				String labelText = labelProvider.getText(element);
				isMatch |= wordMatches(labelText);
				return isMatch;
			}
		};
		patternFilter.setIncludeLeadingWildcard(true);
		fExamplesViewFilters = new ViewerFilter[]{patternFilter,
				fPacksExamplesViewFilter};

		fTree = new FilteredTree(parent,
				SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL,
				patternFilter, true);
		fTree.setInitialText(Messages.ExamplesView_SearchExample);
		fViewer = fTree.getViewer();
		Tree tree = fViewer.getTree();
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		// ------ Start Setting ALL Columns for the Examples View
		// ------ First Column
		TreeViewerColumn column0 = new TreeViewerColumn(fViewer, SWT.LEFT);
		tree.setLinesVisible(true);
		column0.getColumn().setText(CmsisConstants.EXAMPLE_TITLE);
		column0.getColumn().setWidth(300);
		column0.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				ICpExample e = ((IRteExampleItem) element).getExample();

				return e.getId();
			}

			@Override
			public String getToolTipText(Object element) {
				ICpExample e = ((IRteExampleItem) element).getExample();

				return constructExampleTooltipText(e);
			}
		});

		// ------ Second Column
		TreeViewerColumn column1 = new TreeViewerColumn(fViewer, SWT.LEFT);
		column1.getColumn().setText(CmsisConstants.ACTION_TITLE);
		column1.getColumn().setWidth(90);
		fColumnAdvisor = new ExamplesViewColumnAdvisor(fViewer);
		column1.setLabelProvider(
				new AdvisedCellLabelProvider(fColumnAdvisor, COLBUTTON));

		// ------ Third Column
		TreeViewerColumn column2 = new TreeViewerColumn(fViewer, SWT.LEFT);
		column2.getColumn().setText(CmsisConstants.DESCRIPTION_TITLE);
		column2.getColumn().setWidth(400);
		column2.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object obj) {
				IRteExampleItem example = getRteExampleItem(obj);
				if (example != null) {
					return example.getExample().getDescription();
				}
				return null;
			}
		});
		// ------ End Setting ALL Columns for the Examples View

		fViewer.setContentProvider(new TreeObjectContentProvider());
		fViewer.setComparator(
				new ExampleTreeColumnComparator(fViewer, fColumnAdvisor));
		refresh();

		ColumnViewerToolTipSupport.enableFor(fViewer);

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(fViewer.getControl(),
				IHelpContextIds.EXAMPLES_VIEW);

		getSite().setSelectionProvider(fViewer);

		hookViewSelection();

		makeActions();
		contributeToActionBars();

		CpInstallerPlugInUI.registerViewPart(this);

		fViewer.setFilters(fExamplesViewFilters);

		CpPlugIn.addRteListener(this);
	}

	protected void refresh() {
		if (CpPlugIn.getDefault() == null) {
			return;
		}
		ICpPackManager packManager = CpPlugIn.getPackManager();
		if (packManager != null) {
			fViewer.setInput(packManager.getExamples());
		} else {
			fViewer.setInput(null);
		}
	}

	private void hookViewSelection() {
		fViewSelectionListener = new ISelectionListener() {

			@Override
			public void selectionChanged(IWorkbenchPart part,
					ISelection selection) {

				if ((part instanceof DevicesView)
						|| (part instanceof BoardsView)) {
					if (!fTree.getFilterControl().isDisposed()) {
						fTree.getFilterControl()
								.setText(CmsisConstants.EMPTY_STRING);
					}
					fireSelectionChanged(part, selection);
				}
			}
		};
		getSite().getPage().addSelectionListener(fViewSelectionListener);
	}

	protected void fireSelectionChanged(IWorkbenchPart part,
			ISelection selection) {
		fPacksExamplesViewFilter.setSelection(part,
				(IStructuredSelection) selection);
		if (!fViewer.getControl().isDisposed()) {
			fViewer.setFilters(fExamplesViewFilters);
			fViewer.setSelection(null);
		}
	}

	private void makeActions() {
		fShowInstOnlyAction = new Action(
				Messages.ExamplesView_OnlyShowInstalledPack,
				IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				fPacksExamplesViewFilter
						.setInstalledOnly(fShowInstOnlyAction.isChecked());
				fViewer.setFilters(fExamplesViewFilters);
				fViewer.setSelection(null);
				if (fShowInstOnlyAction.isChecked()) {
					fShowInstOnlyAction.setImageDescriptor(CpPlugInUI
							.getImageDescriptor(CpPlugInUI.ICON_CHECKED));
				} else {
					fShowInstOnlyAction.setImageDescriptor(CpPlugInUI
							.getImageDescriptor(CpPlugInUI.ICON_UNCHECKED));
				}
			}
		};
		fShowInstOnlyAction
				.setToolTipText(Messages.ExamplesView_OnlyShowInstalledPack);
		fShowInstOnlyAction.setImageDescriptor(
				CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_UNCHECKED));
		fShowInstOnlyAction.setEnabled(true);

		fHelpAction = new Action(Messages.Help, IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				fViewer.getControl().notifyListeners(SWT.Help, new Event());
			}
		};
		fHelpAction.setToolTipText(Messages.ExamplesView_HelpForExamplesView);
		fHelpAction.setImageDescriptor(
				CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_HELP));
	}


	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(fShowInstOnlyAction);
		manager.add(new Separator());
		manager.add(fHelpAction);
		manager.add(new Separator());
		PackInstallerViewUtils.addManagementCommandsToLocalToolBar(this,
				manager);
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		ActionContributionItem aci = new ActionContributionItem(
				fShowInstOnlyAction);
		aci.setMode(ActionContributionItem.MODE_FORCE_TEXT);
		manager.add(aci);
		manager.add(new Separator());
		manager.add(fHelpAction);
		manager.add(new Separator());
		PackInstallerViewUtils.addManagementCommandsToLocalToolBar(this,
				manager);
	}

	public Composite getComposite() {
		return fTree;
	}

	@Override
	public void setFocus() {
		fViewer.getControl().setFocus();
	}

	@Override
	public void handle(RteEvent event) {
		if (RteEvent.PACKS_RELOADED.equals(event.getTopic())) {
			fPackInstaller.reset();
		}
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				refresh();
			}
		});
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == Viewer.class) {
			return adapter.cast(fViewer);
		}
		return super.getAdapter(adapter);
	}

}
