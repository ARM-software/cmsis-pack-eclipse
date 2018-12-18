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
package com.arm.cmsis.pack.ui.widgets;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.arm.cmsis.pack.events.IRteController;
import com.arm.cmsis.pack.events.IRteEventListener;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.generic.AttributedItem;

public abstract class RteWidget<TController extends IRteController> extends AttributedItem implements IRteEventListener {

	
	protected TController fModelController = null;

	/**
	 * Sets an RTE model controller to be used by the widget 
	 * @param modelController IRteController controller to use
	 */
	public void setModelController(TController modelController) {
		if(fModelController == modelController)
			return;
		if(fModelController != null)
			fModelController.removeListener(this);
		fModelController = modelController;
		if(fModelController != null)
			fModelController.addListener(this);
	}

	/**
	 * Destroys the widget by destroying children and unregistering from the controller and  
	 */
	public void destroy() {
		if(fModelController != null)
			fModelController.removeListener(this);
		fModelController = null;
	}

	
	/**
	 * Returns RTE model controller used by the widget
	 * @return IRteModelController
	 */
	public TController getModelController() {
		return fModelController;
	}
	
	/**
	 * Refresh UI without changing configuration 
	 */
	public abstract void refresh();
	
	/**
	 * refresh UI after having changed configuration 
	 */
	public abstract void update();

	/**
	 * Creates actual control
	 * @param parent parent composite
	 * @return created control
	 */
	public abstract Composite createControl(Composite parent);

	
	/**
	 * Returns Composite that should be used as focus widget
	 * @return widget to set focus to 
	 */
	abstract public Composite getFocusWidget();
	
	
	@Override
	public void handle(RteEvent event) {
		switch(event.getTopic()) {
		case RteEvent.CONFIGURATION_COMMITED:
		case RteEvent.CONFIGURATION_MODIFIED:
			asyncUpdate();
			return;
		default:
			break;
		}
	}
	
	/**
	 *  Updates widget asynchronously, runs  in GUI thread 
	 */
	protected void asyncUpdate() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				update();
			}
		});			
	}
}
