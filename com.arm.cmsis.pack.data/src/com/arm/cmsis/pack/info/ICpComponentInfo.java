/*******************************************************************************
 * Copyright (c) 2015 ARM Ltd and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/
package com.arm.cmsis.pack.info;
import com.arm.cmsis.pack.data.ICpComponent;
import com.arm.cmsis.pack.data.ICpFile;
import com.arm.cmsis.pack.enums.IEvaluationResult;


/**
 * Interface that describes a component instantiated in the configuration
 */
public interface ICpComponentInfo extends ICpComponent, ICpItemInfo, IEvaluationResult {

	/**
	 * Returns actual CMSIS component corresponding to this info  
	 * @return actual component
	 */
	ICpComponent getComponent();
	
	
	/**
	 * Sets actual CMSIS component to this info  
	 */
	void setComponent(ICpComponent component);
	
	
	/**
	 * Returns number of instantiated components
	 * @return number of instantiated components
	 */
	int getInstanceCount();


	/**
	 * Searches for file info corresponding supplied ICpFile 
	 * @param f ICpFile 
	 * @return the resulting ICpFileInfo or null if not found
	 */
	ICpFileInfo getFileInfo(ICpFile f); 
	
	
	/**
	 * Checks if this component info has been saved in the configuration 
	 * @return true if saved
	 */
	boolean isSaved(); 
	
	/**
	 * Sets saved flag to the component info  
	 * @parameter saved flag value to set
	 */
	void setSaved(boolean saved);
	
	/**
	 * Returns relative gpdsc filename associated with the item
	 * @return gpdsc filename or null 
	 */
	String getGpdsc(); 
	
}
