package com.arm.cmsis.pack.installer.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class ManLocalRepoHandler extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

		PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(shell,
				"com.arm.cmsis.pack.ui.CpManLocalRepoPage", null, null); //$NON-NLS-1$
		dialog.open();

		return null;
	}
}
