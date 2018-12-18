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


import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import com.arm.cmsis.pack.rte.IRteModelController;

/**
 * This class implements functionality of component selector page  
 */
public class RteDeviceInfoWidgetWrapper extends RteModelWidget {

	protected RteDeviceInfoWidget deviceWidget = null;

	public RteDeviceInfoWidgetWrapper() {
		super();
	}

	@Override
	public void destroy(){
		deviceWidget = null;
		super.destroy();
	}

	
	public RteDeviceInfoWidget getDeviceInfoWidget() {
		return deviceWidget;
	}
	
	@Override
	public Composite getFocusWidget() {
		return deviceWidget;
	}

	@Override
	public void setModelController(IRteModelController model) {
		super.setModelController(model);
		if(deviceWidget != null)
			deviceWidget.setModelController(model);
		update();
	}

	@Override
	public Composite createControl(Composite parent) {
		deviceWidget = new RteDeviceInfoWidget(parent);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		deviceWidget.setLayoutData(gd);
		getFocusWidget().setFocus();
		return deviceWidget;
	}


	@Override
	public void refresh() {
		IRteModelController modelController = getModelController();
		if(deviceWidget!= null && !deviceWidget.isDisposed() && modelController != null) {
			deviceWidget.setDeviceInfo(modelController.getDeviceInfo());
		}
	}

	@Override
	public void update() {
		refresh();
	}
}
