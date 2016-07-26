/*******************************************************************************
* Copyright (c) 2016 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* IBM Corporation - initial API and implementation
* ARM Ltd and ARM Germany GmbH - application-specific implementation
*******************************************************************************/

package com.arm.cmsis.pack.installer;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.IOverwriteQuery;

/**
 * Utility overwrite query dialog
 */
public class OverwriteQuery implements IOverwriteQuery {

	private final Shell shell;

	public OverwriteQuery(Shell shell) {
		this.shell = shell;
	}

	@Override
	public String queryOverwrite(String pathString) {
		Path path = new Path(pathString);

        String messageString;
        //Break the message up if there is a file name and a directory
        //and there are at least 2 segments.
        if (path.getFileExtension() == null || path.segmentCount() < 2) {
			messageString = NLS.bind(Messages.OverwriteQuery_ExistsQuestion, pathString);
		} else {
			messageString = NLS.bind(Messages.OverwriteQuery_OverwriteNameAndPathQuestion, path.lastSegment(),
			path.removeLastSegments(1).toOSString());
		}

        final MessageDialog dialog = new MessageDialog(shell, Messages.OverwriteQuery_Question,
                null, messageString, MessageDialog.QUESTION, new String[] {
                        IDialogConstants.YES_LABEL,
                        IDialogConstants.YES_TO_ALL_LABEL,
                        IDialogConstants.NO_LABEL,
                        IDialogConstants.NO_TO_ALL_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0) {
        	@Override
			protected int getShellStyle() {
        		return super.getShellStyle() | SWT.SHEET;
        	}
        };
        String[] response = new String[] { YES, ALL, NO, NO_ALL, CANCEL };
        //run in syncExec because callback is from an operation,
        //which is probably not running in the UI thread.
        shell.getDisplay().syncExec(new Runnable() {
            @Override
			public void run() {
                dialog.open();
            }
        });
        return dialog.getReturnCode() < 0 ? CANCEL : response[dialog
                .getReturnCode()];
	}

}
