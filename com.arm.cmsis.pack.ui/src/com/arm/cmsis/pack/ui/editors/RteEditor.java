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
package com.arm.cmsis.pack.ui.editors;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.IDocument;
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

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.events.IRteEventListener;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.info.ICpConfigurationInfo;
import com.arm.cmsis.pack.parser.ConfigParser;
import com.arm.cmsis.pack.rte.IRteModelController;
import com.arm.cmsis.pack.rte.RteModel;
import com.arm.cmsis.pack.rte.RteModelController;
import com.arm.cmsis.pack.ui.CpStringsUI;
import com.arm.cmsis.pack.ui.xmleditor.XMLEditor;

/**
 * An example showing how to create an RTE configuration multi-page editor. This
 * example has 4 pages:
 * <ul>
 * <li>page 0 contains an RteManagerWidget
 * <li>page 1 contains an RteDesviceInfo
 * <li>page 1 contains an RtePackSelectorWidget
 * <li>page 3 shows XML representation of saved configuration info
 * </ul>
 */
public class RteEditor extends MultiPageEditorPart implements IResourceChangeListener, IRteEventListener {

	private RteComponentPage rteComponentPage;
	private RteDevicePage rteDevicePage;
	private RtePackPage rtePackPage;

	private int componentPageIndex = 0;
	private int devicePageIndex = 1;
	private int packPageIndex = 2;
	private int xmlPageIndex = 3;
	private int activePageIndex = 0; // initially the page with index 0 is activated
	
	
	/** The text editor used in XML page  */
	TextEditor textEditor;

	IRteModelController fModelController = null;
	ConfigParser parser = null;

	public RteEditor() {
		super();
		parser = new ConfigParser();
		CpPlugIn.addRteListener(this);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	void createXmlPage() {
		try {
			textEditor = new XMLEditor();
			xmlPageIndex = addPage(textEditor, getEditorInput());
			setPageText(xmlPageIndex, CpStringsUI.RteConfigurationEditor_XmlTab);
			setPartName(textEditor.getTitle());
		} catch (PartInitException e) {
			ErrorDialog.openError(getSite().getShell(),
					CpStringsUI.RteConfigurationEditor_ErrorCreatingNestedEditor, null, e.getStatus());
		}
	}

	void createRteManagerPage() {
		rteComponentPage = new RteComponentPage();
		Composite composite = rteComponentPage.createControl(getContainer());
		componentPageIndex = addPage(composite);
		setPageText(componentPageIndex, CpStringsUI.RteConfigurationEditor_ComponentsTab);
	}

	void createPackSelectorPage() {
		rtePackPage = new RtePackPage();
		Composite composite = rtePackPage.createControl(getContainer());

		packPageIndex = addPage(composite);
		setPageText(packPageIndex, CpStringsUI.RteConfigurationEditor_PacksTab);
	}

	void createDeviceSelectorPage() {
		rteDevicePage = new RteDevicePage();
		Composite composite = rteDevicePage.createControl(getContainer());

		devicePageIndex = addPage(composite);
		setPageText(devicePageIndex, CpStringsUI.RteDevicePage_Device);
	}

	
	@Override
	protected void createPages() {
		createRteManagerPage();
		createDeviceSelectorPage();
		createPackSelectorPage();
		createXmlPage();
		createConfiguration();
	}

	protected void createConfiguration() {
		// create configuration out of text read by text editor
		IDocumentProvider dp = textEditor.getDocumentProvider();
		IDocument doc = dp.getDocument(textEditor.getEditorInput());
		String xml = doc.get();

		ICpItem root = parser.parseXmlString(xml);

		fModelController = new RteModelController(new RteModel());
		if (root != null) {
			ICpConfigurationInfo info = (ICpConfigurationInfo) root;
			fModelController.setConfigurationInfo(info);
			rteComponentPage.setModelController(fModelController);
			rteDevicePage.setModelController(fModelController);
			rtePackPage.setModelController(fModelController);
			fModelController.addListener(this);
		}
	}

	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		CpPlugIn.removeRteListener(this);
		parser = null;
		fModelController = null;
		rteComponentPage = null;
		rtePackPage = null;
		rteDevicePage = null;
		super.dispose();
	}

