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

import java.util.Collection;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackInstaller;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.rte.IRteModelController;
import com.arm.cmsis.pack.rte.RteModelUtils;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.CpStringsUI;
import com.arm.cmsis.pack.ui.IHelpContextIds;
import com.arm.cmsis.pack.ui.widgets.RtePackSelectorWidget;
import com.arm.cmsis.pack.ui.widgets.RteWidget;

/**
 * Editor page that wraps RteManagerWidget
 *
 */
public class RtePackPage extends RteModelEditorPage {

	
	IAction useLatestAction = null;
	IAction resolveMissingPacksAction = null;
	IAction usedPacksAction = null;

	public RtePackPage() {
	}

	@Override
	protected RteWidget<IRteModelController> createContentWidget() {
		return new RtePackSelectorWidget();
	}

	@Override
	protected String getHelpID() {
		return IHelpContextIds.PACKS_PAGE;
	}

	@Override
	protected Image getImage() {
		return CpPlugInUI.getImage(CpPlugInUI.ICON_PACKAGES_FILTER);
	}

	@Override
	protected String getLabel() {
		return CpStringsUI.RteConfigurationEditor_PacksTab;
	}


	@Override
	public boolean isModified() {
		if(getModelController() != null)
			return getModelController().isPackFilterModified();
		return false;
	}

	@Override
	protected void createActions() {		
		useLatestAction = new Action(CpStringsUI.UseAllLatestPacks, IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				setUseAllLatest(isChecked());
			}
		};
		useLatestAction.setToolTipText(CpStringsUI.UseAllLatestPacksTooltip);
		headerWidget.addAction(useLatestAction, SWT.LEFT, true);

		resolveMissingPacksAction = new Action(CpStringsUI.RteComponentTreeWidget_Install, IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				IRteModelController model = getModelController();
				if (model != null) {
					final ICpPackInstaller packInstaller = CpPlugIn.getPackManager().getPackInstaller();
					if (packInstaller == null) {
						return;
					}

					String packRoot = CpPlugIn.getPackManager().getCmsisPackRootDirectory();
					if (packRoot == null || packRoot.isEmpty()){
						return;
					}

					for (String packId : RteModelUtils.getMissingPacks(model)) {
						if (CpPlugIn.getPackManager().getInstalledPacks().getPack(packId) != null) {
							model.setUseAllLatestPacks(true);
						} else {
							packInstaller.installPack(packId);
						}
					}
				}
			}
		};
		resolveMissingPacksAction.setToolTipText(CpStringsUI.RteComponentTreeWidget_InstallMissingPacks);
		resolveMissingPacksAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_RTE));
		resolveMissingPacksAction.setDisabledImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_RTE_GREY));
		headerWidget.addAction(resolveMissingPacksAction, SWT.LEFT, true);

		//Used packs
		usedPacksAction = new Action(CpStringsUI.ShowOnlyUsedPacks, IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				setUsedPacks(isChecked());
			}
		};
		usedPacksAction.setToolTipText(CpStringsUI.RtePackPage_ShowOnlyUsedPacksTooltip);	
		headerWidget.addAction(usedPacksAction, SWT.LEFT, true);
		
		super.createActions();
	}

	void setUseAllLatest(boolean bUse) {
		if(fModelController != null){
			fModelController.setUseAllLatestPacks(bUse);
		}
	}

	//Used packs
	void setUsedPacks(boolean bUse) {
		if(fModelController != null){
			fModelController.setShowUsedPacksOnly(bUse);			
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

	//Used packs
	void updateUsedPacks() {
		boolean bUse = true;
		if(fModelController != null){
			bUse = fModelController.isShowUsedPacksOnly();
		}
		usedPacksAction.setChecked(bUse);
		if(bUse) {
			usedPacksAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_CHECKED));
		} else {
			usedPacksAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_UNCHECKED));
		}
	}
	//
	
	
	@Override
	public void handle(RteEvent event) {
		switch(event.getTopic()) {
		case RteEvent.FILTER_MODIFIED:
			update();
			return;
		case RteEvent.COMPONENT_SELECTION_MODIFIED:
			refresh();
			return;
		default:
			super.handle(event);
		}
	}

	@Override
	public void updateActions() {
		if(getModelController() != null) {
			Collection<String> missingPacks = RteModelUtils.getMissingPacks(getModelController());
			resolveMissingPacksAction.setEnabled(!missingPacks.isEmpty());
		}
		updateUseAllLatest();
		updateUsedPacks();
		super.updateActions();
	}
}
