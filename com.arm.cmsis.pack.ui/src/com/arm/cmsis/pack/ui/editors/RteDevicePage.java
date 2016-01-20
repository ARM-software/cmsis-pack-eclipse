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

package com.arm.cmsis.pack.ui.editors;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.rte.IRteModelController;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.CpStringsUI;
import com.arm.cmsis.pack.ui.widgets.RteDeviceInfoWidget;
import com.arm.cmsis.pack.ui.wizards.RteDeviceSelectorWizard;
import com.arm.cmsis.pack.ui.wizards.RteWizardDialog;

/**
 * Editor page that wraps RteManagerWidget
 *
 */
public class RteDevicePage extends RteEditorPage {

	private RteDeviceInfoWidget deviceWidget = null;
	
	public RteDevicePage() {
	}

	
	@Override
	public void setModelController(IRteModelController model) {
		super.setModelController(model);
		deviceWidget.setModelController(model);
		update();
	}


	@Override
	public Composite getFocusWidget() {
		return deviceWidget;
	}


	@Override
	public void createPageContent(Composite parent) {
		deviceWidget = new RteDeviceInfoWidget(parent);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		deviceWidget.setLayoutData(gd);

		deviceWidget.setSelectionAdapter( new SelectionAdapter(){
	    	 @Override
	         public void widgetSelected(SelectionEvent e) {
	    		 changeDevice();
	    	}
	    });
		
    	headerWidget.setFocusWidget(getFocusWidget());
	}

	@Override
	protected void setupHeader() {
    	headerWidget.setLabel(CpStringsUI.RteDevicePage_Device, CpPlugInUI.getImage(CpPlugInUI.ICON_DEVICE));
    	super.setupHeader();
	}

	protected void changeDevice() {
		IRteModelController model = getModelController();
		if(model != null){
			RteDeviceSelectorWizard wizard = 
					new RteDeviceSelectorWizard(CpStringsUI.RteDeviceSelectorPage_SelectDevice, model.getDevices(), model.getDeviceInfo());
			RteWizardDialog dlg = new RteWizardDialog(deviceWidget.getShell(), wizard);
			dlg.setPageSize(600, 400); // limit initial size 

			if(dlg.open() == Window.OK) {
				ICpDeviceInfo deviceInfo = wizard.getDeviceInfo();
				//deviceWidget.setDeviceInfo(deviceInfo);
				model.setDeviceInfo(deviceInfo);	
			}
		}
	}


	@Override
	public void handle(RteEvent event) {
		switch(event.getTopic()) {
		case RteEvent.CONFIGURATION_COMMITED:
		case RteEvent.CONFIGURATION_MODIFIED:
			update();
			return;
		default: 
			super.handle(event);
		}
	}

	@Override
	public void update() {
		if (headerWidget != null && getModelController() != null) {
			bModified = getModelController().isDeviceModified();
			headerWidget.setModified(bModified);
		}
		refresh();
		super.update();
	}

	@Override
	public void refresh() {
		IRteModelController modelController = getModelController();
		if(deviceWidget!= null && modelController != null) {
			deviceWidget.setDeviceInfo(modelController.getDeviceInfo());
		}
	}
}
