package com.arm.cmsis.zone.ui.wizards;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.zone.project.CmsisZoneProjectCreator;
import com.arm.cmsis.zone.ui.Messages;
import com.arm.cmsis.zone.ui.editors.CmsisZoneEditor;

/**
 * 	This wizard.
 *  Its role is to create a new file resource in the provided container. 
 *  The wizard creates one file with the extension * "azone". 
 */

public class CmsisZoneAzoneFileWizard extends Wizard implements INewWizard {
	private CmsisZoneAzoneFileWizardPage page;
	private ISelection selection;
	CmsisZoneEditor cmsisZoneEditor;
	boolean result = false;

	/**
	 * Constructor for CmsisZoneAzoneFileWizard.
	 */
	public CmsisZoneAzoneFileWizard() {
		super();
		setNeedsProgressMonitor(true);
		setWindowTitle(Messages.CmsisZoneAzoneFileWizard_NewCmsisZoneAssignmentFile);
	}
	
	/**
	 * Adding the page to the wizard.
	 */
	@Override
	public void addPages() {
		page = new CmsisZoneAzoneFileWizardPage(selection);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	@Override
	public boolean performFinish() {		
		final String rzoneFile = page.getRzoneFile();
		final String azoneFile = page.getAzoneFile();	
		
		IProgressMonitor monitor = new NullProgressMonitor();
		try {
			doFinish(rzoneFile, azoneFile, monitor);
			result = true;
		} catch (CoreException e) {
			String message = e.getMessage();
			showInfoDialog(Messages.CmsisZoneAzoneFileWizard_ExceptionError, message);			
			result = false;
			
		}		
		return result;
	}	
	
	private void doFinish(String rzoneFile, String azoneFile, IProgressMonitor monitor)throws CoreException {		
		//Build azone file path
		IPath rzoneFilePath =  new Path(rzoneFile);		
		IPath azoneFilePath = rzoneFilePath.removeLastSegments(1);
		String azoneFileName = azoneFilePath.toPortableString() + CmsisConstants.SLASH + azoneFile;

		monitor.beginTask(Messages.CmsisZoneAzoneFileWizard_Creating + azoneFileName, 1);
		boolean result = CmsisZoneProjectCreator.createAzoneFile(rzoneFile, azoneFile);

		if(!result) {
			String msg = Messages.CmsisZoneAzoneFileWizard_File + azoneFileName + Messages.CmsisZoneAzoneFileWizard_CouldNotBeCreated;
			throw CmsisZoneProjectCreator.createErrorException(msg);
		}
		monitor.done();
	}
	

	protected void showInfoDialog(String title,String message){
		  MessageBox mb = new MessageBox(getShell(),SWT.OK | SWT.ICON_ERROR);
		  mb.setText(title);
		  mb.setMessage(message);
		  mb.open();
		}
	
		
	/**
	 * We will accept the selection in the workbench to see if
	 * we can initialize from it.
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
	

	
}