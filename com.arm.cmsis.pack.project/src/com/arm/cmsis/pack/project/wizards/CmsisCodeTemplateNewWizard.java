/*******************************************************************************
* Copyright (c) 2015 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.project.wizards;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.arm.cmsis.pack.project.CpProjectPlugIn;
import com.arm.cmsis.pack.project.Messages;
import com.arm.cmsis.pack.ui.CpPlugInUI;

/**
 * CMSIS Code Template Wizard
 */
public class CmsisCodeTemplateNewWizard extends Wizard implements INewWizard {
	private CmsisCodeTemplateNewWizardPage page;
	private ISelection selection;
	boolean overwrite;

	/**
	 * Constructor for CmsisCodeTemplateNewWizard.
	 */
	public CmsisCodeTemplateNewWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	/**
	 * Adding the page to the wizard.
	 */
	@Override
	public void addPages() {
		page = new CmsisCodeTemplateNewWizardPage(selection);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We
	 * will create an operation and run it using wizard as execution context.
	 */
	@Override
	public boolean performFinish() {
		final String containerName = page.getContainerName();
		final String[] fileNames = page.getFileName().split(" "); //$NON-NLS-1$
		final String[] templateFileNames = page.getCodeTemplateFileNames();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(containerName, fileNames, templateFileNames, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), Messages.CmsisCodeTemplate_Error, realException.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * The worker method. It will find the container, create the file if missing
	 * or just replace its contents, and open the editor on the newly created
	 * file.
	 */
	void doFinish(String containerName, String[] fileNames, String[] templateFileNames, IProgressMonitor monitor)
			throws CoreException {
		// create a sample file
		for (int i = 0; i < fileNames.length; i++) {
			String fileName = fileNames[i];
			monitor.beginTask(Messages.CmsisCodeTemplate_CreatingFile + fileName, 2);
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IResource resource = root.findMember(new Path(containerName));
			if (!resource.exists() || !(resource instanceof IContainer)) {
				String message = NLS.bind(Messages.CmsisCodeTemplate_ContainerNotExist, containerName);
				throwCoreException(message);
			}
			IContainer container = (IContainer) resource;
			final IFile file = container.getFile(new Path(fileName));
			try {
				InputStream stream = openContentStream(templateFileNames[i]);
				if (file.exists()) {
					getShell().getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							String message = NLS.bind(Messages.CmsisCodeTemplate_FileAlreadyExist, file.getName());
							overwrite = MessageDialog.openQuestion(getShell(),
									Messages.CmsisCodeTemplate_OverwriteExistingFile, message);
						}
					});
					if (overwrite) {
						file.setContents(stream, true, true, monitor);
					}
				} else {
					file.create(stream, true, monitor);
				}
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			monitor.worked(1);
			monitor.setTaskName(Messages.CmsisCodeTemplate_OpeningFileForEditing);
			getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					try {
						IDE.openEditor(page, file, true);
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			});
			monitor.worked(1);
		}
	}

	/**
	 * We will initialize file contents with the code template.
	 *
	 * @throws FileNotFoundException
	 */
	private InputStream openContentStream(String fileName) throws FileNotFoundException {
		return new FileInputStream(new File(fileName));
	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, CpProjectPlugIn.PLUGIN_ID, IStatus.OK, message, null);
		throw new CoreException(status);
	}

	/**
	 * We will accept the selection in the workbench to see if we can initialize
	 * from it.
	 *
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
		setWindowTitle(Messages.CmsisCodeTemplate_WindowTitle); 
		setDefaultPageImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.NEWFILE_WIZARD));
	}
}