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

package com.arm.cmsis.pack.rte.dependencies;

import java.util.Collection;


/**
 * Interface describing result of dependency evaluation for an item, contains collection of dependencies 
 */
public interface IRteDependencyResult extends IRteDependencyItem{
	
	/**
	 * Adds dependency to the list of dependencies
	 * @param dependency IRteDependency to add
	 */
	void addDependency(IRteDependency dependency);
	
	/**
	 * Removes dependency from the list of dependencies
	 * @param dependency IRteDependency to remove
	 */
	void removeDependency(IRteDependency dependency);
	
	/**
	 * Returns list of unresolved and conflicting dependencies
	 * @return collection of unresolved and conflicting dependencies  
	 */
	Collection<IRteDependency> getDependencies();
	
}
