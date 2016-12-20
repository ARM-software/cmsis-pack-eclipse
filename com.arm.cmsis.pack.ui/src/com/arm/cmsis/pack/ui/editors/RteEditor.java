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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.events.IRteEventListener;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.info.ICpConfigurationInfo;
import com.arm.cmsis.pack.parser.CpConfigParser;
import com.arm.cmsis.pack.rte.IRteModelController;
import com.arm.cmsis.pack.rte.RteModel;
import com.arm.cmsis.pack.ui.CpStringsUI;

/**
 * An example showing how to create an RTE configuration multi-page editor. This
 * example has 4 pages:
 * <ul>
 * <li>page 0 contains an RteManagerWidget
 * <li>page 1 contains an RteDesviceInfo
 * <li>page 1 contains an RtePackSelectorWidget
 * </ul>
 */
public class RteEditor extends MultiPageEditorPart implements IResourceChangeListener, IRteEventListener {

	private RteComponentPage rteComponentPage;
	private RteDevicePage rteDevicePage;
	private RtePackPage rtePackPage;

	private int componentPageIndex = 0;
	private int devicePageIndex = 1;
	private int packPageIndex = 2;
	private int activePageIndex = 0; // initially the page with index 0 is activated

	IRteModelController fModelController = null;
	CpConfigParser parser = null;
	IFile iFile;

	public RteEditor() {
		super();
		parser = new CpConfigParser();
		CpPlugIn.addRteListener(this);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
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
	protected void setInput(IEditorInput input) {
		super.setInput(input);
		iFile = ResourceUtil.getFile(input);
		String title= input.getName();
		setPartName(title);
	}

	@Override
	protected void createPages() {
		createRteManagerPage();
		createDeviceSelectorPage();
		createPackSelectorPage();
		createConfiguration();
	}

	protected void createConfiguration() {
		File file = iFile.getLocation().toFile();
		ICpItem root = parser.parseFile(file.getAbsolutePath());

		fModelController = new RteEditorController(new RteModel());
		if (root != null) {
			ICpConfigurationInfo info = (ICpConfigurationInfo) root;
			fModelController.setConfigurationInfo(info);
			rteComponentPage.setModelController(fModelController);
			rteDevicePage.setModelController(fModelController);
			rtePackPage.setModelController(fModelController);
			fModelController.addListener(this);
		}
	}

	@Override
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

	protected String getXmlString() {
		if (fModelController != null) {
			ICpConfigurationInfo info = fModelController.getConfigurationInfo();
			return parser.writeToXmlString(info);
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
		fModelController.commit();

		String xml = getXmlString();
		try {
			IProgressMonitor monitor = new NullProgressMonitor();
			iFile.setContents(new ByteArrayInputStream(xml.getBytes(Charset.defaultCharset())),
					true, true, monitor);
			iFile.refreshLocal(IResource.DEPTH_ZERO, monitor);
		} catch (CoreException e) {
		}
		firePropertyChange(IEditorPart.PROP_DIRTY);
	}

	public void gotoMarker(IMarker marker) {
		setActivePage(0);
		IDE.gotoMarker(getEditor(0), marker);
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
			fModelController.updateConfigurationInfo();
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
				firePropertyChange(IEditorPart.PROP_DIRTY);
				return;
			case RteEvent.PACKS_RELOADED:
			case RteEvent.PACKS_UPDATED:
				if (fModelController != null) {
					fModelController.reloadPacks();
				}
				break;
			case RteEvent.GPDSC_CHANGED:
				if (fModelController != null) {
					if(fModelController.isGeneratedPackUsed((String)event.getData())){
						fModelController.update();
					}
				}
				break;
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

	public IRteModelController getModelController() {
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

	// bind to framework
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getAdapter(Class required) {
		if (IContentOutlinePage.class.equals(required)) {
			// two outline views for Components and xml views.
			// These views must implements IContentOutlinePage or extends
			// ContentOutlinePage
			// OutlineView ov = new OutlineView();
			// return ov;
			if (getActivePage() == 1) {
				// return new XMLContentOutlinePage(this);
			}
		}

		return super.getAdapter(required);
	}

}
