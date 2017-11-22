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
 * The CMSIS code template
 */
public interface ICpCodeTemplate extends ICpItem {

	/**
	 * Returns the component name
	 * @return component name
	 */
	String getComponentName();
	
	/**
	 * Return's the selection name
	 * @return selection name
	 */
	String getSelectionName();

	/**
	 * Some code template item contains multiple templates
	 * @return code templates
	 */
	String[] getCodeTemplates();
	
	/**
	 * @param filePath the file's path to this code template
	 */
	void addCodeTemplate(String filePath);
}
