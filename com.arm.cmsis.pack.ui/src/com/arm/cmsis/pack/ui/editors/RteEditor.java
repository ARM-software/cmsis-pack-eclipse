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
	protected IFile iFile = null;

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
		super.dispose();
	}

	
	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);
		iFile = ResourceUtil.getFile(input);
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
		fModelController = createController();
		if(fModelController != null)
			fModelController.addListener(this);

		File file = iFile.getLocation().toFile();
		
		ICpXmlParser parser = getParser();
		if(parser == null)
			return;
		ICpItem root = parser.parseFile(file.getAbsolutePath());
		fModelController.setDataInfo(root);
	}
	
	protected String getXmlString() {
		if (fModelController != null && getParser()!= null) {
			ICpItem info = fModelController.getDataInfo();
			return fParser.writeToXmlString(info);
		}
		return CmsisConstants.EMPTY_STRING;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		fModelController.commit();

		String xml = getXmlString();
		try {
			iFile.setContents(new ByteArrayInputStream(xml.getBytes(Charset.defaultCharset())),
					true, true, monitor);
			iFile.refreshLocal(IResource.DEPTH_ZERO, monitor);
		} catch (CoreException e) {
		}
		firePropertyChange(IEditorPart.PROP_DIRTY);
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
				IProject project = iFile.getProject();
				IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
				for (int i = 0; i < pages.length; i++) {
					if (project.equals(event.getResource())) {
						IEditorPart editorPart = ResourceUtil.findEditor(pages[i], iFile);
						pages[i].closeEditor(editorPart, true);
					}
				}
			});
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

					if (type == IResource.FILE && kind == IResourceDelta.REMOVED && resource.equals(iFile)) {
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
