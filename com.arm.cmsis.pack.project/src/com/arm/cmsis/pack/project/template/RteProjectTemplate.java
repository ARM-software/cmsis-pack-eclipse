/*******************************************************************************
* Copyright (c) 2021 ARM Ltd. and others
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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.project.Messages;

/**
 * This class provides extra pages for new Project wizard and static functions
 * helping in project creation
 */
public class RteProjectTemplate implements IPagesAfterTemplateSelectionProvider {

    protected List<IWizardDataPage> fPages = null;

    protected static ICpDeviceInfo selectedDeviceInfo = null;

    @Override
    public IWizardDataPage[] createAdditionalPages(IWorkbenchWizard wizard, IWorkbench workbench,
            IStructuredSelection selection) {

        fPages = new ArrayList<IWizardDataPage>();

        RteTemplateDeviceSelectorPage devicePage = new RteTemplateDeviceSelectorPage();

        RteTemplateCmsisProjectPage toolChainAdapterPage = new RteTemplateCmsisProjectPage(
                Messages.RteProjectTemplate_CMSIS_RTE_Project, Messages.RteProjectTemplate_CMSIS_RTE_Project, null,
                true); // always hide select main as it no longer corresponds to RTOS2 API

        fPages.add(devicePage);
        fPages.add(toolChainAdapterPage);
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

}
