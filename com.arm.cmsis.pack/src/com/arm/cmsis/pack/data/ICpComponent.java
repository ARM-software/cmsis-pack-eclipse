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
 * Interface describing a CMSIS component  
 */
public interface ICpComponent extends ICpItem {

	/**
	 * Returns if this item is an API 
	 * @return true if the component is API
	 */
	boolean isApi();

	/**
	 * Checks if this component is a device startup one (Cclass="Device" Cgroup="Startup" Csub="" )
	 * @return true if this component is a startup one
	 */
	boolean isDeviceStartupComponent();

	/**
	 * Checks if this component is a Core one (Cclass="CMSIS" Cgroup="CoreS" Csub="")<br>
	 * @return true if this component is a startup one
	 */
	boolean isCmsisCoreComponent();

	/**
	 * Checks if this component is an RTOS one (Cclass="CMSIS" Cgroup="RTOS")<br>
	 * @return true if this component is a startup one
	 */
	boolean isCmsisRtosComponent();

	
	/**
	 * Checks if component should be treated as multi-instance one, even if max instance count is 1
	 * @return true if component is a multi-instance one  
	 */
	boolean isMultiInstance();

	/**
	 * Checks if component is a bootstrap for generator (launches specified generator) 
	 * @return true if component is bootstrap one  
	 */
	boolean isBootstrap();
	
	/**
	 * Returns generator associated with the component (generated or bootstrap) 
	 * @return ICpGenerator if component is bootstrap or generated, null otherwise  
	 */
	 ICpGenerator getGenerator();
	
	/**
	 * Returns number of maximum instances for the component 
	 * @return true if the component is API
	 */
	int getMaxInstances();
	
	/**
	 * Returns piece of code corresponding this component to copy to RteComponents.h file
	 * @return code to copy to RteComponents.h file as string 
	 */
	String getRteComponentsHCode();
}
