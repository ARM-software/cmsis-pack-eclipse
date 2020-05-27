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

import com.arm.cmsis.pack.dsq.IDsqClient;
import com.arm.cmsis.pack.dsq.IDsqEngine;
import com.arm.cmsis.pack.dsq.IDsqEngineFactory;
import com.arm.cmsis.pack.dsq.IDsqLogger;
import com.arm.cmsis.pack.info.ICpDeviceInfo;

/**
 * Responsible for managing debug sequence factories contributed through the extension point
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class CpDsqEngineFactory extends CpAbstractExtensionFactory<IDsqEngineFactory> {

	private static CpDsqEngineFactory theFactory = null;
	public static final String DSQ_ENGINE_FACTORY = "DsqEngineFactory"; //$NON-NLS-1$

	/**
	 *  Private constructor to avoid subclassing
	 */
	private CpDsqEngineFactory() {
		super(DSQ_ENGINE_FACTORY);
	}

	/**
	 * Singleton method that returns CpDsqEngineFactory instance
	 * @return CpDsqEngineFactory instance
	 */
	public static CpDsqEngineFactory getInstance() {
		if(theFactory == null) {
			theFactory = new CpDsqEngineFactory();
		}
		return theFactory;
	}

	/**
	 *  Destroys CpDsqEngineFactory singleton
	 */
	public static void destroy() {
		theFactory = null;
	}

	@Override
	protected IDsqEngineFactory castToExtenderClass(Object extender) {
		if(extender instanceof IDsqEngineFactory) {
			return (IDsqEngineFactory)extender;
		}
		return null;
	}

	public static IDsqEngine create(ICpDeviceInfo di, IDsqClient ta, IDsqLogger log) {
		IDsqEngineFactory factory = getInstance().getExtender();
		if(factory == null) {
			return null;
		}

		return factory.create(di, ta, log);
	}

}
