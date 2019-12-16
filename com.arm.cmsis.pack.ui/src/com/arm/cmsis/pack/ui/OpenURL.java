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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpEnvironmentProvider;

/**
 * Utility class to open an URL in browser or editor 
 *
 */
public class OpenURL implements IOpenURL{

	/**
	 * Opens an URL in a browser or associated system editor 
	 * @param url URL to open
	 * @param parent parent shell to display error message, can be null 
	 */
	static public void open(String url, Shell shell){
		if (url == null || url.isEmpty()) {
			return;
		}
		String msg = open(url); 
		if(msg != null) {
			String message = CpStringsUI.CannotOpenURL + url; 
			message += "\n"; //$NON-NLS-1$
			message += msg;
			MessageDialog.openError(shell, CpStringsUI.CannotOpenURL, message);
		}
	}

	
	/**
	 * Opens an URL in a browser or associated system editor 
	 * @param url URL to open
	 * @return null if successfully opened, otherwise reason why operation failed
	 */
	static public String open(String url){
		IOpenURL openURL = null;
		ICpEnvironmentProvider provider = CpPlugIn.getEnvironmentProvider();
		if(provider != null) {
			openURL = provider.getAdapter(IOpenURL.class);  
		}
		if(openURL == null) {
			// use default implementation
			openURL = new OpenURL();
		}
		return openURL.openUrl(url);
	}


	@Override
	public String openUrl(String url) {
		if (url == null || url.isEmpty()) {
			return null;
		}
		
		String tmp = url.toLowerCase();
		boolean isUrl = false;
		if(tmp.startsWith("file:") || tmp.startsWith("http:") || tmp.startsWith("www.") || tmp.startsWith("https:")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			isUrl = true;
		else if(tmp.indexOf(".html#") > 0  || tmp.indexOf(".htm#") > 0) { //$NON-NLS-1$ //$NON-NLS-2$
			isUrl = true;
			url = "file:///" + url; //$NON-NLS-1$
		}
		String message = CpStringsUI.OperationNotSupported;
		try {
			if (isUrl) {
				URI uri = new URI(url);
				IWebBrowser browser = null;
				IWorkbench workbench = PlatformUI.getWorkbench();
				if(workbench != null) {
					browser =  PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser();
				}
				if(browser != null) {
					browser.openURL(uri.toURL());
					message = null;
				} else { 
					Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
					if (desktop != null) {
						desktop.browse(uri);
						message = null;
					}
				}
			} else {
				Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
				if (desktop != null) {
					File file = new File(url);
					desktop.open(file);
					message = null;
				}
			}
		} catch (IOException | URISyntaxException | PartInitException e) {
			e.printStackTrace();
			if(e.getMessage() != null)
				message = e.getMessage();
			
		}
		return message;
	}
}
