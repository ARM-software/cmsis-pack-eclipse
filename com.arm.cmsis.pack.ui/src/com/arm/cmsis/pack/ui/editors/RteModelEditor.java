/*******************************************************************************
 * Copyright (c) 2017 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Eclipse Project - generation from template
 * ARM Ltd and ARM Germany GmbH - application-specific implementation
 *******************************************************************************/
package com.arm.cmsis.pack.ui.editors;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpPack;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.info.ICpComponentInfo;
import com.arm.cmsis.pack.parser.CpConfigParser;
import com.arm.cmsis.pack.parser.ICpXmlParser;
import com.arm.cmsis.pack.rte.IRteModelController;
import com.arm.cmsis.pack.rte.RteModel;
import com.arm.cmsis.pack.rte.components.IRteComponentItem;
import com.arm.cmsis.pack.rte.dependencies.IRteDependencyItem;
import com.arm.cmsis.pack.rte.packs.IRtePackFamily;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.CpStringsUI;

/**
 * RTE configuration editor with three pages:
 * <ul>
 * <li>page 0 contains an RteManagerWidget
 * <li>page 1 contains an RteDeviceInfoWidget
 * <li>page 1 contains an RtePackSelectorWidget
 * </ul>
 */
public class RteModelEditor extends RteEditor<IRteModelController> {

	private RteComponentPage rteComponentPage;
	private RteDevicePage rteDevicePage;
	private RtePackPage rtePackPage;

	private int componentPageIndex = 0;
	private int devicePageIndex = 1;
	private int packPageIndex = 2;


	public RteModelEditor() {
		super();
		CpPlugIn.addRteListener(this);
	}

	@Override
	public void dispose() {
		CpPlugIn.removeRteListener(this);
		rteComponentPage = null;
		rtePackPage = null;
		rteDevicePage = null;
		super.dispose();
	}

	@Override
	protected ICpXmlParser createParser() {
		return new CpConfigParser();
	}

	@Override
	protected IRteModelController createController() {
		return new RteModelEditorController(new RteModel());
	}
	void createRteManagerPage() {
		rteComponentPage = new RteComponentPage();
		Composite composite = rteComponentPage.createControl(getContainer());
		componentPageIndex = addPage(composite);
		setPageText(componentPageIndex, CpStringsUI.RteConfigurationEditor_ComponentsTab);
	}

	void createPackSelectorPage() {
		rtePackPage = new RtePackPage();
		Composite composite = rtePackPage.createControl(getContainer());

		packPageIndex = addPage(composite);
		setPageText(packPageIndex, CpStringsUI.RteConfigurationEditor_PacksTab);
	}

	void createDeviceSelectorPage() {
		rteDevicePage = new RteDevicePage();
		Composite composite = rteDevicePage.createControl(getContainer());

		devicePageIndex = addPage(composite);
		setPageText(devicePageIndex, CpStringsUI.RteDevicePage_Device);
	}


	@Override
	protected void createPages() {
		createRteManagerPage();
		createDeviceSelectorPage();
		createPackSelectorPage();
		rteComponentPage.setModelController(fModelController);
		rteDevicePage.setModelController(fModelController);
		rtePackPage.setModelController(fModelController);
	}


	@Override
	public void gotoMarker(IMarker marker) {
		try {
			IRteDependencyItem depItem = (IRteDependencyItem) marker.getAttribute(CpPlugInUI.RTE_PROBLEM_MARKER_DEP_ITEM);
			if (depItem == null) {
				return;
			}
			IRteComponentItem rteComponent = depItem.getComponentItem();
			if (rteComponent == null) {
				return;
			}

			ICpComponentInfo ci = rteComponent.getActiveCpComponentInfo();
			boolean packInstalled = false;
			String packId = CmsisConstants.EMPTY_STRING;
			if (ci != null) {
				packId = ci.getPackId();
				packInstalled = ci.getPack() != null;
			} else if (rteComponent.getActiveCpItem() != null) {
				packId = rteComponent.getActiveCpItem().getPackId();
				packInstalled = rteComponent.getActiveCpItem().getPack() != null;
			}
			IRtePackFamily packFamily = fModelController.getRtePackCollection().getRtePackFamily(CpPack.familyFromId(packId));

			if (depItem.getEvaluationResult() != EEvaluationResult.UNAVAILABLE &&
					// if the pack isn't installed or the pack is excluded
					(!packInstalled || (packFamily != null && packFamily.getSelectedPacks().isEmpty()))) {
				setActivePage(2);
				fModelController.emitRteEvent(RteEvent.PACK_FAMILY_SHOW, packFamily);
			} else {
				setActivePage(0);
				fModelController.emitRteEvent(RteEvent.COMPONENT_SHOW,
						fModelController.getComponents().findChild(rteComponent.getKeyPath(), false));
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void handle(RteEvent event) {
		if (fModelController == null) {
			return;
		}

		switch (event.getTopic()) {
		case RteEvent.CONFIGURATION_MODIFIED:
		case RteEvent.COMPONENT_SELECTION_MODIFIED:
		case RteEvent.FILTER_MODIFIED:
			super.handle(event);
			return;
		case RteEvent.PACKS_RELOADED:
		case RteEvent.PACKS_UPDATED:
			if (fModelController != null) {
				fModelController.reloadPacks();
			}
			break;
		case RteEvent.GPDSC_CHANGED:
			if (fModelController != null) {
				if(fModelController.isGeneratedPackUsed((String)event.getData())){
					fModelController.update();
				}
			}
			break;
		default:
		}
	}

}
