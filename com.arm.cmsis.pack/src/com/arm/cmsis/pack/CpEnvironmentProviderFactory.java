/*******************************************************************************
* Copyright (c) 2021 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack;

/**
 * Responsible for managing environment providers contributed through the
 * extension point
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class CpEnvironmentProviderFactory extends CpAbstractExtensionFactory<ICpEnvironmentProvider> {

    private static CpEnvironmentProviderFactory theFactory = null;
    public static final String ENVIRONMENT_PROVIDER = "EnvironmentProvider"; //$NON-NLS-1$

    /**
     * Private constructor to avoid subclassing
     */
    private CpEnvironmentProviderFactory() {
        super(ENVIRONMENT_PROVIDER);
    }

    /**
     * Singleton method that returns CpEnvironmentProviderFactory instance
     *
     * @return CpEnvironmentProviderFactory instance
     */
    public static CpEnvironmentProviderFactory getInstance() {
        if (theFactory == null) {
            theFactory = new CpEnvironmentProviderFactory();
        }
        return theFactory;
    }

    /**
     * Destroys CpEnvironmentProviderFactory singleton
     */
    public static void destroy() {
        theFactory = null;
    }

    @Override
    protected ICpEnvironmentProvider castToExtenderClass(Object extender) {
        if (extender instanceof ICpEnvironmentProvider)
            return (ICpEnvironmentProvider) extender;
        return null;
    }

}
