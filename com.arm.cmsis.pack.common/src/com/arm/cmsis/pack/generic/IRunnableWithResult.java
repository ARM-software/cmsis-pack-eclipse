/*******************************************************************************
* Copyright (c) 2017 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.generic;

/**
 *  Runnable that returns a result of type T, run() method must set it 
 */
public interface IRunnableWithResult<T> extends Runnable {

	/**
	 * Returns result set by run() method 
	 * @return result set by run() method or null if run has not been executed
	 */
	T getResult();
}
