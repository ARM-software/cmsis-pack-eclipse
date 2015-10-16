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

package com.arm.cmsis.pack.ui;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;

/**
 * Utility class to open an URL in browser or editor 
 *
 */
public class OpenURL {

	/**
	 * Opens an URL in a browser or associated system editor 
	 * @param url URL to open
	 * @param parent parent shell to display error message, can be null 
	 */
	static public void open(String url, Shell shell){
		if (url == null || url.isEmpty()) {
			return;
		}
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null) {

			String tmp = url.toLowerCase();
			boolean isUrl = false;
			if(tmp.startsWith("file:") || tmp.startsWith("http:") || tmp.startsWith("www.") || tmp.startsWith("https:")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				isUrl = true;
			else if(tmp.indexOf(".html#") > 0  || tmp.indexOf(".htm#") > 0) { //$NON-NLS-1$ //$NON-NLS-2$
				isUrl = true;
				url = "file:///" + url; //$NON-NLS-1$
			}
			try {
				if (isUrl) {
					URI uri = new URI(url);
					final IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser();
					if(browser != null)
						browser.openURL(uri.toURL());
				} else {
					File file = new File(url);
					desktop.open(file);
				}
			} catch (IOException | URISyntaxException e) {
				String message = CpStringsUI.RteComponentTreeWidget_CannotOpenURL + url; 
				String eMsg = e.getMessage();
				if(eMsg != null && !eMsg.isEmpty()) {
					message += "\n"; //$NON-NLS-1$
					message += eMsg;
				}
				MessageDialog.openError(shell, CpStringsUI.RteComponentTreeWidget_CannotOpenURL, message); 
			} catch (PartInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
}
