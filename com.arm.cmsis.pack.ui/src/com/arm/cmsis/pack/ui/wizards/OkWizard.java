package com.arm.cmsis.pack.ui.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Wizard to show either OK or Finish button  
 */

public abstract class OkWizard extends Wizard implements INewWizard {
	
	/**
	 * Runs the wizard in a dialog
	 * @param shell
	 * @return Wizard dialog return code
	 */
	public int execute(Shell shell){
		WizardDialog dlg;
		if(isShowOkButton()) {
			dlg = new OkWizardDialog(shell, this);
		} else {
			dlg = new WizardDialog(shell, this);
		}
		dlg.setPageSize(600, 400); // limit initial size 

		return dlg.open();
	}

	/**
	 * Checks if show "OK" button instead of "Finish", default is true
	 * @return true if show "OK" button instead of "Finish"  
	 */
	public boolean isShowOkButton() {
		return true;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// default does nothing 
	}
	
}