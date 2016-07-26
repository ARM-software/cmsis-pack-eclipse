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

package com.arm.cmsis.pack.data;

/**
 *  Interface to expand raw path by replacing Sequences ($P, $D, $S, etc.)
 */
public interface ICpStringExpander {
	
	/**
	 * Expands supplied string by substituting sequences
	 * @param src source path or string to expand
	 * @return expanded string
	 */
	String expand(String src);

}
