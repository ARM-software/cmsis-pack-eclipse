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
 *  An abstract convenience class implementing IRunnableWithResult interface 
 */
public abstract class RunnableWithResult<T> implements IRunnableWithResult<T> {

	protected T result;
	/**
	 * Default constructor 
	 */
	public RunnableWithResult() {
		result = null;
	}

	/**
	 * Constructor with initial (default) result value
	 * @param defaultResult to be returned if run() is not called or failed
	 */
	public RunnableWithResult(T defaultResult) {
		result = defaultResult;
	}

	@Override
	public T getResult() {
		return result;
	}
}
