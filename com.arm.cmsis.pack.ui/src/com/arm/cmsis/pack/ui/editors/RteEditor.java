/*******************************************************************************
 * Copyright (c) 2017 ARM Ltd. and others
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.Charset;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.events.IRteController;
import com.arm.cmsis.pack.events.IRteEventListener;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.parser.ICpXmlParser;
import com.arm.cmsis.pack.ui.CpStringsUI;

/**
 * An abstract multi-page editor for IRteCondroller-backed models. 
 */
public abstract class RteEditor<TController extends IRteController> extends MultiPageEditorPart implements IResourceChangeListener, IRteEventListener, IGotoMarker {

	protected int activePageIndex = 0; // initially the page with index 0 is activated

	protected TController fModelController = null;
	protected ICpXmlParser fParser = null;
	protected IFile fFile = null;
	
	protected boolean fbSaving = false;
	protected String fXmlString = CmsisConstants.EMPTY_STRING; // saved XML string for modification comparison

	abstract protected ICpXmlParser createParser();
	abstract protected TController createController();
	
	public RteEditor() {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	@Override
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		fParser = null;
		fModelController = null;
		fXmlString = null;
		super.dispose();
	}

	
	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);
		fFile = ResourceUtil.getFile(input);
		String title= input.getName();
		setPartName(title);
		loadData();
	}


	protected ICpXmlParser getParser() {
		if(fParser == null) {
			fParser = createParser();			
		}
		return fParser;
	}

	protected void loadData() {
		if(fModelController == null) {
			fModelController = createController();
			fModelController.addListener(this);
		}

		File file = fFile.getLocation().toFile();
		
		ICpXmlParser parser = getParser();
		if(parser == null)
			return;
		ICpItem root = parser.parseFile(file.getAbsolutePath());
		String xmlString = getXmlString(root);
		if(xmlString.equals(fXmlString))
			return; // nothing has changed

		fXmlString = xmlString; 
		fModelController.setDataInfo(root);
		
	}
	
	/**
	 * Generates XML string from editor's datata
	 * root ICpItem representing data root
	 * @return generated XML String
	 */
	protected String getXmlString(ICpItem root) {
		if (root != null && getParser()!= null) {
			return fParser.writeToXmlString(root);
		}
		return CmsisConstants.EMPTY_STRING;
	}

	/**
	 * Generates XML string from editor's datata
	 * @return generated XML String
	 */
	protected String getXmlString() {
		if (fModelController != null) {
			ICpItem info = fModelController.getDataInfo();
			return getXmlString(info);
		}
		return CmsisConstants.EMPTY_STRING;
	}

	
	protected synchronized boolean isSaving() {
		return fbSaving;
	}
	
	protected synchronized void setSaving(boolean bSaving) {
		fbSaving = bSaving;
	}
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		if(isSaving())
			return;
		setSaving(true);
		fModelController.commit();
		try {
			saveXml(monitor);
			fModelController.emitRteEvent(RteEvent.CONFIGURATION_COMMITED, fModelController);
			firePropertyChange(IEditorPart.PROP_DIRTY);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{ 
			setSaving(false);
		}
	}

	
	/**
	 * Saves editor XML content to file
	 * @param monitor IProgressMonitor
	 * @throws CoreException 
	 */
	protected void saveXml(IProgressMonitor monitor) throws CoreException{
		fXmlString = getXmlString();
		saveXmlToFile(fXmlString, fFile, monitor);
	}

	/**
	 * Saves XML string to file
	 * @param xml XML string to save
	 * @param file destination IFile
	 * @param monitor IProgressMonitor
	 * @throws CoreException 
	 */
	protected void saveXmlToFile(String xml, IFile file, IProgressMonitor monitor) throws CoreException{
		file.setContents(new ByteArrayInputStream(xml.getBytes(Charset.defaultCharset())),
				true, true, monitor);
		file.refreshLocal(IResource.DEPTH_ZERO, monitor);
	}
	
	
	@Override
	public void doSaveAs() {
		doSave( new NullProgressMonitor());
	}

	@Override
	public void gotoMarker(IMarker marker) {
		// default does nothing
	}

	/**
	 * The <code>MultiPageEditorExample</code> implementation of this method
	 * checks that the input is an instance of <code>IFileEditorInput</code>.
	 */
	@Override
	public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
		if (!(editorInput instanceof IFileEditorInput)) {
			throw new PartInitException(CpStringsUI.RteConfigurationEditor_InvalidInput);
		}
		super.init(site, editorInput);
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	protected void pageChange(int newPageIndex) {
		super.pageChange(newPageIndex);
		if (activePageIndex == newPageIndex) {
			return;
		}
		activePageIndex = newPageIndex;
		if (fModelController != null) {
			fModelController.updateDataInfo();
		}
	}

	@Override
	public void handle(RteEvent event) {
		if (fModelController == null) {
			return;
		}
		switch (event.getTopic()) {
		case RteEvent.CONFIGURATION_MODIFIED:
		case RteEvent.COMPONENT_SELECTION_MODIFIED:
		case RteEvent.FILTER_MODIFIED:			
			Display.getDefault().asyncExec(() -> {
				firePropertyChange(IEditorPart.PROP_DIRTY);
			});			
			return;
		default:
		}
	}

	@Override
	public boolean isDirty() {
		if (fModelController != null) {
			return fModelController.isModified();
		}
		return false;
	}

	public TController getModelController() {
		return fModelController;
	}

	/**
	 * Closes all project files on project close.
	 */
	@Override
	public void resourceChanged(final IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
			Display.getDefault().asyncExec(() -> {
				IProject project = fFile.getProject();
				IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
				for (int i = 0; i < pages.length; i++) {
					if (project.equals(event.getResource())) {
						IEditorPart editorPart = ResourceUtil.findEditor(pages[i], fFile);
						pages[i].closeEditor(editorPart, true);
					}
				}
			});
			return;
		}
		if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
			IResourceDelta resourseDelta = event.getDelta();
			IResourceDeltaVisitor deltaVisitor = new IResourceDeltaVisitor() {
				@Override
				public boolean visit(IResourceDelta delta) {
					IResource resource = delta.getResource();
					int type = resource.getType();
					if (type == IResource.ROOT || type == IResource.PROJECT) {
						return true; // workspace or project => visit children
					}

					int kind = delta.getKind();
					int flags = delta.getFlags();

					if (type == IResource.FILE  && resource.equals(fFile)) {
						if ( kind == IResourceDelta.REMOVED) {
							if ((flags & IResourceDelta.MOVED_TO) == IResourceDelta.MOVED_TO) {
								// renamed
								IPath newPath = delta.getMovedToPath();
								IFile r = (IFile) ResourcesPlugin.getWorkspace().getRoot().findMember(newPath);
								final FileEditorInput fileEditorInput = new FileEditorInput(r);
								Display.getDefault().asyncExec(() -> setInput(fileEditorInput));
								return false;
							} else if (flags == 0) { // project deleted
								Display.getDefault().asyncExec(() -> {
									RteEditor.this.getEditorSite().getPage()
									.closeEditor(RteEditor.this, true);
								});
								return false;
							}
							return false;
						} else if ( kind == IResourceDelta.CHANGED && (flags & IResourceDelta.CONTENT) == IResourceDelta.CONTENT ) {
							if(isSaving())
								return false;
							final FileEditorInput fileEditorInput = new FileEditorInput(fFile);
							Display.getDefault().asyncExec(() -> setInput(fileEditorInput));
							return false;
						}
					}
					return true;
				}
			};
			try {
				resourseDelta.accept(deltaVisitor);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

}
