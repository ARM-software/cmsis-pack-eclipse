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

package com.arm.cmsis.pack.dsq;

/**
 * Interface to provision for forward compatibility between
 * {@link IDsqClient} and {@link IDsqEngine}
 */
public interface IDsqApiVersion {

	/**
	 * Returns {@link IDsqClient} or {@link IDsqEngine} version
	 * @return version string in the form major.minor.patch, may not be null
	 */
	default String getApiVersion() { return "1.0.0";  } //$NON-NLS-1$

}
