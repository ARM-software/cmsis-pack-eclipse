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
 * Item that can have an associated
 */
public interface ICmsisErrorItem{
	
	/**
	 * Returns error associated with the item
	 * @return CmsisError if any
	 */
	default CmsisError getError() {return null;}

	/**
	 * Sets an error to the item 
	 * @param error CmsisZoneError to set
	 */
	default void setError(CmsisError error) { /* default does nothing */};

}
