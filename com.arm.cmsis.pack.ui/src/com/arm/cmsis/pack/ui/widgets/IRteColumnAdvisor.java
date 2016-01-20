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

package com.arm.cmsis.pack.ui.widgets;

import com.arm.cmsis.pack.rte.IRteModelController;
import com.arm.cmsis.pack.ui.tree.IColumnAdvisor;

/**
 *  Extends IRteColumnAdvisor with IRteModelController awareness
 */
public interface IRteColumnAdvisor extends IColumnAdvisor {
	
	/**
	 * Sets an RTE model controller to be used by the advisor 
	 * @param IRteModelController controller to use
	 */
	public void setModelController(IRteModelController modelController);
	
	
	/**
	 * Returns RTE model controller used by the widget
	 * @return IRteModelController
	 */
	public IRteModelController getModelController();

}
