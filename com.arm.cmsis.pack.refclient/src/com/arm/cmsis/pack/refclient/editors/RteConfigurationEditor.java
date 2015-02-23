package com.arm.cmsis.pack.refclient.editors;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import xmleditor.XMLEditor;

import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.events.IRteConfigurationProxy;
import com.arm.cmsis.pack.events.IRteEventListener;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.info.ICpConfigurationInfo;
import com.arm.cmsis.pack.parser.ConfigParser;
import com.arm.cmsis.pack.refclient.RefClient;
import com.arm.cmsis.pack.rte.IRteConfiguration;
import com.arm.cmsis.pack.rte.RteConfiguration;
import com.arm.cmsis.pack.ui.RteConfigurationProxy;
import com.arm.cmsis.pack.widgets.RteManagerWidget;

/**
 * An example showing how to create an RTE configuration multi-page editor. This
 * example has 2 pages:
 * <ul>
 * <li>page 0 contains an RteManagerWidget
 * <li>page 1 shows XML representation of saved configuration info
 * </ul>
 */
public class RteConfigurationEditor extends MultiPageEditorPart implements
		IResourceChangeListener, IRteEventListener {

	/** The RteManagerWidget chosen in page 0. */
	private RteManagerWidget rteManagerWidget;

	/** The text editor used in page 1. */
	private TextEditor textEditor;

	IRteConfigurationProxy configProxy = null;
	ConfigParser parser = null;

	boolean bDirty = false;

	public RteConfigurationEditor() {
		super();
		parser = new ConfigParser();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	void createXmlPage() {
		try {
			textEditor = new XMLEditor();
			int index = addPage(textEditor, getEditorInput());
			setPageText(index, "xml");
			setPartName(textEditor.getTitle());
		} catch (PartInitException e) {
			ErrorDialog.openError(getSite().getShell(),
					"Error creating nested text editor", null, e.getStatus());
		}
	}

	void createRteManagerPage() {

		Composite composite = new Composite(getContainer(), SWT.NONE);
		FillLayout layout = new FillLayout();
		composite.setLayout(layout);

		rteManagerWidget = new RteManagerWidget();
		rteManagerWidget.createControl(composite);

		int index = addPage(composite);
		setPageText(index, "Components");
	}

	protected void createPages() {
		createRteManagerPage();
		createXmlPage();
		createConfiguration();
	}

	protected void createConfiguration() {
		// create configuration out of text read by text editor
		IDocumentProvider dp = textEditor.getDocumentProvider();
		IDocument doc = dp.getDocument(textEditor.getEditorInput());
		String xml = doc.get();

		ICpItem root = parser.parseXmlString(xml);

		IRteConfiguration conf = new RteConfiguration();
		if (root != null) {
			ICpConfigurationInfo info = (ICpConfigurationInfo) root;
			conf.setConfigurationInfo(info);
			configProxy = new RteConfigurationProxy(conf);
			rteManagerWidget.setConfiguration(configProxy);
			configProxy.addRteEventListener(this);
		}
		bDirty = false;
	}

	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		if (configProxy == RefClient.getDefault().getActiveRteConfiguration())
			RefClient.getDefault().setActiveRteConfiguration(null);
		parser = null;
		configProxy = null;
		rteManagerWidget = null;
		super.dispose();
	}

	public void doSave(IProgressMonitor monitor) {
		if (textEditor == null)
			return;

		if (configProxy != null) {
			configProxy.apply();
			ICpConfigurationInfo info = configProxy.getConfigurationInfo();

			String xml = parser.writeToXmlString(info);
			if (xml != null) {
				IDocumentProvider dp = textEditor.getDocumentProvider();
				IDocument doc = dp.getDocument(textEditor.getEditorInput());
				doc.set(xml);
				bDirty = false;
			}
		}

		IEditorPart editor = getEditor(1);
		editor.doSave(monitor);
	}

	public void doSaveAs() {
		IEditorPart editor = getEditor(1);
		editor.doSaveAs();
		setPageText(0, editor.getTitle());
		setInput(editor.getEditorInput());
	}

	/*
	 * (non-Javadoc) Method declared on IEditorPart
	 */
	public void gotoMarker(IMarker marker) {
		setActivePage(0);
		IDE.gotoMarker(getEditor(0), marker);
	}

	/**
	 * The <code>MultiPageEditorExample</code> implementation of this method
	 * checks that the input is an instance of <code>IFileEditorInput</code>.
	 */
	public void init(IEditorSite site, IEditorInput editorInput)
			throws PartInitException {
		if (!(editorInput instanceof IFileEditorInput))
			throw new PartInitException(
					"Invalid Input: Must be IFileEditorInput");
		super.init(site, editorInput);
	}

	/*
	 * (non-Javadoc) Method declared on IEditorPart.
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}

	protected void pageChange(int newPageIndex) {
		super.pageChange(newPageIndex);
		if (newPageIndex == 2) {
			// TODO : sync pages
		}
	}

	/**
	 * Closes all project files on project close.
	 */
	public void resourceChanged(final IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchPage[] pages = getSite().getWorkbenchWindow()
							.getPages();
					for (int i = 0; i < pages.length; i++) {
						if (((FileEditorInput) textEditor.getEditorInput())
								.getFile().getProject()
								.equals(event.getResource())) {
							IEditorPart editorPart = pages[i]
									.findEditor(textEditor.getEditorInput());
							pages[i].closeEditor(editorPart, true);
						}
					}
				}
			});
		}
	}

	@Override
	public void handleRteEvent(RteEvent event) {
		if (event.getTopic().equals(RteEvent.CONFIGURATION_MODIFIED)) {
			bDirty = true;
			firePropertyChange(IEditorPart.PROP_DIRTY);
		}
	}

	@Override
	public boolean isDirty() {
		if (bDirty)
			return true;
		return super.isDirty();
	}

	public IRteConfigurationProxy getConfiguration() {
		return configProxy;
	}

	// bind to frame work
	public Object getAdapter(Class required) {
		if (IContentOutlinePage.class.equals(required)) {
			// two outline vies for Components and xml views.
			// These views must implements IContentOutlinePage or extends ContentOutlinePage
			// OutlineView ov = new OutlineView();
			// return ov;
			if (getActivePage() == 1) {
				//return new XMLContentOutlinePage(this);
			}
	    }
		//System.out.println("active page: " + getActivePage() + ", required: " + required.toString());
		
		return super.getAdapter(required);
	}

}
