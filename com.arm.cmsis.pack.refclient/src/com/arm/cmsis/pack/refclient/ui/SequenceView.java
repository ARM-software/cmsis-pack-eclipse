package com.arm.cmsis.pack.refclient.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.arm.cmsis.pack.CpDsqEngineFactory;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.configuration.IRteConfiguration;
import com.arm.cmsis.pack.dsq.DsqException;
import com.arm.cmsis.pack.dsq.DsqSequence;
import com.arm.cmsis.pack.dsq.IDsqContext;
import com.arm.cmsis.pack.dsq.IDsqEngine;
import com.arm.cmsis.pack.events.IRteEventListener;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.project.CpProjectPlugIn;
import com.arm.cmsis.pack.project.IRteProject;
import com.arm.cmsis.pack.refclient.RefDebugSeqClient;
import com.arm.cmsis.pack.refclient.ui.console.SequenceLogger;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.tree.AdvisedCellLabelProvider;
import com.arm.cmsis.pack.ui.tree.ColumnAdvisor;

public class SequenceView extends ViewPart implements ISelectionListener, IRteEventListener {

	public static final String ID = "com.arm.cmsis.pack.refclient.ui.SequenceView"; //$NON-NLS-1$
	private static final int COLBUTTON = 1;

	IRteProject selectedProject = null;
	IDsqEngine engine = null;

	private TableViewer viewer;
	SequenceLogger logger;

	class RefDsqSequenceContext extends DsqSequence {

		public RefDsqSequenceContext(String name) {
			super(name);
		}

		@Override
		public Long getPredefinedVariableValue(String name) {
			switch (name) {
			case IDsqContext.AP:
				return 2L;
			case IDsqContext.DP:
				return 0L;
			case IDsqContext.PROTOCOL:
				return 0x00010002L;
			case IDsqContext.CONNECTION:
				return 0L;
			case IDsqContext.TRACEOUT:
				return 0L;
			case IDsqContext.ERRORCONTROL:
				return 0L;
			default:
				return null;
			}
		}
	}

	class SequenceViewLabelProvider extends ColumnLabelProvider {
		@Override
		public Image getImage(Object element) {
			return CpPlugInUI.getImage(CpPlugInUI.ICON_COMPONENT);
		}

		@Override
		public String getText(Object element) {
			return element instanceof String ? (String)element : "";//$NON-NLS-1$
		}
	}

	class SequenceViewColumnAdvisor extends ColumnAdvisor {

		public SequenceViewColumnAdvisor(ColumnViewer columnViewer) {
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
			try {
				return !engine.isSequenceDisabled((String) obj);
			} catch (DsqException e) {
				throwException("Error while parsing the debug sequences", e); //$NON-NLS-1$
			}
			return false;
		}

		@Override
		public Image getImage(Object obj, int columnIndex) {
			return CpPlugInUI.getImage(CpPlugInUI.ICON_RUN);
		}

		@Override
		public String getString(Object obj, int columnIndex) {
			return "Execute"; //$NON-NLS-1$
		}

		@Override
		protected void handleMouseUp(MouseEvent e) {
			if (e.button == 1) {
				Point pt = new Point(e.x, e.y);
				ViewerCell cell = getViewer().getCell(pt);
				if (cell == null) {
					return;
				}

				int colIndex = cell.getColumnIndex();
				Object element = cell.getElement();
				if (getCellControlType(element, colIndex) != CellControlType.BUTTON ||
						!isEnabled(element, colIndex) ||
						!isButtonPressed(element, colIndex)) {
					return;
				}

				String seqName = (String) element;
				Job job = new Job("Sequence " + seqName) { //$NON-NLS-1$
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						try {
							engine.execute(new RefDsqSequenceContext(seqName));
							return Status.OK_STATUS;
						} catch (DsqException e) {
							throwException("Error while executing the debug sequences", e); //$NON-NLS-1$
							return Status.CANCEL_STATUS;
						}
					}
				};
				job.schedule();

				setButtonPressed(null, COLBUTTON, null);
				this.control.redraw();
			}
		}

	}

	public SequenceView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		Table table = new Table(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		viewer = new TableViewer(table);

		TableViewerColumn column0 = new TableViewerColumn(viewer, SWT.LEFT);
		column0.getColumn().setAlignment(SWT.LEFT);
		column0.getColumn().setText("Sequence"); //$NON-NLS-1$
		column0.getColumn().setWidth(200);
		column0.setLabelProvider(new SequenceViewLabelProvider());

		TableViewerColumn column1 = new TableViewerColumn(viewer, SWT.LEFT);
		column1.getColumn().setText(CmsisConstants.ACTION_TITLE);
		column1.getColumn().setWidth(100);
		column1.setLabelProvider(new AdvisedCellLabelProvider(
				new SequenceViewColumnAdvisor(viewer), COLBUTTON));

		viewer.setContentProvider(ArrayContentProvider.getInstance());

		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), ID);

		CpProjectPlugIn.getRteProjectManager().addListener(this);
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);

		ISelection sel = getSite().getWorkbenchWindow().getSelectionService().getSelection();
		updateView(sel);
	}

	@Override
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

		if(rteProject == null || rteProject == selectedProject) {
			return;
		}

		updateViewer(rteProject);

	}

	IRteConfiguration getRteConfiguration() {
		if(selectedProject == null) {
			return null;
		}
		return selectedProject.getRteConfiguration();
	}

	void updateViewer(IRteProject rteProject) {
		selectedProject = rteProject;

		IRteConfiguration rteConf = getRteConfiguration();
		if(rteConf != null && !viewer.getControl().isDisposed()) {
			engine = CpDsqEngineFactory.create(rteConf.getDeviceInfo(), new RefDebugSeqClient(), SequenceLogger.getInstance(selectedProject));
			if (engine == null) {
				viewer.setInput(null);
				MessageDialog.openError(null,
						"No Debug Sequence Engine Defined", //$NON-NLS-1$
						"There is no Debug Sequence Engine defined in the application"); //$NON-NLS-1$
				return;
			}
			try {
				viewer.setInput(engine.getDefaultSequenceNames());
			} catch (DsqException e) {
				viewer.setInput(null);
				throwException("Error while parsing the debug sequences", e); //$NON-NLS-1$
			}
		}
	}

	@Override
	public void handle(RteEvent event) {
		IRteProject rteProject = null;
		if(event.getTopic().equals(RteEvent.PROJECT_REMOVED)) {
			if(event.getData() != selectedProject) {
				return;
			}
		} else if(event.getTopic().equals(RteEvent.PROJECT_UPDATED)) {
			rteProject = (IRteProject)event.getData();
			if(rteProject != selectedProject) {
				return;
			}
		}

		final IRteProject rteProjectToSet = rteProject;
		Display.getDefault().asyncExec(() -> updateViewer(rteProjectToSet));
	}

	void throwException(String title, DsqException e) {
		MessageDialog.openError(null, title, e.getMessage());
	}

}
