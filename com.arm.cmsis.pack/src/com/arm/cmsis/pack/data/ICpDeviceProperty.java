/*******************************************************************************
* Copyright (c) 2015 ARM Ltd. and others
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
 * Interface represent device property: processor, book, feature, debug, memory, etc.
 *
 */
public interface ICpDeviceProperty extends ICpItem {

	/**
	 * Returns __dp (Debug Port) index  
	 * @return __dp index  
	 */
	long getDP(); 
	
	/**
	 * Returns __ap (Access Port) index  
	 * @return __ap index  
	 */
	long getAP();
	
	
	/**
	 * Returns item-depended address (default is 0)
	 * @return address as long value
	 */
	long getAddress();	

	/**
	 * Returns item-depended start address (default is 0)
	 * @return start address as long value
	 */
	long getStart();	

	
	/**
	 * Returns item-depended start address (default is 0)
	 * @return start address as long value
	 */
	long getSize();	

	/**
	 * Checks if item is default one (memory, algorithm )
	 * @return true if item is default one
	 */
	boolean isDefault();

	/**
	 * Checks if sequence block is atomic
	 * @return true if atomic
	 */
	boolean isAtomic();
	
	
}
