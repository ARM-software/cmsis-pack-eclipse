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
import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.cdt.ui.templateengine.IPagesAfterTemplateSelectionProvider;
import org.eclipse.cdt.ui.templateengine.IWizardDataPage;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import com.arm.cmsis.pack.build.settings.RteToolChainAdapterFactory;
import com.arm.cmsis.pack.build.settings.RteToolChainAdapterInfo;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.info.ICpBoardInfo;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.project.Messages;
import com.arm.cmsis.pack.project.utils.CDTUtils;

/**
 * This class provides extra pages for new Project wizard and static functions
 * helping in project creation
 */
public class RteProjectTemplate implements IPagesAfterTemplateSelectionProvider {

    protected List<IWizardDataPage> fPages = null;

    protected static ICpDeviceInfo selectedDeviceInfo = null;
    protected static ICpBoardInfo selectedBoardInfo = null;
    protected static IToolChain selectedToolchain = null;

    protected static String Tcompiler = CmsisConstants.EMPTY_STRING;
    protected static String Toutput = CmsisConstants.EMPTY_STRING;

    protected static String adapterId = CmsisConstants.EMPTY_STRING;

    @Override
    public IWizardDataPage[] createAdditionalPages(IWorkbenchWizard wizard, IWorkbench workbench,
            IStructuredSelection selection) {

        updateToolChainAdapter();
        fPages = new ArrayList<>();

        RteTemplateDeviceSelectorPage devicePage = new RteTemplateDeviceSelectorPage();

        RteTemplateCmsisProjectPage toolChainAdapterPage = new RteTemplateCmsisProjectPage(
                Messages.RteProjectTemplate_CMSIS_RTE_Project, Messages.RteProjectTemplate_CMSIS_RTE_Project, null);

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

    /**
     * Sets device info selected in RteTemplateDeviceSelectorPage
     *
     * @param selectedDeviceInfo ICpDeviceInfo to set
     */
    public static void setSelectedDeviceInfo(ICpDeviceInfo selectedDeviceInfo) {
        RteProjectTemplate.selectedDeviceInfo = selectedDeviceInfo;
    }

    /**
     * Returns board info selected in RteTemplateBoardSelectorPage
     *
     * @return ICpBoardInfo
     */
    public static ICpBoardInfo getSelectedBoardInfo() {
        return selectedBoardInfo;
    }

    /**
     * Sets board info selected in RteTemplateBoardSelectorPage
     *
     * @param selectedBoardInfo ICpBoardInfo to set
     */
    public static void setSelectedBoardInfo(ICpBoardInfo selectedBoardInfo) {
        RteProjectTemplate.selectedBoardInfo = selectedBoardInfo;
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

    public static void updateToolChainAdapter() {
        IToolChain tc = getSelectedToolChain();
        if (tc == null || selectedToolchain == tc)
            return;
        selectedToolchain = tc;

        IConfiguration cfg = tc.getParent();
        if (cfg != null) {
            IProjectType pt = cfg.getProjectType();
            if (pt != null) {
                IBuildPropertyValue artefact = pt.getBuildArtefactType();
                if (artefact != null) {
                    String aId = artefact.getId();
                    if (CDTUtils.BUILD_ARTEFACT_TYPE_LIB.equals(aId)) {
                        Toutput = CmsisConstants.TOUTPUT_LIB;
                    } else if (CDTUtils.BUILD_ARTEFACT_TYPE_EXE.equals(aId)) {
                        Toutput = CmsisConstants.TOUTPUT_EXE;
                    }
                }
            }
        }

        RteToolChainAdapterFactory factory = RteToolChainAdapterFactory.getInstance();
        Collection<RteToolChainAdapterInfo> adapters = factory.getAdapterInfos(tc);
        if (adapters.size() == 1) {
            RteToolChainAdapterInfo info = adapters.iterator().next();
            Tcompiler = info.getFamily();
            adapterId = info.getId();
        } else {
            Tcompiler = CmsisConstants.EMPTY_STRING;
            adapterId = CmsisConstants.EMPTY_STRING;
        }
    }

    public static String getTcompiler() {
        return Tcompiler;
    }

    public static void setTcompiler(String tcompiler) {
        Tcompiler = tcompiler;
    }

    public static String getToutput() {
        return Toutput;
    }

    public static String getAdapterId() {
        return adapterId;
    }

    public static void setAdapterId(String adapterId) {
        RteProjectTemplate.adapterId = adapterId;
    }

}
