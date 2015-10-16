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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.rte.IRteModelController;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.CpStringsUI;
import com.arm.cmsis.pack.ui.widgets.RtePackSelectorWidget;

/**
 * Editor page that wraps RteManagerWidget
 *
 */
public class RtePackPage extends RteEditorPage {

	protected RtePackSelectorWidget rtePackSelectorTree = null;
	IAction useLatestAction = null;
	
	public RtePackPage() {
		rtePackSelectorTree = new RtePackSelectorWidget();
	}

	
	@Override
	public void setModelController(IRteModelController model) {
		super.setModelController(model);
		rtePackSelectorTree.setModelController(model);
		update();
	}

	@Override
	public Composite getFocusWidget() {
		return rtePackSelectorTree.getFocusWidget();
	}

	@Override
	public void createPageContent(Composite parent) {
		rtePackSelectorTree.createControl(parent);
    	headerWidget.setFocusWidget(getFocusWidget());
	}

	@Override
	protected void setupHeader() {
    	headerWidget.setLabel(CpStringsUI.RteConfigurationEditor_PacksTab, CpPlugInUI.getImage(CpPlugInUI.ICON_PACKAGES_FILTER));

    	useLatestAction = new Action(CpStringsUI.UseAllLatestPacks, IAction.AS_CHECK_BOX) { 
			public void run() {
				setUseAllLatest(isChecked());
			}
		};
		useLatestAction.setToolTipText(CpStringsUI.UseAllLatestPacksTooltip);
		headerWidget.addAction(useLatestAction, SWT.LEFT, true);
				
		super.setupHeader();
	}

	void setUseAllLatest(boolean bUse) {
		if(fModelController != null){
			fModelController.setUseAllLatestPacks(bUse);
		}
	}

	void updateUseAllLatest() {
		boolean bUse = true;
		if(fModelController != null){
			bUse = fModelController.isUseAllLatestPacks();
		}
		useLatestAction.setChecked(bUse);
		if(bUse) {
			useLatestAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_CHECKED));
		} else {
			useLatestAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_UNCHECKED));
		}
	}
	
	@Override
	public void handle(RteEvent event) {
		switch(event.getTopic()) {
		case RteEvent.FILTER_MODIFIED:
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
		if (getModelController() != null && rtePackSelectorTree != null) {
			bModified = getModelController().isPackFilterModified();
			headerWidget.setModified(bModified);
		}
		refresh();
		super.update();
	}

	@Override
	public void refresh() {
		updateUseAllLatest();
		rtePackSelectorTree.refresh();
	}

}
