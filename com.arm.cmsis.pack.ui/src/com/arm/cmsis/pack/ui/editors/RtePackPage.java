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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackInstaller;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.rte.IRteModelController;
import com.arm.cmsis.pack.rte.RteModelUtils;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.CpStringsUI;
import com.arm.cmsis.pack.ui.IHelpContextIds;
import com.arm.cmsis.pack.ui.widgets.RtePackSelectorWidget;

/**
 * Editor page that wraps RteManagerWidget
 *
 */
public class RtePackPage extends RteEditorPage {

	protected RtePackSelectorWidget rtePackSelectorTree = null;
	IAction useLatestAction = null;
	IAction resolveMissingPacksAction = null;

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
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getFocusWidget(), IHelpContextIds.PACKS_PAGE);
	}

	@Override
	protected void setupHeader() {
		headerWidget.setLabel(CpStringsUI.RteConfigurationEditor_PacksTab, CpPlugInUI.getImage(CpPlugInUI.ICON_PACKAGES_FILTER));

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
			Collection<String> missingPacks = RteModelUtils.getMissingPacks(getModelController());
			resolveMissingPacksAction.setEnabled(!missingPacks.isEmpty());
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
