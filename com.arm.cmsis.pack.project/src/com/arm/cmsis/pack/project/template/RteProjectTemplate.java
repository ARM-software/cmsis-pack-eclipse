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

package com.arm.cmsis.pack.project.template;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.cdt.ui.templateengine.IPagesAfterTemplateSelectionProvider;
import org.eclipse.cdt.ui.templateengine.IWizardDataPage;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.project.Messages;

/**
 * This class provides extra pages for new Project wizard and static functions
 * helping in project creation
 */
public class RteProjectTemplate implements IPagesAfterTemplateSelectionProvider {

	protected List<IWizardDataPage> fPages = null;

	static protected ICpDeviceInfo selectedDeviceInfo = null;

	@Override
	public IWizardDataPage[] createAdditionalPages(IWorkbenchWizard wizard, IWorkbench workbench,
			IStructuredSelection selection) {

		fPages = new ArrayList<IWizardDataPage>();

		getSelectedToolChain();

		RteTemplateDeviceSelectorPage devicePage = new RteTemplateDeviceSelectorPage();

		RteTemplateCmsisProjectPage toolChainAdapterPage = new RteTemplateCmsisProjectPage(
				Messages.RteProjectTemplate_CMSIS_RTE_Project, Messages.RteProjectTemplate_CMSIS_RTE_Project, null,
				wizard instanceof IImportWizard);

		fPages.add(toolChainAdapterPage);

		fPages.add(devicePage);
		return getCreatedPages(wizard);
	}

	@Override
	public IWizardDataPage[] getCreatedPages(IWorkbenchWizard wizard) {
		return fPages.toArray(new IWizardDataPage[0]);
	}

	/**
	 * Returns device info selected in RteTemplateDeviceSelectorPage
	 *
	 * @return ICpDeviceInfo
	 */
	public static ICpDeviceInfo getSelectedDeviceInfo() {
		return selectedDeviceInfo;
	}

	public static void setSelectedDeviceInfo(ICpDeviceInfo selectedDeviceInfo) {
		RteProjectTemplate.selectedDeviceInfo = selectedDeviceInfo;
	}

	/**
	 * Helper method that returns selected in the main page of CDT new project
	 * wizard
	 *
	 * @return selected IToolChain object
	 */
	public static IToolChain getSelectedToolChain() {
		Object tsProperty = MBSCustomPageManager.getPageProperty(MBSCustomPageManager.PAGE_ID,
				MBSCustomPageManager.TOOLCHAIN);
		if (tsProperty instanceof List<?>) {
			List<?> l = (List<?>) tsProperty;
			for (Object o : l) {
				if (o instanceof IToolChain) {
					return (IToolChain) o;
				}
			}
		}
		return null;
	}

	/**
	 * Creates toolchain info for given compiler and output type
	 *
	 * @param Tcompiler
	 *            compiler family
	 * @param Toutput
	 *            output type : " exe" or "lib"
	 * @return toolchain info as ICpItem
	 */
	public static ICpItem createToolChainInfo(String Tcompiler, String Toutput) {
		ICpItem toolchainInfo = new CpItem(null, CmsisConstants.TOOLCHAIN_TAG);
		toolchainInfo.attributes().setAttribute(CmsisConstants.TCOMPILER, Tcompiler);
		toolchainInfo.attributes().setAttribute(CmsisConstants.TOUTPUT, Toutput);
		return toolchainInfo;

	}

}
