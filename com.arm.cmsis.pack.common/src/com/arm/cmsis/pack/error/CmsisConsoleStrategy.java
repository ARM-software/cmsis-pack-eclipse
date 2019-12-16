/*******************************************************************************
* Copyright (c) 2019 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.error;

/**
 *  Default ICmsisConsoleStrategy implementation
 */
public class CmsisConsoleStrategy implements ICmsisConsoleStrategy {

	protected ICmsisConsole fConsole = null;


	@Override
	public ICmsisConsole getCmsisConsole() {
		if(fConsole == null) {
			fConsole = createDefaultCmsisConsole();
		}
		return fConsole;
	}


	@Override
	public ICmsisConsole createDefaultCmsisConsole() {
		return null;
	}


	@Override
	public void setCmsisConsole(ICmsisConsole console) {
		fConsole = console;
	}

}