	protected String getXmlString(){
		if (fModelController != null) {
			ICpConfigurationInfo info = fModelController.getConfigurationInfo();
			return  parser.writeToXmlString(info);
		}
		return CmsisConstants.EMPTY_STRING;
	}
	
	protected String getEditorXmlString(){
		IDocumentProvider dp = textEditor.getDocumentProvider();
		IDocument doc = dp.getDocument(textEditor.getEditorInput());
		String xml = doc.get();
		return xml;
	}


	protected void setXmlToEditor(String xml ){
		if (xml != null) {
			IDocumentProvider dp = textEditor.getDocumentProvider();
			IDocument doc = dp.getDocument(textEditor.getEditorInput());
			doc.set(xml);
		}
	}

	
	@Override
	public void doSave(IProgressMonitor monitor) {
		
		if (textEditor == null)
			return;

		if(!isDirty())
			return;

		fModelController.commit();
		String xml = getXmlString();
		if (xml != null) {
			setXmlToEditor(xml);
		}

		IEditorPart editor = getEditor(xmlPageIndex);
		editor.doSave(monitor);
	}

	@Override
	public void doSaveAs() {
		IEditorPart editor = getEditor(1);
		editor.doSaveAs();
		setPageText(0, editor.getTitle());
		setInput(editor.getEditorInput());
	}

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
			throw new PartInitException(CpStringsUI.RteConfigurationEditor_InvalidInput);
		super.init(site, editorInput);
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	protected void pageChange(int newPageIndex) {
		super.pageChange(newPageIndex);
		if(activePageIndex == newPageIndex) 
			return;
		activePageIndex = newPageIndex;
		if (fModelController != null ) {
			fModelController.updateConfigurationInfo();
			if(newPageIndex == xmlPageIndex) {
				refreshXmlPage();
			}
		}
	}
	
	protected void refreshXmlPage()	{
		String modelXml = getXmlString();
		String editorXml = getEditorXmlString();
		if (modelXml != null) {
			if(editorXml == null || !editorXml.equals(modelXml))
				setXmlToEditor(modelXml);
		}
	}

	@Override
	public void handle(RteEvent event) {
		if (fModelController == null)
			return;
		
		switch(event.getTopic()) {
		case RteEvent.CONFIGURATION_MODIFIED:
		case RteEvent.COMPONENT_SELECTION_MODIFIED:
		case RteEvent.FILTER_MODIFIED:
			firePropertyChange(IEditorPart.PROP_DIRTY);
			return;
		case RteEvent.PACKS_RELOADED:
			if(fModelController != null) {
				fModelController.reloadPacks();
				if(activePageIndex == xmlPageIndex)
					refreshXmlPage();
			}
		default: 
		}
	}

	@Override
	public boolean isDirty() {
		if(fModelController != null)
			return fModelController.isModified();
		return false;
	}

	public IRteModelController getModelController() {
		return fModelController;
	}

	/**
	 * Closes all project files on project close.
	 */
	public void resourceChanged(final IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
					for (int i = 0; i < pages.length; i++) {
						if (((FileEditorInput) textEditor.getEditorInput())
								.getFile().getProject()
								.equals(event.getResource())) {
							IEditorPart editorPart = pages[i].findEditor(textEditor.getEditorInput());
							pages[i].closeEditor(editorPart, true);
						}
					}
				}
			});
		}
	}

	
	// bind to framework
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class required) {
		if (IContentOutlinePage.class.equals(required)) {
			// two outline views for Components and xml views.
			// These views must implements IContentOutlinePage or extends ContentOutlinePage
			// OutlineView ov = new OutlineView();
			// return ov;
			if (getActivePage() == 1) {
				//return new XMLContentOutlinePage(this);
			}
	    }
		
		return super.getAdapter(required);
	}

}
