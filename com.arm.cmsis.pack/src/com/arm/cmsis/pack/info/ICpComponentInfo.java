/*******************************************************************************
 * Copyright (c) 2014 ARM Ltd and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/
package com.arm.cmsis.pack.info;
import com.arm.cmsis.pack.base.IEvaluationResult;
import com.arm.cmsis.pack.data.ICpComponent;


/**
 * Interface that describes component that is instantiated in the project
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
	
	
}
