/*******************************************************************************
* Copyright (c) 2016 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack;


/**
 * Responsible for managing pack installers contributed through the extension point
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class CpPackInstallerFactory extends CpAbstractExtensionFactory<ICpPackInstaller> {

	private static CpPackInstallerFactory theFactory = null;
	public static final String PACK_INSTALLER = "PackInstaller"; //$NON-NLS-1$

	/**
	 *  Private constructor to avoid subclassing
	 */
	private CpPackInstallerFactory() {
		super(PACK_INSTALLER);
	}

	/**
	 * Singleton method that returns CpPackInstallerFactory instance
	 * @return CpPackInstallerFactory instance
	 */
	public static CpPackInstallerFactory getInstance() {
		if(theFactory == null) {
			theFactory = new CpPackInstallerFactory();
		}
		return theFactory;
	}

	/**
	 *  Destroys CpPackInstallerFactory singleton
	 */
	public static void destroy() {
		theFactory = null;
	}

	@Override
	protected ICpPackInstaller castToExtenderClass(Object extender) {
		if(extender instanceof ICpPackInstaller)
			return (ICpPackInstaller)extender;
		return null;
	}

}
